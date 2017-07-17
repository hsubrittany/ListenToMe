package com.example.brittanyhsu.bhspotify;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;

import com.example.brittanyhsu.bhspotify.Models.SnapshotId;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by brittanyhsu on 7/12/17.
 */

public class AddToPlaylist {

    String add(SpotifyAPI client, String user_id, String playlist_id, String uri) {
        Call<SnapshotId> addTrack = client.addToPlaylist(user_id, playlist_id, uri);

        addTrack.enqueue(new Callback<SnapshotId>() {
            @Override
            public void onResponse(Call<SnapshotId> call, Response<SnapshotId> response) {
                if(!response.isSuccessful()) {
                    try {
                        Log.d("AddToPlaylist", "Error: " + response.errorBody().string());
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                else {
                    String snapshotId = response.body().getSnapshotId();
                    Log.d("AddToPlaylist", "Displaying snapshotID...  "+ snapshotId);
                }
            }

            @Override
            public void onFailure(Call<SnapshotId> call, Throwable t) {
                Log.d("AddToPlaylist", "Add to playlist failed");
                return;
            }
        });
        return "success";

    }

}
