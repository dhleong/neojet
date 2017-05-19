package io.neovim.java;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.annotations.CheckReturnValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An abstraction over a set of lines
 * @author dhleong
 */
public class Lines {

    private final Buffer buffer;
    private final int start;
    private final int end;
    private boolean strictIndexing;

    Lines(Buffer buffer, int start, int end, boolean strictIndexing) {
        this.buffer = buffer;
        this.start = start;
        this.end = end;
        this.strictIndexing = strictIndexing;
    }

    public int size() {
        return end - start + 1;
    }

    /**
     * Delete the lines represented by this set
     */
    @CheckReturnValue
    public Single<Boolean> delete() {
        return replace(Collections.emptyList());
    }

    /**
     * Replace the lines represented by this set
     * with the given array of lines
     */
    @CheckReturnValue
    public Single<Boolean> replace(String... replacement) {
        return replace(Arrays.asList(replacement));
    }

    /**
     * Replace the lines represented by this set
     * with the given List of lines
     */
    @CheckReturnValue
    public Single<Boolean> replace(List<String> replacement) {
        return buffer.setLines(start, end, strictIndexing, replacement);
    }

    /**
     * @return a Flowable that emits each line in this set
     *  of Lines
     */
    public Flowable<String> get() {
        return get(0, 0);
    }

    /**
     * @return A Single line in this set
     */
    public Single<String> get(int index) {
        int adjusted = start + index;
        return get(adjusted, adjusted + 1).firstOrError();
    }

    /**
     * @param start An offset relative to the start of this buffer
     * @param end If positive, the absolute end line, relative to
     *            the start of this buffer. If negative or 0, assumed
     *            to be a number relative to the end of this buffer
     * @return a Flowable that emits each line in this set,
     *  between start and end
     */
    public Flowable<String> get(int start, int end) {
        // TODO since it's a flowable anyway, we might be able
        //  to split the fetch into batches, so we can be a bit
        //  lazy and quit early if possible
        final int adjustedStart = this.start + start;
        final int adjustedEnd = (end <= 0)
            ? this.end + end
            : this.start + end;
        return buffer.fetchLines(adjustedStart, adjustedEnd, strictIndexing)
            .flatMapPublisher(Flowable::fromIterable);
    }
}
