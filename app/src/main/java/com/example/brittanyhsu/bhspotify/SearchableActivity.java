package com.example.brittanyhsu.bhspotify;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brittanyhsu.bhspotify.Models.Data;
import com.example.brittanyhsu.bhspotify.Models.ItemSearch;
import com.example.brittanyhsu.bhspotify.Models.Playlist;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 * Created by brittanyhsu on 6/27/17.
 */

public class SearchableActivity extends AppCompatActivity {

    public final String BASE_URL = Constants.BASE_URL;
    private ImageView albumArt;
    public String accessToken = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if(intent.getStringExtra("access token") != null)
            accessToken = intent.getStringExtra("access token");

        Log.d("SearchableActivity", "AccessToken onCreate: " + accessToken);
    }

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
        setContentView(R.layout.activity_result);
        albumArt = (ImageView) findViewById(R.id.albumArt);
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

        final SpotifyAPI client = retrofit.create(SpotifyAPI.class);

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
                    ItemSearch item = response.body().getTracksSearch().getItems().get(0);

                    String artistString = "";
                    for(int i = 0; i < item.getArtists().size(); i++) {
                        artistString += item.getArtists().get(i).getName();
                        if(i != item.getArtists().size()-1)
                            artistString += ", ";
                    }

                    Log.d("SearchableActivity", "Displaying search results...  \n" +
                    "Title: " + item.getName() + '\n' + "Artist(s): " + artistString +
                            '\n' + "Album: " + item.getAlbum().getName() + '\n' + "URI: " + item.getUri());

                    String albumUrl = item.getAlbum().getImages().get(0).getUrl();

                    // Displaying album art with Picasso
                    // If error occurs, will show android launcher icon
                    Picasso.with(getApplicationContext())
                            .load(albumUrl)
                            .error(R.mipmap.ic_launcher)
                            .resize(750,750)
                            .into(albumArt);

                    TextView title = (TextView) findViewById(R.id.trackTitle);
                    TextView artist = (TextView) findViewById(R.id.artistName);

                    // Displaying title and artist of track
                    title.setText(item.getName());
                    artist.setText(artistString);

                    Button button = (Button) findViewById(R.id.button_add_track);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            getPlaylists(client);
                        }
                    });

                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.d("SearchableActivity","Search failed");
            }
        });
    }



    void getPlaylists(SpotifyAPI client) {
        Call<Playlist> call = client.getMyPlaylists();
        call.enqueue(new Callback<Playlist>() {
            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                Log.d("SearchableActivity", "onResponse");

                if(!response.isSuccessful()) {
                    try {
                        Log.d("SearchableActivity", "Error " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(response);
                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        JSONObject myResponse = jsonObject.getJSONObject("body");
                        JSONArray itemResponse = (JSONArray) myResponse.get("items");

                        String[] playlists = new String[itemResponse.length()];
                        for(int i = 0; i < itemResponse.length(); i++) {
                            playlists[i] = itemResponse.getJSONObject(i).getString("name");
                        }

                        openDialog(playlists);

//                        String ownerID = response.body().getItems().get(0).getOwner().getId();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                Log.d("SearchableActivity", "Failed to get playlists :-(");
            }
        });
    }

    private void openDialog(String[] play) {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle("Add to playlist")
                .setItems(play,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int chosen) { // int chosen : position of chosen playlist
                        Log.d("SearchableActivity", "Me do a thing");

                    }
                });
        myDialog.create().show();
    }
}
