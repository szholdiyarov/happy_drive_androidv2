package kz.telecom.happydrive.data.network.internal;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by Galymzhan Sh on 11/18/15.
 */
public class OkHttpCallerWrapper implements Caller {
    private final Call mCaller;

    public OkHttpCallerWrapper(Call caller) {
        mCaller = caller;
    }

    @Override
    public NetworkResponse execute() throws IOException {
        Response response = mCaller.execute();
        return new NetworkResponse(response.code(),
                response.body().bytes(),
                response.headers().toMultimap());
    }

    @Override
    public void cancel() {
        mCaller.cancel();
    }

    @Override
    public boolean isCanceled() {
        return mCaller.isCanceled();
    }
}
