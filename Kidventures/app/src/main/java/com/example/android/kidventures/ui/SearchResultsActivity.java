package com.example.android.kidventures.ui;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.android.kidventures.R;
import com.example.android.kidventures.data.Place;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchResultsActivity extends AppCompatActivity implements
        SearchResultsFragment.SearchResultsListener {

  private static final String LOG_TAG = SearchResultsActivity.class.getSimpleName();
  private static final Integer REQUEST_CODE_SEARCH_RESULTS_ACTIVITY = 999;

  public static final String PLACE_SELECTED = "place_selected";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search_results);
    ButterKnife.bind(this);
  }

  @SuppressLint("RestrictedApi")
  @Override
  public void onPlaceSelected(Place currentPlace, View sharedView) {
    //When the user selects a page - navigate to Place Details Activity.
    Intent placeDetailActivityIntent = new Intent(this, PlaceDetailsActivity.class);
    Bundle currentPlaceBundle = new Bundle();
    currentPlaceBundle.putParcelable(PLACE_SELECTED, currentPlace);
    placeDetailActivityIntent.putExtras(currentPlaceBundle);
    //Create Bundle for Transitions
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Bundle transitionBundle = ActivityOptionsCompat
              .makeSceneTransitionAnimation(this,
                      sharedView,
                      sharedView.getTransitionName())
              .toBundle();
      startActivityForResult(placeDetailActivityIntent,
              REQUEST_CODE_SEARCH_RESULTS_ACTIVITY,
              transitionBundle);
    } else {
      startActivityForResult(placeDetailActivityIntent, REQUEST_CODE_SEARCH_RESULTS_ACTIVITY);
    }
  }

  public String getQuery() {
    Bundle queryBundle = getIntent().getExtras();
    String query = null;
    if (queryBundle != null &&
            queryBundle.containsKey(MainActivity.SEARCH_QUERY)) {
      query = queryBundle.getString(MainActivity.SEARCH_QUERY);
    }
    return query;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == REQUEST_CODE_SEARCH_RESULTS_ACTIVITY) {
      if(resultCode == RESULT_OK && data.getExtras() != null){
        Place updatedPlace = data.getExtras().getParcelable(PlaceDetailsActivity.UPDATED_PLACE);

        SearchResultsFragment resultsFragment = (SearchResultsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.search_results_fragment);
        resultsFragment.getUpdatedPlace(updatedPlace);
      }
    }
  }
}
