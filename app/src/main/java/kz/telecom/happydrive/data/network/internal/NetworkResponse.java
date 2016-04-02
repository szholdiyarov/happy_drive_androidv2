package kz.telecom.happydrive.data.network.internal;

import java.util.List;
import java.util.Map;

/**
 * Created by Galymzhan Sh on 11/18/15.
 */
public class NetworkResponse {
    public final int code;
    public final byte[] data;
    public final Map<String, List<String>> headers;

    NetworkResponse(int code, byte[] data, Map<String, List<String>> headers) {
        this.code = code;
        this.data = data;
        this.headers = headers;
    }
}
