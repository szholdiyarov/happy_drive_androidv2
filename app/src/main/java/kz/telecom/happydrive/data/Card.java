package kz.telecom.happydrive.data;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class Card implements Comparable<Card>, Parcelable {
    static final String API_KEY_CARD_ID = "card_id";
    static final String API_KEY_CATEGORY_ID = "category_id";
    static final String API_KEY_FIRST_NAME = "first_name";
    static final String API_KEY_LAST_NAME = "last_name";
    static final String API_KEY_PHONE = "phone";
    static final String API_KEY_EMAIL = "email";
    static final String API_KEY_ADDRESS = "address";
    static final String API_KEY_WORK_PLACE = "work_place";
    static final String API_KEY_POSITION = "position";
    static final String API_KEY_SHORT_DESC = "short_description";
    static final String API_KEY_FULL_DESC = "full_description";
    static final String API_KEY_AVATAR = "avatar";
    static final String API_KEY_AUDIO_FILE_URL = "audio_file_url";
    static final String API_KEY_BACKGROUND_FILE_URL = "background_file_url";
    static final String API_KEY_FACEBOOK = "facebook";
    static final String API_KEY_TWITTER = "twitter";
    static final String API_KEY_VKONTAKTE = "vkontakte";
    static final String API_KEY_INSTAGRAM = "instagram";
    static final String API_KEY_VISIBILITY = "visible";
    static final String API_PATH_GET_CARDS = "card/list/";


    public final int id;
    private int mCategoryId;
    private String mFirstName;
    private String mLastName;
    private String mPhone;
    private String mEmail;
    private String mAddress;
    private String mWorkPlace;
    private String mPosition;
    private String mShortDesc;
    private String mFullDesc;
    private String mAvatar;
    private String mBackground;
    public boolean visible;

    public final List<FolderObject> publicFolders;

    public Card(Map<String, Object> data, List<Map<String, Object>> folders) {
        if (data == null || !data.containsKey(API_KEY_CARD_ID)) {
            throw new IllegalArgumentException("data argument is null or it doesn't contain " +
                    API_KEY_CARD_ID + " value");
        }

        this.id = Utils.getValue(Integer.class, API_KEY_CARD_ID, -1, data);
        if (this.id < 0) {
            throw new IllegalArgumentException("data argument with null card id");
        }

        this.mCategoryId = Utils.getValue(Integer.class, API_KEY_CATEGORY_ID, -1, data);
        mFirstName = Utils.getValue(String.class, API_KEY_FIRST_NAME, null, data);
        mLastName = Utils.getValue(String.class, API_KEY_LAST_NAME, null, data);
        mPhone = Utils.getValue(String.class, API_KEY_PHONE, null, data);
        mEmail = Utils.getValue(String.class, API_KEY_EMAIL, null, data);
        mAddress = Utils.getValue(String.class, API_KEY_ADDRESS, null, data);
        mWorkPlace = Utils.getValue(String.class, API_KEY_WORK_PLACE, null, data);
        mPosition = Utils.getValue(String.class, API_KEY_POSITION, null, data);
        mShortDesc = Utils.getValue(String.class, API_KEY_SHORT_DESC, null, data);
        mFullDesc = Utils.getValue(String.class, API_KEY_FULL_DESC, null, data);
        mAvatar = Utils.getValue(String.class, API_KEY_AVATAR, null, data);
        mBackground = Utils.getValue(String.class, API_KEY_BACKGROUND_FILE_URL, null, data);
        visible = Utils.getValue(Boolean.class, API_KEY_VISIBILITY, false, data);

        publicFolders = new ArrayList<>(2);
        if (folders != null) {
            for (Map<String, Object> f : folders) {
                try {
                    publicFolders.add(new FolderObject(
                            Utils.getValue(Integer.class, FolderObject.API_FOLDER_ID, -1, f),
                            Utils.getValue(String.class, FolderObject.API_FOLDER_NAME, "", f),
                            true, 0
                    ));
                } catch (Exception ignored) {
                }
            }
        } else {
            int photoFolderId = Utils.getValue(Integer.class,
                    UserHelper.PREFS_KEY_PHOTO_FOLDER_ID, -1, data);
            if (photoFolderId > 0) {
                publicFolders.add(new FolderObject(photoFolderId, "Фотографии", true, 0));
            }

            int videoFolderId = Utils.getValue(Integer.class,
                    UserHelper.PREFS_KEY_VIDEO_FOLDER_ID, -1, data);
            if (videoFolderId > 0) {
                publicFolders.add(new FolderObject(videoFolderId, "Видеозаписи", true, 0));
            }
        }
    }

    protected Card(Parcel in) {
        id = in.readInt();
        mCategoryId = in.readInt();
        mFirstName = in.readString();
        mLastName = in.readString();
        mPhone = in.readString();
        mEmail = in.readString();
        mAddress = in.readString();
        mWorkPlace = in.readString();
        mPosition = in.readString();
        mShortDesc = in.readString();
        mFullDesc = in.readString();
        mAvatar = in.readString();
        mBackground = in.readString();
        publicFolders = in.readArrayList(getClass()
                .getClassLoader());
    }

    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    public void setCategoryId(int categoryId) {
        mCategoryId = categoryId;
    }

    public int getCategoryId() {
        return mCategoryId;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setPhone(String phoneNumber) {
        mPhone = phoneNumber;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setWorkPlace(String workPlace) {
        mWorkPlace = workPlace;
    }

    public String getWorkPlace() {
        return mWorkPlace;
    }

    public void setPosition(String position) {
        mPosition = position;
    }

    public String getPosition() {
        return mPosition;
    }

    public void setShortDesc(String shortDesc) {
        mShortDesc = shortDesc;
    }

    public String getShortDesc() {
        return mShortDesc;
    }

    public void setFullDesc(String fullDesc) {
        mFullDesc = fullDesc;
    }

    public String getFullDesc() {
        return mFullDesc;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setBackground(String background) {
        mBackground = background;
    }

    public String getBackground() {
        return mBackground;
    }

    @Override
    public int compareTo(@NonNull Card other) {
        return id - other.id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mCategoryId);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mPhone);
        dest.writeString(mEmail);
        dest.writeString(mAddress);
        dest.writeString(mWorkPlace);
        dest.writeString(mPosition);
        dest.writeString(mShortDesc);
        dest.writeString(mFullDesc);
        dest.writeString(mAvatar);
        dest.writeString(mBackground);
        dest.writeList(publicFolders);
    }

    static void saveUserCard(Card card, SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(API_KEY_CARD_ID, card.id);
        editor.putInt(API_KEY_CATEGORY_ID, card.mCategoryId);
        editor.putString(API_KEY_FIRST_NAME, card.mFirstName);
        editor.putString(API_KEY_LAST_NAME, card.mLastName);
        editor.putString(API_KEY_PHONE, card.mPhone);
        editor.putString(API_KEY_EMAIL, card.mEmail);
        editor.putString(API_KEY_ADDRESS, card.mAddress);
        editor.putString(API_KEY_WORK_PLACE, card.mWorkPlace);
        editor.putString(API_KEY_POSITION, card.mPosition);
        editor.putString(API_KEY_SHORT_DESC, card.mShortDesc);
        editor.putString(API_KEY_FULL_DESC, card.mFullDesc);
        editor.putString(API_KEY_AVATAR, card.mAvatar);
        editor.putString(API_KEY_BACKGROUND_FILE_URL, card.mBackground);
        editor.putBoolean(API_KEY_VISIBILITY, card.visible);

        for (FolderObject fo : card.publicFolders) {
            if ("фотографии".equalsIgnoreCase(fo.name)) {
                editor.putInt(UserHelper.PREFS_KEY_PHOTO_FOLDER_ID, fo.id);
            } else if ("видеозаписи".equalsIgnoreCase(fo.name)) {
                editor.putInt(UserHelper.PREFS_KEY_VIDEO_FOLDER_ID, fo.id);
            }
        }

        editor.apply();
    }

    static Map<String, Object> restoreUserCard(SharedPreferences prefs) {
        return (Map<String, Object>) prefs.getAll();
    }


    public static class OnCardUpdatedEvent {
        public final Card card;

        public OnCardUpdatedEvent(Card card) {
            this.card = card;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        return id == card.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
