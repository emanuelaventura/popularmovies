package com.emanuelaventura.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.emanuelaventura.popularmovies.model.Movie;

import java.util.ArrayList;
import java.util.List;


class MovieRecyclerAdapter extends RecyclerView.Adapter<MovieRecyclerAdapter.MovieItemViewHolder> {

    private static final String TAG = MovieRecyclerAdapter.class.getSimpleName();

    private Context mContext;
    private List<Movie> mMovies;

    private final MovieRecyclerAdapterOnClickHandler mClickHandler;

    public interface MovieRecyclerAdapterOnClickHandler {
        void onClick(long movieId);
    }

    MovieRecyclerAdapter(Context context, MovieRecyclerAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        mMovies = new ArrayList<>();
    }

    @Override
    public MovieItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Create a new View
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false);
        return new MovieItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieItemViewHolder holder, int position) {

        Movie movie = mMovies.get(position);

        //if the poster is already stored in the db then I'll show it
        if (movie.getPosterBytes() != null) {
            Glide.with(mContext)
                    .load(movie.getPosterBytes())
                    //.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.moviePosterThumbnail);
        } else {
            //otherwise I'll try to get it from te network
            String posterPath = movie.getPosterPath();
            String urlPosterThumbnail = "http://image.tmdb.org/t/p/" + "w185" + posterPath;

            Glide.with(mContext)
                    .load(urlPosterThumbnail)
                    //.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.moviePosterThumbnail);
            //.error(R.drawable.imagenotfound);
        }
    }

    @Override
    public int getItemCount() {
        if (mMovies == null) return 0;
        return mMovies.size();
    }

    void setMoviesData(List<Movie> movies) {
        Log.d(TAG, "Setting movies list " + mMovies.size() + " " + movies.size());
        mMovies.clear();
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }

    class MovieItemViewHolder extends RecyclerView.ViewHolder {
        ImageView moviePosterThumbnail;

        MovieItemViewHolder(View itemView) {
            super(itemView);
            moviePosterThumbnail = (ImageView) itemView.findViewById(R.id.iv_movie_poster_thumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Movie movie = mMovies.get(getAdapterPosition());
                    mClickHandler.onClick(movie.getId());
                }
            });
        }
    }
}
