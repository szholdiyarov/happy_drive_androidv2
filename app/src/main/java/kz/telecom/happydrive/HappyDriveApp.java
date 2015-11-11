package kz.telecom.happydrive;

import android.app.Application;
import android.support.v7.widget.AppCompatButton;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
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

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .addCustomStyle(AppCompatButton.class, R.attr.mediumButtonStyle)
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}
