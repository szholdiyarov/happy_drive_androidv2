package kz.telecom.happydrive;

import android.app.Application;
import android.support.v7.widget.AppCompatButton;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;

import io.fabric.sdk.android.Fabric;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.util.Logger;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Galymzhan Sh on 10/27/15.
 */
public class HappyDriveApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "Ui29l0hA6jcp2x8wQ1I8DaWhod3thG5qYBFE3g9z", "KxuNX9dJtSeqcgsu9MTZQAFSwRT7pR1V2vrZU65S");
        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });

        Fabric.with(this, new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build());

        Logger.setLevel(BuildConfig.DEBUG ?
                Logger.Level.VERBOSE : Logger.Level.WARNING);
        NetworkManager.init(this);
        DataManager.init(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .addCustomStyle(AppCompatButton.class, R.attr.mediumButtonStyle)
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}
