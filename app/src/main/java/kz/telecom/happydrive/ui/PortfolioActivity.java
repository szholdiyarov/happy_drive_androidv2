package kz.telecom.happydrive.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.fragment.PortfolioPhotoFragment;

/**
 * Created by shgalym on 11/22/15.
 */
public class PortfolioActivity extends BaseActivity {
    public static final String EXTRA_TYPE = "extra:type";
    public static final int EXTRA_TYPE_PHOTO = 1;
    public static final int EXTRA_TYPE_VIDEO = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        Card card = User.currentUser().card;
        int type = getIntent().getIntExtra(EXTRA_TYPE, EXTRA_TYPE_PHOTO);

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_toolbar);
        ActionBar actionBar = initToolbar(toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(type == EXTRA_TYPE_PHOTO ?
                "Фотографии" : "Видеозаписи");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        if (savedInstanceState == null) {
            replaceContent(type == EXTRA_TYPE_PHOTO ?
                            PortfolioPhotoFragment.newInstance(card) :
                            PortfolioPhotoFragment.newInstance(card),
                    false, FragmentTransaction.TRANSIT_NONE);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = false;
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            handled = true;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    @Override
    protected int getDefaultContentViewContainerId() {
        return R.id.activity_portfolio_view_container;
    }
}
