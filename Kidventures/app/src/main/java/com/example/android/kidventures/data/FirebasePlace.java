package com.example.android.kidventures.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.kidventures.utilities.FirebaseUtils;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by apoorva on 12/12/17.
 */

public class FirebasePlace implements Parcelable {

  private String placeId;
  private String placeName;
  private Float placeRating;
  private Integer placeNumOfReviews;
  //This field has total recommendations in each age category
  private HashMap<String, Integer> placeRecommendedChildAgesCnts =
          FirebaseUtils.getDefaultRecommendationCounts();
  //The Avg fields denote the average value for those attributes.
  private Float hasKidsMenuAvg;
  private Float hasKidsBathroomsAvg;
  private Float hasKidsEquipmentRentalAvg;
  private Float hasKidsSpecialProgramsAvg;
  ;
  private Float hasKidsPlayAreaAvg;

  public FirebasePlace() {
  }

  public FirebasePlace(String placeId, String placeName, Float placeRating,
                       Integer placeNumOfReviews, HashMap<String, Integer> recommendedAgesCnt,
                       Float menuAvg, Float bathroomsAvg, Float equipRentalAvg, Float programsAvg,
                       Float playAreaAvg) {
    this.placeId = placeId;
    this.placeName = placeName;
    this.placeRating = placeRating;
    this.placeNumOfReviews = placeNumOfReviews;
    this.placeRecommendedChildAgesCnts = recommendedAgesCnt;
    this.hasKidsMenuAvg = menuAvg;
    this.hasKidsBathroomsAvg = bathroomsAvg;
    this.hasKidsEquipmentRentalAvg = equipRentalAvg;
    this.hasKidsSpecialProgramsAvg = programsAvg;
    this.hasKidsPlayAreaAvg = playAreaAvg;
  }

  /**
   * Constructor to use when re-constructing object
   * from a parcel
   *
   * @param in a parcel from which to read this object
   */
  public FirebasePlace(Parcel in) {
    this.placeId = in.readString();
    this.placeName = in.readString();
    this.placeRating = (Float) in.readValue(Float.class.getClassLoader());
    this.placeNumOfReviews = (Integer) in.readValue(Integer.class.getClassLoader());
    placeRecommendedChildAgesCnts = new HashMap<>();
    this.placeRecommendedChildAgesCnts = in.readHashMap(HashMap.class.getClassLoader());
    this.hasKidsMenuAvg = (Float) in.readValue(Float.class.getClassLoader());
    this.hasKidsBathroomsAvg = (Float) in.readValue(Float.class.getClassLoader());
    this.hasKidsEquipmentRentalAvg = (Float) in.readValue(Float.class.getClassLoader());
    this.hasKidsSpecialProgramsAvg = (Float) in.readValue(Float.class.getClassLoader());
    this.hasKidsPlayAreaAvg = (Float) in.readValue(Float.class.getClassLoader());
  }

  public static final Parcelable.Creator<FirebasePlace> CREATOR =
          new Parcelable.Creator<FirebasePlace>() {
            public FirebasePlace createFromParcel(Parcel in) {
              return new FirebasePlace(in);
            }

            public FirebasePlace[] newArray(int size) {
              return new FirebasePlace[size];
            }
          };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.placeId);
    dest.writeString(this.placeName);
    dest.writeValue(this.placeRating);
    dest.writeValue(this.placeNumOfReviews);
    dest.writeMap(this.placeRecommendedChildAgesCnts);
    dest.writeValue(this.hasKidsMenuAvg);
    dest.writeValue(this.hasKidsBathroomsAvg);
    dest.writeValue(this.hasKidsEquipmentRentalAvg);
    dest.writeValue(this.hasKidsSpecialProgramsAvg);
    dest.writeValue(this.hasKidsPlayAreaAvg);
  }


  public String getPlaceId() {
    return this.placeId;
  }

  public String getPlaceName() {
    return this.placeName;
  }

  public Float getPlaceRating() {
    return this.placeRating;
  }

  public Integer getPlaceNumOfReviews() {
    return this.placeNumOfReviews;
  }

  public HashMap<String, Integer> getPlaceRecommendedChildAgesCnts() {
    return this.placeRecommendedChildAgesCnts;
  }

  public Float getHasKidsMenuAvg() {
    return this.hasKidsMenuAvg;
  }

  public Float getHasKidsBathroomsAvg() {
    return this.hasKidsBathroomsAvg;
  }

  public Float getHasKidsEquipmentRentalAvg() {
    return this.hasKidsEquipmentRentalAvg;
  }

  public Float getHasKidsSpecialProgramsAvg() {
    return this.hasKidsSpecialProgramsAvg;
  }

  public Float getHasKidsPlayAreaAvg() {
    return this.hasKidsPlayAreaAvg;
  }

}
