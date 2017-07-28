package com.example.brittanyhsu.bhspotify;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.brittanyhsu.bhspotify.Models.Playlist;
import com.google.gson.GsonBuilder;

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

/**
 * Created by brittanyhsu on 7/7/17.
 */

public class GetMyPlaylists extends Activity {

    public final String BASE_URL = Constants.BASE_URL;
<<<<<<< HEAD
    public ArrayList<String> pl = new ArrayList<>();
    public String[] playlists;
=======
>>>>>>> 3bd07be0f7c8b51c2579b84aaf66689b093149df

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPlaylists();
    }

<<<<<<< HEAD
    String[] getPlaylists() {
=======
    void getPlaylists() {
//        Intent intent = getIntent();
>>>>>>> 3bd07be0f7c8b51c2579b84aaf66689b093149df
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
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(httpClient.build()).build();


        SpotifyAPI client = retrofit.create(SpotifyAPI.class);
        Call<Playlist> call = client.getMyPlaylists();
        call.enqueue(new Callback<Playlist>() {
            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                Log.d("GetMyPlaylists", "onResponse");

                if(!response.isSuccessful()) {
                    try {
                        Log.d("GetMyPlaylists", "Error " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(response);
                    // Displaying all details of playlists in JSON format
                    Log.d("GetMyPlaylists", "Displaying my playlists...  "+ jsonString);
                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        JSONObject myResponse = jsonObject.getJSONObject("body");
                        JSONArray itemResponse = (JSONArray) myResponse.get("items");

<<<<<<< HEAD
                        playlists = new String[itemResponse.length()];
                        for(int i = 0; i < itemResponse.length(); i++) {
                            pl.add(itemResponse.getJSONObject(i).getString("name"));
                            playlists[i] = itemResponse.getJSONObject(i).getString("name");
                        }
                        Log.d("GetMyPlaylists", "Displaying names of my playlists... " + pl);
=======
                        ArrayList<String> list = new ArrayList<String>();
                        for(int i = 0; i < itemResponse.length(); i++) {
                            list.add(itemResponse.getJSONObject(i).getString("name"));
                        }
                        Log.d("GetMyPlaylists", "Displaying names of my playlists... " + list);
>>>>>>> 3bd07be0f7c8b51c2579b84aaf66689b093149df

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
<<<<<<< HEAD
=======


>>>>>>> 3bd07be0f7c8b51c2579b84aaf66689b093149df
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                Log.d("GetMyPlaylists", "Failed to get playlists :-(");
            }
        });
<<<<<<< HEAD
        return playlists;
=======
>>>>>>> 3bd07be0f7c8b51c2579b84aaf66689b093149df
    }

}
