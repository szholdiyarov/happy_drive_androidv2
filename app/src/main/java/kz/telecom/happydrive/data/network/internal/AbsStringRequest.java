package kz.telecom.happydrive.data.network.internal;

import android.support.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import kz.telecom.happydrive.data.network.Request;

/**
 * Created by shgalym on 11/21/15.
 */
public abstract class AbsStringRequest<T> extends Request<String> {
    public AbsStringRequest(Method method, String path) {
        super(method, path);
    }

    public AbsStringRequest(Method method, String path, String host) {
        super(method, path, host);
    }

    @NonNull
    protected final String parseString(NetworkResponse networkResponse) {
        try {
            return new String(networkResponse.data,
                    parseCharset(networkResponse.headers, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {
            return new String(networkResponse.data);
        }
    }

    public static String parseCharset(Map<String, List<String>> headers, String defaultCharset) {
        List<String> valueList = headers.get("Content-Type");
        String contentType = valueList != null && valueList.size() > 0 ?
                valueList.get(0) : null;
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }

        return defaultCharset;
    }
}
