package kz.telecom.happydrive.data;

import android.os.Parcel;

import com.fasterxml.jackson.databind.JsonNode;

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
    public boolean isFolder() {
        return false;
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
}
