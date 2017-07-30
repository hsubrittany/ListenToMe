package com.example.brittanyhsu.bhspotify;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public String track = "";
    public String artist = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if(intent.getStringExtra("access token") != null)
            accessToken = intent.getStringExtra("access token");

        if(intent.getStringExtra("track") != null)
            track = intent.getStringExtra("track");

        if(intent.getStringExtra("artist") != null) {
            artist = intent.getStringExtra("artist");
        }

        Log.d("SearchableActivity", "AccessToken onCreate: " + accessToken);
        Log.d("SearchableActivity", "track + artist: " + track + " " + artist);

        if(artist != null && track != null)
            doMySearch(track + " " + artist);

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

    // not needed for fingerprint but needed for search bar.
    private void handleIntent(Intent intent) {
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    public void doMySearch(String query) {
        String withoutFeat = query;
        String[] splitQuery = query.split(" ");
        int deleteMePos = -1;
        for(int i = 0; i < splitQuery.length; i++) {
            if(splitQuery[i].equalsIgnoreCase("feat.")) {
                deleteMePos = i;
                break;
            }
        }

        if(deleteMePos != -1) {
            List<String> queryList = new ArrayList<>(Arrays.asList(splitQuery));
            queryList.remove(deleteMePos);
            withoutFeat = TextUtils.join(" ", queryList);
        }

        Log.d("SearchableActivity", withoutFeat);


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

        Call<Data> search = client.searchTrack(withoutFeat);
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
                    if(response.body().getTracksSearch().getItems().isEmpty()) {
                        openSearchErrorDialog();
                        return;
                    }

                    final ItemSearch item = response.body().getTracksSearch().getItems().get(0);

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

                    int imageWidth = getScreenWidth() - 100;

                    // Displaying album art with Picasso
                    // If error occurs, will show android launcher icon
                    Picasso.with(getApplicationContext())
                            .load(albumUrl)
                            .error(R.mipmap.ic_launcher)
                            .resize(imageWidth,imageWidth)
                            .into(albumArt);

                    TextView title = (TextView) findViewById(R.id.trackTitle);
                    TextView artist = (TextView) findViewById(R.id.artistName);

                    // Displaying title and artist of track
                    title.setText(item.getName());
                    artist.setText(artistString);

                    Button button = (Button) findViewById(R.id.button_add_track);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            getPlaylists(client, item.getUri(), item.getName());
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

    void getPlaylists(final SpotifyAPI client, final String uri, final String trackTitle) {
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
                    String owner_id = "";
                    ArrayList<String> myOwnPlaylists = new ArrayList<>();

                    for(int i = 0; i < response.body().getItems().size(); i++) {
                        owner_id = response.body().getItems().get(i).getOwner().getId();

                        if(owner_id.equals(Constants.OWNER_ID)) {
                            myOwnPlaylists.add(response.body().getItems().get(i).getName());
                        }
                    }

                    String[] playlists = new String[myOwnPlaylists.size()];
                    String[] playlist_ids = new String[myOwnPlaylists.size()];
                    for(int i = 0; i < myOwnPlaylists.size(); i++) {
                        playlists[i] = myOwnPlaylists.get(i);
                        playlist_ids[i] = response.body().getItems().get(i).getId();
                    }

                    Log.d("SearchableActivity", "my playlists... " + myOwnPlaylists);

                    openListDialog(playlists,client,owner_id,playlist_ids, uri, trackTitle);
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                Log.d("SearchableActivity", "Failed to get playlists :-(");
            }
        });
    }

    private void openListDialog(final String[] play, final SpotifyAPI client, final String owner_id,
                                final String[] playlist_ids, final String uri, final String trackName) {
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle(Html.fromHtml("<font color='#000000'>Add to playlist</font>"))
                .setItems(play,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int chosen) {
                        // int chosen : position of chosen playlist
                        String playlistChosen = "";
                        String playlistID = "";
                        for(int i = 0; i < play.length; i++) {
                            if(i == chosen) {
                                playlistChosen = play[i];
                                playlistID = playlist_ids[i];
                                Log.d("SearchableActivity", "playlistChosen: " + playlistChosen);
                                Log.d("SearchableActivity", "playlistID: " + playlistID);
                                break;
                            }
                        }

                        AddToPlaylist ap = new AddToPlaylist();
                        String addTrack = ap.add(client, owner_id, playlistID, uri);

                        if(addTrack.equals("success")) {
                            Log.d("SearchableActivity", "added track successfully");
                            openSuccessDialog(playlistChosen, trackName);
                        }

                    }
                });
        myDialog.create().show();
    }

    private void openSuccessDialog(String playlistName, String trackName) {
        final AlertDialog.Builder successDialog = new AlertDialog.Builder(this);
        successDialog.setTitle(Html.fromHtml("<font color='#000000'>Success</font>"))
                .setMessage(trackName + " added to " + playlistName);

        final AlertDialog alert = successDialog.create();
        alert.show();

        // Dialog disappears after 10 seconds
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(alert.isShowing())
                    alert.dismiss();
            }
        };

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 10000);
    }

    private void openSearchErrorDialog() {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle(Html.fromHtml("<font color='#000000'>Error</font>"))
                .setMessage("No tracks found. Try again.")
                .create().show();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }
}
