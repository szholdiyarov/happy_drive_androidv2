package kz.telecom.happydrive.proxy;

import org.json.JSONObject;

/**
 * Created by darkhan on 07.11.15.
 */
public interface ResponseCallback {
    public abstract void done(JSONObject obj);
}
