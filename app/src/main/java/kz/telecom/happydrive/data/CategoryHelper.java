package kz.telecom.happydrive.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.data.network.Response;
import kz.telecom.happydrive.data.network.internal.NetworkResponse;
import kz.telecom.happydrive.util.Logger;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by darkhan on 20.11.15.
 */
public class CategoryHelper {
    private static final String APT_PATH_GET_CATEGORIES = "card/categories/";

    static JsonNode getCategories() throws Exception {
        Request<String> request = new Request<String>(Request.Method.GET, APT_PATH_GET_CATEGORIES) {
            @Override
            public Response<String> parseNetworkResponse(NetworkResponse networkResponse) {
                try {
                    return new Response<>(new String(networkResponse.data, "UTF-8"), null);
                } catch (UnsupportedEncodingException e) {
                    return new Response<>(null, e);
                }
            }
        };

        Map<String, String> body = new HashMap<>();
        request.setBody(body);

        Response<String> response = NetworkManager.execute(request);
        Logger.i("TEST", "result: " + response.result);
        return new ObjectMapper().readTree(response.result);
    }

}
