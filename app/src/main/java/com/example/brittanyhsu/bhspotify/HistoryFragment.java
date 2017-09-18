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

public class HistoryFragment extends Fragment {
    String TAG = "HistoryFragment";
    private LayoutInflater mInflater;
    private ViewGroup mContainer;
    private ViewGroup placeholder;

    private ListView mListView;
    HistoryDBHelper myDb;
    List<String> historyList = new ArrayList<>();
    private ArrayAdapter adapter = null;

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
                historyList.clear();
                adapter.notifyDataSetChanged();
            }
        });

        // put db data in arraylist

        addDataToList();
        adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1,historyList);
        mListView.setAdapter(adapter);
    }

    public void addDataToList() {
        Cursor res = myDb.getAllData();
        if(res.getCount() == 0) {
            // show message
            return;
        }


        while(res.moveToNext()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Title: " + res.getString(1) + "\n");
            buffer.append("Artist: " + res.getString(2) + "\n");
            Log.d(TAG,buffer.toString());
            historyList.add(buffer.toString());
        }
        res.close();
    }



}
