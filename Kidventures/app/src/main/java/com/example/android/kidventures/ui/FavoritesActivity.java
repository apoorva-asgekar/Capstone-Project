package com.example.android.kidventures.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.android.kidventures.R;

/**
 * Created by apoorva on 3/1/18.
 */

public class FavoritesActivity extends AppCompatActivity {

  private static final String LOG_TAG = FavoritesActivity.class.getSimpleName();
  public static final String ARG_FIREBASE_UID = "firebaseUid";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_favorites);
  }

  @Override
  public void onBackPressed() {
    getSupportFragmentManager().popBackStack();
    setResult(Activity.RESULT_OK);
    supportFinishAfterTransition();
    return;
  }
}