package kz.telecom.happydrive.data;

import android.os.Parcel;
import android.webkit.MimeTypeMap;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

import kz.telecom.happydrive.util.DefaultValueHashMap;

/**
 * Created by shgalym on 11/22/15.
 */
public class FileObject extends ApiObject {
    static final String API_FILE_ID = "file_id";
    static final String API_NAME = "name";
    static final String API_SIZE = "size";
    static final String API_TIMESTAMP = "timestamp";
    static final String API_URL = "url";

    public final int id;
    public final String name;
    public final long size;
    public final long timestamp;
    public final String url;

    private String mExtension;
    private int mType = TYPE_INTERNAL_NOT_SET;

    private static final Map<String, Integer> FILE_TYPES =
            new DefaultValueHashMap<String, Integer>(TYPE_FILE_UNKNOWN) {{
                // photo file types
                put("jpg", TYPE_FILE_PHOTO);
                put("jpeg", TYPE_FILE_PHOTO);
                put("png", TYPE_FILE_PHOTO);
                put("bmp", TYPE_FILE_PHOTO);
                put("tiff", TYPE_FILE_PHOTO);
                put("gif", TYPE_FILE_PHOTO);

                // video file types
                put("flv", TYPE_FILE_VIDEO);
                put("webm", TYPE_FILE_VIDEO);
                put("mkv", TYPE_FILE_VIDEO);
                put("avi", TYPE_FILE_VIDEO);
                put("mov", TYPE_FILE_VIDEO);
                put("wmv", TYPE_FILE_VIDEO);
                put("mp4", TYPE_FILE_VIDEO);
                put("m4p", TYPE_FILE_VIDEO);
                put("mpg", TYPE_FILE_VIDEO);
                put("mp2", TYPE_FILE_VIDEO);
                put("mpeg", TYPE_FILE_VIDEO);
                put("mpe", TYPE_FILE_VIDEO);
                put("mpv", TYPE_FILE_VIDEO);
                put("m2v", TYPE_FILE_VIDEO);
                put("3gp", TYPE_FILE_VIDEO);

                // music file types
                put("aac", TYPE_FILE_MUSIC);
                put("amr", TYPE_FILE_MUSIC);
                put("flac", TYPE_FILE_MUSIC);
                put("m4p", TYPE_FILE_MUSIC);
                put("mp3", TYPE_FILE_MUSIC);
                put("ogg", TYPE_FILE_MUSIC);
                put("wav", TYPE_FILE_MUSIC);
                put("wma", TYPE_FILE_MUSIC);

                // doc file types
                put("doc", TYPE_FILE_DOCUMENT);
                put("docx", TYPE_FILE_DOCUMENT);
                put("xls", TYPE_FILE_DOCUMENT);
                put("xlsx", TYPE_FILE_DOCUMENT);
                put("pdf", TYPE_FILE_DOCUMENT);
            }};

    public FileObject(int id, String name, long size, long timestamp, String url) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.timestamp = timestamp;
        this.url = url;
    }

    protected FileObject(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.size = in.readLong();
        this.timestamp = in.readLong();
        this.url = in.readString();
    }

    public FileObject(JsonNode node) {
        this.id = node.get(API_FILE_ID).asInt(-1);
        if (this.id < 0) {
            throw new IllegalArgumentException("node has no " + API_FILE_ID + " value");
        }

        this.name = node.get(API_NAME).asText(null);
        this.size = node.get(API_SIZE).asLong(-1);
        this.timestamp = node.get(API_TIMESTAMP).asLong(0);
        this.url = node.get(API_URL).asText(null);
    }

    @Override
    public int getType() {
        if (mType != TYPE_INTERNAL_NOT_SET) {
            return mType;
        }

        mType = FILE_TYPES.get(getExtension());
        return mType;
    }

    public String getExtension() {
        if (mExtension != null) {
            return mExtension;
        }

        String[] comps = name.split("\\.");
        if (comps.length > 0) {
            mExtension = comps[comps.length - 1].toLowerCase();

            if (mExtension.length() > 5) {
                mExtension = "jpg";
            }
        }

        return mExtension;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeLong(size);
        dest.writeLong(timestamp);
        dest.writeString(url);
    }

    public static final Creator<FileObject> CREATOR = new Creator<FileObject>() {
        @Override
        public FileObject createFromParcel(Parcel in) {
            return new FileObject(in);
        }

        @Override
        public FileObject[] newArray(int size) {
            return new FileObject[size];
        }
    };
}
