package com.emanuelaventura.popularmovies.net;

import com.emanuelaventura.popularmovies.model.Movie;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import static com.emanuelaventura.popularmovies.net.RetrofitTheMovieDBUtils.MOVIEDB_API_VERSION;

public interface RetrofitTheMovieDBAPI {

    @GET("/" + MOVIEDB_API_VERSION + "/movie/popular")
    Call<GetMoviesResponse> getPopularMovies();

    @GET("/" + MOVIEDB_API_VERSION + "/movie/top_rated")
    Call<GetMoviesResponse> getTopRatedMovies();

    @GET("/" + MOVIEDB_API_VERSION + "/movie/{movie_id}")
    Call<Movie> getMovieDetail(@Path("movie_id") long id);

    @GET("/" + MOVIEDB_API_VERSION + "/movie/{movie_id}/videos")
    Call<GetMovieVideosResponse> getVideos(@Path("movie_id") long id);

    @GET("/" + MOVIEDB_API_VERSION + "/movie/{movie_id}/reviews")
    Call<GetMovieReviewsResponse> getReviews(@Path("movie_id") long id);
}

