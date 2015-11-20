package kz.telecom.happydrive.data.network;

import android.support.annotation.NonNull;

import java.util.Map;

import kz.telecom.happydrive.data.network.internal.Caller;
import kz.telecom.happydrive.data.network.internal.NetworkResponse;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 11/18/15.
 */
public abstract class Request<T> implements Comparable<Request<T>> {
    public static final String DEFAULT_HOST = "http://hd.todo.kz";

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

    private Map<String, String> mParameters;
    private Map<String, String> mBody;

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

    public void setParameters(Map<String, String> parameters) {
        mParameters = parameters;
    }

    public Map<String, String> getParameters() {
        return mParameters;
    }

    public void setBody(Map<String, String> body) {
        mBody = body;
    }

    public Map<String, String> getBody() {
        return mBody;
    }

    public void cancel() {
        mCaller.cancel();
    }

    public boolean isCanceled() {
        return mCaller.isCanceled();
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
}
