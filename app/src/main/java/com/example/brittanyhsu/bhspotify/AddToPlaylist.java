package com.example.brittanyhsu.bhspotify;

import android.app.Activity;
import android.os.Bundle;
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

public class AddToPlaylist extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        add(Constants.OWNER_ID, Constants.DEMO_PLAYLIST_ID, Constants.DEMO_TRACK_URI);
    }

    void add(String user_id, String playlist_id, String uri) {

        final String accessToken = getIntent().getStringExtra("access token");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("Authorization","Bearer " + accessToken)
                        .build();
                return chain.proceed(newRequest);
            }
        });

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(httpClient.build()).build();
        SpotifyAPI client = retrofit.create(SpotifyAPI.class);

        Call<SnapshotId> addTrack = client.addToPlaylist(user_id, playlist_id, uri);

        addTrack.enqueue(new Callback<SnapshotId>() {
            @Override
            public void onResponse(Call<SnapshotId> call, Response<SnapshotId> response) {
                if(!response.isSuccessful()) {
                    try {
                        Log.d("AddToPlaylist", "Error: " + response.errorBody().string());
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
            }
        });

    }
}
