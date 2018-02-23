package com.emanuelaventura.popularmovies.net;

import com.emanuelaventura.popularmovies.model.Review;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetMovieReviewsResponse {

    private int id;

    private int page;

    @SerializedName("results")
    private ArrayList<Review> reviews;

    private int total_pages;

    private int total_results;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(int total_pages) {
        this.total_pages = total_pages;
    }

    public int getTotal_results() {
        return total_results;
    }

    public void setTotal_results(int total_results) {
        this.total_results = total_results;
    }
}
