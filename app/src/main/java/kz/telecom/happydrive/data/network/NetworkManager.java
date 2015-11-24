package kz.telecom.happydrive.data.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.internal.NetworkResponse;
import kz.telecom.happydrive.data.network.internal.OkHttpCallerWrapper;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class NetworkManager {
    private static NetworkManager sManager;
    private final OkHttpClient httpClient;
    private final Picasso picasso;

    private final NetworkDispatcher[] mNetworkDispatchers = new NetworkDispatcher[4];
    private final ResponsePoster mResponsePoster = new ResponsePoster(new Handler(Looper.getMainLooper()));

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


    public synchronized static void enqueue(Request<?> request, Response.Listener<?> listener) {
        try {
            request.setListener(listener);
            sManager.prepareRequest(request);
            sManager.mQueue.add(request);
        } catch (Exception e) {
            sManager.mResponsePoster.post(request,
                    new Response<>(null, e));
        }
    }

    public static Picasso getPicasso() {
        return sManager.picasso;
    }

    public synchronized static void setCookie(String host, String name, String value)
            throws URISyntaxException, IOException {
        CookieHandler cookieHandler = sManager.httpClient.getCookieHandler();
        if (cookieHandler == null) {
            cookieHandler = new CookieManager();
        }

        Map<String, List<String>> cookies = new HashMap<>();
        cookies.put("Set-Cookie", Collections.singletonList(name + "=" + value));
        cookieHandler.put(new URI(host), cookies);

        sManager.httpClient.setCookieHandler(cookieHandler);
    }

    public synchronized static void removeCookie(String host)
            throws URISyntaxException, IOException {
        // TODO remove host specific cookie only
        sManager.httpClient.setCookieHandler(null);
    }

    private void prepareRequest(Request<?> request) throws MalformedURLException {
        try {
            URI uri = new URI(request.host);
            HttpUrl.Builder httpBuilder = new HttpUrl.Builder()
                    .scheme(uri.getScheme())
                    .host(uri.getHost())
                    .addPathSegment(request.path);

            String finalUrl;
            RequestBody requestBody = null;
            if (request.method != Request.Method.GET) {
                Request.Body<?> body = request.getBody();
                if (body instanceof Request.StringBody) {
                    Map<String, String> bodyValues = ((Request.StringBody) body).value;
                    FormEncodingBuilder builder = new FormEncodingBuilder();
                    for (Map.Entry<String, String> v : bodyValues.entrySet()) {
                        builder.add(v.getKey(), v.getValue());
                    }

                    requestBody = builder.build();
                } else if (body instanceof Request.FileBody) {
                    Request.FileBody fileBody = (Request.FileBody) body;
                    requestBody = RequestBody.create(MediaType.parse(fileBody.contentType),
                            fileBody.value);
                } else if (body == null) {
                    requestBody = RequestBody.create(null, new byte[0]);
                } else {
                    throw new RuntimeException("unsupported request body type");
                }
                finalUrl = (httpBuilder.build()).toString();
            } else {
                Map<String, String> params = request.getParams();
                String tempPath = "?";
                if (params != null) {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        tempPath += entry.getKey() + "=" + entry.getValue() + "&";
                    }
//                    httpBuilder.addPathSegment(tempPath);
                }
                finalUrl = (httpBuilder.build()).toString() + tempPath;
            }


            com.squareup.okhttp.Request.Builder builder = new com.squareup.okhttp.Request
                    .Builder()
                    .url(finalUrl)
                    .method(request.method.name(), requestBody);

            Map<String, String> headers = request.getHeaders();
            if (headers != null) {
                for (Map.Entry<String, String> h : headers.entrySet()) {
                    builder.addHeader(h.getKey(), h.getValue());
                }
            }

            request.setCaller(new OkHttpCallerWrapper(httpClient.newCall(builder.build())));
            request.setSequence(mSequenceGenerator.getAndIncrement());
        } catch (URISyntaxException e) {
            throw new MalformedURLException(e.getLocalizedMessage());
        }
    }

    public static boolean init(Context context) {
        sManager = new NetworkManager(context);
        NetworkDispatcher[] dispatchers = sManager.mNetworkDispatchers;
        for (int i = 0; i < dispatchers.length; i++) {
            dispatchers[i] = new NetworkDispatcher(sManager.mQueue,
                    sManager.mResponsePoster);
            dispatchers[i].start();
        }

        return true;
    }

    private NetworkManager(Context context) {
        httpClient = new OkHttpClient();
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                com.squareup.okhttp.Request.Builder builder = chain.request().newBuilder();
                if (User.currentUser() != null) {
                    builder.addHeader("Auth-Token", User.currentUser().token);
                }

                return chain.proceed(builder.build());
            }
        });

        picasso = new Picasso.Builder(context).downloader(new OkHttpDownloader(httpClient)).build();
    }

}
