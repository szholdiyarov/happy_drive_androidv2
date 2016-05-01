package kz.telecom.happydrive.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import kz.telecom.happydrive.ui.fragment.BaseFragment;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class Utils {
    private Utils() {
        throw new IllegalStateException(Utils.class.getSimpleName()
                + " class should never have an instance.");
    }

    public static boolean isEmpty(String text) {
        return text == null || text.trim().length() <= 0;
    }

    public static boolean isEmail(String text) {
        return Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    public static boolean isDomain(String text) {
        return Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-))")
                .matcher(text).matches();
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

    public static void openGallery(BaseFragment fragment, String intentTitle, String type, int code) {
        Intent intent = new Intent();
        intent.setType(type);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fragment.startActivityForResult(Intent.createChooser(intent, intentTitle), code);
    }

    public static File tempFile(String env, String extension) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = extension.toUpperCase() + "_" + timestamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(env);

        return File.createTempFile(fileName, "." + extension, storageDir);
    }

    public static File tempFileWithName(String env, String name, String extension) throws IOException {
        String fileName = extension.toUpperCase() + "_" + name + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(env);

        return File.createTempFile(fileName, "." + extension, storageDir);
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();

        return filePath;
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }

        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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

    public static void goToAppStore(Context context, final String packageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    public static int dipToPixels(float dp, DisplayMetrics dm) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    public static String fileExtension(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf("."));
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    public static void takeScreenshot(View view, File file) throws IOException {
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.flush();
        outputStream.close();
    }

    // Returns the URI path to the Bitmap displayed in specified ImageView
    public static Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else if (drawable instanceof GlideBitmapDrawable) {
            bmp = ((GlideBitmapDrawable) drawable).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
