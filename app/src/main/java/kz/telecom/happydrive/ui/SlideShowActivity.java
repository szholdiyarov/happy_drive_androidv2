package kz.telecom.happydrive.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixelcan.inkpageindicator.InkPageIndicator;

import kz.telecom.happydrive.R;

/**
 * Created by shgalym on 4/2/16.
 */
public class SlideShowActivity extends BaseActivity {
    public static final String SP_HAS_SHOWN = "slide:shown";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        setContentView(R.layout.activity_slide_show);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new SlideAdapter(getSupportFragmentManager()));

        InkPageIndicator indicator = (InkPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
    }

    private class SlideAdapter extends FragmentStatePagerAdapter {
        final int[][] data = {
                {R.drawable.slide_logo_1, R.string.slide_title_1, R.string.slide_text_1},
                {R.drawable.slide_logo_2, R.string.slide_title_2, R.string.slide_text_2},
                {R.drawable.slide_logo_3, R.string.slide_title_3, R.string.slide_text_3},
                {R.drawable.slide_logo_4, R.string.slide_title_4, R.string.slide_text_4},
                {0, R.string.slide_title_5, R.string.slide_text_5},
        };

        public SlideAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int[] data = this.data[position];
            return SlideFragment.newInstance(data[0], data[1], data[2]);
        }

        @Override
        public int getCount() {
            return data.length;
        }
    }

    public static class SlideFragment extends Fragment {
        static Fragment newInstance(int imageResId, int titleResId, int textResId) {
            Bundle args = new Bundle();
            args.putInt("image", imageResId);
            args.putInt("title", titleResId);
            args.putInt("text", textResId);
            Fragment fragment = new SlideFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_slide, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            Bundle args = getArguments();

            ImageView imageView = (ImageView) view.findViewById(R.id.slide_logo);
            imageView.setImageResource(args.getInt("image"));

            TextView titleTextView = (TextView) view.findViewById(R.id.slide_title);
            titleTextView.setText(args.getInt("title"));

            TextView textTextView = (TextView) view.findViewById(R.id.slide_text);
            textTextView.setText(args.getInt("text"));

            // последний слайд без иконки, значит, добавить кнопку начать
            if (args.getInt("image") == 0) {
                Button button = (Button) view.findViewById(R.id.slide_next);
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        sPrefs.edit().putBoolean(SlideShowActivity.SP_HAS_SHOWN, true).apply();

                        Intent intent = new Intent(getContext(), AuthActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
            }
        }
    }
}
