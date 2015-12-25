package kz.telecom.happydrive.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.ui.fragment.StoragePhotoDetailsFragment;

/**
 * Created by shgalym on 25.12.2015.
 */
public class StorageDetailsActivity extends BaseActivity {
    public static final String EXTRA_FILE = "extra:file";
    public static final String EXTRA_CARD = "extra:card";
    public static final String EXTRA_TYPE = "extra:type";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_toolbar);
        ActionBar actionBar = initToolbar(toolbar);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            FileObject fileObject = intent.getParcelableExtra(EXTRA_FILE);
            final Card card = intent.getParcelableExtra(EXTRA_CARD);
            int type = intent.getIntExtra(EXTRA_TYPE, StorageActivity.TYPE_UNKNOWN);

            if (fileObject == null || card == null) {
                throw new IllegalStateException("EXTRA_FILE or EXTRA_CARD not passed");
            }

            replaceContent(StoragePhotoDetailsFragment.newInstance(fileObject),
                    false, FragmentTransaction.TRANSIT_NONE);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getDefaultContentViewContainerId() {
        return R.id.activity_storage_details_view_container;
    }
}
