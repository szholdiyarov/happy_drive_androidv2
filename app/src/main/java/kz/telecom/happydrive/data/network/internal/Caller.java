package kz.telecom.happydrive.data.network.internal;

import java.io.IOException;

/**
 * Created by Galymzhan Sh on 11/18/15.
 */
public interface Caller {
    NetworkResponse execute() throws IOException;
    void cancel();
    boolean isCanceled();
}