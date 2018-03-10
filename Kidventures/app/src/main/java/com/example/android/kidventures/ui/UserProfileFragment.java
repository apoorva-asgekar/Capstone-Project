package com.example.android.kidventures.ui;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.kidventures.R;
import com.example.android.kidventures.data.UserProfile;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserProfileFragment extends Fragment {

    private final static String LOG_TAG = UserProfileFragment.class.getSimpleName();
    private final static String USER_PROFILE_FRAG_ARG_CURRENT_USER = "current_user";

    @BindView(R.id.user_profile_photo)
    ImageView mUserPhoto;
    @BindView(R.id.user_profile_name)
    TextView mUserDisplayName;
    @BindView(R.id.user_profile_email)
    TextView mUserEmailAddress;
    @BindView(R.id.age0_2)
    CheckBox mAge0to2Chkbox;
    @BindView(R.id.age2_5)
    CheckBox mAge2to5Chkbox;
    @BindView(R.id.age5_8)
    CheckBox mAge5to8Chkbox;
    @BindView(R.id.age8_12)
    CheckBox mAge8to12Chkbox;
    @BindView(R.id.age12plus)
    CheckBox mAge12PlusChkbox;
    @BindView(R.id.user_profile_edit_prefs_button)
    Button mEditPrefsButton;

    private UserProfile mCurrentUser;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment UserProfileFragment.
     */
    public static UserProfileFragment newInstance(UserProfile currentUserProfile) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(USER_PROFILE_FRAG_ARG_CURRENT_USER, currentUserProfile);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        ButterKnife.bind(this, rootView);

        Bundle args = getArguments();
        mCurrentUser = args.getParcelable(USER_PROFILE_FRAG_ARG_CURRENT_USER);

        setUserPhoto();

        if(mCurrentUser.getUserName() == null) {
            mUserDisplayName.setText(mCurrentUser.getUserDisplayName());
        } else {
            String displayName = mCurrentUser.getUserDisplayName() + " ("
                    + mCurrentUser.getUserName() + ")";
            mUserDisplayName.setText(displayName);
        }
        mUserEmailAddress.setText(mCurrentUser.getUserEmailAddress());

        //Make checkboxes uncheckable since these are only displaying the current preference
        makeCheckboxesUnclickable();

        setCheckboxValues();

        mEditPrefsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userPrefsActivity =
                        new Intent(getActivity(), UserPreferencesActivity.class);
                Bundle userBundle = new Bundle();
                userBundle.putParcelable(
                        UserPreferencesActivity.KIDVENTURES_USER_PROFILE, mCurrentUser);
                userPrefsActivity.putExtra(MainActivity.USER_BUNDLE, userBundle);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(userPrefsActivity,
                            UserPreferencesActivity.REQUEST_CODE_EDIT_PROFILE,
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    getActivity()).toBundle());
                } else {
                    startActivityForResult(userPrefsActivity,
                            UserPreferencesActivity.REQUEST_CODE_EDIT_PROFILE);
                }
            }
        });

        return rootView;
    }

    private void setUserPhoto() {
      String photoUrl = mCurrentUser.getUserPhotoUrl();
      if(photoUrl != null) {
        photoUrl = photoUrl.replace("/s96-c/","/s500-c/");
        //Setting the views
        Glide.with(getActivity())
                .load(photoUrl)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.image_placeholder)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(mUserPhoto);
      }
    }

    private void makeCheckboxesUnclickable() {
        mAge0to2Chkbox.setClickable(false);
        mAge2to5Chkbox.setClickable(false);
        mAge5to8Chkbox.setClickable(false);
        mAge8to12Chkbox.setClickable(false);
        mAge12PlusChkbox.setClickable(false);
        mAge0to2Chkbox.setFocusable(false);
        mAge2to5Chkbox.setFocusable(false);
        mAge5to8Chkbox.setFocusable(false);
        mAge8to12Chkbox.setFocusable(false);
        mAge12PlusChkbox.setFocusable(false);
    }

    private void setCheckboxValues() {
      HashMap<String, Object> childAges = mCurrentUser.getUserChildAges();
      if((boolean)childAges.get(UserProfile.AGE0TO2)) {
        mAge0to2Chkbox.setChecked(true);
      } else {
        mAge0to2Chkbox.setChecked(false);
      }
      if((boolean)childAges.get(UserProfile.AGE2TO5)) {
        mAge2to5Chkbox.setChecked(true);
      } else {
        mAge2to5Chkbox.setChecked(false);
      }
      if((boolean)childAges.get(UserProfile.AGE5TO8)) {
        mAge5to8Chkbox.setChecked(true);
      } else {
        mAge5to8Chkbox.setChecked(false);
      }
      if((boolean)childAges.get(UserProfile.AGE8TO12)) {
        mAge8to12Chkbox.setChecked(true);
      } else {
        mAge8to12Chkbox.setChecked(false);
      }
      if((boolean)childAges.get(UserProfile.AGE12PLUS)) {
        mAge12PlusChkbox.setChecked(true);
      } else {
        mAge12PlusChkbox.setChecked(false);
      }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == UserPreferencesActivity.REQUEST_CODE_EDIT_PROFILE) {
        if (resultCode == getActivity().RESULT_OK &&
                data.getExtras() != null) {
          mCurrentUser = data.getExtras()
                  .getParcelable(UserPreferencesActivity.RETURN_USER_PROFILE);

          //Update Chkbox values
          setCheckboxValues();
          //Update photo
          setUserPhoto();
        }
      }

    }
}
