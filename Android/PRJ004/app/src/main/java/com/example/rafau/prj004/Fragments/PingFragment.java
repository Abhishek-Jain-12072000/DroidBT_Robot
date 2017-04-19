package com.example.rafau.prj004.Fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.rafau.prj004.Command;
import com.example.rafau.prj004.MainActivity;
import com.example.rafau.prj004.R;
import com.example.rafau.prj004.RadarView;

import java.text.ParseException;

/**
 * A simple {@link Fragment} subclass.
 */
public class PingFragment extends Fragment {

    private SeekBar seekBarH;
    private SeekBar seekBarV;
    private Button measureButton;
    private Button radarButton;
    private boolean measureEnbl = true;
    private boolean waitForData = false;
    private boolean response = false;
    private TextView distanceTextView;
    private LinearLayout linearLayout;
    private int[] radarData = new int[180];
    private int divider = 58;
    private float dist = 0;
    private String unit = "cm";

    public PingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ping, container, false);

        seekBarH = (SeekBar) view.findViewById(R.id.ping_seek_h);
        seekBarH.setMax(180);
        seekBarH.setProgress(90);
        seekBarV = (SeekBar) view.findViewById(R.id.ping_seek_v);
        seekBarV.setMax(180);
        seekBarV.setProgress(90);
        measureButton = (Button) view.findViewById(R.id.measure_button);
        radarButton = (Button) view.findViewById(R.id.radar_button);
        distanceTextView = (TextView) view.findViewById(R.id.distance_text_view);
        RadioButton cmRadio = (RadioButton) view.findViewById(R.id.radio_cm);

        linearLayout = (LinearLayout) view.findViewById(R.id.radarLayout);

        View radarView = new RadarView(getContext());
        radarView.setBackgroundColor(Color.BLACK);
        linearLayout.addView(radarView);

        for (int i = 0; i < 179; i++) {
            radarData[i] = 0;
        }

        cmRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    divider = 58; //bo taka matematyka
                    unit = "cm";
                    if (dist != 0)
                        distanceTextView.setText("=" + (dist / ((float) divider)) + unit);
                } else {
                    divider = 148; //bo taka matematyka
                    unit = "in";
                    if (dist != 0)
                        distanceTextView.setText("=" + (dist / ((float) divider)) + unit);
                }
            }
        });

        seekBarH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                waitForData = false;
                ((MainActivity) getActivity()).sendToDevice(Command.ping(seekBarH.getProgress(), seekBarV.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                waitForData = false;
                ((MainActivity) getActivity()).sendToDevice(Command.ping(seekBarH.getProgress(), seekBarV.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        measureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (measureEnbl) {
                    if(!waitForData)
                    ((MainActivity) getActivity()).sendToDevice(Command.measure());
                    measureEnbl = false;
//                    waitForData = false;
                    response = false;
                    timerStart();
                }
            }
        });


        radarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!waitForData) {
                    ((MainActivity) getActivity()).sendToDevice(Command.radar());
                    waitForData = true;
                    response = false;
                    timerStart();
                    for (int i = 0; i < 180; i++) {
                        radarData[i] = 0;
                    }
                }
            }


        });

        return view;
    }

    public boolean isMeasureEnbl() {
        return measureEnbl;
    }

    public void setMeasureEnbl(boolean measureEnbl) {
        this.measureEnbl = measureEnbl;
    }

    public void setDistanceTextView(String distance) {
        String[] split = null;
        split = distance.split(",");
        dist = Float.parseFloat(split[0]);
        distanceTextView.setText("=" + (dist / ((float) divider)) + unit);
    }

    public boolean isWaitForData() {
        return waitForData;
    }

    public void setWaitForData(boolean waitForData) {
        this.waitForData = waitForData;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    public void drawRadar(String measure) {
        String[] split = null;
        split = measure.split(",");
        int r = Integer.parseInt(split[0]);
        r /= 58; //zamiana na cm
        try {
            if (r > 100)
                radarData[Integer.parseInt(split[1])] = 100;
            else
                radarData[Integer.parseInt(split[1])] = r;
        }
        catch (Exception e){

        }
        linearLayout.removeAllViews();
        View radarView = new RadarView(getContext(), radarData);
        radarView.setBackgroundColor(Color.BLACK);
        linearLayout.addView(radarView);
    }

    @Override
    public void onStop() {
        waitForData = false;
        ((MainActivity) getActivity()).sendToDevice(Command.ping(90, 90));
        super.onStop();
    }

    @Override
    public void onResume() {
        distanceTextView.setText(R.string.distance);
        dist = 0;
        for (int i = 0; i < 180; i++) {
            radarData[i] = 0;
        }
        super.onResume();
    }

    private void timerStart() {
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (!response) {
                    ((MainActivity) getActivity()).openDialog("ERROR", "No response. Try Again.");
                    ((MainActivity) getActivity()).sendToDevice(Command.ping(90, 90));
                    waitForData = false;
                    measureEnbl = true;
                }
            }

        }.start();
    }
}
