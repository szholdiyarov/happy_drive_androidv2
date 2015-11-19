package kz.telecom.happydrive;

import android.app.Application;
import android.support.v7.widget.AppCompatButton;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.data.network.Response;
import kz.telecom.happydrive.data.network.internal.NetworkResponse;
import kz.telecom.happydrive.util.Logger;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Galymzhan Sh on 10/27/15.
 */
public class HappyDriveApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build());

        Logger.setLevel(BuildConfig.DEBUG ?
                Logger.Level.VERBOSE : Logger.Level.WARNING);
        DataManager.init(this);
        NetworkManager.init();

        new Thread() {
            @Override
            public void run() {
                try {
                    Request<String> request2 = new Request<String>(Request.Method.GET, "card/get/") {
                        @Override
                        public Response<String> parseNetworkResponse(NetworkResponse networkResponse) {
                            return null;
                        }
                    };

                    NetworkManager.execute(request2);

                    Request<String> request = new Request<String>(Request.Method.POST, "auth/getToken/") {
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
                    body.put("email", "shgalym6@gmail.com");
                    body.put("password", "1234567");
                    request.setBody(body);

                    Response<String> response = NetworkManager.execute(request);
                    Logger.i("TEST", "result: " + response.result);
                } catch (Exception e) {
                    Logger.e("TEST", e.getLocalizedMessage(), e);
                }

//                try {
//                    final String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6OCwiZW1haWwiOiJzaGdhbHltNkBnbWFpbC5jb20ifQ.4OujkvyjLrL34nKv_Vu7E91g04NMq1Dm1vgebx7_kds";
//
//                    OkHttpClient httpClient = new OkHttpClient();
//                    httpClient.setCookieHandler(new CookieManager());
//
//                    Map<String, List<String>> cookies = new HashMap<>();
//                    cookies.put("Set-Cookie", Arrays.asList("auth-token=" + token));
//                    httpClient.getCookieHandler()
//                            .put(new URI("http://hd.todo.kz"), cookies);
//
//                    RequestBody body = new FormEncodingBuilder()
//                            .add("visible", "true")
//                            .build();
//
//                    Request request = new Request.Builder()
//                            .url("http://hd.todo.kz/card/visibility/")
//                            .post(body)
//                            .build();
//
//                    Response response = httpClient.newCall(request).execute();
//                    Logger.i("TEST", "response: " + response.body().string());
//                } catch (Exception e) {
//                    Logger.e("TEST", e.getLocalizedMessage(), e);
//                }
            }
        }.start();


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .addCustomStyle(AppCompatButton.class, R.attr.mediumButtonStyle)
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}
