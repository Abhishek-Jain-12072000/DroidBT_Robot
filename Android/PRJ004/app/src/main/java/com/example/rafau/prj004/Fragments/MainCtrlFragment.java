package com.example.rafau.prj004.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.rafau.prj004.Command;
import com.example.rafau.prj004.MainActivity;
import com.example.rafau.prj004.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainCtrlFragment extends Fragment implements View.OnClickListener {

    private SeekBar armSeekBar;
    private boolean followerOff = true;
    private Button followerButton;

    public MainCtrlFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_ctrl, container, false);

        Button upButton = (Button) view.findViewById(R.id.button_up);
        upButton.setOnClickListener(this);
        Button downButton = (Button) view.findViewById(R.id.button_down);
        downButton.setOnClickListener(this);
        Button stopButton = (Button) view.findViewById(R.id.button_stop);
        stopButton.setOnClickListener(this);
        Button upRightButton = (Button) view.findViewById(R.id.button_up_right);
        upRightButton.setOnClickListener(this);
        Button upLeftButton = (Button) view.findViewById(R.id.button_up_left);
        upLeftButton.setOnClickListener(this);
        Button downRightButton = (Button) view.findViewById(R.id.button_down_right);
        downRightButton.setOnClickListener(this);
        Button downLeftButton = (Button) view.findViewById(R.id.button_down_left);
        downLeftButton.setOnClickListener(this);
        Button roundRightButton = (Button) view.findViewById(R.id.button_round_r);
        roundRightButton.setOnClickListener(this);
        Button roundLeftButton = (Button) view.findViewById(R.id.button_round_l);
        roundLeftButton.setOnClickListener(this);
        followerButton = (Button) view.findViewById(R.id.line_follower_button);
        followerButton.setOnClickListener(this);

        armSeekBar = (SeekBar) view.findViewById(R.id.seekBar_arm);
        armSeekBar.setMax(100);

        armSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((MainActivity) getActivity()).sendToDevice(Command.arm(armSeekBar.getProgress()));
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

    @Override
    public void onClick(View v) {
        String command = new String();
        switch (v.getId()) {
            case R.id.button_up:
                command = Command.motor(50, 50);
                turnOffFollower();
                break;
            case R.id.button_down:
                command = Command.motor(-50,-50);
                turnOffFollower();
                break;
            case R.id.button_stop:
                command = Command.motor(0,0);
                turnOffFollower();
                break;
            case R.id.button_up_right:
                command = Command.motor(3,50);
                turnOffFollower();
                break;
            case R.id.button_up_left:
                command = Command.motor(50,3);
                turnOffFollower();
                break;
            case R.id.button_down_right:
                command = Command.motor(-3,-50);
                turnOffFollower();
                break;
            case R.id.button_down_left:
                command = Command.motor(-50,-3);
                turnOffFollower();
                break;
            case R.id.button_round_r:
                command = Command.motor(50,-50);
                turnOffFollower();
                break;
            case R.id.button_round_l:
                command = Command.motor(-50,50);
                turnOffFollower();
                break;
            case R.id.line_follower_button:
                if (followerOff) {
                    command = Command.line();
                    followerOff = false;
                    followerButton.setText(getString(R.string.line_follower_turn_off));
                } else {
                    command = Command.motor(0,0);
                    turnOffFollower();
                }
            default:
                break;
        }
        ((MainActivity) getActivity()).sendToDevice(command);
    }

    private void turnOffFollower() {
        followerOff = true;
        followerButton.setText(getString(R.string.line_follower_turn_on));
    }

    @Override
    public void onStop() {
        turnOffFollower();
        ((MainActivity) getActivity()).sendToDevice(Command.motor(0,0));
        super.onStop();
    }
}
