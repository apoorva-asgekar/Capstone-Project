package com.example.android.kidventures.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.kidventures.R;
import com.example.android.kidventures.data.Favorites;
import com.example.android.kidventures.data.FirebasePlace;
import com.example.android.kidventures.data.Place;
import com.example.android.kidventures.utilities.FirebaseUtils;
import com.example.android.kidventures.utilities.LocationUtils;
import com.example.android.kidventures.utilities.NetworkUtils;
import com.example.android.kidventures.utilities.PlacesApiUtils;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apoorva on 12/19/17.
 */

public class SearchResultsFragment extends Fragment
        implements PlaceAdapter.PlaceAdapterOnClickHandler {

  @BindView(R.id.recyclerview_search_results)
  RecyclerView mSearchResultsRecyclerView;
  @BindView(R.id.search_results_error_image)
  ImageView mErrorImage;
  @BindView(R.id.search_results_error_message)
  TextView mErrorMessage;
  @BindView(R.id.search_results_loading_indicator)
  ProgressBar mSearchProgressBar;
  @BindView(R.id.search_results_more_card)
  CardView mMoreSearchResultsCard;
  @BindView(R.id.search_results_more_text)
  TextView mMoreSearchResultsText;
  @BindView(R.id.search_results_toolbar)
  Toolbar mToolbar;

  private static final String LOG_TAG = SearchResultsFragment.class.getSimpleName();

  private static final int PLACES_WEBAPI_LOADER_ID = 44;
  private static final int PLACES_ANDROIDAPI_LOADER_ID = 55;

  private static final float METERS_TO_MILES_CONVERSION_FACTOR = 0.000621371192f;

  private static final String LOADER_PARAM_QUERY = "query";
  private static final String LOADER_PARAM_PLACE_IDS = "placeIds";
  private static final String PLACE_IDS = "placeIds";
  private static final String PLACES_DISPLAY_LIST = "listOfPlacesToDisplay";
  private static final String MORE_RESULTS_TOKEN = "moreResultsToken";
  private static final String ERROR_DISPLAYED = "errorDisplayed";

  private boolean mFirebasePlacesRetrieved = false;
  private GeoDataClient mGeoDataClient;
  private PlaceAdapter mPlaceAdapter;
  private String currentLocation;
  private String placeIds;
  private String moreResultsToken;
  private boolean errorDisplayed = false;
  private List<Place> listOfPlacesToDisplay;
  private Favorites currentUserFavoritePlaces;
  private HashMap<String, Bitmap> placePhotos = new HashMap<>();
  private HashMap<String, FirebasePlace> firebasePlaceDetails = new HashMap<>();
  private Place updatedPlaceFromDetails;
  public SearchResultsListener mCallback;

  //Firebase variables
  private DatabaseReference mPlacesDatabaseReference;
  private DatabaseReference mFavoritesDatabaseReference;
  private ValueEventListener mPlaceListener = new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
      if (dataSnapshot.getValue() != null) {
        FirebasePlace firebasePlace = dataSnapshot.getValue(FirebasePlace.class);
        firebasePlaceDetails.put(dataSnapshot.getKey(), firebasePlace);
      }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
  };

  LoaderManager.LoaderCallbacks<HashMap<String, String>> placesWebApiLoadListener =
          new LoaderManager.LoaderCallbacks<HashMap<String, String>>() {

            @Override
            public Loader<HashMap<String, String>> onCreateLoader(int id, Bundle args) {
              final String query = args.get(LOADER_PARAM_QUERY).toString();
              return new AsyncTaskLoader<HashMap<String, String>>(getActivity()) {
                @Override
                protected void onStartLoading() {
                  if (placeIds == null) {
                    forceLoad();
                    showLoading();
                  }
                }

                @Override
                public HashMap<String, String> loadInBackground() {
                  HashMap<String, String> apiResults = new HashMap<String, String>();
                  currentLocation = LocationUtils.getCurrentLocation(getActivity());

                  URL requestUrl = NetworkUtils.buildUrl(currentLocation, query);
                  try {
                    apiResults = PlacesApiUtils.getPlaceIds(requestUrl);
                  } catch (JSONException e) {
                    Log.e(LOG_TAG, "JSONException: " + e);
                  }
                  return apiResults;
                }
              };
            }

            @Override
            public void onLoadFinished(Loader<HashMap<String, String>> loader, HashMap<String, String> data) {
              HashMap<String, String> placeResults = data;

              if (placeResults.containsKey(PlacesApiUtils.JSON_STATUS) &&
                      placeResults.get(PlacesApiUtils.JSON_STATUS)
                              .equals(PlacesApiUtils.STATUS_OK)) {
                if (placeResults.containsKey(PlacesApiUtils.JSON_RESULTS)) {
                  placeIds = placeResults.get(PlacesApiUtils.JSON_RESULTS);
                  Log.d(LOG_TAG, "placeIds: " + placeIds);
                }

                if (placeResults.containsKey(PlacesApiUtils.JSON_NEXT_PAGE)) {
                  moreResultsToken = placeResults.get(PlacesApiUtils.JSON_NEXT_PAGE);
                  Log.d(LOG_TAG, "moreResultsToken: " + moreResultsToken);
                } else {
                  moreResultsToken = null;
                }

                if (placeIds != null) {
                  //Get place details from Android Place Details API
                  Bundle placeIdsForLoader = new Bundle();
                  placeIdsForLoader.putString(LOADER_PARAM_PLACE_IDS, placeIds);
                  getLoaderManager()
                          .restartLoader(PLACES_ANDROIDAPI_LOADER_ID, placeIdsForLoader,
                                  placesAndroidApiLoadListener);

                  //Simultaneously get place data from Firebase
                  getFirebasePlaceDetails();
                }
              } else {
                String status = "";
                String errorMessage = "";
                if (placeResults.containsKey(PlacesApiUtils.JSON_STATUS)) {
                  status = placeResults.get(PlacesApiUtils.JSON_STATUS);
                  if (placeResults.containsKey(PlacesApiUtils.JSON_ERROR_MSG)) {
                    errorMessage = placeResults.get(PlacesApiUtils.JSON_ERROR_MSG);
                  }
                  if (status.equals("ZERO_RESULTS")) {
                    showNoResultsFound();
                  } else {
                    Log.e(LOG_TAG, "Places Web Api returned a JSON error.");
                    Log.e(LOG_TAG, "Error Status: " + status);
                    Log.e(LOG_TAG, "Error Message: " + errorMessage);
                    showError();
                  }
                }
              }

            }

            @Override
            public void onLoaderReset(Loader<HashMap<String, String>> loader) {
            }
          };

  LoaderManager.LoaderCallbacks<List<Place>> placesAndroidApiLoadListener =
          new LoaderManager.LoaderCallbacks<List<Place>>() {
            @Override
            public Loader<List<Place>> onCreateLoader(int id, Bundle args) {
              final String placeIds = args.get(LOADER_PARAM_PLACE_IDS).toString();

              return new AsyncTaskLoader<List<Place>>(getActivity()) {

                List<Place> loadInBackgroundPlaceList = new ArrayList<Place>();
                private boolean mPlaceInfoRetrieved = false;

                @Override
                protected void onStartLoading() {
                  if (listOfPlacesToDisplay == null) {
                    forceLoad();
                  }
                }

                @Override
                public List<Place> loadInBackground() {
                  int threadWaitCnt = 0;
                  String[] placeIdsArray = placeIds.split(",");
                  mPlaceInfoRetrieved = false;
                  mGeoDataClient.getPlaceById(placeIdsArray).addOnCompleteListener(
                          new OnCompleteListener<PlaceBufferResponse>() {
                            List<Place> listOfPlacesLocal = new ArrayList<Place>();

                            @Override
                            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {

                              if (!task.isSuccessful() || task.getResult() == null) {
                                Log.e(LOG_TAG, "Error while fetching place details from Android API");
                                return;
                              }
                              PlaceBufferResponse places = task.getResult();
                              Log.i(LOG_TAG, "Number of places: " + places.getCount());
                              for (int i = 0; i < places.getCount(); i++) {
                                com.google.android.gms.location.places.Place androidPlace = places.get(i);
                                String placeWebUri = null;
                                if (androidPlace.getWebsiteUri() != null) {
                                  placeWebUri = androidPlace.getWebsiteUri().toString();
                                }
                                String placeAddress = null;
                                if (androidPlace.getAddress() != null) {
                                  placeAddress = androidPlace.getAddress().toString();
                                }
                                String placePhoneNumber = null;
                                if (androidPlace.getPhoneNumber() != null) {
                                  placePhoneNumber = androidPlace.getPhoneNumber().toString();
                                }
                                double placeLat = androidPlace.getLatLng().latitude;
                                double placeLong = androidPlace.getLatLng().longitude;
                                String[] currentLocationLatLng = currentLocation.split(",");
                                double currentLocationLat = Double.parseDouble(currentLocationLatLng[0]);
                                double currentLocationLong = Double.parseDouble(currentLocationLatLng[1]);
                                float[] placeDistanceResults = new float[3];
                                Location.distanceBetween(
                                        currentLocationLat, currentLocationLong,
                                        placeLat, placeLong,
                                        placeDistanceResults);
                                float placeDistance = placeDistanceResults[0] * METERS_TO_MILES_CONVERSION_FACTOR;
                                ArrayList<String> placeTypes = (ArrayList<String>)
                                        PlacesApiUtils.getPlaceTypeStrings(androidPlace.getPlaceTypes());
                                Place newPlace = new Place(
                                        androidPlace.getId(),
                                        androidPlace.getName().toString(),
                                        placeAddress,
                                        placePhoneNumber,
                                        placeWebUri,
                                        placeDistance,
                                        placeLat,
                                        placeLong,
                                        placeTypes
                                );
                                listOfPlacesLocal.add(newPlace);
                              }
                              places.release();
                              loadInBackgroundPlaceList = listOfPlacesLocal;
                              mPlaceInfoRetrieved = true;
                            }
                          });
                  threadWaitCnt = 0;
                  while (!mPlaceInfoRetrieved && threadWaitCnt < 20) {
                    try {
                      Thread.sleep(500);
                      threadWaitCnt++;
                      Log.i(LOG_TAG, "Waiting for places..." + threadWaitCnt);
                    } catch (InterruptedException e) {
                      Log.e(LOG_TAG, "Interrupted Exception: " + e);
                    }
                  }
                  return loadInBackgroundPlaceList;
                }

              };
            }

            @Override
            public void onLoadFinished(Loader<List<Place>> loader, List<Place> data) {
              listOfPlacesToDisplay = data;
              if(listOfPlacesToDisplay.size() > 0) {
                finalizePlaceData(placeIds);
              } else {
                showError();
              }
            }

            @Override
            public void onLoaderReset(Loader<List<Place>> loader) {
            }
          };

  //Mandatory empty constructor
  public SearchResultsFragment() {
  }

  /**
   * This method makes sure that the container activity has implemented the callback.
   * If not it throws an Exception.
   *
   * @param context
   */
  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    try {
      mCallback = (SearchResultsListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString()
              + " must implement SearchResultsListener");
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    final View rootView = inflater.inflate(R.layout.fragment_search_results, container, false);

    ButterKnife.bind(this, rootView);

    ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);

    mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

    FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    mPlacesDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.PLACES_FIREBASE_DATABASE_TABLE);
    mFavoritesDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.FAVORITES_FIREBASE_DATABASE_TABLE);

    mPlaceAdapter = new PlaceAdapter(getActivity(), this);
    mSearchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mSearchResultsRecyclerView.setAdapter(mPlaceAdapter);

    if(savedInstanceState != null) {
      placeIds = savedInstanceState.getString(PLACE_IDS);
      listOfPlacesToDisplay = savedInstanceState.getParcelableArrayList(PLACES_DISPLAY_LIST);
      moreResultsToken = savedInstanceState.getString(MORE_RESULTS_TOKEN);
      errorDisplayed = savedInstanceState.getBoolean(ERROR_DISPLAYED);
      if(listOfPlacesToDisplay != null) {
        mPlaceAdapter.setPlaceData(listOfPlacesToDisplay);
      } else if(placeIds == null) {
        if(errorDisplayed) {
          showError();
        } else {
          showNoResultsFound();
        }
      }
    }

    //Get the search query from the host activity.
    String query = mCallback.getQuery();
    Log.d(LOG_TAG, "Query: " + query);
    if(savedInstanceState == null) {
      if (query != null) {
        //Get places data from APIs
        Bundle queryForLoader = new Bundle();
        queryForLoader.putString(LOADER_PARAM_QUERY, query);
        getLoaderManager()
                .initLoader(PLACES_WEBAPI_LOADER_ID, queryForLoader, placesWebApiLoadListener);
      } else {
        Log.e(LOG_TAG, "Query passed to SearchResultsFragment is null");
        showError();
      }
    }

    RecyclerView.OnScrollListener mScrollListener =
            new RecyclerView.OnScrollListener() {
              @Override
              public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager mLayoutManager =
                        (LinearLayoutManager) mSearchResultsRecyclerView.getLayoutManager();
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                if ((pastVisibleItems + visibleItemCount >= totalItemCount) &&
                        moreResultsToken != null) {
                  mMoreSearchResultsCard.setVisibility(View.VISIBLE);
                } else {
                  mMoreSearchResultsCard.setVisibility(View.GONE);
                }
              }
            };
    mSearchResultsRecyclerView.addOnScrollListener(mScrollListener);

    //To display More results
    mMoreSearchResultsCard.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (moreResultsToken != null) {
          String query = PlacesApiUtils.JSON_NEXT_PAGE + "=" + moreResultsToken;
          placeIds = null;
          listOfPlacesToDisplay = null;
          Bundle queryForLoader = new Bundle();
          queryForLoader.putString(LOADER_PARAM_QUERY, query);
          getLoaderManager()
                  .restartLoader(PLACES_WEBAPI_LOADER_ID, queryForLoader, placesWebApiLoadListener);
        }
      }
    });

    return rootView;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(PLACE_IDS, placeIds);
    outState.putParcelableArrayList(PLACES_DISPLAY_LIST, (ArrayList)listOfPlacesToDisplay);
    outState.putString(MORE_RESULTS_TOKEN, moreResultsToken);
    outState.putBoolean(ERROR_DISPLAYED, errorDisplayed);
  }

  public void getUpdatedPlace(Place newPlace) {
    updatedPlaceFromDetails = newPlace;
  }

  @Override
  public void onResume() {
    Log.d(LOG_TAG, "In OnResume");
    super.onResume();
    //Get favorite places for the current user
    //Update the favorites in the current list of places.
    getUserFavoritePlaces();

    //Get updated details for the place - they may have changed
    if (placeIds != null &&
            listOfPlacesToDisplay != null &&
            updatedPlaceFromDetails != null) {
      List<Place> newListOfPlaces = new ArrayList<>();
      for (int i = 0; i < listOfPlacesToDisplay.size(); i++) {
        Place currentPlace = listOfPlacesToDisplay.get(i);
        if (currentPlace.getPlaceId().equals(updatedPlaceFromDetails.getPlaceId())) {
          Bitmap currentPhoto = currentPlace.getPlacePhoto();
          currentPlace = updatedPlaceFromDetails;
          currentPlace.setPlacePhoto(currentPhoto);
        }
        newListOfPlaces.add(currentPlace);
      }
      listOfPlacesToDisplay = newListOfPlaces;
      mPlaceAdapter.setPlaceData(listOfPlacesToDisplay);
    }
  }

  @Override
  public void onClick(Place currentPlace, View sharedView) {
    mCallback.onPlaceSelected(currentPlace, sharedView);
  }

  public interface SearchResultsListener {
    String getQuery();

    void onPlaceSelected(Place currentPlace, View sharedView);
  }

  private void getFirebasePlaceDetails() {
    List<String> placeList =
            new ArrayList<String>(Arrays.asList(placeIds.split(",")));
    for (int i = 0; i < placeList.size(); i++) {
      mPlacesDatabaseReference
              .child(placeList.get(i))
              .addListenerForSingleValueEvent(mPlaceListener);
      if (i == (placeList.size() - 1)) {
        mFirebasePlacesRetrieved = true;
      }
    }
  }

  private void getUserFavoritePlaces() {
    mFavoritesDatabaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                  currentUserFavoritePlaces = dataSnapshot.getValue(Favorites.class);
                  if (listOfPlacesToDisplay != null) {
                    List<Place> newListOfPlaces = new ArrayList<>();
                    for (int j = 0; j < listOfPlacesToDisplay.size(); j++) {
                      Place currentPlace = listOfPlacesToDisplay.get(j);
                      Boolean isFavorite = false;
                      if (currentUserFavoritePlaces != null &&
                              currentUserFavoritePlaces.getFavoritePlaceIds()
                                      .contains(currentPlace.getPlaceId())) {
                        isFavorite = true;
                      }
                      currentPlace.setIsFavorite(isFavorite);
                      newListOfPlaces.add(currentPlace);
                    }
                    listOfPlacesToDisplay = newListOfPlaces;
                    mPlaceAdapter.setPlaceData(listOfPlacesToDisplay);
                  }
                }
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
            });
  }

  private void finalizePlaceData(String placeIds) {
    final String[] placeIdsArray = placeIds.split(",");

    final AtomicInteger counter = new AtomicInteger();
    for (int i = 0; i < placeIdsArray.length; i++) {
      final String placeId = placeIdsArray[i];

      Task<PlacePhotoMetadataResponse> photoMetadataResponseTask =
              mGeoDataClient.getPlacePhotos(placeId);
      photoMetadataResponseTask.addOnCompleteListener(
              new OnCompleteListener<PlacePhotoMetadataResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                  if (!task.isSuccessful()) {
                    Log.w(LOG_TAG, "Could not fetch photo for place id: " + placeId);
                    return;
                  }
                  PlacePhotoMetadataResponse photoMetadataResponse = task.getResult();
                  PlacePhotoMetadataBuffer photoMetadataBuffer =
                          photoMetadataResponse.getPhotoMetadata();

                  // If no photos found, increment counter and get out.
                  if (photoMetadataBuffer.getCount() == 0) {
                    Log.d(LOG_TAG, "Photo not found");
                    counter.incrementAndGet();
                    Log.d(LOG_TAG, "counter value: " + counter.get());
                    if (counter.get() == placeIdsArray.length) {
                      updateUI();
                    }
                    return;
                  }

                  //Get the first photo's metadata
                  PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                  Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                  photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                      if (task.isSuccessful()) {
                        Bitmap placePhotoBitmap = task.getResult().getBitmap();
                        if (placePhotoBitmap != null) {
                          placePhotos.put(placeId, placePhotoBitmap);
                        }
                      }
                      counter.incrementAndGet();
                      Log.d(LOG_TAG, "counter value: " + counter.get());
                      if (counter.get() == placeIdsArray.length) {
                        Log.d(LOG_TAG, "Last place photo retrieved");
                        updateUI();
                      }
                    }
                  });
                  photoResponse.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      counter.incrementAndGet();
                      Log.d(LOG_TAG, "counter value: " + counter.get());
                      if (counter.get() == placeIdsArray.length) {
                        Log.d(LOG_TAG, "Last place photo retrieved");
                        updateUI();
                      }
                    }
                  });
                  photoMetadataBuffer.release();

                }
              });
      photoMetadataResponseTask.addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          counter.incrementAndGet();
          Log.d(LOG_TAG, "counter value: " + counter.get());
          if (counter.get() == placeIdsArray.length) {
            Log.d(LOG_TAG, "Last place photo retrieved");
            updateUI();
          }
        }
      });
    }
  }

  private void updateUI() {
    //Add photos for each place in the list of places.
    List<Place> newListOfPlaces = new ArrayList<>();
    //Add the photos to the Place objects
    for (int j = 0; j < listOfPlacesToDisplay.size(); j++) {
      Place currentPlace = listOfPlacesToDisplay.get(j);
      Bitmap currentPhoto = placePhotos.get(currentPlace.getPlaceId());
      currentPlace.setPlacePhoto(currentPhoto);
      Boolean isFavorite = false;
      if (currentUserFavoritePlaces != null &&
              currentUserFavoritePlaces.getFavoritePlaceIds()
                      .contains(currentPlace.getPlaceId())) {
        isFavorite = true;
      }
      currentPlace.setIsFavorite(isFavorite);
      int threadWaitCnt = 0;
      while (!mFirebasePlacesRetrieved && threadWaitCnt < 20) {
        try {
          Thread.sleep(500);
          threadWaitCnt++;
          Log.i(LOG_TAG, "Waiting for firebase places..." + threadWaitCnt);
        } catch (InterruptedException e) {
          Log.e(LOG_TAG, "Interrupted Exception: " + e);
        }
      }
      //Add rating and child related attributes for each place in the list of places.
      if (mFirebasePlacesRetrieved && (firebasePlaceDetails.size() > 0)) {
        if (firebasePlaceDetails.containsKey(currentPlace.getPlaceId())) {
          FirebasePlace currentFirebasePlace =
                  firebasePlaceDetails.get(currentPlace.getPlaceId());
          Integer currentNumOfReviews = currentFirebasePlace.getPlaceNumOfReviews();
          Boolean hasKidsMenu = FirebaseUtils.
                  getBooleanForAttribute(currentFirebasePlace.getHasKidsMenuAvg(),
                          currentNumOfReviews);
          Boolean hasBathrooms = FirebaseUtils.
                  getBooleanForAttribute(currentFirebasePlace.getHasKidsBathroomsAvg(),
                          currentNumOfReviews);
          Boolean hasKidsEquipRental = FirebaseUtils.
                  getBooleanForAttribute(currentFirebasePlace.getHasKidsEquipmentRentalAvg(),
                          currentNumOfReviews);
          Boolean hasKidsPrograms = FirebaseUtils.
                  getBooleanForAttribute(currentFirebasePlace.getHasKidsSpecialProgramsAvg(),
                          currentNumOfReviews);
          Boolean hasKidsPlayArea = FirebaseUtils.
                  getBooleanForAttribute(currentFirebasePlace.getHasKidsPlayAreaAvg(),
                          currentNumOfReviews);
          String ageRecommendations = FirebaseUtils.getAgeRecommendations(
                  currentFirebasePlace.getPlaceRecommendedChildAgesCnts(),
                  currentNumOfReviews, getActivity()
          );

          currentPlace.setFirebasePlaceDetails(
                  currentFirebasePlace.getPlaceRating(),
                  currentFirebasePlace.getPlaceNumOfReviews(),
                  ageRecommendations,
                  hasKidsMenu,
                  hasBathrooms,
                  hasKidsEquipRental,
                  hasKidsPrograms,
                  hasKidsPlayArea
          );
        }
      }
      newListOfPlaces.add(currentPlace);
    }
    listOfPlacesToDisplay = newListOfPlaces;
    mPlaceAdapter.setPlaceData(listOfPlacesToDisplay);
    mSearchProgressBar.setVisibility(View.INVISIBLE);
    mSearchResultsRecyclerView.setVisibility(View.VISIBLE);
    mSearchResultsRecyclerView.smoothScrollToPosition(0);
    errorDisplayed = false;
  }

  private void showError() {
    errorDisplayed = true;
    mSearchProgressBar.setVisibility(View.INVISIBLE);
    mSearchResultsRecyclerView.setVisibility(View.INVISIBLE);
    mMoreSearchResultsCard.setVisibility(View.GONE);
    Glide.with(getActivity())
            .load(R.drawable.error)
            .into(mErrorImage);
    mErrorImage.setVisibility(View.VISIBLE);
    mErrorMessage.setText(getString(R.string.error_message));
    mErrorMessage.setVisibility(View.VISIBLE);
  }

  private void showNoResultsFound() {
    mSearchProgressBar.setVisibility(View.INVISIBLE);
    mSearchResultsRecyclerView.setVisibility(View.INVISIBLE);
    mMoreSearchResultsCard.setVisibility(View.GONE);
    Glide.with(getActivity())
            .load(R.drawable.no_results_found)
            .into(mErrorImage);
    mErrorImage.setVisibility(View.VISIBLE);
    mErrorMessage.setText(getString(R.string.no_results_found));
    mErrorMessage.setVisibility(View.VISIBLE);
    errorDisplayed = false;
  }

  private void showLoading() {
    mSearchProgressBar.setVisibility(View.VISIBLE);
    mSearchResultsRecyclerView.setVisibility(View.INVISIBLE);
    mMoreSearchResultsCard.setVisibility(View.GONE);
    mErrorImage.setVisibility(View.INVISIBLE);
    mErrorMessage.setVisibility(View.INVISIBLE);
  }

}
