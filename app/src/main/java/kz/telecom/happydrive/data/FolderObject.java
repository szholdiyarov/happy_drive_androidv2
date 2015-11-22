package kz.telecom.happydrive.data;

/**
 * Created by shgalym on 11/22/15.
 */
public class FolderObject {
    public final int id;
    public final String name;
    public final boolean isPublic;
    public final long timestamp;

    public FolderObject(int id, String name, boolean isPublic, long timestamp) {
        this.id = id;
        this.name = name;
        this.isPublic = isPublic;
        this.timestamp = timestamp;
    }
}
