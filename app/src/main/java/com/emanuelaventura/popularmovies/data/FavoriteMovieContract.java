package com.emanuelaventura.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoriteMovieContract {

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.emanuelaventura.popularmovies";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "tasks" directory
    public static final String PATH_FAVORITE_MOVIES = "favoritemovies";

    /* FavoriteMovieEntry is an inner class that defines the contents of the task table */
    public static final class FavoriteMovieEntry implements BaseColumns {

        // FavoriteMovieEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE_MOVIES).build();


        // FavoriteMovie table and column names
        public static final String TABLE_NAME = "favorite_movies";

        // Since FavoriteMovieEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the others below
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_USER_RATING = "user_rating";

    }
}