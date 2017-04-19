package com.example.rafau.prj004.Fragments;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.rafau.prj004.Command;
import com.example.rafau.prj004.InputFilterMinMax;
import com.example.rafau.prj004.MainActivity;
import com.example.rafau.prj004.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private EditText rightServoTrimET, leftServoTrimET, followerKpEditText, followerKdEditText, sequenceSettingET;
    private Button getTrimButton, setTrimButon, getFollowerButton, setFollowerButton, sequenceSettingButton;
    private MainActivity mainActivity;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mainActivity = (MainActivity) getActivity();

        rightServoTrimET = (EditText) view.findViewById(R.id.right_trim_edittext);
        leftServoTrimET = (EditText) view.findViewById(R.id.left_trim_edittext);
        followerKpEditText = (EditText) view.findViewById(R.id.kp_edittext);
        followerKdEditText = (EditText) view.findViewById(R.id.kd_edittext);
        sequenceSettingET = (EditText) view.findViewById(R.id.sequence_setting_ET);

        getTrimButton = (Button) view.findViewById(R.id.get_trim_button);
        setTrimButon = (Button) view.findViewById(R.id.set_trim_button);
        getFollowerButton = (Button) view.findViewById(R.id.get_follower_button);
        setFollowerButton = (Button) view.findViewById(R.id.set_follower_button);
        sequenceSettingButton = (Button) view.findViewById(R.id.sequence_setting_button);

        rightServoTrimET.setFilters(new InputFilter[]{new InputFilterMinMax(-10, 10)});
        leftServoTrimET.setFilters(new InputFilter[]{new InputFilterMinMax(-10, 10)});
        followerKpEditText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 20)});
        followerKdEditText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 20)});
        sequenceSettingET.setFilters(new InputFilter[]{new InputFilterMinMax(0,10000)});

        int limit = mainActivity.getSharedPreferences().getInt(MainActivity.TIME_OUT_KEY,500);
        sequenceSettingET.setText("" + limit);

        getTrimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.sendToDevice(Command.trim());
            }
        });

        setTrimButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightServoTrimET.getText().toString().equals(""))
                    rightServoTrimET.setText("0");
                if (leftServoTrimET.getText().toString().equals(""))
                    leftServoTrimET.setText("0");
                mainActivity.sendToDevice(Command.trim(Integer.parseInt(rightServoTrimET.getText().toString()), Integer.parseInt(leftServoTrimET.getText().toString())));
            }
        });

        getFollowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.sendToDevice(Command.regPD());
            }
        });

        setFollowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (followerKpEditText.getText().toString().equals(""))
                    followerKpEditText.setText("0");
                if (followerKdEditText.getText().toString().equals(""))
                    followerKdEditText.setText("0");
                mainActivity.sendToDevice(Command.regPD(Integer.parseInt(followerKpEditText.getText().toString()), Integer.parseInt(followerKdEditText.getText().toString())));
            }
        });

        sequenceSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sequenceSettingET.getText().toString().equals(""))
                    sequenceSettingET.setText("500");
                int tmp = Integer.parseInt(sequenceSettingET.getText().toString());
                if (tmp<100) {
                    tmp = 100;
                    sequenceSettingET.setText("100");
                }
                //TODO: SAVE limit to ptefs
                SharedPreferences.Editor editor = mainActivity.getSharedPreferences().edit();
                editor.putInt(mainActivity.TIME_OUT_KEY,tmp);
                editor.apply();
                editor.commit();
            }
        });

        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        };

        rightServoTrimET.setOnFocusChangeListener(onFocusChangeListener);
        leftServoTrimET.setOnFocusChangeListener(onFocusChangeListener);
        followerKpEditText.setOnFocusChangeListener(onFocusChangeListener);
        followerKdEditText.setOnFocusChangeListener(onFocusChangeListener);
        sequenceSettingET.setOnFocusChangeListener(onFocusChangeListener);

        return view;
    }

    public void setTrimValues(String message){
        String[] split = null;
        split = message.split(",");
        rightServoTrimET.setText(split[1]);
        leftServoTrimET.setText(split[2]);
}

    public void setFollowerValues(String message){
        String[] split = null;
        split = message.split(",");
        followerKdEditText.setText(split[1]);
        followerKpEditText.setText(split[2]);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)mainActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
