package com.example.brittanyhsu.bhspotify;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
 * Created by brittanyhsu on 6/27/17.
 */

// TODO: SEARCH WORKS!! find a way to get the title or url only.

public class SearchableActivity extends AppCompatActivity {

    public final String BASE_URL = Constants.BASE_URL;

    public String accessToken = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();

        if(intent.getStringExtra("access token") != null)
            accessToken = intent.getStringExtra("access token");

        Log.d("SearchableActivity", "AccessToken onCreate: " + accessToken);
        handleIntent(getIntent());
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        Log.d("SearchableActivity", "onSaveInstanceState called");
//        outState.putString("access token", accessToken);
//        super.onSaveInstanceState(outState);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("SearchableActivity", "onCreateOptionsMenu called");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("SearchableActivity", "onNewIntent called");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    public void doMySearch(String query) {
        Log.d("SearchableActivity", "AccessToken doMySearch: " + accessToken);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("Authorization","Bearer " + accessToken)
                        .build();
                return chain.proceed(newRequest);
            }
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson));

        Retrofit retrofit = builder.client(httpClient.build()).build();

        SpotifyAPI client = retrofit.create(SpotifyAPI.class);

        Call<Data> search = client.searchTrack(query);
        search.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                if(!response.isSuccessful()) {
                    try {
                        Log.d("SearchableActivity", "Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                else {
//                    String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(response);
//                    Log.d("SearchableActivity", "Displaying search results...  "+ jsonString);
                    ItemSearch item = response.body().getTracksSearch().getItems().get(0);

                    ArrayList<String> artists = new ArrayList<>();
                    for(int i = 0; i < item.getArtists().size(); i++) {
                        artists.add(item.getArtists().get(i).getName());
                    }

                    Log.d("SearchableActivity", "Displaying search results...  \n" +
                    "Title: " + item.getName() + '\n' + "Artist(s): " + artists + '\n' + "URI: " + item.getUri());
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.d("SearchableActivity","Search failed");
            }
        });
    }


}
