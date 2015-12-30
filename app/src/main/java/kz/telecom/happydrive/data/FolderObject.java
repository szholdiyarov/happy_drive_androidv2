package kz.telecom.happydrive.data;

import android.os.Parcel;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by shgalym on 11/22/15.
 */
public class FolderObject extends ApiObject {
    static final String API_FOLDER_ID = "folder_id";
    static final String API_FOLDER_NAME = "name";
    static final String API_IS_PUBLIC = "is_public";
    static final String API_TIMESTAMP = "timestamp";

    public final int id;
    public final String name;
    public final boolean isPublic;
    public final long timestamp;

    private int mType = TYPE_INTERNAL_NOT_SET;

    public FolderObject(int id, String name, boolean isPublic, long timestamp) {
        this.id = id;
        this.name = name;
        this.isPublic = isPublic;
        this.timestamp = timestamp;
    }

    protected FolderObject(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.isPublic = in.readInt() != 0;
        this.timestamp = in.readLong();
    }

    FolderObject(JsonNode node) {
        this.id = node.get(API_FOLDER_ID).asInt(-1);
        if (this.id < 0) {
            throw new IllegalArgumentException("node has no " + API_FOLDER_ID + " value");
        }

        this.name = node.get(API_FOLDER_NAME).asText(null);
        this.isPublic = node.get(API_IS_PUBLIC).asBoolean(false);
        this.timestamp = node.get(API_TIMESTAMP).asLong(0);
    }

    @Override
    public int getType() {
        if (mType != TYPE_INTERNAL_NOT_SET) {
            return mType;
        }

        if ("фотографии".equalsIgnoreCase(name) ||
                "фото".equalsIgnoreCase(name)) {
            mType = TYPE_FOLDER_PHOTO;
        } else if ("видеозаписи".equalsIgnoreCase(name) ||
                "видео".equalsIgnoreCase(name)) {
            mType = TYPE_FOLDER_VIDEO;
        } else if ("музыка".equalsIgnoreCase(name)) {
            mType = TYPE_FOLDER_MUSIC;
        } else if ("документы".equalsIgnoreCase(name)) {
            mType = TYPE_FOLDER_DOCUMENT;
        }

        return mType;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(isPublic ? 1 : 0);
        dest.writeLong(timestamp);
    }

    public static final Creator<FolderObject> CREATOR = new Creator<FolderObject>() {
        @Override
        public FolderObject createFromParcel(Parcel in) {
            return new FolderObject(in);
        }

        @Override
        public FolderObject[] newArray(int size) {
            return new FolderObject[size];
        }
    };
}
