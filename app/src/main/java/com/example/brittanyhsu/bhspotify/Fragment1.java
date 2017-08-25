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

public class Fragment1 extends android.support.v4.app.Fragment {
    private String title;
    private int image;

    public static Fragment1 newInstance(String title, int resImage) {
        Fragment1 frag = new Fragment1();
        Bundle args = new Bundle();
        args.putString("title",title);
        args.putInt("image", resImage);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getArguments().getString("title");
        image = getArguments().getInt("image",0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_one, container, false);
        TextView titleLabel = (TextView) v.findViewById(R.id.text_fragment1);
        titleLabel.setText(title);

        ImageView imageView = (ImageView) v.findViewById(R.id.image_fragment1);
        imageView.setImageResource(image);

        return v;

    }
}
