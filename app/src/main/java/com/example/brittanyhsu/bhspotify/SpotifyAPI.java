package com.example.brittanyhsu.bhspotify;

import com.example.brittanyhsu.bhspotify.Models.Data;
import com.example.brittanyhsu.bhspotify.Models.Playlist;
import com.example.brittanyhsu.bhspotify.Models.SnapshotId;
import com.example.brittanyhsu.bhspotify.Models.UserProfile;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by brittanyhsu on 6/27/17.
 */

public interface SpotifyAPI {

    @GET("/v1/search?type=track&limit=1")
    Call<Data> searchTrack(@Query("q") String q);

    @GET("/v1/me/playlists")
    Call<Playlist> getMyPlaylists();

    @GET("/v1/me")
    Call<UserProfile> getUser();

    @POST("/v1/users/{user_id}/playlists/{playlist_id}/tracks")
    Call<SnapshotId> addToPlaylist(@Path("user_id") String user_id, @Path("playlist_id") String playlist_id, @Query("uris") String uri);

    @PUT("/v1/me/tracks")
    Call<Response> saveTrack(@Query("ids") String trackID);
}
