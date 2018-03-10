package com.example.android.kidventures.ui;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.kidventures.R;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

  private static final String LOG_TAG = HomeFragment.class.getSimpleName();

  @BindView(R.id.search_query)
  EditText searchQueryText;
  @BindView(R.id.search_icon)
  ImageButton searchImageButton;

  public HomeFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment HomeFragment.
   */
  public static HomeFragment newInstance() {
    HomeFragment fragment = new HomeFragment();
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    final View view = inflater.inflate(R.layout.fragment_home, container, false);
    ButterKnife.bind(this, view);

    searchQueryText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          onSubmitSearchQuery();
          handled = true;
        }
        return handled;
      }
    });

    searchImageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onSubmitSearchQuery();
      }
    });
    return view;
  }

  private void onSubmitSearchQuery() {
    String query = searchQueryText.getText().toString();
    if (query == null) {
      Toast.makeText(getActivity(), getString(R.string.query_null), Toast.LENGTH_LONG).show();
    } else {
      Intent searchActivityIntent = new Intent(getActivity(), SearchResultsActivity.class);
      Bundle queryBundle = new Bundle();
      queryBundle.putString(MainActivity.SEARCH_QUERY, query);
      searchActivityIntent.putExtras(queryBundle);
      startActivity(searchActivityIntent);
    }
  }

}
