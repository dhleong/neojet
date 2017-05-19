package io.neovim.java.rpc.impl;

import io.neovim.java.Rpc;
import io.neovim.java.rpc.RequestPacket;
import io.reactivex.Single;

/**
 * @author dhleong
 */
public abstract class RemoteObject {

    public final long id;

    private final Rpc rpc;
    private final String apiPrefix;
    private final Long idObject; // to avoid boxing

    protected RemoteObject(Rpc rpc, String apiPrefix, long id) {
        this.rpc = rpc;
        this.apiPrefix = apiPrefix;
        this.id = id;
        this.idObject = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteObject that = (RemoteObject) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
            "id=" + id +
            '}';
    }

    public Single<Boolean> isValid() {
        return request(Boolean.class, apiPrefix + "is_valid");
    }

    protected <T> Single<T> request(Class<T> responseValueType,
                                    String method, Object... args) {
        Object[] actualArgs = new Object[args.length + 1];
        actualArgs[0] = idObject;
        System.arraycopy(args, 0, actualArgs, 1, args.length);
        return rpc.request(responseValueType,
            RequestPacket.create(method, actualArgs)
        );
    }
}
