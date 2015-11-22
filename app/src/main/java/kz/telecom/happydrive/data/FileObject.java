package kz.telecom.happydrive.data;

/**
 * Created by shgalym on 11/22/15.
 */
public class FileObject {
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
}
