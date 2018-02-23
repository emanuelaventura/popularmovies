package com.emanuelaventura.popularmovies.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitTheMovieDBUtils {

    private static final String MOVIEDB_BASE_URL_SCHEME = "http";
    private static final String MOVIEDB_BASE_URL_AUTHORITY = "api.themoviedb.org";
    public static final String MOVIEDB_API_VERSION = "3";

    public static RetrofitTheMovieDBAPI getRetrofitTheMovieDBAPI() {

        // Add the interceptor to OkHttpClient to add API KEY to every call
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(new RetrofitTheMovieDBInterceptor());
        OkHttpClient client = builder.build();

        // Create the GsonBuilder
        Gson gson = new GsonBuilder()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        return retrofit.create(RetrofitTheMovieDBAPI.class);
    }
}
