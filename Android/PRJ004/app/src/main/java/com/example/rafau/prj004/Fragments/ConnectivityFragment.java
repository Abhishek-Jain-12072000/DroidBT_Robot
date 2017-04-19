package com.example.rafau.prj004.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.rafau.prj004.MainActivity;
import com.example.rafau.prj004.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectivityFragment extends Fragment {

    private Button connectButton;
    private MainActivity mainActivity;
    private TextView statusTextView;

    public ConnectivityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connectivity, container, false);

        mainActivity = (MainActivity) getActivity();
        statusTextView = (TextView) view.findViewById(R.id.status);

        refreshStatus();

        connectButton = (Button) view.findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mainActivity.isBluetoothConnected()) {
                    mainActivity.disconnectDevice();
                } else
                    mainActivity.lookingForDevice();
            }
        });
        Button clearButton = (Button) view.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.setFullStatus("");
                mainActivity.addToLog(getString(R.string.status));
                refreshStatus();
            }
        });
        Button saveButton = (Button) view.findViewById(R.id.saving_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(mainActivity.getFileDir(), System.currentTimeMillis() + ".txt");
                try {
                    FileOutputStream os = new FileOutputStream(file);
                    String log = statusTextView.getText().toString();
                    os.write(log.getBytes());
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mainActivity.initializeBluetooth();
        updateConnectButton();
        return view;
    }

    public void enableConnection(boolean enable) {
        connectButton.setEnabled(enable);
    }

    public void updateConnectButton() {
        if (mainActivity.isBluetoothConnected())
            connectButton.setText(getString(R.string.disconnect));
        else
            connectButton.setText(getString(R.string.connect));
        if (mainActivity.isBluetoothtOn())
            connectButton.setEnabled(true);
        refreshStatus();
    }

    public void refreshStatus() {
        StringBuilder sb = new StringBuilder(Calendar.getInstance().getTime().toString() + "\n");
        sb.append(readTextLog(mainActivity.getFileLog()));
        statusTextView.setText(sb.toString());
    }

    private String readTextLog(File logfile) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(logfile));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();

        }

        return text.toString();
    }

}
