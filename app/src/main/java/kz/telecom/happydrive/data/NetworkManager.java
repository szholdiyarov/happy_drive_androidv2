package kz.telecom.happydrive.data;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class NetworkManager {
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String API_ENDPIONT = "http://hd.todo.kz";
    private static OkHttpClient sClient;

    static String post(String path, RequestBody body) throws IOException {
        Request request = new Request.Builder()
                .url(API_ENDPIONT + path)
                .post(body)
                .build();

        Response response = getClient().newCall(request).execute();
        return response.body().string();
    }

    protected static OkHttpClient getClient() {
        if (sClient == null) {
            sClient = new OkHttpClient();
        }

        return sClient;
    }
}
