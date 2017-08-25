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

public class Fragment2 extends android.support.v4.app.Fragment {
    private String text1;
    private String text2;
    private int image;

    public static Fragment2 newInstance(String text1, String text2, int resImage) {
        Fragment2 frag = new Fragment2();
        Bundle args = new Bundle();
        args.putString("text1",text1);
        args.putString("text2",text2);

        args.putInt("image", resImage);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        text1 = getArguments().getString("text1");
        text2 = getArguments().getString("text2");
        image = getArguments().getInt("image",0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_two, container, false);
        TextView text1label = (TextView) v.findViewById(R.id.text_fragment2_1);
        text1label.setText(text1);

        TextView text2label = (TextView) v.findViewById(R.id.text_fragment2_2);
        text2label.setText(text2);

        ImageView imageView = (ImageView) v.findViewById(R.id.image_fragment2);
        imageView.setImageResource(image);

        return v;

    }
}
