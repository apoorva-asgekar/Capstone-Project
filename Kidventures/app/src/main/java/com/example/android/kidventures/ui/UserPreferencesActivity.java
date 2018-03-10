package com.example.android.kidventures.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.kidventures.R;
import com.example.android.kidventures.data.UserProfile;
import com.example.android.kidventures.utilities.FirebaseUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apoorva on 1/17/18.
 */

public class UserPreferencesActivity extends AppCompatActivity {

  private static final String LOG_TAG = UserPreferencesActivity.class.getSimpleName();

  private static final String EDIT_PROFILE_SAVE = "save";
  private static final String EDIT_PROFILE_CANCEL = "cancel";
  private static final int REQUEST_CODE_PHOTO_PICKER = 444;

  public final static String KIDVENTURES_USER_PROFILE = "kidventures_user";
  public static final String RETURN_USER_PROFILE = "returnUser";
  public final static int REQUEST_CODE_EDIT_PROFILE = 555;

  @BindView(R.id.root_layout)
  RelativeLayout mRootLayout;
  @BindView(R.id.tv_profile_username_title)
  TextView mUsernameTitle;
  @BindView(R.id.edit_tv_username)
  EditText mUsername;
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
  @BindView(R.id.photoPickerButton)
  ImageButton mPhotoPickerButton;
  @BindView(R.id.saveButton)
  Button mSaveButton;
  @BindView(R.id.skipButton)
  Button mSkipButton;

  private UserProfile mCurrentUserProfile;
  private UserProfile mOriginalUserProfile;
  private HashMap<String, Object> mNewChildAgesMap = UserProfile.getDefaultChildAges();

  //Firebase Variables
  private DatabaseReference mUserProfilesDatabaseReference;
  private StorageReference mUserPhotosStorageReference;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_preferences);

    ButterKnife.bind(this);

    //Initialize Firebase components
    FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();

    mUserProfilesDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.USER_PROFILE_FIREBASE_DATABASE_TABLE);
    mUserPhotosStorageReference = mFirebaseStorage.getReference()
            .child(FirebaseUtils.USER_PHOTOS_FIREBASE_STORAGE_FOLDER);

    Bundle userBundle = getIntent().getBundleExtra(MainActivity.USER_BUNDLE);
    if (userBundle.containsKey(KIDVENTURES_USER_PROFILE)) {
      mCurrentUserProfile = userBundle.getParcelable(KIDVENTURES_USER_PROFILE);
      //Save the original user profile in case the user decides not to save changes.
      mOriginalUserProfile = mCurrentUserProfile;
      Log.d(LOG_TAG, "Current user profile: " + mCurrentUserProfile.getUserDisplayName());
    }

    //Setting current values on screen to the current user's user profile
    HashMap<String, Object> currentUserChildAgesMap = mCurrentUserProfile.getUserChildAges();

    mAge0to2CheckBox.setChecked(
            (boolean) currentUserChildAgesMap.get(UserProfile.AGE0TO2));
    mAge2to5CheckBox.setChecked(
            (boolean) currentUserChildAgesMap.get(UserProfile.AGE2TO5));
    mAge5to8CheckBox.setChecked(
            (boolean) currentUserChildAgesMap.get(UserProfile.AGE5TO8));
    mAge8to12CheckBox.setChecked(
            (boolean) currentUserChildAgesMap.get(UserProfile.AGE8TO12));
    mAge12PlusCheckBox.setChecked(
            (boolean) currentUserChildAgesMap.get(UserProfile.AGE12PLUS));

    mNewChildAgesMap = currentUserChildAgesMap;

    mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //Fire an intent to show an image picker
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent
                .createChooser(intent, "Complete action using"), REQUEST_CODE_PHOTO_PICKER);
      }
    });

    if (mOriginalUserProfile.getUserName() != null) {
      mUsernameTitle.setVisibility(View.GONE);
      mUsername.setVisibility(View.GONE);
    }

    mSaveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mOriginalUserProfile.getUserName() == null) {
          String newUserName = null;
          if (mUsername.getText() != null &&
                  validUserName(mUsername.getText().toString())) {
            newUserName = mUsername.getText().toString();
            mCurrentUserProfile.setUserName(newUserName);
          } else {
            Snackbar.make(mRootLayout,
                    getString(R.string.username_null_msg), Snackbar.LENGTH_LONG).show();
            return;
          }
        }
        //Save the updated user profile in firebase
        mUserProfilesDatabaseReference
                .child(mCurrentUserProfile.getUserFirebaseUid())
                .setValue(mCurrentUserProfile);
        Snackbar.make(mRootLayout,
                getString(R.string.user_profile_saved), Snackbar.LENGTH_LONG).show();
        endActivity(EDIT_PROFILE_SAVE);
      }
    });

    mSkipButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //Do nothing and end activity.
        endActivity(EDIT_PROFILE_CANCEL);
      }
    });

  }

  private void endActivity(String result) {
    if (result.equals(EDIT_PROFILE_SAVE)) {
      Intent returnIntent = new Intent();
      Bundle returnBundle = new Bundle();
      returnBundle.putParcelable(RETURN_USER_PROFILE, mCurrentUserProfile);
      returnIntent.putExtras(returnBundle);
      setResult(RESULT_OK, returnIntent);
    } else if (result.equals(EDIT_PROFILE_CANCEL)) {
      setResult(RESULT_CANCELED);
    }
    supportFinishAfterTransition();
    return;
  }

  private boolean validUserName(String username) {
    boolean valid = false;
    if (username.length() >= 5 && username.length() <= 15) {
      valid = true;
    }
    return valid;
  }

  public void onChildAgeSelected(View view) {
    //Current checkbox status
    boolean isChecked = ((CheckBox) view).isChecked();

    //Check which checkbox was checked and update the appropriate value in hashmap
    switch (view.getId()) {
      case R.id.age0_2:
        if (isChecked) {
          mNewChildAgesMap.put(UserProfile.AGE0TO2, true);
        } else {
          mNewChildAgesMap.put(UserProfile.AGE0TO2, false);
        }
        break;
      case R.id.age2_5:
        if (isChecked) {
          mNewChildAgesMap.put(UserProfile.AGE2TO5, true);
        } else {
          mNewChildAgesMap.put(UserProfile.AGE2TO5, false);
        }
        break;
      case R.id.age5_8:
        if (isChecked) {
          mNewChildAgesMap.put(UserProfile.AGE5TO8, true);
        } else {
          mNewChildAgesMap.put(UserProfile.AGE5TO8, false);
        }
        break;
      case R.id.age8_12:
        if (isChecked) {
          mNewChildAgesMap.put(UserProfile.AGE8TO12, true);
        } else {
          mNewChildAgesMap.put(UserProfile.AGE8TO12, false);
        }
        break;
      case R.id.age12plus:
        if (isChecked) {
          mNewChildAgesMap.put(UserProfile.AGE12PLUS, true);
        } else {
          mNewChildAgesMap.put(UserProfile.AGE12PLUS, false);
        }
        break;
    }

    mCurrentUserProfile.setUserChildAges(mNewChildAgesMap);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_PHOTO_PICKER) {
      if (resultCode == RESULT_OK) {
        Uri selectedImageUri = data.getData();
        StorageReference photoRef =
                mUserPhotosStorageReference.child(mCurrentUserProfile.getUserFirebaseUid());
        photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            Uri downloadUrl = taskSnapshot.getDownloadUrl();
            if (downloadUrl != null) {
              mCurrentUserProfile.setUserPhotoUrl(downloadUrl.toString());
            }
          }
        });
      }
    }
  }
}
