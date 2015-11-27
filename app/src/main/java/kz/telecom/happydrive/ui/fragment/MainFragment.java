package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.MainActivity;
import kz.telecom.happydrive.ui.StorageActivity;
import kz.telecom.happydrive.util.GlideRoundedCornersTransformation;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 11/22/15.
 */
public class MainFragment extends BaseFragment implements View.OnClickListener {
    public static final String EXTRA_CARD = "extra:card";

    public static BaseFragment newInstance(Card card) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_CARD, card);

        BaseFragment fragment = new MainFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.app_name);

        TextView usernameTextView = (TextView) view.findViewById(R.id.user_name);
        TextView positionTextView = (TextView) view.findViewById(R.id.user_position);

        final Card card = getArguments().getParcelable(EXTRA_CARD);
        String lastName = card.getLastName();
        String username = card.getFirstName();
        if (!Utils.isEmpty(lastName)) {
            if (!Utils.isEmpty(username)) {
                username += " " + lastName;
            } else {
                username = lastName;
            }
        }

        usernameTextView.setText(username);
        positionTextView.setText(card.getPosition());

        view.findViewById(R.id.photo_image_view).setOnClickListener(this);
        view.findViewById(R.id.video_image_view).setOnClickListener(this);
        view.findViewById(R.id.music_image_view).setOnClickListener(this);
        view.findViewById(R.id.document_image_view).setOnClickListener(this);

        if (card.getAvatar() != null) {
            final ImageView userPhotoImageView = (ImageView) view.findViewById(R.id.user_photo);
            userPhotoImageView.post(new Runnable() {
                @Override
                public void run() {
                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.user_photo);
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    NetworkManager.getGlide()
                            .load(card.getAvatar())
                            .signature(GlideCacheSignature
                                    .ownerAvatarKey(card.getAvatar()))
                            .placeholder(drawable)
                            .error(drawable)
                            .bitmapTransform(new CenterCrop(getContext()),
                                    new GlideRoundedCornersTransformation(getContext(),
                                            Utils.dipToPixels(6f, dm), Utils.dipToPixels(2f, dm)))
                            .override(userPhotoImageView.getWidth(),
                                    userPhotoImageView.getHeight())
                            .into(userPhotoImageView);
                }
            });
        }

        if (card.getBackground() != null) {
            final ImageView backgroundImageView = (ImageView) view.findViewById(R.id.background);
            backgroundImageView.post(new Runnable() {
                @Override
                public void run() {
                    NetworkManager.getGlide()
                            .load(card.getBackground())
                            .signature(GlideCacheSignature
                                    .ownerBackgroundKey(card.getBackground()))
                            .override(backgroundImageView.getWidth(),
                                    backgroundImageView.getHeight())
                            .centerCrop()
                            .into(backgroundImageView);

                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.photo_image_view) {
//            intent = new Intent(getContext(), StorageActivity.class);
//            intent.putExtra(StorageActivity.EXTRA_TYPE, StorageActivity.EXTRA_TYPE_PHOTO);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
