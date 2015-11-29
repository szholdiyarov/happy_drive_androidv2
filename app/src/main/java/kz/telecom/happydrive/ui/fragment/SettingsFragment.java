package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.ChangePasswordActivity;

/**
 * Created by darkhan on 24.11.15.
 */
public class SettingsFragment extends BaseFragment implements View.OnClickListener {
    private ImageButton ibShow;

    private boolean visible = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, parent, false);
    }

    @MainThread
    private void disableProgressBar() {
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.fragment_settings_progress_bar);
        progressBar.setVisibility(View.GONE);
        getView().findViewById(R.id.main_linear_layout).setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        BaseActivity activity = (BaseActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.action_settings);

        ibShow = (ImageButton) view.findViewById(R.id.ibShow);
        ibShow.setOnClickListener(this);
        view.findViewById(R.id.tvExit).setOnClickListener(this);
        view.findViewById(R.id.tvChangePwd).setOnClickListener(this);
        visible = User.currentUser().card.visible;
        int newImage = visible ? R.drawable.btn_switch_pressed : R.drawable.btn_switch_normal;
        ibShow.setImageResource(newImage);
        new Thread() {
            @Override
            public void run() {
                try {
                    Card mCard = ApiClient.getCard(User.currentUser().card.id);
                    User.currentUser().card.visible = mCard.visible;
                    visible = mCard.visible;
                } catch (Exception e) {
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int newImage = visible ? R.drawable.btn_switch_pressed : R.drawable.btn_switch_normal;
                        ibShow.setImageResource(newImage);
                        disableProgressBar();
                    }
                });
            }
        }.start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibShow:
                if (User.currentUser().card.getCategoryId() <= 0) {
                    Toast.makeText(getContext(), "Вы не можете открыть доступ к " +
                            "визитке пока не заполните обязательные поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                final boolean newStatus = !visible;
                final int newImage = newStatus ? R.drawable.btn_switch_pressed : R.drawable.btn_switch_normal;
                final int oldImage = !newStatus ? R.drawable.btn_switch_pressed : R.drawable.btn_switch_normal;
                ibShow.setImageResource(newImage);
                visible = !visible;
                new Thread() {
                    @Override
                    public void run() {
                        final boolean success = ApiClient.setVisibility(newStatus);
                        if (!success) {
                            BaseActivity activity = (BaseActivity) getActivity();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Roll back old status if query was unsuccessful.
                                    ibShow.setImageResource(oldImage);
                                    visible = !visible;
                                }
                            });
                        }
                    }

                }.start();
                break;
            case R.id.tvExit:
                DataManager.getInstance().bus.post(new User.SignedOutEvent());
                break;
            case R.id.tvChangePwd:
                getActivity().startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
            default:
                break;
        }
    }
}
