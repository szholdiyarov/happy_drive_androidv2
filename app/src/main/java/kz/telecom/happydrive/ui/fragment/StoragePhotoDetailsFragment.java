package kz.telecom.happydrive.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.ui.StorageDetailsActivity;

/**
 * Created by shgalym on 25.12.2015.
 */
public class StoragePhotoDetailsFragment extends BaseFragment {
    public static BaseFragment newInstance(FileObject fileObject) {
        if (fileObject == null) {
            throw new IllegalArgumentException("fileObject is null");
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(StorageDetailsActivity.EXTRA_FILE, fileObject);
        BaseFragment fragment = new StoragePhotoDetailsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_storage_photo_details, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final FileObject fileObject = getArguments().getParcelable(StorageDetailsActivity.EXTRA_FILE);
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view
                .findViewById(R.id.fragment_storage_photo_details_sub_image_view);
        imageView.setMaxScale(10f);
        NetworkManager.getGlide()
                .load(fileObject.url)
                .asBitmap()
                .into(new ViewTarget<SubsamplingScaleImageView, Bitmap>(imageView) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        imageView.setImage(ImageSource.bitmap(resource));
                    }
                });
    }
}
