package kz.telecom.happydrive.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 12.12.2015.
 */
public class LockedActivity extends BaseActivity implements View.OnClickListener {
    public static final String EXTRA_CAUSE = "extra:cause";
    public static final int CAUSE_PAYED_STATUS = 0;
    public static final int CAUSE_UPDATE_REQUIRED = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);

        ActionBar actionBar = initToolbar(R.id.layout_toolbar);
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                .getColor(this, R.color.colorPrimary)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        TextView textView = (TextView) findViewById(R.id.activity_locked_text);
        Button actionBtn1 = (Button) findViewById(R.id.activity_locked_btn);
        Button actionBtn2 = (Button) findViewById(R.id.activity_locked_btn_secondary);
        actionBtn1.setOnClickListener(this);
        actionBtn2.setOnClickListener(this);

        if (getIntent().getIntExtra(EXTRA_CAUSE, CAUSE_PAYED_STATUS)
                == CAUSE_PAYED_STATUS) {
            textView.setText("Ваш аккаунт заблокирован");
            actionBtn1.setText("Перейти на сайт");
            actionBtn2.setText("Выйти из аккаунта");
        } else {
            textView.setText("Версия приложения устарела.");
            actionBtn1.setText("Обновить");
            actionBtn2.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (getIntent().getIntExtra(EXTRA_CAUSE, CAUSE_PAYED_STATUS)
                == CAUSE_PAYED_STATUS) {
            if (view.getId() == R.id.activity_locked_btn) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://happy-drive.kz/user/changePlan?token="
                        + User.currentUser().token)));
            } else {
                User.currentUser().signOut();
                DataManager.getInstance().bus.post(new User.SignedOutEvent());
            }
        } else {
            final String packageName = getPackageName();
            Utils.goToAppStore(this, packageName);
        }
    }
}
