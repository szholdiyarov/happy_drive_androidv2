package kz.telecom.happydrive.data;

import android.os.Parcelable;

/**
 * Created by shgalym on 11/22/15.
 */
public abstract class ApiObject implements Parcelable {
    public static final int TYPE_FOLDER = 0;
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_MUSIC = 3;
    public static final int TYPE_DOCUMENT = 4;
    public static final int TYPE_FILE_UNKNOWN = 5;
    static final int TYPE_FILE_INTERNAL_NOT_SET = 6;

    public abstract int getType();
}
