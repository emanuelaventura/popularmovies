package com.emanuelaventura.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.emanuelaventura.popularmovies.model.Review;

import java.util.ArrayList;
import java.util.List;

public class MovieReviewRecyclerAdapter extends RecyclerView.Adapter<MovieReviewRecyclerAdapter.MovieReviewItemViewHolder> {

    private static final String TAG = MovieReviewRecyclerAdapter.class.getSimpleName();

    private Context mContext;
    private List<Review> mReviews;

    /*
    * Below, we've defined an interface to handle clicks on items within this Adapter. In the
    * constructor of our MovieReviewRecyclerAdapter, we receive an instance of a class that has implemented
    * said interface. We store that instance in this variable to call the onMovieReviewClick method whenever
    * an item is clicked in the list.
    */
    private final MovieReviewRecyclerAdapter.MovieReviewRecyclerAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onMovieVideoClick messages.
     */
    public interface MovieReviewRecyclerAdapterOnClickHandler {
        void onMovieReviewClick(String url);
    }

    MovieReviewRecyclerAdapter(Context context, MovieReviewRecyclerAdapter.MovieReviewRecyclerAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        mReviews = new ArrayList<>();
    }

    @Override
    public MovieReviewRecyclerAdapter.MovieReviewItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Create a new View
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_review_item, parent, false);
        return new MovieReviewRecyclerAdapter.MovieReviewItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieReviewRecyclerAdapter.MovieReviewItemViewHolder holder, int position) {
        Review review = mReviews.get(position);
        holder.movieReviewAuthor.setText(review.getAuthor());
        holder.movieReviewContent.setText(review.getContent());
    }

    @Override
    public int getItemCount() {
        if (mReviews == null) return 0;
        return mReviews.size();
    }

    void setMovieReviewsData(List<Review> reviews) {
        Log.d(TAG, "Setting movie reviews list " + mReviews.size() + " " + reviews.size());
        mReviews.clear();
        mReviews.addAll(reviews);
        notifyDataSetChanged();
    }

    class MovieReviewItemViewHolder extends RecyclerView.ViewHolder {
        TextView movieReviewAuthor;
        TextView movieReviewContent;

        MovieReviewItemViewHolder(View itemView) {
            super(itemView);
            movieReviewAuthor = (TextView) itemView.findViewById(R.id.tv_movie_review_author);
            movieReviewContent = (TextView) itemView.findViewById(R.id.tv_movie_review_content);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Review review = mReviews.get(getAdapterPosition());
                    mClickHandler.onMovieReviewClick(review.getUrl());
                }
            });
        }
    }
}

