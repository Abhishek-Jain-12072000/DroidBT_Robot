package com.example.rafau.prj004.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.rafau.prj004.Command;
import com.example.rafau.prj004.MainActivity;
import com.example.rafau.prj004.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SlidersFragment extends Fragment {

    private SeekBar leftSeekBar;
    private SeekBar rightSeekBar;
    private TextView leftTextView;
    private TextView rightTextView;

    private int leftValue = 0;
    private int rightValue = 0;

    public SlidersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sliders, container, false);

        leftSeekBar = (SeekBar) view.findViewById(R.id.seekBar_left);
        rightSeekBar = (SeekBar) view.findViewById(R.id.seekBar_right);
        leftTextView = (TextView) view.findViewById(R.id.text_left_slider);
        rightTextView = (TextView) view.findViewById(R.id.text_right_slider);

        leftSeekBar.setMax(100);
        rightSeekBar.setMax(100);

        leftSeekBar.setProgress(50);
        rightSeekBar.setProgress(50);

        updateLeftValue();
        updateRightValue();

        leftTextView.setText(leftValue + "");
        rightTextView.setText(rightValue + "");

        leftSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateLeftValue();
                leftTextView.setText(leftValue + "");
                ((MainActivity) getActivity()).sendToDevice(Command.motor(rightValue,leftValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        rightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateRightValue();
                rightTextView.setText(rightValue + "");
                ((MainActivity) getActivity()).sendToDevice(Command.motor(rightValue, leftValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return view;
    }

    private void updateLeftValue() {
        leftValue = leftSeekBar.getProgress() - 50;
    }

    private void updateRightValue() {
        rightValue = rightSeekBar.getProgress() - 50;
    }

    @Override
    public void onStop() {
//        ((MainActivity) getActivity()).sendToDevice("motor,0,0\n");
        super.onStop();
    }

}
