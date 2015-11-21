package kz.telecom.happydrive.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Map;

import kz.telecom.happydrive.data.network.ResponseParseError;

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

    public final int id;
    private int mCategoryId;
    private String mFirstName;
    private String mLastName;
    private String mPhoneNumber;
    private String mEmail;
    private String mAddress;
    private String mWorkPlace;
    private String mPosition;
    private String mShortDesc;
    private String mFullDesc;
    private String mAvatar;

    public Card(int id) {
        this.id = id;
    }

    public Card(Map<String, Object> data) {
        if (data == null || !data.containsKey(API_KEY_CARD_ID)) {
            throw new IllegalArgumentException("data argument is null or it doesn't contain " +
                    API_KEY_CARD_ID + " value");
        }

        this.id = (Integer) data.get(API_KEY_CARD_ID);
    }

    protected Card(Parcel in) {
        id = in.readInt();
        mCategoryId = in.readInt();
        mFirstName = in.readString();
        mLastName = in.readString();
        mPhoneNumber = in.readString();
        mEmail = in.readString();
        mAddress = in.readString();
        mWorkPlace = in.readString();
        mPosition = in.readString();
        mShortDesc = in.readString();
        mFullDesc = in.readString();
        mAvatar = in.readString();
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

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
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
        dest.writeString(mPhoneNumber);
        dest.writeString(mEmail);
        dest.writeString(mAddress);
        dest.writeString(mWorkPlace);
        dest.writeString(mPosition);
        dest.writeString(mShortDesc);
        dest.writeString(mFullDesc);
        dest.writeString(mAvatar);
    }

    public static class OnCardUpdatedEvent {
        public final Card card;

        public OnCardUpdatedEvent(Card card) {
            this.card = card;
        }
    }
}
