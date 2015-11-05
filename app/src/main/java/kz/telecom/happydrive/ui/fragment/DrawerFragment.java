package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.ui.MainActivity;

/**
 * Created by Galymzhan Sh on 10/29/15.
 */
public class DrawerFragment extends BaseFragment {
    private Callback mCallback;

    private static final int TEMP_VERSION_1 = 0;
    private static final int TEMP_VERSION_2 = 1;

    private int mVer = TEMP_VERSION_1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final NavigationView navi = (NavigationView) view.findViewById(R.id.fragment_main_navigation_view);
        View headerView = navi.inflateHeaderView(R.layout.layout_drawer_header);
        navi.inflateMenu(mVer == TEMP_VERSION_1 ?
                R.menu.drawer_main : R.menu.drawer_temp_main);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int viewId = v.getId();
                if (viewId == R.id.temp_drawer_v1) {
                    if (mVer != TEMP_VERSION_1) {
                        mVer = TEMP_VERSION_1;
                        navi.getMenu().clear();
                        navi.inflateMenu(R.menu.drawer_main);
                    }
                } else if (viewId == R.id.temp_drawer_v2) {
                    if (mVer != TEMP_VERSION_2) {
                        mVer = TEMP_VERSION_2;
                        navi.getMenu().clear();
                        navi.inflateMenu(R.menu.drawer_temp_main);
                    }
                }
            }
        };

        headerView.findViewById(R.id.temp_drawer_v1).setOnClickListener(listener);
        headerView.findViewById(R.id.temp_drawer_v2).setOnClickListener(listener);

        navi.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                return item.isCheckable() && mCallback.onDrawerMenuItemSelected(item.getItemId());
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        try {
            mCallback = (MainActivity) context;
        } catch (ClassCastException ignored) {
            throw new IllegalStateException("Context must implement " +
                    DrawerFragment.class.getSimpleName() + "." + Callback.class.getSimpleName());
        }

        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public interface Callback {
        boolean onDrawerMenuItemSelected(int itemId);
    }
}
