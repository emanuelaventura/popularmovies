package com.emanuelaventura.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.emanuelaventura.popularmovies.model.Video;

import java.util.ArrayList;
import java.util.List;

public class MovieVideoRecyclerAdapter extends RecyclerView.Adapter<MovieVideoRecyclerAdapter.MovieVideoItemViewHolder> {

    private static final String TAG = MovieVideoRecyclerAdapter.class.getSimpleName();

    private Context mContext;
    private List<Video> mVideos;

    /*
    * Below, we've defined an interface to handle clicks on items within this Adapter. In the
    * constructor of our MovieVideoRecyclerAdapter, we receive an instance of a class that has implemented
    * said interface. We store that instance in this variable to call the onMovieVideoClick method whenever
    * an item is clicked in the list.
    */
    private final MovieVideoRecyclerAdapter.MovieVideoRecyclerAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onMovieVideoClick messages.
     */
    public interface MovieVideoRecyclerAdapterOnClickHandler {
        void onMovieVideoClick(String videoKey);
    }

    MovieVideoRecyclerAdapter(Context context, MovieVideoRecyclerAdapter.MovieVideoRecyclerAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;

        mVideos = new ArrayList<>();
    }

    @Override
    public MovieVideoRecyclerAdapter.MovieVideoItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Create a new View
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_video_item, parent, false);
        return new MovieVideoRecyclerAdapter.MovieVideoItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieVideoRecyclerAdapter.MovieVideoItemViewHolder holder, int position) {

        Video video = mVideos.get(position);

            String videoKey = video.getKey();
            String urlVideoThumbnail = "https://img.youtube.com/vi/"+videoKey +"/default.jpg" ;

            Glide.with(mContext)
                    .load(urlVideoThumbnail)
                    //.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.movieVideoThumbnail);
            //.error(R.drawable.imagenotfound);
        }

    @Override
    public int getItemCount() {
        if (mVideos == null) return 0;
        return mVideos.size();
    }

    void setMovieVideosData(List<Video> videos) {
        Log.d(TAG, "Setting movie videos list " + mVideos.size() + " " + videos.size());
        mVideos.clear();
        mVideos.addAll(videos);
        notifyDataSetChanged();
    }

    class MovieVideoItemViewHolder extends RecyclerView.ViewHolder {
        ImageView movieVideoThumbnail;

        MovieVideoItemViewHolder(View itemView) {
            super(itemView);
            movieVideoThumbnail = (ImageView) itemView.findViewById(R.id.iv_movie_video_thumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Video video = mVideos.get(getAdapterPosition());
                    mClickHandler.onMovieVideoClick(video.getKey());
                }
            });
        }
    }
}

