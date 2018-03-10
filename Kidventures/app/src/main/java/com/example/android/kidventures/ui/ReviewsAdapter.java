package com.example.android.kidventures.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.android.kidventures.R;
import com.example.android.kidventures.data.Review;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apoorva on 2/21/18.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    private final String LOG_TAG = ReviewsAdapter.class.getSimpleName();

    private Context mContext;
    private List<Review> mListOfReviews;

    public ReviewsAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public ReviewsAdapter.ReviewsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.item_place_review;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        return new ReviewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewsAdapter.ReviewsAdapterViewHolder holder, int position) {
        Review currentReview = mListOfReviews.get(position);
        holder.reviewAuthor.setText(currentReview.getReviewerUsername());
        holder.reviewRating.setRating(currentReview.getRating());
        holder.reviewContent.setText(currentReview.getReviewComments());
    }

    @Override
    public int getItemCount() {
        if(mListOfReviews == null) {
            return 0;
        }
        return mListOfReviews.size();
    }

    public class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.place_review_author)
        TextView reviewAuthor;
        @BindView(R.id.place_review_rating)
        RatingBar reviewRating;
        @BindView(R.id.place_review_content)
        TextView reviewContent;
        public ReviewsAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public void setReviewDetails(List<Review> reviews) {
        mListOfReviews = reviews;
        notifyDataSetChanged();
    }
}
