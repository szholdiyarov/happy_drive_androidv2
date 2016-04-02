package kz.telecom.happydrive.data.network;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import kz.telecom.happydrive.data.network.internal.Caller;
import kz.telecom.happydrive.data.network.internal.NetworkResponse;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 11/18/15.
 */
public abstract class Request<T> implements Comparable<Request<T>> {
    public static final String DEFAULT_HOST = "https://apihd.happy-drive.kz"; //89.218.185.181 TEST

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    public final Method method;
    @NonNull
    public final String path;
    @NonNull
    public final String host;

    private Map<String, String> mHeaders;

    private Map<String, String> params;
    private Body<?> mBody;

    private Caller mCaller;
    private Response.Listener<?> mListener;

    private Integer mSequence;
    private Priority mPriority = Priority.NORMAL;

    public Request(Method method, String path) {
        this(method, path, DEFAULT_HOST);
    }

    public Request(Method method, String path, String host) {
        if (Utils.isEmpty(path)) {
            throw new IllegalArgumentException("path cannot be null");
        } else if (Utils.isEmpty(host)) {
            throw new IllegalArgumentException("host cannot be null");
        }

        this.method = method;
        this.path = path;
        this.host = host;
    }

    public void setBody(Body<?> body) {
        mBody = body;
    }


    public Body<?> getBody() {
        return mBody;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public void setHeaders(Map<String, String> headers) {
        mHeaders = headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void cancel() {
        if (mCaller != null) {
            mCaller.cancel();
        }
    }

    public boolean isCanceled() {
        return mCaller != null && mCaller.isCanceled();
    }

    public void setPriority(Priority priority) {
        mPriority = priority;
    }

    public Priority getPriority() {
        return mPriority;
    }

    void setCaller(Caller caller) {
        mCaller = caller;
    }

    Caller getCaller() {
        return mCaller;
    }

    void setListener(Response.Listener<?> listener) {
        mListener = listener;
    }

    Response.Listener<?> getListener() {
        return mListener;
    }

    void setSequence(Integer sequence) {
        mSequence = sequence;
    }

    Integer getSequence() {
        if (mSequence == null) {
            throw new IllegalStateException("Sequence not inited yet, " +
                    "or being called before execution");
        }

        return mSequence;
    }

    abstract public Response<T> parseNetworkResponse(NetworkResponse networkResponse);


    @Override
    public int compareTo(@NonNull Request<T> other) {
        Priority left = getPriority();
        Priority right = other.getPriority();

        return left == right ? mSequence - other.mSequence :
                right.ordinal() - left.ordinal();
    }

    static class Body<T> {
        public final T value;

        public Body(T value) {
            this.value = value;
        }
    }

    public static class StringBody extends Body<Map<String, String>> {
        public StringBody(Map<String, String> value) {
            super(value);
        }

        public StringBody add(String key, String value) {
            this.value.put(key, value);
            return this;
        }

        public static class Builder {
            final Map<String, String> value;

            public Builder() {
                value = new HashMap<>();
            }

            public Builder add(String key, String value) {
                this.value.put(key, value);
                return this;
            }

            public Builder remove(String key) {
                this.value.remove(key);
                return this;
            }

            public StringBody build() {
                return new StringBody(value);
            }
        }
    }

    public static class FileBody extends Body<File> {
        public static final String CONTENT_TYPE_RAW = "file/raw";

        public final String contentType;

        public FileBody(String contentType, File file) {
            super(file);
            this.contentType = contentType;
        }
    }
}
