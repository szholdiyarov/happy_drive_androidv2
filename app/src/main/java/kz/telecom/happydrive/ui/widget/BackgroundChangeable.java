package kz.telecom.happydrive.ui.widget;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by shgalym on 11.12.2015.
 */
public interface BackgroundChangeable {
    void changeBackground(Bitmap bitmap);
    ImageView getBackgroundImageView();
}