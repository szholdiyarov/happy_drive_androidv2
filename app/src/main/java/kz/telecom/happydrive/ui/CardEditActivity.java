package kz.telecom.happydrive.ui;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.ui.fragment.BaseFragment;
import kz.telecom.happydrive.ui.fragment.CardEditParamsAdditionalFragment;
import kz.telecom.happydrive.ui.fragment.CardEditParamsMainFragment;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class CardEditActivity extends BaseActivity implements View.OnClickListener {
    private static final String EXTRA_CARD = "extra:card";

    private Button mBackButton;
    private Button mNextButton;
    private View mStepper1;
    private View mStepper2;

    private Card mCard;
    private File mAudioFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_edit);

        mBackButton = (Button) findViewById(R.id.stepper_btn_left);
        mNextButton = (Button) findViewById(R.id.stepper_btn_right);
        mStepper1 = findViewById(R.id.stepper1);
        mStepper2 = findViewById(R.id.stepper2);
        mBackButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);

        if (savedInstanceState != null) {
            mCard = savedInstanceState.getParcelable(EXTRA_CARD);
        } else {
            mCard = Card.copyOf(User.currentUser().card);
        }

        findViewById(R.id.activity_card_edit_toolbar_fake_back_btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

        TextView titleTextView = (TextView) findViewById(R.id.activity_card_edit_toolbar_fake_tv_title);
        titleTextView.setText(mCard.getCategoryId() > 0 ?
                "РЕДАКТИРОВАНИЕ" : "СОЗДАНИЕ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        if (savedInstanceState == null) {
            replaceContent(CardEditParamsMainFragment.newInstance(mCard), false,
                    FragmentTransaction.TRANSIT_NONE);
        }

        mStepper1.post(new Runnable() {
            @Override
            public void run() {
                updateStepperStates();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_CARD, mCard);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCard = savedInstanceState.getParcelable(EXTRA_CARD);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_card_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = false;
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            handled = true;
        } else if (itemId == R.id.action_save) {
            BaseFragment baseFragment = findDefaultContent();
            if (baseFragment instanceof IsSavable) {
                if (((IsSavable) baseFragment).readyForSave()) {
                    final ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage("Сохранение...");
                    dialog.setCancelable(false);
                    dialog.show();

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                ApiClient.updateCard(mCard);
                                if (mAudioFile != null && mAudioFile.length() > 0) {
                                    User.currentUser().changeAudio(mAudioFile);
                                }

                                if (User.currentUser().card.getCategoryId() <= 0) {
                                    ApiClient.setVisibility(true);
                                }

                                User.currentUser().updateCard();
                                CardEditActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataManager.getInstance().bus
                                                .post(new Card.OnCardUpdatedEvent(mCard));
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                            } catch (final Exception e) {
                                CardEditActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        if (e instanceof NoConnectionError) {
                                            Toast.makeText(CardEditActivity.this, "Нет подключения к интернету",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(CardEditActivity.this, "Произошла ошибка при сохранении визитки",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }
                    }.start();
                } else {
                    Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                }
            }

            handled = true;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.stepper_btn_left) {
            onBackPressed();
        } else if (view.getId() == R.id.stepper_btn_right) {
            BaseFragment baseFragment = findDefaultContent();
            if (baseFragment instanceof IsSavable) {
                if (((IsSavable) baseFragment).readyForSave()) {
                    if (mAudioFile == null) {
                        try {
                            mAudioFile = Utils.tempFile(Environment.DIRECTORY_MUSIC, "3gp");
                        } catch (Exception ignored) {
                            Toast.makeText(this, "Не удалось создать файл для аудио-приветствия. " +
                                    "Функция записи будет недоступна", Toast.LENGTH_SHORT).show();
                        }
                    }

                    replaceContent(CardEditParamsAdditionalFragment.newInstance(mCard, mAudioFile), true,
                            FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                } else {
                    Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                }
            }
        }

        mStepper1.post(new Runnable() {
            @Override
            public void run() {
                updateStepperStates();
            }
        });
    }

    private void updateStepperStates() {
        BaseFragment fragment = findDefaultContent();
        if (fragment instanceof CardEditParamsMainFragment) {
            mStepper1.setSelected(true);
            mStepper2.setSelected(false);
            mBackButton.setVisibility(View.INVISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
        } else {
            mStepper1.setSelected(false);
            mStepper2.setSelected(true);
            mBackButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected int getDefaultContentViewContainerId() {
        return R.id.activity_card_edit_view_container;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateStepperStates();
    }

    public interface IsSavable {
        boolean readyForSave();
    }
}
