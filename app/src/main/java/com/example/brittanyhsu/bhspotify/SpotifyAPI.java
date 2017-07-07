package com.example.brittanyhsu.bhspotify;

import com.spotify.sdk.android.player.Metadata;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by brittanyhsu on 6/27/17.
 */

public interface SpotifyAPI {

//    @GET("/v1/search?type=track")
//    Call<Metadata.Track> searchTrack(@Query("q") String q);

    @GET("/v1/me/playlists")
    Call<Playlist> getMyPlaylists();
}
