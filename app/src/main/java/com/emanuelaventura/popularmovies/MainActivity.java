package com.emanuelaventura.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emanuelaventura.popularmovies.data.FavoriteMovieContract;
import com.emanuelaventura.popularmovies.model.Movie;
import com.emanuelaventura.popularmovies.net.GetMoviesResponse;
import com.emanuelaventura.popularmovies.net.RetrofitTheMovieDBAPI;
import com.emanuelaventura.popularmovies.net.RetrofitTheMovieDBUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.emanuelaventura.popularmovies.MovieDetailActivity.EXTRA_MOVIE_ID;
import static com.emanuelaventura.popularmovies.utils.UiUtils.calculateNoOfColumns;

public class MainActivity extends AppCompatActivity implements MovieRecyclerAdapter.MovieRecyclerAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String MOVIES_ORDER_PREFERENCE = "MOVIES_ORDER";
    public static final int MOVIES_ORDER_POPULAR = 0;
    public static final int MOVIES_ORDER_TOP_RATED = 1;
    public static final int MOVIES_ORDER_FAVORITE = 2;

    private static final int FAVORITE_MOVIES_LOADER_ID = 1;

    private RetrofitTheMovieDBAPI mRetrofitTheMovieDBAPI;

    private RecyclerView mRecyclerView;
    private MovieRecyclerAdapter mMovieRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private int mMoviesOrder;

    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    SharedPreferences sharedPref;

    private static final String LIST_STATE_KEY = "listState";
    private Parcelable mListState = null;

    private static final String LIST_MOVIES_KEY = "listMovies";
    private ArrayList<Movie> mListMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,"onCreate");

        mRetrofitTheMovieDBAPI = RetrofitTheMovieDBUtils.getRetrofitTheMovieDBAPI();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.main_activity_title);
        toolbarTitle.setText(getString(R.string.app_name));

        // Using SharedPreference, I'll save the kind of movies' order chosen by the user
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        mMoviesOrder = sharedPref.getInt(MOVIES_ORDER_PREFERENCE, MOVIES_ORDER_POPULAR);

        // Get the application context
        Context mApplicationContext = getApplicationContext();

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

         /*
         * The ProgressBar that will indicate to the user that I'm loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /*
         * Using findViewById, I get a reference to my RecyclerView from xml. This allows me to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);

        /*
         * I'm using this setting to improve performance because I know that I'll display always 20
         * movie items
         */
        mRecyclerView.setHasFixedSize(true);

        /* Set the layoutManager on mRecyclerView */
        mLayoutManager = new GridLayoutManager(mApplicationContext, calculateNoOfColumns(this));
        mRecyclerView.setLayoutManager(mLayoutManager);

        /*
         * The MovieRecyclerAdapter is responsible for linking our movie data with the Views that
         * will end up displaying our movie data.
         *
         * Although passing in "this" twice may seem strange, it is actually a sign of separation
         * of concerns, which is best programming practice. The MovieRecyclerAdapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * MainActivity implements the MovieRecyclerAdapter MovieRecyclerOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
        mMovieRecyclerAdapter = new MovieRecyclerAdapter(this, this);
        mRecyclerView.setAdapter(mMovieRecyclerAdapter);

        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: loadMovieData");
            loadMoviesData();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"onSaveInstanceState");
        // Save list state
        mListState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, mListState);
        outState.putParcelableArrayList(LIST_MOVIES_KEY, mListMovies);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(TAG,"onRestoreInstanceState");
        // Retrieve list state and list/item positions
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
            if (mListState != null) {
                mLayoutManager.onRestoreInstanceState(mListState);
            }

            mListMovies = savedInstanceState.getParcelableArrayList(LIST_MOVIES_KEY);
            if (mListMovies != null && !mListMovies.isEmpty()) {
                mMovieRecyclerAdapter.setMoviesData(mListMovies);
                showMoviesDataView();
            }else{
                Log.d(TAG,"onRestoreInstanceState: loadMovieData because the list is null and empty");
                loadMoviesData();
            }
        } else {
            Log.d(TAG,"onRestoreInstanceState: loadMovieData");
            loadMoviesData();
        }
    }

    /**
     * This method will get the movies
     */
    private void loadMoviesData() {

        mLoadingIndicator.setVisibility(View.VISIBLE);

        switch (mMoviesOrder) {
            case MOVIES_ORDER_POPULAR:
                Call<GetMoviesResponse> callPopularMovies = mRetrofitTheMovieDBAPI.getPopularMovies();
                //asynchronous call
                callPopularMovies.enqueue(new Callback<GetMoviesResponse>() {

                    @Override
                    public void onResponse(Call<GetMoviesResponse> call, Response<GetMoviesResponse> response) {
                        onCallMoviesSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<GetMoviesResponse> call, Throwable t) {
                        onCallMoviesError();
                    }
                });
                break;
            case MOVIES_ORDER_TOP_RATED:
                Call<GetMoviesResponse> callTopRatedMovies = mRetrofitTheMovieDBAPI.getTopRatedMovies();
                //asynchronous call
                callTopRatedMovies.enqueue(new Callback<GetMoviesResponse>() {

                    @Override
                    public void onResponse(Call<GetMoviesResponse> call, Response<GetMoviesResponse> response) {
                        onCallMoviesSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<GetMoviesResponse> call, Throwable t) {
                        onCallMoviesError();
                    }
                });
                break;
            case MOVIES_ORDER_FAVORITE:
                getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER_ID, null, this);
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new CursorLoader(this,
                FavoriteMovieContract.FavoriteMovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param cursor The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mListMovies = new ArrayList<>();
        if (cursor != null && cursor.getCount() != 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                // The Cursor is now set to the right position
                Movie movie = new Movie();
                movie.setId(cursor.getInt(cursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry._ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_TITLE)));
                movie.setPosterBytes(cursor.getBlob(cursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER)));
                Log.d(TAG, "Loading favorite movie " + movie);
                mListMovies.add(movie);
            }
            mMovieRecyclerAdapter.setMoviesData(mListMovies);
            showMoviesDataView();
        } else {
            showErrorMessage(R.string.no_favorite_movies_error);
        }
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

    /**
     * This method will make the View for the movies data visible and
     * hide the error message.
     */
    private void showMoviesDataView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     */
    private void showErrorMessage(int message) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setText(message);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public void onCallMoviesSuccess(GetMoviesResponse response) {
        if (response != null) {
            mListMovies = response.getMovies();
            if (mListMovies != null) {
                Log.d(TAG,"onCallMoviesSuccess: loaded data from the network");
                mMovieRecyclerAdapter.setMoviesData(mListMovies);
                showMoviesDataView();
            }else{
                mListMovies = new ArrayList<>();
            }
        } else {
            showErrorMessage(R.string.no_valid_data_error);
        }
    }

    public void onCallMoviesError() {
        showErrorMessage(R.string.no_connection_error);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        SharedPreferences.Editor editor = sharedPref.edit();
        switch (itemId) {
            case R.id.mi_popular_order:
                mMoviesOrder = MOVIES_ORDER_POPULAR;
                editor.putInt(MOVIES_ORDER_PREFERENCE, mMoviesOrder);
                editor.apply();
                loadMoviesData();
                return true;
            case R.id.mi_top_rated_order:
                mMoviesOrder = MOVIES_ORDER_TOP_RATED;
                editor.putInt(MOVIES_ORDER_PREFERENCE, mMoviesOrder);
                editor.apply();
                loadMoviesData();
                return true;
            case R.id.mi_favorite_order:
                mMoviesOrder = MOVIES_ORDER_FAVORITE;
                editor.putInt(MOVIES_ORDER_PREFERENCE, mMoviesOrder);
                editor.apply();
                loadMoviesData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(long movieId) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(EXTRA_MOVIE_ID, movieId);
        startActivity(intent);
    }
}
