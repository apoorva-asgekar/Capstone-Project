package com.example.android.kidventures.data;

import com.example.android.kidventures.utilities.FirebaseUtils;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Created by apoorva on 2/8/18.
 */

public class Review {

    private static final String TIMESTAMP = "timestamp";

    private String reviewKey;
    private String placeId;
    private String reviewerFirebaseUid;
    private String reviewerUsername;
    private Float rating;
    private String reviewComments;
    private HashMap<String, Object> timeStampCreated;
    private HashMap<String, Object> recommendedChildAges = FirebaseUtils.getDefaultRecommendations();
    private Integer hasKidsMenu;
    private Integer hasBathrooms;
    private Integer hasEquipRental;
    private Integer hasKidsSpecialPrograms;
    private Integer hasKidsPlayArea;

    public Review() {}

    public Review(String reviewKey, String placeId, String firebaseUid, String username,
                  Float rating, String reviewComments, HashMap<String, Object> recommendedChildAges,
                  Integer hasKidsMenu, Integer hasBathrooms, Integer hasEquipRental,
                  Integer hasKidsSpecialPrograms, Integer hasKidsPlayArea) {
        this.reviewKey = reviewKey;
        this.placeId = placeId;
        this.reviewerFirebaseUid = firebaseUid;
        this.reviewerUsername = username;
        this.rating = rating;
        this.reviewComments = reviewComments;
        HashMap<String, Object> timeStampNow = new HashMap<>();
        timeStampNow.put(TIMESTAMP, ServerValue.TIMESTAMP);
        this.timeStampCreated = timeStampNow;
        this.recommendedChildAges = recommendedChildAges;
        this.hasKidsMenu = hasKidsMenu;
        this.hasBathrooms = hasBathrooms;
        this.hasEquipRental = hasEquipRental;
        this.hasKidsSpecialPrograms = hasKidsSpecialPrograms;
        this.hasKidsPlayArea = hasKidsPlayArea;
    }

    public String getReviewKey() { return this.reviewKey; }

    public String getPlaceId() {
        return this.placeId;
    }

    public String getReviewerFirebaseUid() {
        return this.reviewerFirebaseUid;
    }

    public String getReviewerUsername() { return this.reviewerUsername; }

    public Float getRating() {
        return this.rating;
    }

    public String getReviewComments() {
        return this.reviewComments;
    }

    public HashMap<String, Object> getTimeStampCreated() {
        return this.timeStampCreated;
    }

    @Exclude
    public long getTimeStampCreatedLong() {
        return (long)this.timeStampCreated.get(TIMESTAMP);
    }

    public HashMap<String, Object> getRecommendedChildAges() {
        return recommendedChildAges;
    }

    public Integer getHasKidsMenu() {
        return this.hasKidsMenu;
    }

    public Integer getHasBathrooms() {
        return this.hasBathrooms;
    }

    public Integer getHasEquipRental() {
        return this.hasEquipRental;
    }

    public Integer getHasKidsSpecialPrograms() {
        return this.hasKidsSpecialPrograms;
    }

    public Integer getHasKidsPlayArea() {
        return this.hasKidsPlayArea;
    }
}
