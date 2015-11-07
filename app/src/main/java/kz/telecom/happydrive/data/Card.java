package kz.telecom.happydrive.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class Card {
    public int catId = 0;
    public String firstName;
    public String lastName;
    public String middleName;
    public String position;
    public String companyName;
    public String phoneNumber;
    public String email;
    public String website;
    public String workAddress;
    public String about;

    public void wipe(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.apply();
    }

    public void save(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("catId", catId);
        edit.putString("firstName", firstName);
        edit.putString("lastName", lastName);
        edit.putString("middleName", middleName);
        edit.putString("position", position);
        edit.putString("companyName", companyName);
        edit.putString("phoneNumber", phoneNumber);
        edit.putString("email", email);
        edit.putString("website", website);
        edit.putString("workAddress", workAddress);
        edit.putString("about", about);

        edit.apply();
    }

    public static synchronized Card getUserCard(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt("catId", 0) > 0) {
            Card card = new Card();
            card.catId = prefs.getInt("catId", 0);
            card.firstName = prefs.getString("firstName", null);
            card.lastName = prefs.getString("lastName", null);
            card.middleName = prefs.getString("middleName", null);
            card.position = prefs.getString("position", null);
            card.companyName = prefs.getString("companyName", null);
            card.phoneNumber = prefs.getString("phoneNumber", null);
            card.email = prefs.getString("email", null);
            card.website = prefs.getString("website", null);
            card.workAddress = prefs.getString("workAddress", null);
            card.about = prefs.getString("about", null);

            return card;
        }

        return null;
    }

    public static class OnCardUpdateEvent {
    }
}
