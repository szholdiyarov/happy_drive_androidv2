package kz.telecom.happydrive.data.network;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class NoConnectionError extends Exception {
    public NoConnectionError(String msg, Throwable cause) {
        super(msg, cause);
    }
}
