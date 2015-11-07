package kz.telecom.happydrive.data;

import com.squareup.otto.Bus;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class OttoBus {
    private static Bus sBus = new Bus();

    public static Bus getInstance() {
        return sBus;
    }
}
