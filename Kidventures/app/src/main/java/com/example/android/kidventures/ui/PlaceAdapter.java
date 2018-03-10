package com.example.android.kidventures.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.kidventures.R;
import com.example.android.kidventures.data.Place;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apoorva on 12/19/17.
 */

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceAdapterViewHolder> {

    List<Place> mListOfPlaces;
    PlaceAdapterOnClickHandler mOnClickHandler;
    Context mContext;

    public PlaceAdapter(Context context, PlaceAdapterOnClickHandler handler) {
        this.mContext = context;
        this.mOnClickHandler = handler;
    }

    public interface PlaceAdapterOnClickHandler {
        void onClick(Place place, View sharedView);
    }

    @Override
    public PlaceAdapter.PlaceAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int idForListItem = R.layout.item_search_result;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(idForListItem, parent, shouldAttachToParentImmediately);

        return new PlaceAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaceAdapter.PlaceAdapterViewHolder holder, int position) {
        //Get the current place
        Place currentPlace = mListOfPlaces.get(position);

        //Get the place attributes to be displayed.
        String placeName = currentPlace.getPlaceName();
        String placeCategory = currentPlace.getPlaceTypes().get(0);
        Float placeRating = currentPlace.getPlaceRating();
        Float distance = currentPlace.getPlaceDistance();
        Bitmap placePhoto = currentPlace.getPlacePhoto();

        //Setting the attributes on the view
        holder.placeNameTextView.setText(placeName);
        holder.placeCategory.setText(placeCategory);
        if(distance != null) {
            String distanceStr = mContext.getString(R.string.distance_with_miles,
                    String.format(Locale.ENGLISH, "%.2f", currentPlace.getPlaceDistance()));
            holder.distance.setText(distanceStr);
        }
        if(placeRating != null) {
            holder.placeRatingBar.setRating(placeRating);
        } else {
            holder.placeRatingBar.setRating(0.0F);
        }
        if(placePhoto != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            placePhoto.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Glide.with(mContext)
                    .asBitmap()
                    .load(stream.toByteArray())
                    .into(holder.placeImageView);
        } else {
            Glide.with(mContext)
                    .clear(holder.placeImageView);
            holder.placeImageView.setImageDrawable(
                    mContext.getResources().getDrawable(R.drawable.no_image_available));
        }
    }

    @Override
    public int getItemCount() {
        if(mListOfPlaces == null) {
            return 0;
        }
        return mListOfPlaces.size();
    }

    public void setPlaceData(List<Place> listOfPlaces) {
        mListOfPlaces = listOfPlaces;
        notifyDataSetChanged();
    }

    public class PlaceAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.place_imageview)
        ImageView placeImageView;

        @BindView(R.id.place_name)
        TextView placeNameTextView;

        @BindView(R.id.place_rating)
        RatingBar placeRatingBar;

        @BindView(R.id.place_category)
        TextView placeCategory;

        @BindView(R.id.place_distance)
        TextView distance;

        public PlaceAdapterViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            ButterKnife.bind(this, view);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Place currentPlace = mListOfPlaces.get(position);
            mOnClickHandler.onClick(currentPlace, placeImageView);
        }
    }
}
