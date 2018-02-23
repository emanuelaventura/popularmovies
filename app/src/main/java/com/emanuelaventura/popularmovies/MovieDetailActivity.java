package com.emanuelaventura.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.emanuelaventura.popularmovies.data.FavoriteMovieContract;
import com.emanuelaventura.popularmovies.model.Movie;
import com.emanuelaventura.popularmovies.model.Review;
import com.emanuelaventura.popularmovies.model.Video;
import com.emanuelaventura.popularmovies.net.GetMovieReviewsResponse;
import com.emanuelaventura.popularmovies.net.GetMovieVideosResponse;
import com.emanuelaventura.popularmovies.net.RetrofitTheMovieDBAPI;
import com.emanuelaventura.popularmovies.net.RetrofitTheMovieDBUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.emanuelaventura.popularmovies.net.NetworkUtils.downloadUrl;
import static com.emanuelaventura.popularmovies.utils.UiUtils.showErrorSnackbar;

public class MovieDetailActivity extends AppCompatActivity implements
        MovieVideoRecyclerAdapter.MovieVideoRecyclerAdapterOnClickHandler,
        MovieReviewRecyclerAdapter.MovieReviewRecyclerAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    // Constants for logging and referring to a unique loader
    private static final String TAG = MovieDetailActivity.class.getSimpleName();
    private static final int FAVORITE_MOVIE_LOADER_ID = 0;

    private RetrofitTheMovieDBAPI mRetrofitTheMovieDBAPI;


    private ImageView mMoviePosterThumbnail;
    private TextView mReleaseDate;
    private TextView mUserRating;
    private TextView mPlotSynopsis;
    private FloatingActionButton makeFavoriteActionButton;

    private RecyclerView mMovieVideosRecyclerView;
    private MovieVideoRecyclerAdapter mMovieVideoRecyclerAdapter;
    private RecyclerView.LayoutManager mVideosLayoutManager;

    RecyclerView mMovieReviewsRecyclerView;
    private MovieReviewRecyclerAdapter mMovieReviewRecyclerAdapter;
    private RecyclerView.LayoutManager mReviewsLayoutManager;

    private TextView mMovieVideosErrorMessageDisplay;
    private TextView mMovieReviewsErrorMessageDisplay;
    private ProgressBar mMovieVideosLoadingIndicator;
    private ProgressBar mMovieReviewsLoadingIndicator;

    private View mRoot;

    CollapsingToolbarLayout mCollapsingToolbarLayout;

    private static final String LIST_VIDEOS_STATE_KEY = "listVideosState";
    private static final String LIST_VIDEOS_KEY = "listVideos";
    private Parcelable mListVideosState = null;
    private ArrayList<Video> mListVideos;

    private static final String LIST_REVIEWS_STATE_KEY = "listReviewsState";
    private static final String LIST_REVIEWS_KEY = "listReviews";
    private Parcelable mListReviewsState = null;
    private ArrayList<Review> mListReviews;

    private static final String MOVIE_KEY = "movie";
    private Movie mMovie;

    public static final String EXTRA_MOVIE_ID = "movie_id";
    private long mMovieId;

    public static final String IS_FAVORITE_KEY = "isFavorite";
    private boolean mIsFavorite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mRoot = findViewById(R.id.root_detail_activity_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        //The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        mRetrofitTheMovieDBAPI = RetrofitTheMovieDBUtils.getRetrofitTheMovieDBAPI();

        if (savedInstanceState == null) {
            Intent intentThatStartedThisActivity = getIntent();
            if (intentThatStartedThisActivity.hasExtra(EXTRA_MOVIE_ID)) {
                mMovieId = intentThatStartedThisActivity.getLongExtra(EXTRA_MOVIE_ID, -1);
            }

            /*
            Ensure a loader is initialized and active. If the loader doesn't already exist, one is
            created, otherwise the last created loader is re-used.
            */
            if (mMovieId != -1) {
                Log.d(TAG, "onCreate: initLoader");
                getSupportLoaderManager().initLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
            } else {
                Log.e(TAG, "mMovieId not valid!");
                showErrorSnackbar(mRoot, R.string.movie_id_not_valid, new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        finish();
                    }
                });
            }
        }

        mMoviePosterThumbnail = (ImageView) findViewById(R.id.iv_movie_poster_thumbnail_detail);
        mReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        mUserRating = (TextView) findViewById(R.id.tv_user_rating);
        mPlotSynopsis = (TextView) findViewById(R.id.tv_overview);

        /* These TextViews are used to display errors and will be hidden if there are no errors */
        mMovieVideosErrorMessageDisplay = (TextView) findViewById(R.id.tv_videos_error_message_display);
        mMovieReviewsErrorMessageDisplay = (TextView) findViewById(R.id.tv_reviews_error_message_display);

         /*
         * The ProgressBars that will indicate to the user that I'm loading data. It will be
         * hidden when no data is loading.
         */
        mMovieVideosLoadingIndicator = (ProgressBar) findViewById(R.id.pb_videos_loading_indicator);
        mMovieReviewsLoadingIndicator = (ProgressBar) findViewById(R.id.pb_reviews_loading_indicator);

        makeFavoriteActionButton = (FloatingActionButton) findViewById(R.id.make_favourite_floating_action_button);
        if (makeFavoriteActionButton != null) {
            makeFavoriteActionButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    makeFavoriteActionButton.setEnabled(false);
                    if (!mIsFavorite) {
                        new DownloadPosterTask().execute();
                    } else {
                        //Delete this film from the favorite movies db
                        // Build appropriate uri with String row id appended
                        String stringId = Long.toString(mMovieId);
                        Uri uri = FavoriteMovieContract.FavoriteMovieEntry.CONTENT_URI;
                        uri = uri.buildUpon().appendPath(stringId).build();

                        int id_deleted = getContentResolver().delete(uri, null, null);

                        if (id_deleted != -1) {
                            Log.d(TAG, "deleted movie from db");
                            mIsFavorite = false;
                            updateFavoriteButtonUI();
                            makeFavoriteActionButton.setEnabled(true);
                        }
                    }
                }
            });
        }

        mMovieVideosRecyclerView = (RecyclerView) findViewById(R.id.rv_videos);
        mMovieVideosRecyclerView.setHasFixedSize(false);

        /* Set the layoutManager on mMovieVideosRecyclerView */
        mVideosLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mMovieVideosRecyclerView.setLayoutManager(mVideosLayoutManager);

        mMovieVideoRecyclerAdapter = new MovieVideoRecyclerAdapter(this, this);
        mMovieVideosRecyclerView.setAdapter(mMovieVideoRecyclerAdapter);

        mMovieReviewsRecyclerView = (RecyclerView) findViewById(R.id.rv_reviews);
        mMovieReviewsRecyclerView.setHasFixedSize(false);

        /* Set the layoutManager on mMovieReviewsRecyclerView */
        mReviewsLayoutManager = new LinearLayoutManager(getApplicationContext());
        mMovieReviewsRecyclerView.setLayoutManager(mReviewsLayoutManager);

        mMovieReviewRecyclerAdapter = new MovieReviewRecyclerAdapter(this, this);
        mMovieReviewsRecyclerView.setAdapter(mMovieReviewRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");

        outState.putLong(EXTRA_MOVIE_ID, mMovieId);
        outState.putParcelable(MOVIE_KEY, mMovie);
        outState.putBoolean(IS_FAVORITE_KEY, mIsFavorite);

        // Save videoslist state
        mListVideosState = mVideosLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_VIDEOS_STATE_KEY, mListVideosState);
        outState.putParcelableArrayList(LIST_VIDEOS_KEY, mListVideos);


        // Save reviewslist state
        mListReviewsState = mReviewsLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_REVIEWS_STATE_KEY, mListReviewsState);
        outState.putParcelableArrayList(LIST_REVIEWS_KEY, mListReviews);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {

            mMovieId = savedInstanceState.getLong(EXTRA_MOVIE_ID);
            mMovie = savedInstanceState.getParcelable(MOVIE_KEY);
            Log.d(TAG, "onRestoreInstanceState: updateMovieDetailUI");
            updateMovieDetailUI();

            mIsFavorite = savedInstanceState.getBoolean(IS_FAVORITE_KEY);
            updateFavoriteButtonUI();

            mListVideosState = savedInstanceState.getParcelable(LIST_VIDEOS_STATE_KEY);
            if (mListVideosState != null) {
                mVideosLayoutManager.onRestoreInstanceState(mListVideosState);
            }

            mListVideos = savedInstanceState.getParcelableArrayList(LIST_VIDEOS_KEY);
            if (mListVideos != null && !mListVideos.isEmpty()) {
                Log.d(TAG, "onRestoreIstanceState: setMovieVideosData");
                mMovieVideoRecyclerAdapter.setMovieVideosData(mListVideos);
                showMovieVideosDataView();
            } else {
                Log.d(TAG, "onRestoreInstanceState: loadMovieVideosData because the list is null and empty");
                loadMovieVideosData();
            }

            mListReviewsState = savedInstanceState.getParcelable(LIST_REVIEWS_STATE_KEY);
            if (mListReviewsState != null) {
                mReviewsLayoutManager.onRestoreInstanceState(mListReviewsState);
            }
            mListReviews = savedInstanceState.getParcelableArrayList(LIST_REVIEWS_KEY);
            if (mListReviews != null && !mListReviews.isEmpty()) {
                Log.d(TAG, "onRestoreIstanceState: setMovieReviewsData");
                mMovieReviewRecyclerAdapter.setMovieReviewsData(mListReviews);
                showMovieReviewDataView();
            } else {
                Log.d(TAG, "onRestoreInstanceState: loadMovieReviewsData because the list is null and empty");
                loadMovieReviewsData();
            }

        } else {
            if (mMovieId != -1) {
                // re-queries for all tasks
                Log.d(TAG, "onRestoreInstanceState: restartLoader");
                getSupportLoaderManager().restartLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
            } else {
                Log.e(TAG, "onRestoreInstanceState: mMovieId not valid");
                showErrorSnackbar(mRoot, R.string.movie_id_not_valid, new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        finish();
                    }
                });
            }
        }
    }

    @Override
    public void onMovieVideoClick(String videoKey) {
        // Build the intent
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + videoKey));

        // Verify it resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (isIntentSafe) {
            startActivity(intent);
        }
    }

    @Override
    public void onMovieReviewClick(String url) {
        // Build the intent
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        // Verify it resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (isIntentSafe) {
            startActivity(intent);
        }
    }

    private class DownloadPosterTask extends AsyncTask<Void, Void, byte[]> {

        @Override
        protected byte[] doInBackground(Void... params) {
            byte[] posterBytes = null;
            try {
                posterBytes = downloadUrl(new URL("http://image.tmdb.org/t/p/" + "w185" + mMovie.getPosterPath()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return posterBytes;
        }

        protected void onPostExecute(byte[] posterBytes) {

            //Insert this film in the favorite movies db via a ContentResolver
            // Create new empty ContentValues object
            ContentValues contentValues = new ContentValues();
            contentValues.put(FavoriteMovieContract.FavoriteMovieEntry._ID, mMovieId);
            contentValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_TITLE, mMovie.getTitle());
            contentValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER_PATH, mMovie.getPosterPath());
            contentValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER, posterBytes);
            contentValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
            contentValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_USER_RATING, mMovie.getVoteAverage());
            contentValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_SYNOPSIS, mMovie.getOverview());
            // Insert the content values via a ContentResolver
            Uri uri = getContentResolver().insert(FavoriteMovieContract.FavoriteMovieEntry.CONTENT_URI, contentValues);
            Log.d(TAG, "Inserting favorite movie " + mMovie);

            if (uri != null) {
                mIsFavorite = true;
                updateFavoriteButtonUI();
                makeFavoriteActionButton.setEnabled(true);
            }
        }
    }

    private void onCallMovieDetailError() {
        Log.e(TAG, "onCallMovieDetailError: error from the network!");
        showErrorSnackbar(mRoot, R.string.no_connection_error, new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                finish();
            }
        });
    }

    private void onCallMovieDetailSuccess(Movie movie) {
        Log.d(TAG, "onCallMovieDetailSuccess");
        mMovie = movie;
        updateMovieDetailUI();
    }

    /**
     * Instantiates and returns a new AsyncTaskLoader with the given ID.
     * This loader will return favoritemovies data as a Cursor or null if an error occurs.
     * <p>
     * Implements the required callbacks to take care of loading data at all stages of loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        String stringId = Long.toString(mMovieId);
        Uri uri = FavoriteMovieContract.FavoriteMovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        return new CursorLoader(this,
                uri,
                null,
                null,
                null,
                null);
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() != 0) {
            Log.d(TAG, "onLoadFinished: loaded movie from db  ");
            mIsFavorite = true;
            updateFavoriteButtonUI();
            data.moveToFirst();
            mMovie = new Movie();
            mMovie.setId(mMovieId);
            mMovie.setTitle(data.getString(data.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_TITLE)));
            mMovie.setPosterPath(data.getString(data.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER_PATH)));
            mMovie.setPosterBytes(data.getBlob(data.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER)));
            mMovie.setReleaseDate(data.getString(data.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE)));
            mMovie.setVoteAverage(data.getDouble(data.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_USER_RATING)));
            mMovie.setOverview(data.getString(data.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_SYNOPSIS)));

            Log.d(TAG, "onLoadFinished: updateMovieDetailUI ");
            updateMovieDetailUI();
        } else {
            Log.d(TAG, "onLoadFinished: call getMovieDetail ");
            mIsFavorite = false;
            updateFavoriteButtonUI();

            Call<Movie> callMovieDetail = mRetrofitTheMovieDBAPI.getMovieDetail(mMovieId);
            //asynchronous call
            callMovieDetail.enqueue(new Callback<Movie>() {

                @Override
                public void onResponse(Call<Movie> call, retrofit2.Response<Movie> response) {
                    onCallMovieDetailSuccess(response.body());
                }

                @Override
                public void onFailure(Call<Movie> call, Throwable t) {
                    onCallMovieDetailError();
                }
            });
        }
        Log.d(TAG, "onLoadFinished: call loadMovieVideosData ");
        loadMovieVideosData();
        Log.d(TAG, "onLoadFinished: call loadMovieReviewsData ");
        loadMovieReviewsData();
    }

    private void loadMovieVideosData() {
        Call<GetMovieVideosResponse> callVideos = mRetrofitTheMovieDBAPI.getVideos(mMovieId);
        //asynchronous call
        callVideos.enqueue(new Callback<GetMovieVideosResponse>() {

            @Override
            public void onResponse(Call<GetMovieVideosResponse> call, Response<GetMovieVideosResponse> response) {

                mListVideos = response.body().getVideos();
                if (mListVideos == null || mListVideos.isEmpty()) {
                    mListVideos = new ArrayList<>();
                    Log.e(TAG, getString(R.string.no_movie_videos));
                    showMovieVideosErrorMessage(R.string.no_movie_videos);
                } else {
                    Log.d(TAG, "loadMovieVideosData: listVideos got  ");
                    mMovieVideoRecyclerAdapter.setMovieVideosData(mListVideos);
                    showMovieVideosDataView();
                }
            }

            @Override
            public void onFailure(Call<GetMovieVideosResponse> call, Throwable t) {
                Log.e(TAG, getString(R.string.no_movie_videos_error));
                showMovieVideosErrorMessage(R.string.no_movie_videos_error);
            }
        });
    }

    private void loadMovieReviewsData() {
        Call<GetMovieReviewsResponse> callReviews = mRetrofitTheMovieDBAPI.getReviews(mMovieId);
        //asynchronous call
        callReviews.enqueue(new Callback<GetMovieReviewsResponse>() {

            @Override
            public void onResponse(Call<GetMovieReviewsResponse> call, Response<GetMovieReviewsResponse> response) {

                mListReviews = response.body().getReviews();
                if (mListReviews == null || mListReviews.isEmpty()) {
                    mListReviews = new ArrayList<>();
                    Log.e(TAG, getString(R.string.no_movie_reviews));
                    showMovieReviewsErrorMessage(R.string.no_movie_reviews);
                } else {
                    Log.d(TAG, "loadMovieReviewsData: listReviews got ");
                    mMovieReviewRecyclerAdapter.setMovieReviewsData(mListReviews);
                    showMovieReviewDataView();
                }
            }

            @Override
            public void onFailure(Call<GetMovieReviewsResponse> call, Throwable t) {
                Log.e(TAG, getString(R.string.no_movie_reviews_error));
                showMovieReviewsErrorMessage(R.string.no_movie_reviews_error);
            }
        });
    }


    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    public void updateFavoriteButtonUI() {
        if (mIsFavorite) {
            makeFavoriteActionButton.setImageResource(R.drawable.ic_favorite_colored_24dp);
        } else {
            makeFavoriteActionButton.setImageResource(R.drawable.ic_favorite_white_24dp);
        }
    }

    public void updateMovieDetailUI() {
        mCollapsingToolbarLayout.setTitle(mMovie.getTitle());

        if (mMovie.getPosterBytes() != null) {
            //take the poster from the db
            Glide.with(this)
                    .load(mMovie.getPosterBytes())
                    .into(mMoviePosterThumbnail);
            //.error(R.drawable.imagenotfound)
        } else {
            //take the poster from the network
            String posterPath = mMovie.getPosterPath();
            String urlPosterThumbnail = "http://image.tmdb.org/t/p/" + "w185" + posterPath;

            Glide.with(this)
                    .load(urlPosterThumbnail)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mMoviePosterThumbnail);
            //.error(R.drawable.imagenotfound)
        }

        mReleaseDate.setText(String.format("%s %s", getString(R.string.release_date_label), mMovie.getReleaseDate()));
        mUserRating.setText(String.format("%s %s", getString(R.string.movie_rating_label), String.valueOf(mMovie.getVoteAverage())));
        mPlotSynopsis.setText(mMovie.getOverview());
    }

    /**
     * This method will make the View for the movies data visible and
     * hide the error message.
     */
    private void showMovieVideosDataView() {
        mMovieVideosLoadingIndicator.setVisibility(View.INVISIBLE);
        mMovieVideosErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mMovieVideosRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showMovieReviewDataView() {
        mMovieReviewsLoadingIndicator.setVisibility(View.INVISIBLE);
        mMovieReviewsErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mMovieReviewsRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     */
    private void showMovieVideosErrorMessage(int message) {
        mMovieVideosLoadingIndicator.setVisibility(View.INVISIBLE);
        mMovieVideosErrorMessageDisplay.setText(message);
        mMovieVideosRecyclerView.setVisibility(View.INVISIBLE);
        mMovieVideosErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void showMovieReviewsErrorMessage(int message) {
        mMovieReviewsLoadingIndicator.setVisibility(View.INVISIBLE);
        mMovieReviewsErrorMessageDisplay.setText(message);
        mMovieReviewsRecyclerView.setVisibility(View.INVISIBLE);
        mMovieReviewsErrorMessageDisplay.setVisibility(View.VISIBLE);
    }
}
