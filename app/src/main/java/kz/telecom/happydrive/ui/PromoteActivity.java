package kz.telecom.happydrive.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.util.Locale;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.ui.fragment.BaseFragment;
import kz.telecom.happydrive.ui.fragment.PromoteEmailFragment;
import kz.telecom.happydrive.ui.fragment.PromoteFacebookFragment;
import kz.telecom.happydrive.ui.fragment.PromoteInstagramFragment;
import kz.telecom.happydrive.ui.fragment.PromoteLinkFragment;
import kz.telecom.happydrive.ui.fragment.PromoteWhatsAppFragment;

/**
 * Created by shgalym on 4/10/16.
 */
public class PromoteActivity extends BaseActivity {
    public static final String SHARED_TEXT = "shared:text";

    private TextView mToolbarTitleTextView;
    private TextView[] mIndicatorImageViews;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        setContentView(R.layout.activity_promote);

        mToolbarTitleTextView = (TextView) findViewById(R.id.activity_promote_toolbar_fake_tv_title);
        findViewById(R.id.activity_promote_toolbar_fake_back_btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        final FragmentStatePagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager(),
                new ViewData[]{
                        new ViewData(PromoteEmailFragment.class, "РАССЫЛКА", null),
                        new ViewData(PromoteFacebookFragment.class, "FACEBOOK", null),
                        new ViewData(PromoteWhatsAppFragment.class, "WHATSAPP", null),
                        new ViewData(PromoteInstagramFragment.class, "INSTAGRAM", null),
                        new ViewData(PromoteLinkFragment.class, "ПОДЕЛИТЬСЯ ССЫЛКОЙ", null),
                });

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);

        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            private int previousSelected;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (previousSelected < mIndicatorImageViews.length) {
                    mIndicatorImageViews[previousSelected].setSelected(false);
                }

                if (position < mIndicatorImageViews.length) {
                    mIndicatorImageViews[position].setSelected(true);
                }

                final String text = String.format(Locale.getDefault(),
                        "ШАГ%d: %s", position + 1, adapter.getPageTitle(position));
                mToolbarTitleTextView.setText(text);
                previousSelected = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };

        ViewGroup container = (ViewGroup) findViewById(R.id.indicator_container);
        mIndicatorImageViews = new TextView[container.getChildCount()];
        for (int i = 0, size = container.getChildCount(); i < size; i++) {
            mIndicatorImageViews[i] = (TextView) container.getChildAt(i);
        }

        viewPager.addOnPageChangeListener(onPageChangeListener);
        onPageChangeListener.onPageSelected(viewPager.getCurrentItem());
    }


    private static class SectionPagerAdapter extends FragmentStatePagerAdapter {
        private ViewData[] mData;

        public SectionPagerAdapter(FragmentManager fm, ViewData[] viewData) {
            super(fm);
            mData = viewData;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mData[position].title;
        }

        @Override
        public Fragment getItem(int position) {
            try {
                Constructor<?> constructor = mData[position].clz.getDeclaredConstructor();
                constructor.setAccessible(true);

                Fragment fragment = (Fragment) constructor.newInstance();
                fragment.setArguments(mData[position].args);
                return fragment;
            } catch (Exception e) {
                throw new IllegalStateException("Fragment creation failed", e);
            }
        }

        @Override
        public int getCount() {
            return mData != null ? mData.length : 0;
        }
    }

    private static class ViewData {
        private final Class<? extends BaseFragment> clz;
        private final CharSequence title;
        private final Bundle args;

        private ViewData(Class<? extends BaseFragment> clz, CharSequence title, Bundle args) {
            this.clz = clz;
            this.title = title;
            this.args = args;
        }
    }
}
