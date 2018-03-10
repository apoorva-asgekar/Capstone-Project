package com.example.android.kidventures.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.TooltipCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.android.kidventures.R;
import com.example.android.kidventures.data.Favorites;
import com.example.android.kidventures.data.FirebasePlace;
import com.example.android.kidventures.data.Place;
import com.example.android.kidventures.data.Review;
import com.example.android.kidventures.utilities.FirebaseUtils;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apoorva on 2/2/18.
 */

public class PlaceDetailsFragment extends Fragment
        implements OnMapReadyCallback {

  private static final String LOG_TAG = PlaceDetailsFragment.class.getSimpleName();
  private static final int REQUEST_CODE_PLACE_DETAILS = 333;

  public static final Float MAP_ZOOM = 12.0F;
  public static final String CURRENT_PLACE_ID = "current_place_id";
  public static final String CURRENT_PLACE_NAME = "current_place_name";

  private Place currentPlace;
  private GeoDataClient mGeoDataClient;
  private Bitmap placePhoto;
  private LatLng placeLatLng;
  private Favorites currentUserFavoritePlaces;
  private String currentFirebaseUid;
  private ReviewsAdapter mReviewsAdapter;

  @BindView(R.id.root_layout)
  FrameLayout rootLayout;
  @BindView(R.id.detail_app_bar_layout)
  AppBarLayout appBarLayout;
  @BindView(R.id.detail_app_bar_photo)
  ImageView appbarPlacePhoto;
  @BindView(R.id.toolbar_place_name)
  TextView toolbarPlaceName;
  @BindView(R.id.toolbar_fav_icon)
  ImageView toolbarFavIcon;
  @BindView(R.id.place_info_place_category)
  TextView placeCategory;
  @BindView(R.id.place_info_place_phone)
  TextView placePhone;
  @BindView(R.id.place_info_weburl_top_border)
  View weburlTopBorder;
  @BindView(R.id.place_info_place_weburl)
  TextView placeWeburl;
  @BindView(R.id.place_info_num_reviews)
  TextView placeNumReviews;
  @BindView(R.id.place_info_ratingbar)
  RatingBar placeRatingbar;
  @BindView(R.id.map_address)
  TextView mapAddress;
  @BindView(R.id.map_distance)
  TextView mapDistance;
  @BindView(R.id.map_view)
  MapView mapView;
  @BindView(R.id.place_details_recommended_ages_title)
  TextView placeRecommemdedAgesTitle;
  @BindView(R.id.place_details_recommended_ages)
  TextView placeRecommemdedAges;
  @BindView(R.id.detail_toolbar)
  Toolbar toolbar;
  @BindView(R.id.attr_menu_value)
  View hasKidsMenu;
  @BindView(R.id.attr_bathrooms_value)
  View hasKidsBathrooms;
  @BindView(R.id.attr_equip_rental_value)
  View hasKidsEquipRental;
  @BindView(R.id.attr_programs_value)
  View hasKidsSpecialPrograms;
  @BindView(R.id.attr_play_area_value)
  View hasKidsPlayArea;
  @BindView(R.id.recyclerview_reviews_title)
  TextView reviewsTitle;
  @BindView(R.id.recyclerview_reviews)
  RecyclerView reviewsRecyclerView;
  @BindView(R.id.ages_lower_border)
  View agesLowerBorder;

  FirebaseDatabase mFirebaseDatabase;
  DatabaseReference mFavoritesDatabaseReference;
  DatabaseReference mReviewsDatabaseReference;
  DatabaseReference mPlacesDatabaseReference;

  public PlaceDetailsFragment() {
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    final View rootView = inflater.inflate(R.layout.fragment_place_details, container, false);
    ButterKnife.bind(this, rootView);

    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

    mFirebaseDatabase = FirebaseDatabase.getInstance();
    mFavoritesDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.FAVORITES_FIREBASE_DATABASE_TABLE);
    mReviewsDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.REVIEWS_FIREBASE_DATABASE_TABLE);
    mPlacesDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.PLACES_FIREBASE_DATABASE_TABLE);

    currentFirebaseUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    //Getting current users favorite places;
    mFavoritesDatabaseReference.child(currentFirebaseUid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                  currentUserFavoritePlaces = dataSnapshot.getValue(Favorites.class);
                }
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
            });

    mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

    Bundle placeBundle = getActivity().getIntent().getExtras();

    if (placeBundle != null) {
      if (placeBundle.containsKey(SearchResultsActivity.PLACE_SELECTED)) {
        currentPlace = placeBundle.getParcelable(SearchResultsActivity.PLACE_SELECTED);
      }
    }

    if (currentPlace != null) {
      //Get the photo Bitmap from the placeId
      getPlacePhoto(currentPlace.getPlaceId());

      toolbarPlaceName.setText(currentPlace.getPlaceName());
      if (currentPlace.getIsFavorite() != null && currentPlace.getIsFavorite()) {
        toolbarFavIcon.setImageDrawable(getActivity().getResources()
                .getDrawable(R.drawable.ic_favourite));
        toolbarFavIcon.setTag("favorite");
      }

      toolbarFavIcon.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          setOrUnsetFavorite();
        }
      });

      String category = null;
      for (int i = 0; i < currentPlace.getPlaceTypes().size(); i++) {
        if (i > 2) {
          break;
        }
        if (i == 1) {
          category = currentPlace.getPlaceTypes().get(i);
        } else {
          category = category + " / " + currentPlace.getPlaceTypes().get(i);
        }
      }
      placeCategory.setText(category);
      if (currentPlace.getPlacePhoneNumber() != null) {
        placePhone.setVisibility(View.VISIBLE);
        placePhone.setFocusable(true);
        placePhone.setText(currentPlace.getPlacePhoneNumber());
      } else {
        placePhone.setVisibility(View.GONE);
        placePhone.setFocusable(false);
      }
      if (currentPlace.getPlaceWebsiteUrl() != null) {
        weburlTopBorder.setVisibility(View.VISIBLE);
        placeWeburl.setVisibility(View.VISIBLE);
        placeWeburl.setText(currentPlace.getPlaceWebsiteUrl());
      } else {
        weburlTopBorder.setVisibility(View.GONE);
        placeWeburl.setVisibility(View.GONE);
      }

      int numOfReviews = 0;
      if (currentPlace.getPlaceNumOfReviews() != null) {
        numOfReviews = currentPlace.getPlaceNumOfReviews();
      }
      placeNumReviews.setText(getResources().getQuantityString(R.plurals.num_of_reviews,
              numOfReviews, numOfReviews));

      mapAddress.setText(currentPlace.getPlaceAddress());
      String distanceStr = getString(R.string.distance_with_miles,
              String.format(Locale.ENGLISH, "%.2f", currentPlace.getPlaceDistance()));

      mapDistance.setText(distanceStr);
      placeLatLng = new LatLng(currentPlace.getPlaceLatitude(),
              currentPlace.getPlaceLongitude());

      if (currentPlace.getPlaceRecommendedAges() != null &&
              !currentPlace.getPlaceRecommendedAges().equals("")) {
        placeRecommemdedAgesTitle.setVisibility(View.VISIBLE);
        placeRecommemdedAges.setVisibility(View.VISIBLE);
        placeRecommemdedAges.setText(currentPlace.getPlaceRecommendedAges());
      } else {
        placeRecommemdedAgesTitle.setVisibility(View.GONE);
        placeRecommemdedAges.setVisibility(View.GONE);
      }

      //Setting attr tooltips
      TooltipCompat.setTooltipText(hasKidsMenu, getString(R.string.kiddie_attr_tooltip));
      TooltipCompat.setTooltipText(hasKidsBathrooms, getString(R.string.kiddie_attr_tooltip));
      TooltipCompat.setTooltipText(hasKidsEquipRental, getString(R.string.kiddie_attr_tooltip));
      TooltipCompat.setTooltipText(hasKidsSpecialPrograms, getString(R.string.kiddie_attr_tooltip));
      TooltipCompat.setTooltipText(hasKidsPlayArea, getString(R.string.kiddie_attr_tooltip));

      //Update rating values
      if (currentPlace.getPlaceRating() != null) {
        placeRatingbar.setRating(currentPlace.getPlaceRating());
      } else {
        placeRatingbar.setRating(0.0F);
      }
      //Update attribute values
      setAttributeValueonUI(currentPlace.getHasKidsMenu(), hasKidsMenu);
      setAttributeValueonUI(currentPlace.getHasBathrooms(), hasKidsBathrooms);
      setAttributeValueonUI(currentPlace.getHasKidsEquipmentRental(), hasKidsEquipRental);
      setAttributeValueonUI(currentPlace.getHasKidsSpecialPrograms(), hasKidsSpecialPrograms);
      setAttributeValueonUI(currentPlace.getHasKidsPlayArea(), hasKidsPlayArea);

      LinearLayoutManager layoutManagerReviews = new LinearLayoutManager(getActivity());
      layoutManagerReviews.setOrientation(LinearLayoutManager.VERTICAL);
      reviewsRecyclerView.setLayoutManager(layoutManagerReviews);
      mReviewsAdapter = new ReviewsAdapter(getContext());
      reviewsRecyclerView.setAdapter(mReviewsAdapter);

      getActivity().invalidateOptionsMenu();
    }

    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    updatePlaceReviews();
  }

  private void updatePlaceReviews() {
    final List<Review> listOfReviews = new ArrayList<Review>();
    mReviewsDatabaseReference.orderByChild("placeId")
            .equalTo(currentPlace.getPlaceId())
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                  for (DataSnapshot snapshotChild : dataSnapshot.getChildren()) {
                    Review review = snapshotChild.getValue(Review.class);
                    if(review.getReviewComments() != null &&
                            !review.getReviewComments().equals("")) {
                      listOfReviews.add(review);
                    }
                  }
                  mReviewsAdapter.setReviewDetails(listOfReviews);
                }
                if (dataSnapshot.getChildrenCount() > 0) {
                  placeNumReviews
                          .setText(getResources().getQuantityString(R.plurals.num_of_reviews,
                                  (int)dataSnapshot.getChildrenCount(),
                                  (int)dataSnapshot.getChildrenCount()));
                }

                if(listOfReviews.size() == 0) {
                  reviewsTitle.setVisibility(View.GONE);
                  reviewsRecyclerView.setVisibility(View.GONE);
                  agesLowerBorder.setVisibility(View.VISIBLE);
                } else {
                  reviewsTitle.setVisibility(View.VISIBLE);
                  reviewsRecyclerView.setVisibility(View.VISIBLE);
                  agesLowerBorder.setVisibility(View.GONE);
                }
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
            });
  }

  private void setOrUnsetFavorite() {
    Drawable favorite_border = getActivity().getResources()
            .getDrawable(R.drawable.ic_favourite_border);
    Drawable favorite = getActivity().getResources()
            .getDrawable(R.drawable.ic_favourite);

    Favorites newFavorites;
    ArrayList<String> newFavPlaceIds = new ArrayList<>();
    if (currentUserFavoritePlaces != null) {
      newFavPlaceIds = currentUserFavoritePlaces.getFavoritePlaceIds();
    }
    if (toolbarFavIcon.getTag().equals("favorite_border")) {
      toolbarFavIcon.setImageDrawable(favorite);
      toolbarFavIcon.setTag("favorite");
      currentPlace.setIsFavorite(true);

      if (!newFavPlaceIds.contains(currentPlace.getPlaceId())) {
        newFavPlaceIds.add(currentPlace.getPlaceId());
        newFavorites = new Favorites(newFavPlaceIds);
        mFavoritesDatabaseReference.child(currentFirebaseUid)
                .setValue(newFavorites);
      }

      Snackbar.make(rootLayout,
              getActivity().getResources().getString(R.string.save_favorite),
              Snackbar.LENGTH_LONG)
              .show();
    } else if (toolbarFavIcon.getTag().equals("favorite")) {
      toolbarFavIcon.setImageDrawable(favorite_border);
      toolbarFavIcon.setTag("favorite_border");
      currentPlace.setIsFavorite(false);

      newFavPlaceIds.remove(currentPlace.getPlaceId());
      newFavorites = new Favorites(newFavPlaceIds);
      mFavoritesDatabaseReference.child(currentFirebaseUid)
              .setValue(newFavorites);

      Snackbar.make(rootLayout,
              getActivity().getResources().getString(R.string.unsave_favorite),
              Snackbar.LENGTH_LONG)
              .show();
    }
    getActivity().invalidateOptionsMenu();
  }

  private void setAttributeValueonUI(Boolean attrValue, View view) {
    if (attrValue != null) {
      if (attrValue) {
        view.setBackground(
                getActivity().getResources().getDrawable(R.drawable.circle_green));
      } else {
        view.setBackground(
                getActivity().getResources().getDrawable(R.drawable.circle_red));
      }
    }
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mapView.onCreate(savedInstanceState);
    mapView.onResume();
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    if (placeLatLng != null) {
      googleMap.addMarker(new MarkerOptions().position(placeLatLng));
      googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, MAP_ZOOM));
    }
  }

  private void getPlacePhoto(String placeId) {
    final Task<PlacePhotoMetadataResponse> photoMetadataResponse =
            mGeoDataClient.getPlacePhotos(placeId);
    photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
      @Override
      public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
        if (task.isSuccessful() && task.getResult() != null) {
          PlacePhotoMetadataBuffer photoMetadataBuffer =
                  task.getResult().getPhotoMetadata();
          if (photoMetadataBuffer.getCount() > 0) {
            PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
            mGeoDataClient.getPhoto(photoMetadata)
                    .addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                      @Override
                      public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                          placePhoto = task.getResult().getBitmap();
                          if (placePhoto != null) {
                            appbarPlacePhoto.setImageBitmap(placePhoto);
                          }
                        } else {
                          appBarLayout.setExpanded(false);
                        }
                      }
                    });
          } else {
            appBarLayout.setExpanded(false);
          }
        }
      }
    });
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.place_details_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    if (currentPlace.getIsFavorite() != null && currentPlace.getIsFavorite()) {
      MenuItem menuItem = menu.findItem(R.id.favorite);
      menuItem.setTitle(getActivity().getResources().getString(R.string.remove_favorite));
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.share:
        String shareIntentText = getString(R.string.share_intent_text) +
                currentPlace.getPlaceName();
        if(currentPlace.getPlaceWebsiteUrl() != null) {
          shareIntentText = shareIntentText + "\n" + currentPlace.getPlaceWebsiteUrl();
        }
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText(shareIntentText)
                .getIntent(), getString(R.string.share_using)));
        return true;
      case R.id.write_review:
        Intent reviewIntent = new Intent(getActivity(), ReviewActivity.class);
        Bundle reviewBundle = new Bundle();
        reviewBundle.putString(CURRENT_PLACE_ID, currentPlace.getPlaceId());
        reviewBundle.putString(CURRENT_PLACE_NAME, currentPlace.getPlaceName());
        reviewIntent.putExtras(reviewBundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          startActivityForResult(reviewIntent, REQUEST_CODE_PLACE_DETAILS,
                  ActivityOptionsCompat.makeSceneTransitionAnimation(
                          getActivity()).toBundle());
        } else {
          startActivityForResult(reviewIntent, REQUEST_CODE_PLACE_DETAILS);
        }
        return true;
      case R.id.favorite:
        setOrUnsetFavorite();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == REQUEST_CODE_PLACE_DETAILS) {
      if(resultCode == Activity.RESULT_OK) {
        FirebasePlace returnPlace = data.getExtras().getParcelable(ReviewActivity.RETURN_PLACE);
        Boolean hasKidsMenuBool = FirebaseUtils.
                getBooleanForAttribute(returnPlace.getHasKidsMenuAvg(),
                        returnPlace.getPlaceNumOfReviews());
        Boolean hasBathroomsBool = FirebaseUtils.
                getBooleanForAttribute(returnPlace.getHasKidsBathroomsAvg(),
                        returnPlace.getPlaceNumOfReviews());
        Boolean hasKidsEquipRentalBool = FirebaseUtils.
                getBooleanForAttribute(returnPlace.getHasKidsEquipmentRentalAvg(),
                        returnPlace.getPlaceNumOfReviews());
        Boolean hasKidsProgramsBool = FirebaseUtils.
                getBooleanForAttribute(returnPlace.getHasKidsSpecialProgramsAvg(),
                        returnPlace.getPlaceNumOfReviews());
        Boolean hasKidsPlayAreaBool = FirebaseUtils.
                getBooleanForAttribute(returnPlace.getHasKidsPlayAreaAvg(),
                        returnPlace.getPlaceNumOfReviews());
        String newAgeRecommendations =
                FirebaseUtils.getAgeRecommendations(
                        returnPlace.getPlaceRecommendedChildAgesCnts(),
                        returnPlace.getPlaceNumOfReviews(),
                        getActivity()
                );

        currentPlace.setFirebasePlaceDetails(
                returnPlace.getPlaceRating(),
                returnPlace.getPlaceNumOfReviews(),
                newAgeRecommendations,
                hasKidsMenuBool,
                hasBathroomsBool,
                hasKidsEquipRentalBool,
                hasKidsProgramsBool,
                hasKidsPlayAreaBool);

        //Update rating values
        if (currentPlace.getPlaceRating() != null) {
          placeRatingbar.setRating(currentPlace.getPlaceRating());
        } else {
          placeRatingbar.setRating(0.0F);
        }
        //Update attribute values
        setAttributeValueonUI(currentPlace.getHasKidsMenu(), hasKidsMenu);
        setAttributeValueonUI(currentPlace.getHasBathrooms(), hasKidsBathrooms);
        setAttributeValueonUI(currentPlace.getHasKidsEquipmentRental(), hasKidsEquipRental);
        setAttributeValueonUI(currentPlace.getHasKidsSpecialPrograms(), hasKidsSpecialPrograms);
        setAttributeValueonUI(currentPlace.getHasKidsPlayArea(), hasKidsPlayArea);
        //Update ageRecommendations
        if (currentPlace.getPlaceRecommendedAges() != null) {
          placeRecommemdedAgesTitle.setVisibility(View.VISIBLE);
          placeRecommemdedAges.setVisibility(View.VISIBLE);
          placeRecommemdedAges.setText(currentPlace.getPlaceRecommendedAges());
        } else {
          placeRecommemdedAgesTitle.setVisibility(View.GONE);
          placeRecommemdedAges.setVisibility(View.GONE);
        }
      }
    }
  }

  public Place getCurrentUpdatedPlace() {
    return currentPlace;
  }
}
