package com.example.android.kidventures.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by apoorva on 12/8/17.
 */

public class Place implements Parcelable {

    private String placeId;
    private String placeName;
    private ArrayList<String> placeTypes;
    private Float placeRating;
    private Integer placeNumOfReviews;
    private String placeRecommendedAges;
    private Float placeDistance;
    private Double placeLatitude;
    private Double placeLongitude;
    private String placeAddress;
    private String placePhoneNumber;
    private String placeWebsiteUrl;
    private Boolean isFavorite;
    private Boolean hasKidsMenu;
    private Boolean hasBathrooms;
    private Boolean hasEquipRental;
    private Boolean hasKidsSpecialPrograms;
    private Boolean hasKidsPlayArea;

    //Not to be included in Parcel
    private Bitmap placePhoto;

    public Place(String placeId, String placeName, String address, String phoneNumber,
                 String webUrl, Float distance, Double latitude, Double longitude,
                 ArrayList<String> placeTypes) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.placeAddress = address;
        this.placePhoneNumber = phoneNumber;
        this.placeWebsiteUrl = webUrl;
        this.placeDistance = distance;
        this.placeLatitude = latitude;
        this.placeLongitude = longitude;
        this.placeTypes = placeTypes;
    }

    public Place(String placeId, String placeName, ArrayList<String> placeTypes, Float rating,
                 Integer numberOfReviews, String recommendedAges, Float distance, Double latitude,
                 Double longitude, String address, String phoneNumber, String webUrl,
                 boolean kidsMenu, boolean bathrooms, boolean equipRental, boolean isFavorite,
                 boolean kidsPrograms, boolean kidsPlayArea, Bitmap placePhoto) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.placeTypes = placeTypes;
        this.placeRating = rating;
        this.placeNumOfReviews = numberOfReviews;
        this.placeRecommendedAges = recommendedAges;
        this.placeDistance = distance;
        this.placeLatitude = latitude;
        this.placeLongitude = longitude;
        this.placeAddress = address;
        this.placePhoneNumber = phoneNumber;
        this.placeWebsiteUrl = webUrl;
        this.isFavorite = isFavorite;
        this.hasKidsMenu = kidsMenu;
        this.hasBathrooms = bathrooms;
        this.hasEquipRental = equipRental;
        this.hasKidsSpecialPrograms = kidsPrograms;
        this.hasKidsPlayArea = kidsPlayArea;
        this.placePhoto = placePhoto;
    }

    /**
     * Constructor to use when re-constructing object
     * from a parcel
     *
     * @param in a parcel from which to read this object
     */
    public Place(Parcel in) {
        this.placeId = in.readString();
        this.placeName = in.readString();
        this.placeTypes = in.readArrayList(String.class.getClassLoader());
        this.placeRating = (Float) in.readValue(Float.class.getClassLoader());
        this.placeNumOfReviews = (Integer) in.readValue(Integer.class.getClassLoader());
        this.placeRecommendedAges = in.readString();
        this.placeDistance = (Float) in.readValue(Float.class.getClassLoader());
        this.placeLatitude = in.readDouble();
        this.placeLongitude = in.readDouble();
        this.placeAddress = in.readString();
        this.placePhoneNumber = in.readString();
        this.placeWebsiteUrl = in.readString();
        this.isFavorite = (Boolean) in.readSerializable();
        this.hasKidsMenu = (Boolean) in.readSerializable();
        this.hasBathrooms = (Boolean) in.readSerializable();
        this.hasEquipRental = (Boolean) in.readSerializable();
        this.hasKidsSpecialPrograms = (Boolean) in.readSerializable();
        this.hasKidsPlayArea = (Boolean) in.readSerializable();
    }

    public static final Parcelable.Creator<Place> CREATOR =
            new Parcelable.Creator<Place>() {
                public Place createFromParcel(Parcel in) { return new Place(in); }

                public Place[] newArray(int size) {return new Place[size];}
            };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.placeId);
        dest.writeString(this.placeName);
        dest.writeList(this.placeTypes);
        dest.writeValue(this.placeRating);
        dest.writeValue(this.placeNumOfReviews);
        dest.writeString(this.placeRecommendedAges);
        dest.writeValue(this.placeDistance);
        dest.writeDouble(this.placeLatitude);
        dest.writeDouble(this.placeLongitude);
        dest.writeString(this.placeAddress);
        dest.writeString(this.placePhoneNumber);
        dest.writeString(this.placeWebsiteUrl);
        dest.writeSerializable(this.isFavorite);
        dest.writeSerializable(this.hasKidsMenu);
        dest.writeSerializable(this.hasBathrooms);
        dest.writeSerializable(this.hasEquipRental);
        dest.writeSerializable(this.hasKidsSpecialPrograms);
        dest.writeSerializable(this.hasKidsPlayArea);
    }

    public String getPlaceId() { return this.placeId; }

    public String getPlaceName() { return this.placeName; }

    public ArrayList<String> getPlaceTypes() { return this.placeTypes; }

    public Float getPlaceRating() { return this.placeRating; }

    public Integer getPlaceNumOfReviews() { return this.placeNumOfReviews; }

    public String getPlaceRecommendedAges() { return this.placeRecommendedAges; }

    public Float getPlaceDistance() { return this.placeDistance; }

    public Double getPlaceLatitude() { return this.placeLatitude; }

    public Double getPlaceLongitude() { return this.placeLongitude; }

    public String getPlaceAddress() { return this.placeAddress; }

    public String getPlacePhoneNumber() { return this.placePhoneNumber; }

    public String getPlaceWebsiteUrl() { return this.placeWebsiteUrl; }

    public Boolean getIsFavorite() { return this.isFavorite; }

    public Boolean getHasKidsMenu() { return  this.hasKidsMenu; }

    public Boolean getHasBathrooms() { return  this.hasBathrooms; }

    public Boolean getHasKidsEquipmentRental() { return this.hasEquipRental; }

    public Boolean getHasKidsSpecialPrograms() { return this.hasKidsSpecialPrograms; }

    public Boolean getHasKidsPlayArea() { return this.hasKidsPlayArea; }

    public Bitmap getPlacePhoto() { return this.placePhoto; }

    public void setIsFavorite(boolean isFavorite) { this.isFavorite = isFavorite; }

    public void setPlacePhoto(Bitmap photo) {
        this.placePhoto = photo;
    }

    public void setPlaceRating(Float placeRating) { this.placeRating = placeRating; }

    public void setPlaceNumOfReviews(Integer numOfReviews) {
        this.placeNumOfReviews = numOfReviews;
    }

    public void setFirebasePlaceDetails(Float placeRating, Integer placeNumOfReviews,
                                        String recommendedAges, Boolean hasKidsMenu,
                                        Boolean hasBathrooms, Boolean hasEquipRental,
                                        Boolean hasKidsPrograms, Boolean hasKidsPlayArea) {
        this.placeRating = placeRating;
        this.placeNumOfReviews = placeNumOfReviews;
        this.placeRecommendedAges = recommendedAges;
        this.hasKidsMenu = hasKidsMenu;
        this.hasBathrooms = hasBathrooms;
        this.hasEquipRental = hasEquipRental;
        this.hasKidsSpecialPrograms = hasKidsPrograms;
        this.hasKidsPlayArea = hasKidsPlayArea;
    }

}
