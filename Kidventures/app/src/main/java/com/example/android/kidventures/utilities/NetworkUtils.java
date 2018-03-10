package com.example.android.kidventures.utilities;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by apoorva on 12/12/17.
 */

public class NetworkUtils {

    private final static String LOG_TAG = NetworkUtils.class.getSimpleName();

    public final static String GOOGLE_PLACES_WEB_API_URL =
            "https://maps.googleapis.com/maps/api/place/";
    public final static String GOOGLE_PLACES_NEARBY_ENDPOINT = "nearbysearch/json?";
    public final static String GOOGLE_PLACES_TEXTSEARCH_ENDPOINT = "textsearch/json?";

    private final static String GOOGLE_PLACES_API_KEY = "";
    private final static String QUERY_PARAM_API_KEY = "key";
    private final static String QUERY_PARAM_LOCATION = "location";
    private final static String QUERY_PARAM_RANKBY = "rankby";
    private final static String QUERY_PARAM_KEYWORD = "keyword";
    private final static String QUERY_PARAM_PAGETOKEN = "pagetoken";



    /**
     * Connects to the API and gets the response to the Http request.
     *
     * @param url - URL for the API request
     * @return String which contains the API response
     * @throws IOException
     */
    public static String getResponseFromUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        String resultJson = null;

        try {
            if (url != null) {
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            if (urlConnection != null) {
                inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    Scanner scanner = new Scanner(inputStream);
                    scanner.useDelimiter("\\A");

                    if (scanner.hasNext()) {
                        resultJson = scanner.next();
                    }
                }
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return resultJson;
                } else {
                    Log.e(LOG_TAG, "Http request returned with an error code: " + resultJson);
                }
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    /**
     * Returns a java URL object needed to make the network call to the Google Places API.
     *
     * @param locationLatLong - Location as "latitude, longitude" to fetch results for.
     * @param query - User query for which to fetch places from the API.
     * @return URL - java object of type URL
     */
    public static URL buildUrl(String locationLatLong, String query) {
        String requestQuery = GOOGLE_PLACES_WEB_API_URL + GOOGLE_PLACES_NEARBY_ENDPOINT;
        Uri builtUri;

        if(query.contains(PlacesApiUtils.JSON_NEXT_PAGE)) {
            String removeFromQuery = PlacesApiUtils.JSON_NEXT_PAGE + "=";
            query = query.replace(removeFromQuery,"");
            builtUri = Uri.parse(requestQuery).buildUpon()
                    .appendQueryParameter(QUERY_PARAM_PAGETOKEN, query)
                    .appendQueryParameter(QUERY_PARAM_API_KEY,GOOGLE_PLACES_API_KEY)
                    .build();
        } else {
            builtUri = Uri.parse(requestQuery).buildUpon()
                    .appendQueryParameter(QUERY_PARAM_API_KEY, GOOGLE_PLACES_API_KEY)
                    .appendQueryParameter(QUERY_PARAM_LOCATION, locationLatLong)
                    .appendQueryParameter(QUERY_PARAM_RANKBY, "distance")
                    .appendQueryParameter(QUERY_PARAM_KEYWORD, query)
                    .build();

        }

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL Excpetion in buildUrl", e);
        }
        return url;
    }

    /**
     * Returns a java URL object needed to make the network call to the Google Places API.
     *
     * @param pageToken - Page token to fetch the next 20 results for a previously run search.
     * @return URL - java object of type URL
     */
    public static URL buildUrl(String pageToken) {
        String requestQuery = GOOGLE_PLACES_WEB_API_URL + GOOGLE_PLACES_NEARBY_ENDPOINT;
        Uri builtUri = Uri.parse(requestQuery).buildUpon()
                .appendQueryParameter(QUERY_PARAM_API_KEY, GOOGLE_PLACES_API_KEY)
                .appendQueryParameter(QUERY_PARAM_PAGETOKEN, pageToken)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL Excpetion in buildUrl", e);
        }
        return url;
    }

    //Check if internet connection is currently available.
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}

