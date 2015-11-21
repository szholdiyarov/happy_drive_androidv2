package kz.telecom.happydrive;

import android.app.Application;
import android.support.v7.widget.AppCompatButton;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.data.network.Response;
import kz.telecom.happydrive.data.network.internal.NetworkResponse;
import kz.telecom.happydrive.util.Logger;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Galymzhan Sh on 10/27/15.
 */
public class HappyDriveApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build());

        Logger.setLevel(BuildConfig.DEBUG ?
                Logger.Level.VERBOSE : Logger.Level.WARNING);
        DataManager.init(this);
        NetworkManager.init();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .addCustomStyle(AppCompatButton.class, R.attr.mediumButtonStyle)
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}
