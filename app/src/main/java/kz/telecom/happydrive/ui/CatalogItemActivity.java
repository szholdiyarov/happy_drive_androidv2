package kz.telecom.happydrive.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.widget.ImageView;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
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

        Intent intent = getIntent();
        final Card card = intent.getParcelableExtra(EXTRA_CARD);
        if (savedInstanceState == null) {
            if (card != null) {
                replaceContent(CardDetailsFragment.newInstance(card, false),
                        false, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            } else {
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
        NetworkManager.getGlide()
                .load(card.getBackground())
                .signature(GlideCacheSignature.foreignCacheKey(card.getBackground()))
                .placeholder(R.drawable.default_bkg)
                .error(R.drawable.default_bkg)
                .centerCrop()
                .into((ImageView) findViewById(R.id.activity_catalog_img_view_background));
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
