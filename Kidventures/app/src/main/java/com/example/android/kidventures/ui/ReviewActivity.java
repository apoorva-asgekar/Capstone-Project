package com.example.android.kidventures.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.kidventures.R;
import com.example.android.kidventures.data.FirebasePlace;
import com.example.android.kidventures.data.Review;
import com.example.android.kidventures.data.UserProfile;
import com.example.android.kidventures.utilities.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewActivity extends AppCompatActivity {

  private static final String LOG_TAG = ReviewActivity.class.getSimpleName();
  private static final String REVIEW_STATUS_SAVE = "review_save";
  private static final String REVIEW_STATUS_CANCEL = "review_cancel";
  private final String YES = "Yes";
  private final String NO = "No";
  private final String UNKNOWN = "Unknown";

  public static final String RETURN_PLACE = "return_place";

  private String placeId = null;
  private String placeName = null;
  private Float rating = 0.0f;
  private Integer hasKidsMenu = 0;
  private Integer hasKidsBathrooms = 0;
  private Integer hasKidsEquipRental = 0;
  private Integer hasKidsPrograms = 0;
  private Integer hasKidsPlayArea = 0;
  private HashMap<String, Object> ageRecommendations = FirebaseUtils.getDefaultRecommendations();
  private FirebasePlace firebaseOldPlace = null;
  private String reviewerUsername;

  @BindView(R.id.review_root_view)
  NestedScrollView reviewRootView;
  @BindView(R.id.review_place_name)
  TextView mPlaceName;
  @BindView(R.id.review_place_rating)
  RatingBar mPlaceRating;
  @BindView(R.id.age0_2)
  CheckBox mAge0to2CheckBox;
  @BindView(R.id.age2_5)
  CheckBox mAge2to5CheckBox;
  @BindView(R.id.age5_8)
  CheckBox mAge5to8CheckBox;
  @BindView(R.id.age8_12)
  CheckBox mAge8to12CheckBox;
  @BindView(R.id.age12plus)
  CheckBox mAge12PlusCheckBox;
  @BindView(R.id.attr_spinner_menu)
  Spinner mHasKidsMenu;
  @BindView(R.id.attr_spinner_bathrooms)
  Spinner mHasKidsBathrooms;
  @BindView(R.id.attr_spinner_equip_rental)
  Spinner mHasKidsEquipRental;
  @BindView(R.id.attr_spinner_programs)
  Spinner mHasKidsPrograms;
  @BindView(R.id.attr_spinner_play_area)
  Spinner mHasKidsPlayArea;
  @BindView(R.id.review_comments)
  TextView mReviewComments;
  @BindView(R.id.review_saveButton)
  Button mSaveButton;
  @BindView(R.id.review_cancelButton)
  Button mCancelButton;

  private DatabaseReference mReviewsDatabaseReference;
  private DatabaseReference mPlacesDatabaseReference;
  private DatabaseReference mUserProfilesDatabaseReference;

  private ValueEventListener placeListener = new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
      if (dataSnapshot.exists()) {
        firebaseOldPlace = dataSnapshot.getValue(FirebasePlace.class);
      }
      insertOrUpdatePlace();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
  };

  AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      String selectedItem = parent.getSelectedItem().toString();
      Integer valueOfSpinner = 0;
      if (selectedItem.equals(YES)) {
        valueOfSpinner = 1;
      } else if (selectedItem.equals(NO)) {
        valueOfSpinner = -1;
      } else if (selectedItem.equals(UNKNOWN)) {
        valueOfSpinner = 0;
      }
      switch (parent.getId()) {
        case R.id.attr_spinner_menu:
          hasKidsMenu = valueOfSpinner;
          break;
        case R.id.attr_spinner_bathrooms:
          hasKidsBathrooms = valueOfSpinner;
          break;
        case R.id.attr_spinner_equip_rental:
          hasKidsEquipRental = valueOfSpinner;
          break;
        case R.id.attr_spinner_programs:
          hasKidsPrograms = valueOfSpinner;
          break;
        case R.id.attr_spinner_play_area:
          hasKidsPlayArea = valueOfSpinner;
          break;
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_review);

    ButterKnife.bind(this);

    FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    mReviewsDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.REVIEWS_FIREBASE_DATABASE_TABLE);
    mPlacesDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.PLACES_FIREBASE_DATABASE_TABLE);
    mUserProfilesDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.USER_PROFILE_FIREBASE_DATABASE_TABLE);

    //Get the current user's username from firebase
    mUserProfilesDatabaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                  UserProfile currentUser = dataSnapshot.getValue(UserProfile.class);
                  reviewerUsername = currentUser.getUserName();
                }
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
            });

    final Bundle reviewBundle = getIntent().getExtras();
    if (reviewBundle != null) {
      if (reviewBundle.containsKey(PlaceDetailsFragment.CURRENT_PLACE_ID)) {
        placeId = reviewBundle.getString(PlaceDetailsFragment.CURRENT_PLACE_ID);
      }
      if (reviewBundle.containsKey(PlaceDetailsFragment.CURRENT_PLACE_NAME)) {
        placeName = reviewBundle.getString(PlaceDetailsFragment.CURRENT_PLACE_NAME);
      }
    }

    if (placeName != null) {
      mPlaceName.setText(placeName);
    }

    mPlaceRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
      @Override
      public void onRatingChanged(RatingBar ratingBar, float currentRating, boolean fromUser) {
        rating = currentRating;
      }
    });

    mHasKidsMenu.setOnItemSelectedListener(spinnerListener);
    mHasKidsBathrooms.setOnItemSelectedListener(spinnerListener);
    mHasKidsEquipRental.setOnItemSelectedListener(spinnerListener);
    mHasKidsPrograms.setOnItemSelectedListener(spinnerListener);
    mHasKidsPlayArea.setOnItemSelectedListener(spinnerListener);

    mSaveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (rating > 0.0f) {
          saveReviewAndUpdatePlace();
        } else {
          Snackbar.make(reviewRootView
                  , getString(R.string.review_rating_compulsory)
                  , Snackbar.LENGTH_LONG).show();
        }
      }
    });

    mCancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        endActivity(firebaseOldPlace, REVIEW_STATUS_CANCEL);
      }
    });

  }

  private void saveReviewAndUpdatePlace() {
    String reviewComments = mReviewComments.getText().toString();
    String firebaseUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    String reviewKey = mReviewsDatabaseReference.push().getKey();
    Review placeReview = new Review(
            reviewKey,
            placeId,
            firebaseUid,
            reviewerUsername,
            rating,
            reviewComments,
            ageRecommendations,
            hasKidsMenu,
            hasKidsBathrooms,
            hasKidsEquipRental,
            hasKidsPrograms,
            hasKidsPlayArea);

    //Saving the review to firebase
    mReviewsDatabaseReference.child(reviewKey).setValue(placeReview, new DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        //Getting the current place details from Firebase
        mPlacesDatabaseReference.child(placeId).addListenerForSingleValueEvent(placeListener);
      }
    });
  }

  private void endActivity(FirebasePlace returnPlace, String reviewStatus) {

    if (reviewStatus.equals(REVIEW_STATUS_SAVE)) {
      Intent returnIntent = new Intent();
      Bundle returnBundle = new Bundle();
      returnBundle.putParcelable(RETURN_PLACE, returnPlace);
      returnIntent.putExtras(returnBundle);
      setResult(Activity.RESULT_OK, returnIntent);
    } else if (reviewStatus.equals(REVIEW_STATUS_CANCEL)) {
      setResult(Activity.RESULT_CANCELED);
    }
    supportFinishAfterTransition();
    return;
  }

  private void insertOrUpdatePlace() {

    FirebasePlace newOrUpdatedPlace;
    HashMap<String, Integer> updatedAgeRecommendationCounts = new HashMap<>();

    if (firebaseOldPlace != null) {
      Log.d(LOG_TAG, firebaseOldPlace.toString());
      Float oldPlaceRating = firebaseOldPlace.getPlaceRating();
      Integer oldNumOfReviews = firebaseOldPlace.getPlaceNumOfReviews();
      Integer newNumOfReviews = oldNumOfReviews + (int) 1;
      Float newPlaceRating =
              ((oldPlaceRating * oldNumOfReviews) + rating) / newNumOfReviews;

      Float newMenuAvg = (float) ((firebaseOldPlace.getHasKidsMenuAvg() * oldNumOfReviews) + hasKidsMenu)
              / (float) newNumOfReviews;
      Float newBathroomsAvg = (float) ((firebaseOldPlace.getHasKidsBathroomsAvg() * oldNumOfReviews) + hasKidsBathrooms)
              / (float) newNumOfReviews;
      Float newEquipRentalAvg = (float) ((firebaseOldPlace.getHasKidsEquipmentRentalAvg() * oldNumOfReviews) + hasKidsEquipRental)
              / (float) newNumOfReviews;
      Float newProgramsAvg = (float) ((firebaseOldPlace.getHasKidsSpecialProgramsAvg() * oldNumOfReviews) + hasKidsPrograms)
              / (float) newNumOfReviews;
      Float newPlayAreaAvg = (float) ((firebaseOldPlace.getHasKidsPlayAreaAvg() * oldNumOfReviews) + hasKidsPlayArea)
              / (float) newNumOfReviews;
      updatedAgeRecommendationCounts =
              FirebaseUtils.getUpdatedAgeRecommendationCounts(
                      ageRecommendations,
                      firebaseOldPlace.getPlaceRecommendedChildAgesCnts()
              );

      newOrUpdatedPlace = new FirebasePlace(
              placeId,
              placeName,
              newPlaceRating,
              newNumOfReviews,
              updatedAgeRecommendationCounts,
              newMenuAvg,
              newBathroomsAvg,
              newEquipRentalAvg,
              newProgramsAvg,
              newPlayAreaAvg
      );
    } else {
      updatedAgeRecommendationCounts =
              FirebaseUtils.getUpdatedAgeRecommendationCounts(
                      ageRecommendations,
                      FirebaseUtils.getDefaultRecommendationCounts()
              );

      newOrUpdatedPlace = new FirebasePlace(
              placeId,
              placeName,
              rating,
              1,
              updatedAgeRecommendationCounts,
              (float) hasKidsMenu,
              (float) hasKidsBathrooms,
              (float) hasKidsEquipRental,
              (float) hasKidsPrograms,
              (float) hasKidsPlayArea
      );
    }

    //Saving the updated Place to firebase
    mPlacesDatabaseReference.child(placeId).setValue(newOrUpdatedPlace);

    endActivity(newOrUpdatedPlace, REVIEW_STATUS_SAVE);

  }

  public void onChildAgeSelected(View view) {
    //Current checkbox status
    boolean isChecked = ((CheckBox) view).isChecked();

    //Check which checkbox was checked and update the appropriate value in hashmap
    switch (view.getId()) {
      case R.id.age0_2:
        if (isChecked) {
          ageRecommendations.put(FirebaseUtils.AGE0TO2, true);
        } else {
          ageRecommendations.put(FirebaseUtils.AGE0TO2, false);
        }
        break;
      case R.id.age2_5:
        if (isChecked) {
          ageRecommendations.put(FirebaseUtils.AGE2TO5, true);
        } else {
          ageRecommendations.put(FirebaseUtils.AGE2TO5, false);
        }
        break;
      case R.id.age5_8:
        if (isChecked) {
          ageRecommendations.put(FirebaseUtils.AGE5TO8, true);
        } else {
          ageRecommendations.put(FirebaseUtils.AGE5TO8, false);
        }
        break;
      case R.id.age8_12:
        if (isChecked) {
          ageRecommendations.put(FirebaseUtils.AGE8TO12, true);
        } else {
          ageRecommendations.put(FirebaseUtils.AGE8TO12, false);
        }
        break;
      case R.id.age12plus:
        if (isChecked) {
          ageRecommendations.put(FirebaseUtils.AGE12PLUS, true);
        } else {
          ageRecommendations.put(FirebaseUtils.AGE12PLUS, false);
        }
        break;
    }

  }
}
