package com.example.brittanyhsu.bhspotify;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brittanyhsu on 9/18/17.
 */

public class HistoryAdapter extends BaseAdapter {
    private List<TrackInfo> mDataSource;
    private Context mContext;
    private LayoutInflater mInflater;
//    private Player mPlayer;

    public HistoryAdapter(Context context, List<TrackInfo> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get title element
        View rowView = mInflater.inflate(R.layout.list_row,parent,false);
        TextView titleTextView = (TextView) rowView.findViewById(R.id.item_track);
        TextView artistTextView = (TextView) rowView.findViewById(R.id.item_artist);
        ImageView albumImageView = (ImageView) rowView.findViewById(R.id.item_album);
        final ImageButton playButton = (ImageButton) rowView.findViewById(R.id.play_button);

        final TrackInfo trackInfo = (TrackInfo) getItem(position);

        titleTextView.setText(trackInfo.title);
        artistTextView.setText(trackInfo.artist);
        Picasso.with(mContext).load(trackInfo.imageUrl).error(R.mipmap.ic_launcher).into(albumImageView);

        // preview_song preview button
        final Player spotifyPlayer = LoginActivity.mPlayer;
        final Player.OperationCallback oc = new Player.OperationCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Error error) {

            }
        };
        final boolean[] isPlaying = {false};
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HistoryAdapter", "Play button clicked");
                if(isPlaying[0]) {
                    playButton.setImageResource(R.drawable.play_btn);
                    spotifyPlayer.pause(oc);
                }
                else {
                    spotifyPlayer.playUri(null, trackInfo.uri, 0, 0);
                    playButton.setImageResource(R.drawable.stop_btn);
                }

                isPlaying[0] = !isPlaying[0];
            }
        });

        return rowView;
    }
}

class TrackInfo {
    String title;
    String artist;
    String imageUrl;
    String uri;
}