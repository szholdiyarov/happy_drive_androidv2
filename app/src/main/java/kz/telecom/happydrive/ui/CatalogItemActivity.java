package kz.telecom.happydrive.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.ui.fragment.CardDetailsFragment;
import kz.telecom.happydrive.ui.fragment.CatalogItemFragment;

/**
 * Created by shgalym on 11/27/15.
 */
public class CatalogItemActivity extends BaseActivity {
    public static final String EXTRA_CATEGORY_ID = "extra:cat:id";
    public static final String EXTRA_CATEGORY_NAME = "extra:cat:name";
    public static final String EXTRA_CARD = "extra:card";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_item);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_toolbar);
        ActionBar actionBar = initToolbar(toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final Card card = intent.getParcelableExtra(EXTRA_CARD);
        if (savedInstanceState == null) {
            if (card != null) {
                actionBar.hide();
                replaceContent(CardDetailsFragment.newInstance(card),
                        false, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            } else {
                actionBar.show();
                Bundle bundle = new Bundle();
                bundle.putInt("categoryId", intent.getIntExtra(EXTRA_CATEGORY_ID, -1));
                bundle.putString("categoryName", intent.getStringExtra(EXTRA_CATEGORY_NAME));
                CatalogItemFragment catalogItemFragment = new CatalogItemFragment();
                catalogItemFragment.setArguments(bundle);
                replaceContent(catalogItemFragment, false,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        }
    }

    public void changeBackgroundImage(Card card) {
//        NetworkManager.getGlide()
//                .load(card.getBackground())
//                .signature(GlideCacheSignature.foreignCacheKey(card.getBackground()))
//                .placeholder(R.drawable.bkg_auth)
//                .error(R.drawable.bkg_auth)
//                .centerCrop()
//                .into((ImageView) findViewById(R.id.activity_catalog_img_view_background));
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
        return R.id.activity_catalog_item_view_container;
    }
}
