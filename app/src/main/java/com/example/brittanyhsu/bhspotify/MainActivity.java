package com.example.brittanyhsu.bhspotify;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// TODO: RN the response is displayed in log. Display on actual phone now
// TODO: Move getPlaylist to another class? Class will be called when an "add to playlist" button is made
// TODO: Implement search

public class MainActivity extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{
    private final String CLIENT_ID = Constants.CLIENT_ID;
    private final String REDIRECT_URI = Constants.REDIRECT_URI;
    private String accessToken;
    public static final String BASE_URL = "https://api.spotify.com";
    private static final int REQUEST_CODE = 12;
    private Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "playlist-read-private"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                accessToken = response.getAccessToken();
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                        Log.d("MainActivity", "onInitialized");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
            else
                Log.d("MainActivity", "AuthenticationResponse.Type.TOKEN messed up :-(");
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        mPlayer.playUri(null, "spotify:track:4TJNW3JPNoxtsqmZjLKGk0", 0, 0);

        Intent intent = new Intent(MainActivity.this, GetMyPlaylists.class);
//        getPlaylists();
        intent.putExtra("access token", accessToken);

        startActivity(intent);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

//    void getPlaylists() {
//
//        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
//            @Override
//            public okhttp3.Response intercept(Chain chain) throws IOException {
//                Request newRequest = chain.request().newBuilder()
//                        .addHeader("Authorization","Bearer " + accessToken)
//                        .build();
//                return chain.proceed(newRequest);
//            }
//        });
//
//        Retrofit.Builder builder = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create());
//
//        Retrofit retrofit = builder.client(httpClient.build()).build();
//
//
//        SpotifyAPI client = retrofit.create(SpotifyAPI.class);
//
//        Call<Playlist> call = client.getMyPlaylists();
//        call.enqueue(new Callback<Playlist>() {
//            @Override
//            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
//                Log.d("MainActivity", "onResponse");
//
//                if(!response.isSuccessful()) {
//                    try {
//                        Log.d("MainActivity", "Error " + response.errorBody().string());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                else {
//                    String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(response);
//                    // Displaying all details of playlists in JSON format
//                    Log.d("MainActivity", "Displaying my playlists...  "+ jsonString);
//                    try {
//                        JSONObject jsonObject = new JSONObject(jsonString);
//                        JSONObject myResponse = jsonObject.getJSONObject("body");
//                        JSONArray itemResponse = (JSONArray) myResponse.get("items");
//
//                        ArrayList<String> list = new ArrayList<String>();
//                        for(int i = 0; i < itemResponse.length(); i++) {
//                            list.add(itemResponse.getJSONObject(i).getString("name"));
//                        }
//                        Log.d("MainActivity", "Displaying names of my playlists... " + list);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Playlist> call, Throwable t) {
//                Log.d("MainActivity", "Failed to get playlists :-(");
//            }
//        });
//    }




}