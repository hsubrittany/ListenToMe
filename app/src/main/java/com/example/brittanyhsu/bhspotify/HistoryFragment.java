package com.example.brittanyhsu.bhspotify;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brittanyhsu on 9/4/17.
 */


//        mPlayer.playUri(null, "spotify:track:4TJNW3JPNoxtsqmZjLKGk0", 0, 0);


public class HistoryFragment extends Fragment {
    String TAG = "HistoryFragment";
    private LayoutInflater mInflater;
    private ViewGroup mContainer;
    private ViewGroup placeholder;

    private ListView mListView;
    HistoryDBHelper myDb;
//    List<String> historyList = new ArrayList<>();

    List<TrackInfo> historyTrackInfo = new ArrayList<>();
//    private ArrayAdapter adapter = null;
    private HistoryAdapter adapter = null;

    public HistoryFragment() {

    }

    public static HistoryFragment newInstance() {
        HistoryFragment frag = new HistoryFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");

        mInflater = inflater;
        mContainer = container;

        View v = inflater.inflate(R.layout.fragment_history,container,false);
        placeholder = (ViewGroup) v;
        return placeholder;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated called");


        myDb = new HistoryDBHelper(getActivity());

        mListView = (ListView) getView().findViewById(R.id.history_list_view);
        Button button = (Button) getView().findViewById(R.id.clearButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDb.deleteAll();
                historyTrackInfo.clear();
                adapter.notifyDataSetChanged();
            }
        });

        // put db data in arraylist
        addDataToList();
//        adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1,historyList);
        adapter = new HistoryAdapter(getContext(),historyTrackInfo);

        mListView.setAdapter(adapter);
    }

    public void addDataToList() {
        Cursor res = myDb.getAllData();
        if(res.getCount() == 0) {
            return;
        }


        while(res.moveToNext()) {
            TrackInfo trackInfo = new TrackInfo();
            trackInfo.title = res.getString(1);
            trackInfo.artist = res.getString(2);
            trackInfo.imageUrl = res.getString(3);
            historyTrackInfo.add(trackInfo);
            Log.d(TAG,"inserted : " + historyTrackInfo.get(historyTrackInfo.size()-1).title);
        }
        res.close();
    }



}
