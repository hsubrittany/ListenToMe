package com.example.brittanyhsu.bhspotify;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by brittanyhsu on 7/11/17.
 */

public class Data {
    @SerializedName("tracks")
    @Expose
    private TracksSearch tracksSearch;

    public TracksSearch getTracksSearch() {
        return tracksSearch;
    }

    public void setTracksSearch(TracksSearch tracksSearch) {
        this.tracksSearch = tracksSearch;
    }
}
