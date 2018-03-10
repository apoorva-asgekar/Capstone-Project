package com.example.android.kidventures.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.android.kidventures.R;
import com.example.android.kidventures.data.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class PlaceDetailsActivity extends AppCompatActivity
implements OnMapReadyCallback {

    private static final String LOG_TAG = PlaceDetailsActivity.class.getSimpleName();
    public static final String UPDATED_PLACE = "updated_place";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

  @Override
  public void onBackPressed() {
    PlaceDetailsFragment detailsFragment = (PlaceDetailsFragment) getSupportFragmentManager()
            .findFragmentById(R.id.place_details_fragment);
    Place updatedPlace = detailsFragment.getCurrentUpdatedPlace();
    Intent returnIntent = new Intent();
    Bundle returnBundle = new Bundle();
    returnBundle.putParcelable(UPDATED_PLACE, updatedPlace);
    returnIntent.putExtras(returnBundle);
    setResult(RESULT_OK, returnIntent);
    supportFinishAfterTransition();
    return;
  }
}
