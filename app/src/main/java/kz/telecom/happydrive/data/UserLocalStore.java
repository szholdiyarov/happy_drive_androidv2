package kz.telecom.happydrive.data;

import android.content.SharedPreferences;
import android.content.Context;

/**
 * Created by darkhan on 07.11.15.
 */
public class UserLocalStore {

    public static final String SP_NAME = "userDetails";

    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context) {
        userLocalDatabase = context.getSharedPreferences(SP_NAME, 0); // 0 - private
    }

    public void storeUserData(User user) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("username", user.getEmail());
        spEditor.putString("password", user.getPassword());
        spEditor.putString("card_id", user.getCardId());
        spEditor.putString("token", user.getToken());
        spEditor.commit();
    }

    public String getToken() {
        return userLocalDatabase.getString("token", null);
    }

    public void logout() {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.commit();
    }

}
