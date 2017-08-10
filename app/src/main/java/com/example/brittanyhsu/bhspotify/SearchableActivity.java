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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brittanyhsu.bhspotify.Models.Data;
import com.example.brittanyhsu.bhspotify.Models.ItemSearch;
import com.example.brittanyhsu.bhspotify.Models.Playlist;
import com.example.brittanyhsu.bhspotify.Models.UserProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public String accessToken = null;
    public String track = null;
    public String artist = null;
    public String userid = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if(intent.getStringExtra("access token") != null)
            accessToken = intent.getStringExtra("access token");

        if(intent.getStringExtra("track") != null)
            track = intent.getStringExtra("track");

        if(intent.getStringExtra("artist") != null) {
            artist = intent.getStringExtra("artist");
        }

        Log.d("SearchableActivity", "AccessToken onCreate: " + accessToken);

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
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
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

    /*
    they spelled playboi carti, kehlani wrong in featuring...........
     */

    public String parse(String query) {
        // Sometimes the query has '&', 'Featuring', and 'Feat.' which doesn't work in Spotify search.
        // Also sometimes parentheses can mess it up which is annoying
        // So gotta take them out...
        String withoutFeat = query.toLowerCase();
        withoutFeat = withoutFeat.replaceAll("[\\[\\]()]"," "); // Removing () AND []
        withoutFeat = withoutFeat.replaceAll(",",""); // Removing ,
        Log.d("SearchableActivity","DID () [] GO AWAY??? "+withoutFeat);

        String[] splitQuery = withoutFeat.split(" "); // Split query into array of words

        boolean removed = false;
        List<String> queryList = new ArrayList<>(Arrays.asList(splitQuery));

        if(queryList.contains("&")) {
            queryList.removeAll(Collections.singleton("&"));
            removed = true;
        }
        if(queryList.contains("feat.")) {
            queryList.removeAll(Collections.singleton("feat."));
            removed = true;
        }

        if(queryList.contains("featuring")) {
            queryList.removeAll(Collections.singleton("featuring"));
            removed = true;
        }

        if(queryList.contains("original")) {
            queryList.removeAll(Collections.singleton("original"));
            removed = true;
        }

        if(queryList.contains("mix")) {
            queryList.removeAll(Collections.singleton("mix"));
            removed = true;
        }

        if(queryList.contains("amended")) {
            queryList.removeAll(Collections.singleton("amended"));
            removed = true;
        }

        if(queryList.contains("album")) {
            queryList.removeAll(Collections.singleton("album"));
            removed = true;
        }

        if(queryList.contains("version")) {
            queryList.removeAll(Collections.singleton("version"));
            removed = true;
        }

        if(queryList.contains("single")) {
            queryList.removeAll(Collections.singleton("single"));
            removed = true;
        }

        if(queryList.contains("radio")) {
            queryList.removeAll(Collections.singleton("radio"));
            removed = true;
        }

        if(queryList.contains("edit")) {
            queryList.removeAll(Collections.singleton("edit"));
            removed = true;
        }

        if(queryList.contains("extended")) {
            queryList.removeAll(Collections.singleton("extended"));
            removed = true;
        }

        if(removed)
            withoutFeat = TextUtils.join(" ", queryList);

        return withoutFeat;
    }

    public void doMySearch(String query) {
        String withoutFeat = parse(query);


        Log.d("SearchableActivity","After parsing: " + withoutFeat);


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

                    String albumUrl = "";
                    if(item.getAlbum().getImages() != null)
                        albumUrl = item.getAlbum().getImages().get(0).getUrl();

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

    void getUserId(SpotifyAPI client) throws IOException {
        Call<UserProfile> call = client.getUser();
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if(!response.isSuccessful()) {
                    try {
                        Log.d("SearchableActivity", "User Id Error " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                else {
                    userid = response.body().getId();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {

            }
        });
    }

    void getPlaylists(final SpotifyAPI client, final String uri, final String trackTitle) {
        Call<Playlist> call = client.getMyPlaylists();
        try {
            getUserId(client);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

                        if(owner_id.equals(userid)) {
                            myOwnPlaylists.add(response.body().getItems().get(i).getName());
                        }
                    }

                    List<String> playlist_ids = new ArrayList<>();
                    for(int i = 0; i < myOwnPlaylists.size(); i++) {
                        playlist_ids.add(response.body().getItems().get(i).getId());
                    }

                    String[] play = (String[]) myOwnPlaylists.toArray();
                    String[] playID = (String[]) playlist_ids.toArray();

                    Log.d("SearchableActivity", "my playlists... " + myOwnPlaylists);

                    openListDialog(play,client,owner_id,playID, uri, trackTitle);
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                Log.d("SearchableActivity", "Failed to get playlists :-(");
            }
        });
    }

    private void openListDialog(final String[] play, final SpotifyAPI client, final String owner_id,
                                final String[] playID, final String uri, final String trackName) {
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        final List<String> playlistsChosen = new ArrayList<>();
        final List<String> playlistIDs = new ArrayList<>();

        myDialog.setTitle("Add to playlist")
                .setMultiChoiceItems(play,null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked) {
                            playlistsChosen.add(play[which]);
                            playlistIDs.add(playID[which]);
                        }
                        else if(playlistsChosen.contains(play[which])) {
                            playlistsChosen.remove(play[which]);
                            playlistIDs.remove(playID[which]);
                        }
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0; i < playlistIDs.size(); i++) {
                            AddToPlaylist ap = new AddToPlaylist();
                            ap.add(client,owner_id,playlistIDs.get(i),uri);
                        }

                        openSuccessDialog(playlistsChosen.toString().replaceAll("[\\[\\]]",""),trackName);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        myDialog.create().show();
    }

    private void openSuccessDialog(String playlistName, String trackName) {
        final AlertDialog.Builder successDialog = new AlertDialog.Builder(this);
        successDialog.setTitle("Success")
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
        errorDialog.setTitle("Error")
                .setMessage("No tracks found. Try again.")
                .create().show();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }
}
