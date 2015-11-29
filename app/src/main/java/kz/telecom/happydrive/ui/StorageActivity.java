package kz.telecom.happydrive.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.FolderObject;
import kz.telecom.happydrive.ui.fragment.StoragePhotoFragment;

/**
 * Created by shgalym on 11/22/15.
 */
public class StorageActivity extends BaseActivity {
    public static final String EXTRA_FOLDER = "extra:folder";
    public static final String EXTRA_CARD = "extra:card";
    public static final String EXTRA_TYPE = "extra:type";

    static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_MUSIC = 3;
    public static final int TYPE_DOCUMENT = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        Intent intent = getIntent();
        FolderObject folderObject = intent.getParcelableExtra(EXTRA_FOLDER);
        Card card = intent.getParcelableExtra(EXTRA_CARD);
        int type = intent.getIntExtra(EXTRA_TYPE, TYPE_UNKNOWN);

        if (folderObject == null || card == null) {
            throw new IllegalStateException("EXTRA_FOLDER or EXTRA_CARD not passed");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_toolbar);
        ActionBar actionBar = initToolbar(toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        if (savedInstanceState == null) {
            if (type == TYPE_PHOTO) {
                replaceContent(StoragePhotoFragment.newInstance(folderObject, card, type),
                        false, FragmentTransaction.TRANSIT_NONE);
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
    protected int getDefaultContentViewContainerId() {
        return R.id.activity_storage_view_container;
    }
}
