package kz.telecom.happydrive.data.network;

import android.os.Process;

import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

import kz.telecom.happydrive.data.network.internal.NetworkResponse;

/**
 * Created by Galymzhan Sh on 11/19/15.
 */
class NetworkDispatcher extends Thread {
    private final PriorityBlockingQueue<Request<?>> mQueue;
    private ResponsePoster mPoster;
    private boolean mQuit = false;

    private static int index = 0;

    NetworkDispatcher(PriorityBlockingQueue<Request<?>> queue, ResponsePoster poster) {
        super("kz.telecom.network.NetworkDispatcher:" + ++index);
        mQueue = queue;
        mPoster = poster;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            Request<?> request;
            try {
                request = mQueue.take();
            } catch (InterruptedException e) {
                if (mQuit) {
                    return;
                }

                continue;
            }

            try {
                NetworkResponse networkResponse = request.getCaller().execute();
                Response<?> response = request.parseNetworkResponse(networkResponse);
                dispatch(request, response);
            } catch (IOException e) {
                dispatch(request, new Response<>(null,
                        new NoConnectionError("no network error", e)));
            } catch (Exception e) {
                dispatch(request, new Response<>(null, e));
            }
        }
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }

    void dispatch(Request<?> request, Response<?> response) {
        mPoster.post(request, response);
    }
}
