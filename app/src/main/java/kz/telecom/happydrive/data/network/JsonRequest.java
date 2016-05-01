package kz.telecom.happydrive.data.network;


import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.JsonNode;

import kz.telecom.happydrive.data.network.internal.AbsJsonRequest;
import kz.telecom.happydrive.data.network.internal.NetworkResponse;

/**
 * Created by shgalym on 11/21/15.
 */
public class JsonRequest extends AbsJsonRequest<JsonNode> {
    public JsonRequest(Method method, String path) {
        super(method, path);
    }

    public JsonRequest(Method method, String path, String host) {
        super(method, path, host);
    }

    @NonNull
    @Override
    public Response<JsonNode> parseNetworkResponse(NetworkResponse networkResponse) {
        try {
            return new Response<>(parseJsonNode(networkResponse), null);
        } catch (Exception e) {
            return new Response<>(null, e);
        }
    }
}
