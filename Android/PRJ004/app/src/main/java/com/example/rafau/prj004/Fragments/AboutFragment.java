package com.example.rafau.prj004.Fragments;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rafau.prj004.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ImageView imageView = (ImageView)view.findViewById(R.id.image_view);
        imageView.setImageResource(R.drawable.prj004_1);
        TextView textView = (TextView) view.findViewById(R.id.more_text_view);
        textView.setTextColor(Color.BLUE);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.teleinfo.edu.pl/projects.php?prj=004"));
                startActivity(browserIntent);
            }
        });

        return view;
    }

}
