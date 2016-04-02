package kz.telecom.happydrive.data;

import android.content.Context;

import com.squareup.otto.Bus;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class DataManager {
    final Context context;
    public final Bus bus;

    private static DataManager sDataManager;

    public static DataManager getInstance() {
        if (sDataManager == null) {
            throw new IllegalStateException(DataManager.class.getSimpleName() +
                    " not inited yet. Call init() first");
        }

        return sDataManager;
    }

    public static DataManager init(Context context) {
        if (sDataManager != null) {
            throw new IllegalStateException(DataManager.class.getSimpleName() +
                    " has already been inited");
        }

        return sDataManager = new DataManager(context.getApplicationContext());
    }

    private DataManager(Context context) {
        this.context = context;
        this.bus = new Bus();
    }
}
