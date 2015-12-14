package kz.telecom.happydrive.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.ui.fragment.CardDetailsFragment;
import kz.telecom.happydrive.ui.fragment.CatalogItemFragment;
import kz.telecom.happydrive.ui.widget.BackgroundChangeable;

/**
 * Created by shgalym on 11/27/15.
 */
public class CatalogItemActivity extends BaseActivity implements BackgroundChangeable {
    public static final String EXTRA_CATEGORY_ID = "extra:cat:id";
    public static final String EXTRA_CATEGORY_NAME = "extra:cat:name";
    public static final String EXTRA_CARD = "extra:card";

    private ImageView mBackgroundImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_item);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        mBackgroundImageView = (ImageView) findViewById(R.id.activity_catalog_item_img_view_background);
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_toolbar);
        ActionBar actionBar = initToolbar(toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Card card = intent.getParcelableExtra(EXTRA_CARD);
            if (card != null) {
                replaceContent(CardDetailsFragment.newInstance(card),
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
    public void changeBackground(Bitmap bitmap) {
        mBackgroundImageView.setImageBitmap(bitmap);
    }

    @Override
    public ImageView getBackgroundImageView() {
        return mBackgroundImageView;
    }

    @Override
    protected int getDefaultContentViewContainerId() {
        return R.id.activity_catalog_item_view_container;
    }
}
