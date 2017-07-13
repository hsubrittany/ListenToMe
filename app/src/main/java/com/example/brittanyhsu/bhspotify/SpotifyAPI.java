package com.example.brittanyhsu.bhspotify;

import com.example.brittanyhsu.bhspotify.Models.Data;
import com.example.brittanyhsu.bhspotify.Models.Playlist;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by brittanyhsu on 6/27/17.
 */

public interface SpotifyAPI {

    @GET("/v1/search?type=track&limit=1")
    Call<Data> searchTrack(@Query("q") String q);

    @GET("/v1/me/playlists")
    Call<Playlist> getMyPlaylists();
}
