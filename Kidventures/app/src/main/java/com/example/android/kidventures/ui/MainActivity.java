package com.example.android.kidventures.ui;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.kidventures.BuildConfig;
import com.example.android.kidventures.R;
import com.example.android.kidventures.data.UserProfile;
import com.example.android.kidventures.utilities.FirebaseUtils;
import com.example.android.kidventures.utilities.LocationUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

  private final static String LOG_TAG = MainActivity.class.getSimpleName();

  private static final int REQUEST_CODE_SIGN_IN = 111;
  private static final int REQUEST_CODE_FAV_FRAGMENT = 222;
  private final String SAVED_STATE_FRAGMENT = "fragment";
  private final String SAVED_STATE_CURRENT_USER_PROFILE = "current_user_profile";
  private final String HOME_FRAG = "homepage";
  private final String USER_PROFILE_FRAG = "user_profile";
  private final String FAVORITES_FRAG = "favorites";

  public static final String USER_BUNDLE = "user_bundle";
  public static final String SEARCH_QUERY = "query";

  @BindView(R.id.drawer_layout)
  DrawerLayout mDrawerLayout;
  @BindView(R.id.navigation_view)
  NavigationView mNavigationView;
  @BindView(R.id.toolbar)
  Toolbar mToolbar;

  private View navHeader;
  private TextView profileName;
  private ImageView profilePhoto;
  private ActionBarDrawerToggle mDrawerToggle;
  private String CURRENT_FRAG = null;
  private boolean mUserPrefsShown = false;
  private UserProfile mCurrentUserProfile;

  //Firebase Variables
  private FirebaseAuth mFirebaseAuth;
  private FirebaseAuth.AuthStateListener mAuthStateListener;
  private FirebaseUser mFirebaseUser = null;
  private FirebaseDatabase mFirebaseDatabase;
  private DatabaseReference mUserProfilesDatabaseReference;
  private ValueEventListener mUserProfileListener = new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
      mCurrentUserProfile = dataSnapshot.getValue(UserProfile.class);
      Log.d(LOG_TAG, "Current user is: " + mCurrentUserProfile.getUserDisplayName());
      if (CURRENT_FRAG == null) {
        loadHomePage();
      }
      // load nav menu header data
      loadNavHeader();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);

    navHeader = mNavigationView.getHeaderView(0);
    profileName = (TextView) navHeader.findViewById(R.id.profile_name);
    profilePhoto = (ImageView) navHeader.findViewById(R.id.profile_img);

    mFirebaseDatabase = FirebaseDatabase.getInstance();
    mFirebaseAuth = FirebaseAuth.getInstance();

    mUserProfilesDatabaseReference = mFirebaseDatabase.getReference()
            .child(FirebaseUtils.USER_PROFILE_FIREBASE_DATABASE_TABLE);

    mDrawerLayout.setFocusable(true);
    mNavigationView.setFocusable(true);

    mAuthStateListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        mFirebaseUser = firebaseAuth.getCurrentUser();

        if (mFirebaseUser != null) {
          //User is signed in
          FirebaseUserMetadata metadata = mFirebaseUser.getMetadata();
          if (metadata != null) {
            if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp() &&
                    !mUserPrefsShown) {
              //New user
              UserProfile newUser = new UserProfile(
                      mFirebaseUser.getUid(),
                      mFirebaseUser.getDisplayName(),
                      null,
                      mFirebaseUser.getEmail(),
                      mFirebaseUser.getPhotoUrl().toString(),
                      UserProfile.getDefaultChildAges()
              );
              //Save the user in firebase
              mUserProfilesDatabaseReference
                      .child(mFirebaseUser.getUid())
                      .setValue(newUser);
              //Show a user preferences screen
              mUserPrefsShown = true;
              Intent newUserActivity =
                      new Intent(MainActivity.this, UserPreferencesActivity.class);
              Bundle userBundle = new Bundle();
              userBundle.putParcelable(
                      UserPreferencesActivity.KIDVENTURES_USER_PROFILE, newUser);
              newUserActivity.putExtra(USER_BUNDLE, userBundle);
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivity(newUserActivity,
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                MainActivity.this).toBundle());
              } else {
                startActivity(newUserActivity);
              }
            } else {
              //Old User
              mUserProfilesDatabaseReference
                      .child(mFirebaseUser.getUid())
                      .addListenerForSingleValueEvent(mUserProfileListener);
            }
          } else {
            //Old user - metadata is returned as null for old user
            //TODO Remove after Google has fixed this bug

            mUserProfilesDatabaseReference
                    .child(mFirebaseUser.getUid())
                    .addListenerForSingleValueEvent(mUserProfileListener);
          }
        } else {
          //User is signed out
          loadHomePage();
          Toast.makeText(MainActivity.this, R.string.logout_message,
                  Toast.LENGTH_LONG).show();
          startActivityForResult(
                  AuthUI.getInstance()
                          .createSignInIntentBuilder()
                          .setIsSmartLockEnabled(false)
                          .setAvailableProviders(
                                  Arrays.asList(//new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                          new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                          .build(),
                  REQUEST_CODE_SIGN_IN);
        }

      }
    };

    setSupportActionBar(mToolbar);

    mDrawerToggle = new ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            mToolbar,
            R.string.drawer_open,  /* "open drawer" description for accessibility */
            R.string.drawer_close  /* "close drawer" description for accessibility */
    );
    mDrawerLayout.addDrawerListener(mDrawerToggle);
    mDrawerToggle.syncState();

    // initializing navigation menu
    setUpNavigationView();

    //Checking for any saved state
    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(SAVED_STATE_CURRENT_USER_PROFILE)) {
        mCurrentUserProfile =
                savedInstanceState.getParcelable(SAVED_STATE_CURRENT_USER_PROFILE);
      }
      if (savedInstanceState.containsKey(SAVED_STATE_FRAGMENT)) {
        CURRENT_FRAG = savedInstanceState.getString(SAVED_STATE_FRAGMENT);
      }
    }

  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Pass any configuration change to the drawer toggles
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  protected void onStart() {
    super.onStart();
    //Making sure that the app has permissions to access device location.
    if (!LocationUtils.checkPermissions(this)) {
      LocationUtils.requestPermissions(this);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    if(CURRENT_FRAG != null) {
      if (CURRENT_FRAG.equals(FAVORITES_FRAG)) {
        loadHomePage();
      } else if(CURRENT_FRAG.equals(USER_PROFILE_FRAG)) {
        setTitle(mNavigationView.getMenu().getItem(1).getTitle());
      }
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mAuthStateListener != null) {
      mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(SAVED_STATE_FRAGMENT, CURRENT_FRAG);
    outState.putParcelable(SAVED_STATE_CURRENT_USER_PROFILE, mCurrentUserProfile);
  }

  @Override
  public void onBackPressed() {
    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawer(GravityCompat.START);
    } else {
      if (CURRENT_FRAG.equals(HOME_FRAG)) {
        super.onBackPressed();
      } else {
        loadHomePage();
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CODE_SIGN_IN) {
      if (resultCode == RESULT_OK) {
        String signInMsg = "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName()
                + ".\n" + getString(R.string.sign_in_message);
        Toast.makeText(this, signInMsg, Toast.LENGTH_SHORT).show();
      }
    }

  }

  /*
   * Load navigation menu header information
   */
  private void loadNavHeader() {

    if (mCurrentUserProfile.getUserName() == null) {
      profileName.setText(mCurrentUserProfile.getUserDisplayName());
    } else {
      profileName.setText(mCurrentUserProfile.getUserName());
    }
    if (mCurrentUserProfile.getUserPhotoUrl() != null) {
      Glide.with(this)
              .load(mCurrentUserProfile.getUserPhotoUrl())
              .apply(new RequestOptions()
                      .placeholder(R.drawable.image_placeholder)
                      .centerCrop()
                      .bitmapTransform(new CircleCrop())
                      .diskCacheStrategy(DiskCacheStrategy.ALL))
              .thumbnail(0.5f)
              .into(profilePhoto);
    }

  }

  private void setUpNavigationView() {

    //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
    mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

      // This method will trigger on item Click of navigation menu
      @Override
      public boolean onNavigationItemSelected(MenuItem menuItem) {

        menuItem.setChecked(true);

        mDrawerLayout.closeDrawers();

        //Check to see which item was being clicked and perform appropriate action
        switch (menuItem.getItemId()) {
          //Replacing the main content with ContentFragment Which is our Inbox View;
          case R.id.nav_home:
            loadHomePage();
            break;
          case R.id.nav_profile:
            loadUserProfile();
            break;
          case R.id.nav_favorites:
            loadFavorites();
            break;
          case R.id.nav_logout:
            Toast.makeText(MainActivity.this, R.string.sign_out_message, Toast.LENGTH_SHORT).show();
            //Log out of the application
            AuthUI.getInstance().signOut(MainActivity.this);
            break;
        }


        return true;
      }
    });
  }

  private void loadFragment(Fragment fragment) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    if (fragment != null) {
      fragmentManager.beginTransaction()
              .replace(R.id.content_frame, fragment, CURRENT_FRAG)
              .commit();
    }
  }

  private void loadHomePage() {
    Log.d(LOG_TAG, "Loading Homepage");

    CURRENT_FRAG = HOME_FRAG;

    //Select the menuItem
    mNavigationView.getMenu().getItem(0).setChecked(true);
    //Set correct Appbar title
    setTitle(R.string.app_name);

    //Initialize and load fragment
    Fragment fragment = new HomeFragment();
    loadFragment(fragment);
  }

  private void loadUserProfile() {
    Log.d(LOG_TAG, "Loading Profile");

    CURRENT_FRAG = USER_PROFILE_FRAG;

    //Select the menuItem
    mNavigationView.getMenu().getItem(1).setChecked(true);
    //Set correct Appbar title
    setTitle(mNavigationView.getMenu().getItem(1).getTitle());

    //Initialize and load fragment
    Fragment fragment = UserProfileFragment.newInstance(mCurrentUserProfile);
    loadFragment(fragment);
  }

  private void loadFavorites() {
    Log.d(LOG_TAG, "Loading Favorites");

    CURRENT_FRAG = FAVORITES_FRAG;

    //Select the menuItem
    mNavigationView.getMenu().getItem(2).setChecked(true);
    //Set correct Appbar title
    setTitle(mNavigationView.getMenu().getItem(2).getTitle());

    //Initialize and load fragment
    Intent favoritesActivityIntent =
            new Intent(MainActivity.this, FavoritesActivity.class);
    Bundle favoritesBundle = new Bundle();
    favoritesBundle
            .putString(FavoritesActivity.ARG_FIREBASE_UID, mCurrentUserProfile.getUserFirebaseUid());
    favoritesActivityIntent.putExtras(favoritesBundle);
    startActivityForResult(favoritesActivityIntent, REQUEST_CODE_FAV_FRAGMENT);
  }

  /**
   * Callback received when a permissions request has been completed.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    if (requestCode == LocationUtils.REQUEST_PERMISSIONS_REQUEST_CODE) {
      if (grantResults.length <= 0) {
        // If user interaction was interrupted, the permission request is cancelled and you
        // receive empty arrays.
        Log.d(LOG_TAG, "User interaction was cancelled.");
      } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // Permission granted.
        Log.d(LOG_TAG, "Location Permission Granted");
      } else {
        // Permission denied.
        Log.d(LOG_TAG, "Location Permission Denied");
        // Notify the user via a SnackBar that they have rejected a core permission for the
        // app, which makes the Activity useless. In a real app, core permissions would
        // typically be best requested during a welcome-screen flow.

        // Additionally, it is important to remember that a permission might have been
        // rejected without asking the user for permission (device policy or "Never ask
        // again" prompts). Therefore, a user interface affordance is typically implemented
        // when permissions are denied. Otherwise, your app could appear unresponsive to
        // touches or interactions which have required permissions.
        LocationUtils
                .showSnackbar(R.string.permission_denied_explanation, R.string.settings, this,
                        new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                          }
                        });
      }
    }
  }
}
