package kz.telecom.happydrive.data;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class NoConnectionError extends Exception {
    NoConnectionError(String msg, Throwable cause) {
        super(msg, cause);
    }
}
