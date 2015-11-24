package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.facebook.login.LoginManager;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darkhan on 24.11.15.
 */
public class SettingsFragment extends BaseFragment implements View.OnClickListener {


    private ImageButton ibShow;

    // TODO: Tell backend to add API for current visibility status.
    @Deprecated
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        BaseActivity activity = (BaseActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.action_settings);
        ibShow = (ImageButton) view.findViewById(R.id.ibShow);
        ibShow.setOnClickListener(this);
        view.findViewById(R.id.tvExit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibShow:
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
                User.currentUser().signOut();
                ((BaseActivity)getActivity()).onUserSignedOut(null);
                try {
                    LoginManager.getInstance().logOut();
                } catch (Exception ignored) {
                }

                break;
            default:
                break;
        }
    }
}
