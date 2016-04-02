package kz.telecom.happydrive.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.ui.fragment.ChangeDomainFragment;

/**
 * Created by shgalym on 19.12.2015.
 */
public class ChangeDomainActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_domain);
        if (savedInstanceState == null) {
            replaceContent(new ChangeDomainFragment(),
                    false, FragmentTransaction.TRANSIT_NONE);
        }
    }

    @Override
    protected int getDefaultContentViewContainerId() {
        return R.id.activity_domain_view_container;
    }
}
