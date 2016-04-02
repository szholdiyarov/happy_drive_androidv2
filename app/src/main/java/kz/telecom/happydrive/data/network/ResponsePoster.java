package kz.telecom.happydrive.data.network;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * Created by Galymzhan Sh on 11/19/15.
 */
public class ResponsePoster {
    private final Executor mExecutor;

    public ResponsePoster(final Handler handler) {
        mExecutor = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                handler.post(command);
            }
        };
    }

    public void post(Request<?> request, Response<?> response) {
        mExecutor.execute(new ExecutorRunnable(request, response));
    }

    private static class ExecutorRunnable implements Runnable {
        private final Request mRequest;
        private final Response mResponse;

        ExecutorRunnable(Request request, Response response) {
            mRequest = request;
            mResponse = response;
        }

        @Override
        public void run() {
            if (mRequest.isCanceled()) {
                return;
            }

            mRequest.getListener().onResponse(mResponse, mResponse.exception);
        }
    }
}
