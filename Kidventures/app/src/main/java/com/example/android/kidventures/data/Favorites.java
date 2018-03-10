package com.example.android.kidventures.data;

import java.util.ArrayList;

/**
 * Created by apoorva on 2/20/18.
 */

public class Favorites {

  private ArrayList<String> favoritePlaceIds;

  public Favorites() {}

  public Favorites(ArrayList<String> favPlaceIds) {
    this.favoritePlaceIds = favPlaceIds;
  }

  public ArrayList<String> getFavoritePlaceIds() {
    return (this.favoritePlaceIds == null) ? new ArrayList<String>() : this.favoritePlaceIds; }
}
