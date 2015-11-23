package kz.telecom.happydrive.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import kz.telecom.happydrive.ui.fragment.BaseFragment;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class Utils {
    public static boolean isEmpty(String text) {
        return text == null || text.trim().length() <= 0;
    }

    public static boolean openCamera(BaseFragment fragment, File file, int code) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(fragment.getContext().getPackageManager()) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            fragment.startActivityForResult(intent, code);
            return true;
        }

        return false;
    }

    public static void openGallery(Activity activity, String intentTitle, String type, int code) {
        Intent intent = new Intent();
        intent.setType(type);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, intentTitle), code);
    }

    public static File tempFileWithNow(Context context) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "jpeg_" + timestamp + "_";
        File storageDir = context.getCacheDir();

        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    public static <T> T getValue(Class<?> cls, String key, T fallback, Map<String, Object> map) {
        Object obj = map.get(key);
        if (obj == null) {
            return fallback;
        }

        if (cls.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        }

        return fallback;
    }

    private Utils() {
        throw new IllegalStateException(Logger.class.getSimpleName()
                + " class should never have an instance.");
    }
}
