package com.example.brittanyhsu.bhspotify;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by brittanyhsu on 8/24/17.
 */

public class EmptyFragment extends android.support.v4.app.Fragment {

    public static EmptyFragment newInstance() {
        EmptyFragment frag = new EmptyFragment();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_one, container, false);
        return v;

    }
}
