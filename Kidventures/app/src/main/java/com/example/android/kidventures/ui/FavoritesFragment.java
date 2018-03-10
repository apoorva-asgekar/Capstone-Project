package com.example.android.kidventures.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apoorva on 3/1/18.
 */

public class FavoritesFragment extends Fragment
        implements PlaceAdapter.PlaceAdapterOnClickHandler {

  @BindView(R.id.recyclerview_search_results)
  RecyclerView mFavoritesRecyclerView;
  @BindView(R.id.search_results_error_image)
  ImageView mErrorImage;
  @BindView(R.id.search_results_error_message)
  TextView mErrorMessage;
  @BindView(R.id.search_results_loading_indicator)
  ProgressBar mFavoritesProgressBar;
  @BindView(R.id.search_results_more_card)
  CardView mMoreFavoritesCard;
  @BindView(R.id.search_results_more_text)
  TextView mMoreFavoritesText;
  @BindView(R.id.search_results_toolbar)
  Toolbar mToolbar;

  private static final String LOG_TAG = FavoritesFragment.class.getSimpleName();

  private static final int FAVORITES_LOADER_ID = 11;
  private static final int PLACES_ANDROIDAPI_LOADER_ID = 22;

  private static final float METERS_TO_MILES_CONVERSION_FACTOR = 0.000621371192f;

  private static final String LOADER_PARAM_FIREBASE_UID = "firebaseUid";
  private static final String LOADER_PARAM_PLACE_IDS = "placeIds";
  private static final String PLACE_IDS = "placeIds";
  private static final String PLACES_DISPLAY_LIST = "listOfPlacesToDisplay";
  private static final String ERROR_DISPLAYED = "errorDisplayed";

  public static final String PLACE_SELECTED = "place_selected";
  public static final int REQUEST_CODE_FAVORITES_FRAGMENT = 888;

  private boolean mFirebasePlacesRetrieved = false;

  private GeoDataClient mGeoDataClient;
  private PlaceAdapter mPlaceAdapter;
  private String placeIds;
  private boolean errorDisplayed = false;
  private List<Place> listOfPlacesToDisplay;
  private HashMap<String, Bitmap> placePhotos = new HashMap<>();
  private HashMap<String, FirebasePlace> firebasePlaceDetails = new HashMap<>();
  private Place updatedPlaceFromDetails;

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

  LoaderManager.LoaderCallbacks<String> favoritesLoadListener =
          new LoaderManager.LoaderCallbacks<String>() {

            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
              final String firebaseUid = args.get(LOADER_PARAM_FIREBASE_UID).toString();
              return new AsyncTaskLoader<String>(getActivity()) {

                private String loadInBackgroundPlaceIds;
                private Favorites currentUserFavoritePlaces;
                private boolean favoritesRetrieved = false;

                @Override
                protected void onStartLoading() {
                  if (placeIds == null) {
                    forceLoad();
                    mFavoritesProgressBar.setVisibility(View.VISIBLE);
                    mFavoritesRecyclerView.setVisibility(View.INVISIBLE);
                  }
                }

                @Override
                public String loadInBackground() {
                  mFavoritesDatabaseReference.child(firebaseUid)
                          .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                              if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                                currentUserFavoritePlaces = dataSnapshot.getValue(Favorites.class);
                                List<String> favoritePlaceIdsList = currentUserFavoritePlaces.getFavoritePlaceIds();
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < favoritePlaceIdsList.size(); i++) {
                                  sb.append(favoritePlaceIdsList.get(i));
                                  if (i < (favoritePlaceIdsList.size() - 1)) {
                                    sb.append(",");
                                  }
                                }
                                loadInBackgroundPlaceIds = sb.toString();
                                favoritesRetrieved = true;
                              }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                          });
                  int threadWaitCnt = 0;
                  while (!favoritesRetrieved && threadWaitCnt < 20) {
                    try {
                      Thread.sleep(500);
                      threadWaitCnt++;
                      Log.i(LOG_TAG, "Waiting for favorites..." + threadWaitCnt);
                    } catch (InterruptedException e) {
                      Log.e(LOG_TAG, "Interrupted Exception: " + e);
                    }
                  }
                  return loadInBackgroundPlaceIds;
                }
              };
            }

            @Override
            public void onLoadFinished(Loader<String> loader, String data) {
              placeIds = data;

              Log.d(LOG_TAG, "Favorites placeIds: " + placeIds);

              if (placeIds != null) {
                //Get place details from Android Place Details API
                Bundle placeIdsForLoader = new Bundle();
                placeIdsForLoader.putString(LOADER_PARAM_PLACE_IDS, placeIds);
                getLoaderManager()
                        .restartLoader(PLACES_ANDROIDAPI_LOADER_ID, placeIdsForLoader,
                                placesAndroidApiLoadListener);

                //Simultaneously get place data from Firebase
                getFirebasePlaceDetails();
              } else {
                showNoResultsFound();
              }
            }

            @Override
            public void onLoaderReset(Loader<String> loader) {
            }
          };

  LoaderManager.LoaderCallbacks<List<Place>> placesAndroidApiLoadListener =
          new LoaderManager.LoaderCallbacks<List<Place>>() {
            @Override
            public Loader<List<Place>> onCreateLoader(int id, Bundle args) {
              final String placeIds = args.get(LOADER_PARAM_PLACE_IDS).toString();

              return new AsyncTaskLoader<List<Place>>(getActivity()) {

                List<Place> loadInBackgroundPlaceList = new ArrayList<Place>();
                private String currentLocation;
                private boolean mPlaceInfoRetrieved = false;

                @Override
                protected void onStartLoading() {
                  if (listOfPlacesToDisplay == null) {
                    forceLoad();
                  }
                }

                @Override
                public List<Place> loadInBackground() {
                  //Get current location of the device
                  currentLocation = LocationUtils.getCurrentLocation(getActivity());
                  String[] placeIdsArray = placeIds.split(",");
                  mPlaceInfoRetrieved = false;
                  mGeoDataClient.getPlaceById(placeIdsArray).addOnCompleteListener(
                          new OnCompleteListener<PlaceBufferResponse>() {
                            List<Place> listOfPlacesLocal = new ArrayList<Place>();

                            @Override
                            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {

                              if (!task.isSuccessful() || task.getResult() == null) {
                                Log.e(LOG_TAG, "Error while fetching place details from Android API");
                                showError();
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
                                Float placeDistance = null;
                                if(currentLocation != null) {
                                  String[] currentLocationLatLng = currentLocation.split(",");
                                  double currentLocationLat = Double.parseDouble(currentLocationLatLng[0]);
                                  double currentLocationLong = Double.parseDouble(currentLocationLatLng[1]);
                                  float[] placeDistanceResults = new float[3];
                                  Location.distanceBetween(
                                          currentLocationLat, currentLocationLong,
                                          placeLat, placeLong,
                                          placeDistanceResults);
                                  placeDistance = placeDistanceResults[0] * METERS_TO_MILES_CONVERSION_FACTOR;
                                }
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
                  int threadWaitCnt = 0;
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
              finalizePlaceData(placeIds);
            }

            @Override
            public void onLoaderReset(Loader<List<Place>> loader) {
            }
          };

  //Mandatory empty constructor
  public FavoritesFragment() {
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment HomeFragment.
   */
  public static FavoritesFragment newInstance(String firebaseUid) {
    FavoritesFragment fragment = new FavoritesFragment();
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

    FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    mPlacesDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.PLACES_FIREBASE_DATABASE_TABLE);
    mFavoritesDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.FAVORITES_FIREBASE_DATABASE_TABLE);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    final View rootView = inflater.inflate(R.layout.fragment_search_results, container, false);

    ButterKnife.bind(this, rootView);

    ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.nav_favourites);

    //No need to display the more results card for favorites.
    //Since all favorites will be displayed on the same page.
    mMoreFavoritesCard.setVisibility(View.GONE);

    mPlaceAdapter = new PlaceAdapter(getActivity(), this);
    mFavoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mFavoritesRecyclerView.setAdapter(mPlaceAdapter);

    if (savedInstanceState != null) {
      placeIds = savedInstanceState.getString(PLACE_IDS);
      listOfPlacesToDisplay = savedInstanceState.getParcelableArrayList(PLACES_DISPLAY_LIST);
      errorDisplayed = savedInstanceState.getBoolean(ERROR_DISPLAYED);
      if(listOfPlacesToDisplay != null) {
        mPlaceAdapter.setPlaceData(listOfPlacesToDisplay);
      } else if(placeIds == null) {
        showNoResultsFound();
      } else if(errorDisplayed) {
        showError();
      }
    }

    String firebaseUid = getActivity().getIntent().getExtras()
            .getString(FavoritesActivity.ARG_FIREBASE_UID);
    if(savedInstanceState == null) {
      if(firebaseUid != null) {
        Bundle favoritesLoaderArgs = new Bundle();
        favoritesLoaderArgs.putString(LOADER_PARAM_FIREBASE_UID, firebaseUid);
        getLoaderManager().initLoader(FAVORITES_LOADER_ID, favoritesLoaderArgs, favoritesLoadListener);
      } else {
        Log.e(LOG_TAG, "Firebase Uid passed to the Favorites Activity is null");
        showError();
      }
    }
    return rootView;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(PLACE_IDS, placeIds);
    outState.putParcelableArrayList(PLACES_DISPLAY_LIST, (ArrayList) listOfPlacesToDisplay);
    outState.putBoolean(ERROR_DISPLAYED, errorDisplayed);
  }

  @Override
  public void onResume() {
    super.onResume();

    //Get updated details for after back - they may have changed
    if (placeIds != null &&
            listOfPlacesToDisplay != null &&
            updatedPlaceFromDetails != null) {
      Log.d(LOG_TAG, "Updating current list with updatedPlace from details");
      List<Place> newListOfPlaces = new ArrayList<>();
      for (int i = 0; i < listOfPlacesToDisplay.size(); i++) {
        Place currentPlace = listOfPlacesToDisplay.get(i);
        if (currentPlace.getPlaceId().equals(updatedPlaceFromDetails.getPlaceId())) {
          Bitmap currentPhoto = currentPlace.getPlacePhoto();
          currentPlace = updatedPlaceFromDetails;
          currentPlace.setPlacePhoto(currentPhoto);
        }
        //If the updated place has been removed from favorites.
        //Don't display it in the recycler view any more.
        if(currentPlace.getIsFavorite()) {
          newListOfPlaces.add(currentPlace);
        }
      }
      listOfPlacesToDisplay = newListOfPlaces;
      mPlaceAdapter.setPlaceData(listOfPlacesToDisplay);
    }
  }

  @Override
  public void onClick(Place currentPlace, View sharedView) {
    Intent placeDetailActivityIntent = new Intent(getActivity(), PlaceDetailsActivity.class);
    Bundle currentPlaceBundle = new Bundle();
    currentPlaceBundle.putParcelable(PLACE_SELECTED, currentPlace);
    placeDetailActivityIntent.putExtras(currentPlaceBundle);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Bundle transitionBundle = ActivityOptionsCompat
              .makeSceneTransitionAnimation(getActivity(),
                      sharedView,
                      sharedView.getTransitionName())
              .toBundle();
      startActivityForResult(placeDetailActivityIntent,
              REQUEST_CODE_FAVORITES_FRAGMENT,
              transitionBundle);
    } else {
      startActivityForResult(placeDetailActivityIntent, REQUEST_CODE_FAVORITES_FRAGMENT);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == FavoritesFragment.REQUEST_CODE_FAVORITES_FRAGMENT) {
      if(resultCode == Activity.RESULT_OK) {
        updatedPlaceFromDetails = data.getExtras().getParcelable(PlaceDetailsActivity.UPDATED_PLACE);
      }
    }
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
                    Log.e(LOG_TAG, "Error while fetching photo for placeId: " + placeId);
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
      //Always true since we are displaying only favorites here
      currentPlace.setIsFavorite(true);
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
    mFavoritesProgressBar.setVisibility(View.INVISIBLE);
    mFavoritesRecyclerView.setVisibility(View.VISIBLE);
    errorDisplayed = false;
  }

  private void showError() {
    errorDisplayed = true;
    mFavoritesProgressBar.setVisibility(View.INVISIBLE);
    mFavoritesRecyclerView.setVisibility(View.INVISIBLE);
    Glide.with(getActivity())
            .load(R.drawable.error)
            .into(mErrorImage);
    mErrorImage.setVisibility(View.VISIBLE);
    mErrorMessage.setText(getString(R.string.error_message));
    mErrorMessage.setVisibility(View.VISIBLE);
  }

  private void showNoResultsFound() {
    mFavoritesProgressBar.setVisibility(View.INVISIBLE);
    mFavoritesRecyclerView.setVisibility(View.INVISIBLE);
    mErrorImage.setVisibility(View.INVISIBLE);
    mErrorMessage.setText(getString(R.string.no_favorites));
    mErrorMessage.setVisibility(View.VISIBLE);
    errorDisplayed = false;
  }
}
