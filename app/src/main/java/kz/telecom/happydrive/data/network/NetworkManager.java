package kz.telecom.happydrive.data.network;

import android.os.Process;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import kz.telecom.happydrive.data.network.internal.NetworkResponse;
import kz.telecom.happydrive.data.network.internal.OkHttpCallerWrapper;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class NetworkManager {
    private static NetworkManager sManager;
    private final OkHttpClient httpClient;

    private final PriorityBlockingQueue<Request<?>> mQueue = new PriorityBlockingQueue<>();
    private final AtomicInteger mSequenceGenerator = new AtomicInteger();

    public static <T> Response<T> execute(Request<T> request)
            throws NoConnectionError, MalformedURLException {
        sManager.prepareRequest(request);

        try {
            NetworkResponse networkResponse = request.getCaller().execute();
            return request.parseNetworkResponse(networkResponse);
        } catch (IOException ioe) {
            throw new NoConnectionError("no network connection", ioe);
        }
    }

    public synchronized static void enqueue(Request<?> request) {
        try {
            sManager.prepareRequest(request);
            sManager.mQueue.add(request);
        } catch (Exception e) {
        }
    }

    private void prepareRequest(Request<?> request) throws MalformedURLException {
        try {
            URI uri = new URI(request.host);
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme(uri.getScheme())
                    .host(uri.getHost())
                    .addPathSegment(request.path)
                    .build();

            RequestBody requestBody = null;
            if (request.method != Request.Method.GET) {
                FormEncodingBuilder builder = new FormEncodingBuilder();
                Map<String, String> body = request.getBody();
                if (body != null) {
                    for (Map.Entry<String, String> p : body.entrySet()) {
                        builder.add(p.getKey(), p.getValue());
                    }
                }

                requestBody = builder.build();
            }

            com.squareup.okhttp.Request okHttpRequest = new com.squareup.okhttp.Request
                    .Builder()
                    .url(httpUrl)
                    .method(request.method.name(), requestBody)
                    .build();

            request.setCaller(new OkHttpCallerWrapper(httpClient.newCall(okHttpRequest)));
            request.setSequence(mSequenceGenerator.getAndIncrement());
        } catch (URISyntaxException e) {
            throw new MalformedURLException(e.getLocalizedMessage());
        }
    }

    public static String post(String path, RequestBody body) throws IOException {
        com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                .url(Request.DEFAULT_HOST + path)
                .post(body)
                .build();

        com.squareup.okhttp.Response response = sManager.httpClient.newCall(request).execute();
        return response.body().string();
    }

    public static boolean init() {
        sManager = new NetworkManager();
        return true;
    }

    private NetworkManager() {
        httpClient = new OkHttpClient();
    }

    private class NetworkDispatcher extends Thread {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            while (true) {
                Request<?> request;
                try {
                    request = mQueue.take();
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
    }
}
