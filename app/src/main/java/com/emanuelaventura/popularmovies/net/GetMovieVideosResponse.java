package com.emanuelaventura.popularmovies.net;

import com.emanuelaventura.popularmovies.model.Video;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetMovieVideosResponse {

    private int id;

    @SerializedName("results")
    private ArrayList<Video> videos;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }
}
