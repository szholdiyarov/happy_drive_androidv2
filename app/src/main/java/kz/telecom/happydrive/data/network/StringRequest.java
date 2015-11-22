package kz.telecom.happydrive.data.network;

import android.support.annotation.NonNull;

import kz.telecom.happydrive.data.network.internal.AbsStringRequest;
import kz.telecom.happydrive.data.network.internal.NetworkResponse;

/**
 * Created by shgalym on 11/21/15.
 */
public class StringRequest extends AbsStringRequest<String> {
    public StringRequest(Method method, String path) {
        super(method, path);
    }

    public StringRequest(Method method, String path, String host) {
        super(method, path, host);
    }

    @NonNull
    @Override
    public Response<String> parseNetworkResponse(NetworkResponse networkResponse) {
        return new Response<>(parseString(networkResponse), null);
    }
}
