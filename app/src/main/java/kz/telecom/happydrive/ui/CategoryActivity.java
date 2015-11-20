package kz.telecom.happydrive.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.ui.fragment.AuthFragment;
import kz.telecom.happydrive.ui.fragment.CardEditParamsFragment;
import kz.telecom.happydrive.ui.fragment.CatalogFragment;

/**
 * Created by darkhan on 20.11.15.
 */
public class CategoryActivity extends BaseActivity  {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.layout_activity_category);
        if (savedInstanceState == null) {
            replaceContent(new CatalogFragment(), false,
                    FragmentTransaction.TRANSIT_NONE);
        }

    }

    @Override
    protected int getDefaultContentViewContainerId() {
        return R.id.category_activity_view_container;
    }

}
