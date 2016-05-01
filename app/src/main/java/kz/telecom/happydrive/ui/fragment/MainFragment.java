package kz.telecom.happydrive.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.User;

/**
 * Created by shgalym on 4/23/16.
 */
public class MainFragment extends BaseFragment implements View.OnClickListener {
    private ViewPager mViewPager;
    private View mSwitcherCard;
    private View mSwitcherCloud;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSwitcherCard = view.findViewById(R.id.fragment_card_cloud_switcher_card);
        mSwitcherCard.setOnClickListener(this);

        mSwitcherCloud = view.findViewById(R.id.fragment_card_cloud_switcher_cloud);
        mSwitcherCloud.setOnClickListener(this);

        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new MainPagerAdapter(getChildFragmentManager()));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mSwitcherCard.setSelected(position == 0);
                mSwitcherCloud.setSelected(!mSwitcherCard.isSelected());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mViewPager.setCurrentItem(0, false);
        mSwitcherCard.setSelected(true);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.fragment_card_cloud_switcher_card) {
            mViewPager.setCurrentItem(0, true);
        } else if (viewId == R.id.fragment_card_cloud_switcher_cloud) {
            mViewPager.setCurrentItem(1, true);
        }
    }

    private class MainPagerAdapter extends FragmentStatePagerAdapter {
        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return position == 0 ? CardDetailsFragment.newInstance(User.currentUser().card, true)
                    : new CloudFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
