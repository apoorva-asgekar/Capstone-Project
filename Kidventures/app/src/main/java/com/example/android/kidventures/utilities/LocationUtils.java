package com.example.android.kidventures.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.example.android.kidventures.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by apoorva on 1/10/18.
 */

public class LocationUtils {

    public static final String LOG_TAG = LocationUtils.class.getSimpleName();

    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 777;
    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private static FusedLocationProviderClient mFusedLocationClient;
    private static boolean mDeviceLocationMapped = false;
    private static String mLocationLatLong;

    /**
     * Return the current state of the permissions needed.
     */
    public static boolean checkPermissions(Activity activity) {
        int permissionState = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private static void startLocationPermissionRequest(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    public static void showSnackbar(final int mainTextStringId, final int actionStringId,
                              Activity activity, View.OnClickListener listener) {
        Snackbar.make(activity.findViewById(android.R.id.content),
                activity.getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(activity.getString(actionStringId), listener).show();
    }

    public static void requestPermissions(final Activity activity) {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(LOG_TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok, activity,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest(activity);
                        }
                    });

        } else {
            Log.i(LOG_TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest(activity);
        }
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     * <p>
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressWarnings("MissingPermission")
    private static void getLastLocation() {
        mDeviceLocationMapped = false;
        mLocationLatLong = null;
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location lastLocation;
                        if (task.isSuccessful() && task.getResult() != null) {

                            lastLocation = task.getResult();
                            mLocationLatLong = String.valueOf(lastLocation.getLatitude()) + "," +
                                    String.valueOf(lastLocation.getLongitude());
                        } else {
                            Log.e(LOG_TAG, "getLastLocation:exception", task.getException());
                        }
                        mDeviceLocationMapped = true;
                    }
                });
    }

    public static String getCurrentLocation(Context context) {
        mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);
        getLastLocation();
        int threadWaitCnt = 0;
        while (!mDeviceLocationMapped && threadWaitCnt < 20) {
            try {
                Thread.sleep(500);
                threadWaitCnt++;
                Log.i(LOG_TAG, "Waiting for location..." + threadWaitCnt);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception: " + e);
            }
        }
        return mLocationLatLong;
    }

}
