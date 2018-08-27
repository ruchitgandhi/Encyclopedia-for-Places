package com.example.ruchit.placessearch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{

    List<ReviewLine> reviews;
    Context context;

    ReviewAdapter(List<ReviewLine> reviews, Context context){
        this.reviews = reviews;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_reviews_card_view, viewGroup, false);
        ReviewViewHolder pvh = new ReviewViewHolder(v, context, reviews);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder resultViewHolder, int i) {
        Picasso.get().load(reviews.get(i).getProfilePhotoUrl()).into(resultViewHolder.reviewsProfilePhoto);
        resultViewHolder.reviewsPersonName.setText(reviews.get(i).getAuthorName());
        resultViewHolder.reviewsRating.setRating(Float.parseFloat(reviews.get(i).getRating()));
        resultViewHolder.reviewsTimestamp.setText(reviews.get(i).getTimestamp());
        resultViewHolder.reviewsText.setText(reviews.get(i).getReviewText());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView cv;
        TextView reviewsPersonName, reviewsTimestamp, reviewsText;
        RatingBar reviewsRating;
        ImageView reviewsProfilePhoto;
        Context context;
        List<ReviewLine> reviews;

        ReviewViewHolder(View itemView, Context context, List<ReviewLine> reviews) {
            super(itemView);
            this.context = context;
            this.reviews = reviews;
            cv = itemView.findViewById(R.id.reviews_card_view);
            reviewsProfilePhoto = itemView.findViewById(R.id.reviews_profile_photo);
            reviewsPersonName = itemView.findViewById(R.id.reviews_person_name);
            reviewsRating = itemView.findViewById(R.id.reviews_rating);
            reviewsTimestamp = itemView.findViewById(R.id.reviews_timestamp);
            reviewsText = itemView.findViewById(R.id.reviews_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedReviewPosition = getAdapterPosition();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(reviews.get(clickedReviewPosition).getAuthorUrl()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

}