package kz.telecom.happydrive.data.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.squareup.picasso.MemoryPolicy;

import kz.telecom.happydrive.R;

/**
 * Created by shgalym on 11/26/15.
 */
public class PicassoImageDecoder implements ImageDecoder {
    public PicassoImageDecoder() {
    }

    @Override
    public Bitmap decode(Context context, Uri uri) throws Exception {
        return NetworkManager.getPicasso()
                .load(uri)
                .tag(uri.getPath())
                .config(Bitmap.Config.RGB_565)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .get();
    }
}
