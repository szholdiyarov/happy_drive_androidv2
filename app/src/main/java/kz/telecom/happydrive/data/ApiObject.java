package kz.telecom.happydrive.data;

import android.os.Parcelable;

/**
 * Created by shgalym on 11/22/15.
 */
public abstract class ApiObject implements Parcelable {
    public static final int TYPE_FOLDER_PHOTO = 11;
    public static final int TYPE_FOLDER_VIDEO = 12;
    public static final int TYPE_FOLDER_MUSIC = 13;
    public static final int TYPE_FOLDER_DOCUMENT = 14;
    public static final int TYPE_FILE_PHOTO = 21;
    public static final int TYPE_FILE_VIDEO = 22;
    public static final int TYPE_FILE_MUSIC = 23;
    public static final int TYPE_FILE_DOCUMENT = 24;
    public static final int TYPE_FILE_UNKNOWN = 20;
    static final int TYPE_INTERNAL_NOT_SET = 0;

    public abstract int getType();
    public boolean isFolder() {
        return false;
    }
}
