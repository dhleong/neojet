package io.neovim.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tspoon.traceur.Traceur;
import io.neovim.java.event.EventsManager;
import io.neovim.java.rpc.NeovimObjectMapper;
import io.neovim.java.rpc.NotificationPacket;
import io.neovim.java.rpc.Packet;
import io.neovim.java.rpc.RequestPacket;
import io.neovim.java.rpc.ResponsePacket;
import io.neovim.java.rpc.channel.EmbedChannel;
import io.neovim.java.rpc.channel.FallbackChannel;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.UnicastProcessor;
import io.reactivex.schedulers.Schedulers;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dhleong
 */
public class Rpc implements Closeable {

    private static final long TIMEOUT = 5;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * NOTE: Calling {@link #close()} does NOT
     * necessarily close the streams; clients who
     * use them are responsible for closing them.
     */
    public interface Channel extends Closeable {
        void tryOpen() throws Exception;

        InputStream getInputStream();

        InputStream getErrorStream();

        OutputStream getOutputStream();
    }

    final EventsManager eventsManager = new EventsManager();

    final Rpc.Channel channel;
    final OutputStream out;
    final InputStream in;
    final InputStream err;
    final ObjectMapper mapper;

    final AtomicBoolean closed = new AtomicBoolean(false);
    final AtomicBoolean reading = new AtomicBoolean(false);
    final AtomicInteger nextId = new AtomicInteger(0);
    final CompositeDisposable disposable = new CompositeDisposable();
    final CountDownLatch closingLatch = new CountDownLatch(2);

    final UnicastProcessor<Packet> outgoing = UnicastProcessor.create();
    final Flowable<Packet> incoming;
    final ConcurrentHashMap<Integer, Class<?>> requestedTypes =
        new ConcurrentHashMap<>();

    private Rpc(@Nonnull Channel channel) {
        this.channel = channel;
        in = channel.getInputStream();
        out = channel.getOutputStream();
        err = channel.getErrorStream();

        mapper = NeovimObjectMapper.newInstance(
            this, requestedTypes, eventsManager);

        incoming = observeIncoming()
            .subscribeOn(Schedulers.newThread())
            .replay(5, TimeUnit.SECONDS)
            .refCount();

        // internal subscriptions
        subscribe();
    }

    @Override
    public void close() {
        // ensure any queued packets are sent
        outgoing.onComplete();
        closed.set(true);
        try {
            closingLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        silentClose(channel);
        silentClose(in);
        silentClose(out);
        silentClose(err);
        disposable.dispose();
    }

    public Flowable<NotificationPacket> notifications() {
        return receive(NotificationPacket.class);
    }

    /**
     * Perform a request with the given RequestPacket,
     * returning a Single that emits the Response, or times
     * out after TIMEOUT seconds.
     */
    public Single<ResponsePacket> request(RequestPacket request) {
        int id = sendRequest(request);
        return receiveOne(
            ResponsePacket.class,
            packet -> id == packet.requestId
        ).timeout(TIMEOUT, TIMEOUT_UNIT);
    }

    /**
     * Perform a request with the given RequestPacket,
     * returning a Single that emits the response value,
     * coerced to the given type, or times out after
     * TIMEOUT seconds.
     */
    public <T> Single<T> request(Class<T> responseValueType, RequestPacket request) {
        int id = sendRequest(request);
        requestedTypes.put(id, responseValueType);
        return receiveOne(
            ResponsePacket.class,
            packet -> id == packet.requestId
        ).timeout(TIMEOUT, TIMEOUT_UNIT)
         .flatMap(responsePacket -> {
             if (responsePacket.error != null) {
                 // TODO NeovimException?
                 return Single.error(new Exception(
                     responsePacket.error.toString()
                 ));
             }

             if (responsePacket.result == null) {
                 // NOTE: no error, but the result is nil;
                 //  coerce that to TRUE, since rxjava2
                 //  doesn't allow nil values
                 //noinspection unchecked
                 return Single.just((T) Boolean.TRUE);
             }

             //noinspection unchecked
             return Single.just((T) responsePacket.result);
         });
    }

    /**
     * Queue a Packet to be sent
     */
    public void send(Packet object) {
        ensureValid("send()");

        outgoing.onNext(object);
    }

    /**
     * Dispatch a RequestPacket without waiting for the response
     * (which there might not be)
     * @return the requestId
     */
    public int sendRequest(RequestPacket request) {
        final int id = request.requestId = assignRequestId();
        send(request);
        return id;
    }

    /**
     * Get a stream of every Packet received from nvim
     */
    public Flowable<Packet> receive() {
        ensureValid("receive()");
        return incoming;
    }

    public <T extends Packet> Flowable<T> receive(Class<T> type) {
        return receive()
            .filter(p -> type.isAssignableFrom(p.getClass()))
            .cast(type);
    }

    /**
     * Get a Single of the Packet type you specify matching your
     *  provided Predicate
     */
    public <T extends Packet> Single<T> receiveOne(Class<T> type, Predicate<T> filter) {
        return receive(type)
            .filter(filter)
            .firstOrError();
    }

    private int assignRequestId() {
        return nextId.getAndIncrement();
    }

    private void ensureValid(String action) {
        if (disposable.isDisposed()) {
            throw new IllegalStateException(
                "Unable to perform `" + action + "`; Rpc instance is closed");
        }
    }

    private void subscribe() {
        disposable.addAll(
            subscribeOutgoing(),

            observeErrors()
                .subscribeOn(Schedulers.newThread())
                .subscribe(err -> {
                    // TODO ?
                    System.err.println("nvim Error: " + err);
                }),

            // subscribe ourselves to make sure stdin is consumed
            incoming.subscribe(packet -> {
                // ignore
            }, e -> {
                // ignore
            })
        );
    }

    private Flowable<String> observeErrors() {
        return Flowable.create(emitter -> {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(err)
            );
            while (!closed.get()) {
                String line = reader.readLine();
                if (line == null) break;

                emitter.onNext(line);
            }

            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
    }

    private Flowable<Packet> observeIncoming() {
        Traceur.enableLogging();
        return Flowable.create(emitter -> {
            if (reading.getAndSet(true)) {
                System.err.println("observeIncoming " + emitter.getClass() + "@" + emitter.hashCode());
//                Thread.dumpStack();
                emitter.onError(
                    new IllegalStateException("Multiple readers")
                );
                return;
            }

            try {
                while (!closed.get()) {
                    Packet read = mapper.readValue(in, Packet.class);
                    if (read == null) break;

                    if (emitter.requested() > 0) {
                        emitter.onNext(read);
                    }
                }

            } catch (IOException e) {
                if (!e.getMessage().contains("end-of-input")) {
                    closingLatch.countDown();
                    emitter.onError(e);
                    reading.set(false);
                    return;
                }
            }

            emitter.onComplete();
            closingLatch.countDown();
            reading.set(false);

        }, BackpressureStrategy.BUFFER);
    }

    private Disposable subscribeOutgoing() {
        return outgoing.observeOn(Schedulers.io())
            .doOnComplete(closingLatch::countDown)
            .subscribe(packet -> {
                try {
                    mapper.writeValue(out, packet);
                    out.flush();
                } catch (IOException e) {
                    onIOException(e);
                }
            });
    }

    private void onIOException(IOException e) {
        // TODO proper logging?
        System.err.println("IOE; nvim died? Manually Closed=" + closed.get());
        e.printStackTrace();
        close();
    }

    private static void silentClose(Closeable stream) {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience shortcut. If you need more control over
     * how the embedded nvim instance is started, use
     * {@link #create(Channel)}
     */
    public static Rpc createEmbedded() {
        return create(new FallbackChannel(
            // specific choices first to work in intellij sandbox
            new EmbedChannel(Collections.singletonList("/usr/local/bin/nvim")),

            // default args last
            new EmbedChannel()
        ));
    }
    public static Rpc create(Channel channel) {
        try {
            channel.tryOpen();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to open channel", e);
        }

        return new Rpc(channel);
    }
}
