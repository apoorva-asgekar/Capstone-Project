package com.example.android.kidventures.utilities;

import android.content.Context;
import android.util.Log;

import com.example.android.kidventures.R;
import com.example.android.kidventures.data.Review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by apoorva on 1/26/18.
 */

public class FirebaseUtils {

  private static final String LOG_TAG = FirebaseUtils.class.getSimpleName();

  public static String USER_PHOTOS_FIREBASE_STORAGE_FOLDER = "user_photos";
  public static String USER_PROFILE_FIREBASE_DATABASE_TABLE = "user_profiles";
  public static String PLACES_FIREBASE_DATABASE_TABLE = "places";
  public static String REVIEWS_FIREBASE_DATABASE_TABLE = "reviews";
  public static String FAVORITES_FIREBASE_DATABASE_TABLE = "favorites";

  //Firebase Key Values for age recommendations
  public static final String AGE0TO2 = "age0to2";
  public static final String AGE2TO5 = "age2to5";
  public static final String AGE5TO8 = "age5to8";
  public static final String AGE8TO12 = "age8to12";
  public static final String AGE12PLUS = "age12Plus";

  public static Boolean getBooleanForAttribute(Float attrValue, Integer numOfReviews) {
    if ((attrValue >= -1.0f) && (attrValue <= -0.4f)) {
      return false;
    } else if ((attrValue >= 0.4f) && (attrValue <= 1.0f)) {
      return true;
    } else {
      return null;
    }
  }

  public static String getAgeRecommendations(HashMap<String, Integer> ageCntsMap,
                                             Integer numOfReviews,
                                             Context context) {
    String ages = "";
    float currentMax = 0.0f;
    for (Map.Entry<String, Integer> entry : ageCntsMap.entrySet()) {
      float agePercent = (entry.getValue() / numOfReviews) * 100.0f;
      if (agePercent > currentMax) {
        currentMax = agePercent;
        ages = entry.getKey();
      }
    }
    switch (ages) {
      case AGE0TO2:
        ages = context.getString(R.string.chk_age0_2);
        break;
      case AGE2TO5:
        ages = context.getString(R.string.chk_age2_5);
        break;
      case AGE5TO8:
        ages = context.getString(R.string.chk_age5_8);
        break;
      case AGE8TO12:
        ages = context.getString(R.string.chk_age8_12);
        break;
      case AGE12PLUS:
        ages = context.getString(R.string.chk_age12plus);
        break;
      default:
        break;
    }
    return ages;
  }

  public static HashMap<String, Object> getDefaultRecommendations() {
    HashMap<String, Object> defaultRecommendations = new HashMap<>();
    defaultRecommendations.put(AGE0TO2, false);
    defaultRecommendations.put(AGE2TO5, false);
    defaultRecommendations.put(AGE5TO8, false);
    defaultRecommendations.put(AGE8TO12, false);
    defaultRecommendations.put(AGE12PLUS, false);

    return defaultRecommendations;
  }

  public static HashMap<String, Integer> getDefaultRecommendationCounts() {
    HashMap<String, Integer> defaultRecommendationCounts = new HashMap<>();
    defaultRecommendationCounts.put(AGE0TO2, 0);
    defaultRecommendationCounts.put(AGE2TO5, 0);
    defaultRecommendationCounts.put(AGE5TO8, 0);
    defaultRecommendationCounts.put(AGE8TO12, 0);
    defaultRecommendationCounts.put(AGE12PLUS, 0);

    return defaultRecommendationCounts;
  }

  public static HashMap<String, Integer> getUpdatedAgeRecommendationCounts(
          HashMap<String, Object> newAgeRecommendations,
          HashMap<String, Integer> oldAgeRecommendationCounts){
    HashMap<String, Integer> updatedAgeRecommendationCounts = new HashMap<>();
    for(Map.Entry<String, Object> entry : newAgeRecommendations.entrySet()) {
      String key = entry.getKey();
      Boolean value = (Boolean)entry.getValue();
      Integer oldCount = oldAgeRecommendationCounts.get(key);
      if(value) {
        updatedAgeRecommendationCounts.put(key, (oldCount+1));
      } else {
        updatedAgeRecommendationCounts.put(key, oldCount);
      }
    }
    return updatedAgeRecommendationCounts;
  }
}
