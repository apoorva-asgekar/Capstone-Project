package com.example.android.kidventures.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by apoorva on 1/17/18.
 */

public class UserProfile implements Parcelable{

    public static final String AGE0TO2 = "hasChildAge0to2";
    public static final String AGE2TO5 = "hasChildAge2to5";
    public static final String AGE5TO8 = "hasChildAge5to8";
    public static final String AGE8TO12 = "hasChildAge8to12";
    public static final String AGE12PLUS = "hasChildAge12Plus";

    private String userFirebaseUid;
    private String userDisplayName;
    private String userName;
    private String userEmailAddress;
    private String userPhotoUrl;
    private HashMap<String, Object> userChildAges = getDefaultChildAges();

    public  UserProfile() {}

    public UserProfile(String userFirebaseUid, String userDisplayName, String userName,
                       String userEmailAddress, String userPhotoUrl,
                       HashMap<String, Object> childAges) {
        this.userFirebaseUid = userFirebaseUid;
        this.userDisplayName = userDisplayName;
        this.userName = userName;
        this.userEmailAddress = userEmailAddress;
        this.userPhotoUrl = userPhotoUrl;
        if(childAges == null) {
            this.userChildAges = getDefaultChildAges();
        } else {
            this.userChildAges = childAges;
        }
    }

    /**
     * Constructor to use when re-constructing object
     * from a parcel
     *
     * @param in a parcel from which to read this object
     */
    public UserProfile(Parcel in) {
        this.userFirebaseUid = in.readString();
        this.userDisplayName = in.readString();
        this.userName = in.readString();
        this.userEmailAddress = in.readString();
        this.userPhotoUrl = in.readString();
        this.userChildAges = (HashMap<String, Object>) in.readSerializable();
    }

    public static final Parcelable.Creator<UserProfile> CREATOR =
            new Parcelable.Creator<UserProfile>() {
                public UserProfile createFromParcel(Parcel in) { return new UserProfile(in); }

                public UserProfile[] newArray(int size) {return new UserProfile[size];}
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userFirebaseUid);
        dest.writeString(this.userDisplayName);
        dest.writeString(this.userName);
        dest.writeString(this.userEmailAddress);
        dest.writeString(this.userPhotoUrl);
        dest.writeSerializable(this.userChildAges);
    }

    public String getUserFirebaseUid() { return this.userFirebaseUid; }

    public String getUserDisplayName() { return this.userDisplayName; };

    public String getUserName() { return this.userName; }

    public String getUserEmailAddress() { return this.userEmailAddress; }

    public String getUserPhotoUrl() { return this.userPhotoUrl; }

    public HashMap<String, Object> getUserChildAges() { return this.userChildAges; }

    public void setUserPhotoUrl(String newPhotoUrl) {
        this.userPhotoUrl = newPhotoUrl;
    }

    public void setUserChildAges(HashMap<String, Object> newUserChildAges) {
        this.userChildAges = newUserChildAges;
    }

    public void setUserName(String newUserName) {
        this.userName = newUserName;
    }

    public static HashMap<String, Object> getDefaultChildAges() {
        HashMap<String, Object> defaultChildAges = new HashMap<>();
        defaultChildAges.put(AGE0TO2, false);
        defaultChildAges.put(AGE2TO5, false);
        defaultChildAges.put(AGE5TO8, false);
        defaultChildAges.put(AGE8TO12, false);
        defaultChildAges.put(AGE12PLUS, false);

        return defaultChildAges;
    }
}
