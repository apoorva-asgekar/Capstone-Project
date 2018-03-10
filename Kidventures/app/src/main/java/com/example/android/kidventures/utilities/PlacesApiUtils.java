package com.example.android.kidventures.utilities;

import android.util.Log;

import com.google.android.gms.location.places.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by apoorva on 1/9/18.
 */

public class PlacesApiUtils {

    private final static String LOG_TAG = PlacesApiUtils.class.getSimpleName();

    //JSON definitions
    public static final String JSON_STATUS = "status";
    public static final String JSON_ERROR_MSG = "error_message";
    public static final String JSON_RESULTS = "results";
    public static final String JSON_NEXT_PAGE = "next_page_token";
    private static final String JSON_PLACE_ID = "place_id";
    public static final String STATUS_OK = "OK";

    public static HashMap<String,String> getPlaceIds(URL requestUrl) throws JSONException {
        HashMap<String,String> placeIds = new HashMap<String, String>();

        String responseStr = null;
        try {
            responseStr = NetworkUtils.getResponseFromUrl(requestUrl);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOEXception while querying the API.", e);
        }

        JSONObject nearbySearchResultsObject = new JSONObject(responseStr);

        String status = nearbySearchResultsObject.getString(JSON_STATUS);
        Log.i(LOG_TAG, "JSON Status: " + status);
        String msg = "";
        if(!status.equals(STATUS_OK)) {
            Log.e(LOG_TAG, "Error fetching JSON results from Google nearby search.");
            if(nearbySearchResultsObject.has(JSON_ERROR_MSG)) {
                Log.e(LOG_TAG, "Error message: " +
                        nearbySearchResultsObject.getString(JSON_ERROR_MSG));
                msg = nearbySearchResultsObject.getString(JSON_ERROR_MSG);
            }
            placeIds.put(JSON_STATUS, status);
            placeIds.put(JSON_ERROR_MSG, msg);
            placeIds.put(JSON_RESULTS, null);
            placeIds.put(JSON_NEXT_PAGE, null);
            return placeIds;
        }

        JSONArray resultsArray = nearbySearchResultsObject.getJSONArray(JSON_RESULTS);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject resultObject = resultsArray.getJSONObject(i);
            String placeId = resultObject.getString(JSON_PLACE_ID);
            sb.append(resultObject.getString(JSON_PLACE_ID));
            if (i != (resultsArray.length() - 1)) {
                sb.append(",");
            }
            String placeName = resultObject.getString("name");
            Log.d(LOG_TAG, "Name: " + placeName + " Id: " + placeId);
        }
        placeIds.put(JSON_STATUS, status);
        placeIds.put(JSON_ERROR_MSG, msg);
        placeIds.put(JSON_RESULTS, sb.toString());
        if(nearbySearchResultsObject.has(JSON_NEXT_PAGE)) {
            placeIds.put(JSON_NEXT_PAGE, nearbySearchResultsObject.getString(JSON_NEXT_PAGE));
        } else {
            placeIds.put(JSON_NEXT_PAGE, null);
        }
        return placeIds;
    }

    public static List<String> getPlaceTypeStrings(List<Integer> placeTypeConsts) {
        List<String> placeTypeStrings = new ArrayList<>();

        for(int i = 0; i < placeTypeConsts.size(); i++) {
            switch(placeTypeConsts.get(i)) {
                case Place.TYPE_AIRPORT:
                    placeTypeStrings.add("Airport");
                    break;
                case Place.TYPE_AMUSEMENT_PARK:
                    placeTypeStrings.add("Amusement Park");
                    break;
                case Place.TYPE_AQUARIUM:
                    placeTypeStrings.add("Aquarium");
                    break;
                case Place.TYPE_ART_GALLERY:
                    placeTypeStrings.add("Art Gallery");
                    break;
                case Place.TYPE_BAKERY:
                    placeTypeStrings.add("Bakery");
                    break;
                case Place.TYPE_BAR:
                    placeTypeStrings.add("Bar");
                    break;
                case Place.TYPE_BEAUTY_SALON:
                    placeTypeStrings.add("Beauty Salon");
                    break;
                case Place.TYPE_BICYCLE_STORE:
                    placeTypeStrings.add("Bicycle Store");
                    break;
                case Place.TYPE_BOOK_STORE:
                    placeTypeStrings.add("Book Store");
                    break;
                case Place.TYPE_BOWLING_ALLEY:
                    placeTypeStrings.add("Bowling Alley");
                    break;
                case Place.TYPE_CAFE:
                    placeTypeStrings.add("Cafe");
                    break;
                case Place.TYPE_CAMPGROUND:
                    placeTypeStrings.add("Campground");
                    break;
                case Place.TYPE_CASINO:
                    placeTypeStrings.add("Casino");
                    break;
                case Place.TYPE_CHURCH:
                    placeTypeStrings.add("Church");
                    break;
                case Place.TYPE_CITY_HALL:
                    placeTypeStrings.add("City Hall");
                    break;
                case Place.TYPE_CLOTHING_STORE:
                    placeTypeStrings.add("Clothing Store");
                    break;
                case Place.TYPE_CONVENIENCE_STORE:
                    placeTypeStrings.add("Convenience Store");
                    break;
                case Place.TYPE_DENTIST:
                    placeTypeStrings.add("Dentist");
                    break;
                case Place.TYPE_DEPARTMENT_STORE:
                    placeTypeStrings.add("Department Store");
                    break;
                case Place.TYPE_DOCTOR:
                    placeTypeStrings.add("Doctor");
                    break;
                case Place.TYPE_EMBASSY:
                    placeTypeStrings.add("Embassy");
                    break;
                case Place.TYPE_FIRE_STATION:
                    placeTypeStrings.add("Fire Station");
                    break;
                case Place.TYPE_FLORIST:
                    placeTypeStrings.add("Florist");
                    break;
                case Place.TYPE_FOOD:
                    placeTypeStrings.add("Food");
                    break;
                case Place.TYPE_GROCERY_OR_SUPERMARKET:
                    placeTypeStrings.add("Grocery/Supermarket");
                    break;
                case Place.TYPE_GYM:
                    placeTypeStrings.add("Gym");
                    break;
                case Place.TYPE_HEALTH:
                    placeTypeStrings.add("Health");
                    break;
                case Place.TYPE_HINDU_TEMPLE:
                    placeTypeStrings.add("Hindu Temple");
                    break;
                case Place.TYPE_HOME_GOODS_STORE:
                    placeTypeStrings.add("Home Goods Store");
                    break;
                case Place.TYPE_HOSPITAL:
                    placeTypeStrings.add("Hospital");
                    break;
                case Place.TYPE_LIBRARY:
                    placeTypeStrings.add("Library");
                    break;
                case Place.TYPE_LODGING:
                    placeTypeStrings.add("Lodging");
                    break;
                case Place.TYPE_MOSQUE:
                    placeTypeStrings.add("Mosque");
                    break;
                case Place.TYPE_MOVIE_THEATER:
                    placeTypeStrings.add("Movie Theatre");
                    break;
                case Place.TYPE_MUSEUM:
                    placeTypeStrings.add("Museum");
                    break;
                case Place.TYPE_NATURAL_FEATURE:
                    placeTypeStrings.add("Natural Feature");
                    break;
                case Place.TYPE_NIGHT_CLUB:
                    placeTypeStrings.add("Night Club");
                    break;
                case Place.TYPE_OTHER:
                    placeTypeStrings.add("Other");
                    break;
                case Place.TYPE_PARK:
                    placeTypeStrings.add("Park");
                    break;
                case Place.TYPE_PET_STORE:
                    placeTypeStrings.add("Pet Store");
                    break;
                case Place.TYPE_PHYSIOTHERAPIST:
                    placeTypeStrings.add("Physiotherapist");
                    break;
                case Place.TYPE_PLACE_OF_WORSHIP:
                    placeTypeStrings.add("Place of Worship");
                    break;
                case Place.TYPE_POINT_OF_INTEREST:
                    placeTypeStrings.add("Point of Interest");
                    break;
                case Place.TYPE_POST_OFFICE:
                    placeTypeStrings.add("Post Office");
                    break;
                case Place.TYPE_RESTAURANT:
                    placeTypeStrings.add("Restaurant");
                    break;
                case Place.TYPE_RV_PARK:
                    placeTypeStrings.add("RV Park");
                    break;
                case Place.TYPE_SCHOOL:
                    placeTypeStrings.add("School");
                    break;
                case Place.TYPE_SHOE_STORE:
                    placeTypeStrings.add("Shoe Store");
                    break;
                case Place.TYPE_SHOPPING_MALL:
                    placeTypeStrings.add("Shopping Mall");
                    break;
                case Place.TYPE_SPA:
                    placeTypeStrings.add("Spa");
                    break;
                case Place.TYPE_STADIUM:
                    placeTypeStrings.add("Stadium");
                    break;
                case Place.TYPE_STORE:
                    placeTypeStrings.add("Store");
                    break;
                case Place.TYPE_SUBWAY_STATION:
                    placeTypeStrings.add("Subway Station");
                    break;
                case Place.TYPE_SYNAGOGUE:
                    placeTypeStrings.add("Synagogue");
                    break;
                case Place.TYPE_TRAIN_STATION:
                    placeTypeStrings.add("Train Station");
                    break;
                case Place.TYPE_TRANSIT_STATION:
                    placeTypeStrings.add("Transit Station");
                    break;
                case Place.TYPE_UNIVERSITY:
                    placeTypeStrings.add("University");
                    break;
                case Place.TYPE_ZOO:
                    placeTypeStrings.add("Zoo");
                    break;
                default:
                    placeTypeStrings.add("Other");
            }
        }
        return placeTypeStrings;
    }
}
