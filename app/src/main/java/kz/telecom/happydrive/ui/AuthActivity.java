package kz.telecom.happydrive.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.ui.fragment.CatalogFragment;

/**
 * Created by Galymzhan Sh on 11/11/15.
 */
public class AuthActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_auth);
        if (savedInstanceState == null) {
            replaceContent(new CatalogFragment(), false,
                    FragmentTransaction.TRANSIT_NONE);
        }
    }

    @Override
    protected int getDefaultContentViewContainerId() {
        return R.id.activity_auth_view_container;
    }
}