package kz.telecom.happydrive.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.ui.fragment.ChangePasswordFragment;

/**
 * Created by darkhan on 25.11.15.
 */
public class ChangePasswordActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_password);
        if (savedInstanceState == null) {
            replaceContent(new ChangePasswordFragment(),
                    false, FragmentTransaction.TRANSIT_NONE);
        }
    }

    @Override
    protected int getDefaultContentViewContainerId() {
        return R.id.activity_password_view_container;
    }
}
