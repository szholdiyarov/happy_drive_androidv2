package kz.telecom.happydrive.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.ui.fragment.AuthFragment;

/**
 * Created by Galymzhan Sh on 11/11/15.
 */
public class AuthActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sPrefs.getBoolean(SlideShowActivity.SP_HAS_SHOWN, false)) {
            Intent intent = new Intent(this, SlideShowActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_auth);
        if (savedInstanceState == null) {
            replaceContent(new AuthFragment(), false, FragmentTransaction.TRANSIT_NONE);
        }
    }

    @Override
    protected int getDefaultContentViewContainerId() {
        return R.id.activity_auth_view_container;
    }
}