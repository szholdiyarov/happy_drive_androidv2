package kz.telecom.happydrive.data.network;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class ResponseParseError extends RuntimeException {
    public ResponseParseError(String msg) {
        super(msg);
    }

    public ResponseParseError(String msg, Throwable cause) {
        super(msg, cause);
    }
}
