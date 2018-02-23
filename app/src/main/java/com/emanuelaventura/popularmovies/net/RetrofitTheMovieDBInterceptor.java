package com.emanuelaventura.popularmovies.net;

import android.util.Log;

import com.emanuelaventura.popularmovies.BuildConfig;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetrofitTheMovieDBInterceptor implements Interceptor {

    private static final String TAG = RetrofitTheMovieDBInterceptor.class.getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        HttpUrl originalHttpUrl = original.url();

        HttpUrl url = originalHttpUrl.newBuilder()
                .addQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_TOKEN)
                .build();

        Log.d(TAG, "Url: " + url);

        // Request customization: add request headers
        Request.Builder requestBuilder = original.newBuilder()
                .url(url);

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}