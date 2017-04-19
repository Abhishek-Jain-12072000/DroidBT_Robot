package com.example.rafau.prj004.Fragments;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.rafau.prj004.Command;
import com.example.rafau.prj004.InputFilterMinMax;
import com.example.rafau.prj004.MainActivity;
import com.example.rafau.prj004.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SequenceFragment extends Fragment {

    boolean sequenceOn = false;
    int commandIndex = 0;
    private ArrayList<String> seguenceOfCommands = new ArrayList<>();
    private MainActivity mainActivity;
    private Button startButton, addButton;
    private ListView sequenceListView;
    private ArrayAdapter<String> arrayAdapter;
    private int toDelete, timerTick = 0, timerLimit=100;
    private Spinner spinner;
    private EditText firstEditText, secondEditText, thirdEditText;
    private int selectedCommand = 0;
    private CountDownTimer countDownTimer = new CountDownTimer(10000,10) {
        @Override
        public void onTick(long millisUntilFinished) {
            timerTick++;
            if((timerTick>=timerLimit)&&sequenceOn){
                nextCommand();
            }
        }

        @Override
        public void onFinish() {

        }
    };

    final InputFilterMinMax motorFilter = new InputFilterMinMax(-50, 50);
    final InputFilterMinMax armFilter = new InputFilterMinMax(0, 100);
    final InputFilterMinMax pingFilter = new InputFilterMinMax(0, 180);

    public SequenceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sequence, container, false);

        mainActivity = (MainActivity) getActivity();

        seguenceOfCommands.clear();

        sequenceListView = (ListView) view.findViewById(R.id.sequence_list_view);
        arrayAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.custom_adapter, seguenceOfCommands);
        sequenceListView.setAdapter(arrayAdapter);

        startButton = (Button) view.findViewById(R.id.start_sequence_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!seguenceOfCommands.isEmpty()) {
                    nextCommand();
                    timerLimit = mainActivity.getSharedPreferences().getInt(mainActivity.TIME_OUT_KEY,500);
                    countDownTimer.start();
                    sequenceOn = true;
                    startButton.setEnabled(false);
                }
            }
        });

        addButton = (Button) view.findViewById(R.id.sdd_sequence_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //      seguenceOfCommands.add(Command.motor(rightPicker.getValue(), leftPicker.getValue(), distancePicker.getValue()));//////////////////////////////
                int f, s, t;
                boolean var1, var2, var3;
                var1 = firstEditText.getText().toString().isEmpty();
                var2 = secondEditText.getText().toString().isEmpty();
                var3 = thirdEditText.getText().toString().isEmpty();
                switch (selectedCommand) {
                    case 0:
                        if(!(var1 || var2 || var3)) {
                            f = Integer.parseInt(firstEditText.getText().toString());
                            s = Integer.parseInt(secondEditText.getText().toString());
                            t = Integer.parseInt(thirdEditText.getText().toString());
                            seguenceOfCommands.add(Command.motor(f, s, t));
                        }
                        break;
                    case 1:
                        if(!(var1)) {
                            f = Integer.parseInt(firstEditText.getText().toString());
                            seguenceOfCommands.add(Command.arm(f));
                        }
                        break;
                    case 2:
                        if(!(var1 || var2)) {
                            f = Integer.parseInt(firstEditText.getText().toString());
                            s = Integer.parseInt(secondEditText.getText().toString());
                            seguenceOfCommands.add(Command.ping(f, s));
                        }
                        break;
                    default:
                        break;
                }
                sequenceListView.setAdapter(arrayAdapter);

            }
        });

        sequenceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                toDelete = position;
                mainActivity.openDialog2("Are you sure?", "DELETE ITEM");
                return false;
            }
        });

        spinner = (Spinner) view.findViewById(R.id.select_spinner);
        firstEditText = (EditText) view.findViewById(R.id.first_editText);
        secondEditText = (EditText) view.findViewById(R.id.second_editText);
        thirdEditText = (EditText) view.findViewById(R.id.third_editText);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCommand = position;
                firstEditText.setText("");
                secondEditText.setText("");
                thirdEditText.setText("");
                switch (position) {
                    case 0:
                        secondEditText.setEnabled(true);
                        thirdEditText.setEnabled(true);
                        firstEditText.setHint("right");
                        secondEditText.setHint("left");
                        thirdEditText.setHint("dist.");
                        firstEditText.setFilters(new InputFilter[]{motorFilter});
                        secondEditText.setFilters(new InputFilter[]{motorFilter});
                        thirdEditText.setFilters(new InputFilter[]{motorFilter});
                        break;
                    case 1:
                        secondEditText.setEnabled(false);
                        thirdEditText.setEnabled(false);
                        firstEditText.setHint("position");
                        secondEditText.setHint(" ");
                        thirdEditText.setHint(" ");
                        firstEditText.setFilters(new InputFilter[]{armFilter});
                        break;
                    case 2:
                        secondEditText.setEnabled(true);
                        thirdEditText.setEnabled(false);
                        firstEditText.setHint("horiz");
                        secondEditText.setHint("vert");
                        thirdEditText.setHint(" ");
                        firstEditText.setFilters(new InputFilter[]{pingFilter});
                        secondEditText.setFilters(new InputFilter[]{pingFilter});
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

        firstEditText.setOnFocusChangeListener(onFocusChangeListener);
        secondEditText.setOnFocusChangeListener(onFocusChangeListener);
        thirdEditText.setOnFocusChangeListener(onFocusChangeListener);

        return view;
    }

    public boolean isSequenceOn() {
        return sequenceOn;
    }

    public void nextCommand() {
        timerTick = 0;
        if (commandIndex < seguenceOfCommands.size()) {
            mainActivity.sendToDevice(seguenceOfCommands.get(commandIndex));
            commandIndex++;

            sequenceListView.clearFocus();
            sequenceListView.requestFocusFromTouch();
            sequenceListView.post(new Runnable() {
                @Override
                public void run() {
                    sequenceListView.setItemChecked(commandIndex - 1, true);
                    sequenceListView.setSelection(commandIndex - 1);
                }
            });

        } else {
            countDownTimer.cancel();
            sequenceOn = false;
            commandIndex = 0;
            startButton.setEnabled(true);
            sequenceListView.clearFocus();
            sequenceListView.requestFocusFromTouch();
        }
    }

    public void deleteCommand() {
        seguenceOfCommands.remove(toDelete);
        sequenceListView.setAdapter(arrayAdapter);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)mainActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
