package kz.telecom.happydrive.data.network;

/**
 * Created by Galymzhan Sh on 11/18/15.
 */
public class Response<T> {
    public final T result;
    public final Exception exception;

    public Response(T result, Exception e) {
        this.result = result;
        this.exception = e;
    }
}
