package com.example.rafau.prj004;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.example.rafau.prj004.Events.BluetoothEvent;
import com.example.rafau.prj004.Events.BluetoothReceiveEvent;
import com.example.rafau.prj004.Events.DeviceDisconnectEvent;
import com.example.rafau.prj004.Fragments.AboutFragment;
import com.example.rafau.prj004.Fragments.ConnectivityFragment;
import com.example.rafau.prj004.Fragments.MainCtrlFragment;
import com.example.rafau.prj004.Fragments.PingFragment;
import com.example.rafau.prj004.Fragments.SequenceFragment;
import com.example.rafau.prj004.Fragments.SettingsFragment;
import com.example.rafau.prj004.Fragments.SlidersFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    public static final String HC_05_MAC = "20:14:04:17:33:04";
    //    public static final String HC_05_MAC = "98:D3:32:30:55:90";
    public static final UUID androidUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String TIME_OUT_KEY = "TIME_OUT_KEY";
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    private ConnectivityFragment connectivityFragment;
    private MainCtrlFragment mainCtrlFragment;
    private SlidersFragment slidersFragment;
    private PingFragment pingFragment;
    private SequenceFragment sequenceFragment;
    private AboutFragment aboutFragment;
    private ProgressDialog progressDialog;
    private SettingsFragment settingsFragment;

    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private BluetoothAdapter bluetoothAdapter = null;
    private boolean bluetoothConnected = false;
    private boolean pendingRequestEnableBt = false;
    private boolean isCRCdialogShow = false;
    private boolean isBluetoothtOn = false;
    public static final String SAVED_PENDING_REQUEST_ENABLE_BT = "PENDING_REQUEST_ENABLE_BT";
    static final int REQUEST_ENABLE_BT = 2;

    private RxRunner rxRunner = null;

    private String fullStatus = "";
    private File fileLog;
    private File fileDir;

    private int radarCRC = 0;

    public File getFileLog() {
        return fileLog;
    }

    private SharedPreferences sharedPreferences;
    private boolean isAppStop = false;

    public void setFullStatus(String fullStatus) {
        this.fullStatus = fullStatus;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Closing drawer on item click
                drawerLayout.closeDrawers();
                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.connectivity:
                        if (connectivityFragment == null)
                            connectivityFragment = new ConnectivityFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, connectivityFragment).commit();
                        return true;
                    case R.id.main_ctrl:
                        if (mainCtrlFragment == null) mainCtrlFragment = new MainCtrlFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, mainCtrlFragment).commit();
                        return true;
                    case R.id.sliders:
                        if (slidersFragment == null) slidersFragment = new SlidersFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, slidersFragment).commit();
                        return true;
                    case R.id.ping:
                        if (pingFragment == null) pingFragment = new PingFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, pingFragment).commit();
                        return true;
                    case R.id.sequence:
                        if (sequenceFragment == null)
                            sequenceFragment = new SequenceFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, sequenceFragment).commit();
                        return true;
                    case R.id.about:
                        if (aboutFragment == null)
                            aboutFragment = new AboutFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, aboutFragment).commit();
                        return true;
                    case R.id.settings:
                        if (settingsFragment == null)
                            settingsFragment = new SettingsFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, settingsFragment).commit();
                    default:
                        return true;
                }
            }
        });

        initializeDrawer();
        connectivityFragment = new ConnectivityFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, connectivityFragment).commit();

        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        fileDir = new File(sdcard.getAbsolutePath() + "/PRJ004_log/");
        // create this directory if not already created
        fileDir.mkdir();
        // create the file in which we will write the contents
        fileLog = new File(fileDir, "tempLog.txt");
        addToLog(getString(R.string.status));
    }

    @Override
    protected void onStop() {
        isAppStop = true;
        if (isBluetoothConnected())
            disconnectDevice();
        super.onStop();
    }

    @Override
    protected void onResume() {
        isAppStop = false;
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // getSupportFragmentManager().beginTransaction().replace(R.id.frame, connectivityFragment).commit();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public File getFileDir() {
        return fileDir;
    }

    public boolean isBluetoothtOn() {
        return isBluetoothtOn;
    }

    public void onEvent(BluetoothEvent event) {
        int state = event.getState();
        if (state == BluetoothAdapter.STATE_TURNING_OFF) {
            addToLog("bluetooth off");
            connectivityFragment.enableConnection(false);
            connectivityFragment.refreshStatus();
            isBluetoothtOn = false;
        } else if (state == BluetoothAdapter.STATE_ON) {
            addToLog("bluetooth on");
            connectivityFragment.enableConnection(true);
            bluetoothConnected = false;
            connectivityFragment.updateConnectButton();
            isBluetoothtOn = true;
        }
    }

    public void onEvent(DeviceDisconnectEvent event) {
        if (isBluetoothConnected()) {
            if (pingFragment != null) {
                pingFragment.setMeasureEnbl(true);
                pingFragment.setWaitForData(false);
            }
            if (!connectivityFragment.isAdded()) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, connectivityFragment).commit();
            }
            disconnectDevice();
            setBluetoothConnected(false);
        }
    }

    public void onEventMainThread(BluetoothReceiveEvent event) {
        String message = event.getMessage();
        addToLog("Rx:" + message);
        if (connectivityFragment != null) {
            connectivityFragment.refreshStatus();
        }
        if (message.contains("DONE")) {
            if (pingFragment != null) {
                if (pingFragment.isWaitForData()) {
                    openDialog("DONE", "There was " + radarCRC + " fail CRC");
                    sendToDevice(Command.ping(90, 90));
                    radarCRC = 0;
                }
                pingFragment.setWaitForData(false);
            }

            if (sequenceFragment != null) {
                if (sequenceFragment.isSequenceOn()) {
                    sequenceFragment.nextCommand();
                }
            }
        } else if (message.contains("CRC")) {
            if (pingFragment != null) {
                if (!pingFragment.isMeasureEnbl()) {
                    openDialog("ERROR", "CRC Error. Try again");
                    isCRCdialogShow = true;
                    pingFragment.setMeasureEnbl(true);
                }
                if (pingFragment.isWaitForData()) {
                    radarCRC++;
                    message = "0,0,";
                }
            }
        } else if (message.contains("trim")) {
            settingsFragment.setTrimValues(message);
        } else if (message.contains("regPD")) {
            settingsFragment.setFollowerValues(message);
        }
        if (pingFragment != null) {
            pingFragment.setResponse(true);
            if (!pingFragment.isMeasureEnbl()) {
                pingFragment.setDistanceTextView(message);
                pingFragment.setMeasureEnbl(true);
            } else if (pingFragment.isWaitForData()) {
                pingFragment.drawRadar(message);
            }
        }
    }

    public void openDialog(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!isFinishing()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(title)
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (isCRCdialogShow) {
                                        isCRCdialogShow = false;
                                    }
                                }
                            }).create().show();
                }
            }
        });
    }

    public void openDialog2(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!isFinishing()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(title)
                            .setMessage(message)
                            .setCancelable(false)
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sequenceFragment.deleteCommand();
                                }
                            }).create().show();
                }
            }
        });
    }

    private void initializeDrawer() {
        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    public void addToLog(String status) {
        if (status.contains("\n"))
            fullStatus += status;
        else
            fullStatus += (status + "\n");

        try {
            FileOutputStream os = new FileOutputStream(fileLog);
            os.write(fullStatus.getBytes());
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initializeBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            addToLog(getString(R.string.no_bluetooth));
        }
        if (!bluetoothAdapter.isEnabled()) {
            pendingRequestEnableBt = true;
            addToLog(getString(R.string.disabled_bluetooth));
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectivityFragment.enableConnection(true);
            isBluetoothtOn = true;
        }
    }

    public void lookingForDevice() {
        if (bluetoothAdapter.isEnabled())
            progressDialog = ProgressDialog.show(this, "Wait..", "", true);
        Set<BluetoothDevice> BtList = bluetoothAdapter.getBondedDevices();
        boolean found = false;
        for (BluetoothDevice bt : BtList) {
            if (bt.getAddress().equals(HC_05_MAC)) {
                bluetoothDevice = bt;
                found = true;
                break;
            }
        }
        if (found) {
            addToLog(getString(R.string.device_paired));
            new BluetoothConnect().execute();
        } else
            addToLog(getString(R.string.device_not_paired));
    }

    public void disconnectDevice() {
        new BluetoothDisconnect().execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_PENDING_REQUEST_ENABLE_BT, pendingRequestEnableBt);
    }

    public void enableControlGroup(boolean enable) {
        navigationView.getMenu().setGroupEnabled(R.id.control_group, enable);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void sendToDevice(String command) {
        radarCRC = 0;
        try {   //obsługa wyjątków podczas wysyłania
            bluetoothSocket.getOutputStream().write(Command.countAndAddCRC(command)); //wysyłanie
        } catch (IOException e) {
            e.printStackTrace();
        }
        addToLog("Tx:" + command);  //dopisanie kolejnej linijki do logu
    }

    public boolean isBluetoothConnected() {
        return bluetoothConnected;
    }

    public void setBluetoothConnected(boolean bluetoothConnected) {
        this.bluetoothConnected = bluetoothConnected;
    }


    private class BluetoothConnect extends AsyncTask<Void, Void, Void> {
        private boolean connected = false;

        @Override
        protected void onPreExecute() {
            addToLog(getString(R.string.connecting));
            connectivityFragment.enableConnection(false);
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (bluetoothSocket == null || !bluetoothConnected) {
                    bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(androidUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();
                }
                connected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!connected) {
                addToLog(getString(R.string.cant_connect));
                enableControlGroup(false);
            } else {
                bluetoothConnected = true;
                addToLog(getString(R.string.connected));
                enableControlGroup(true);
                rxRunner = new RxRunner();
            }
            if (!isAppStop)
                connectivityFragment.updateConnectButton();
        }
    }

    private class BluetoothDisconnect extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            addToLog(getString(R.string.disconnecting));
            connectivityFragment.enableConnection(false);
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (rxRunner != null) {
                rxRunner.stop();
                while (rxRunner.isRunning()) ;
                rxRunner = null;
            }

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            bluetoothConnected = false;
            addToLog(getString(R.string.disconnected));
            enableControlGroup(false);
            if (!isAppStop)
                connectivityFragment.updateConnectButton();
        }
    }

    private class RxRunner implements Runnable {
        private boolean halt = false;
        private Thread rxThread;

        public RxRunner() {
            rxThread = new Thread(this, "Input Thread");
            rxThread.start();
        }

        public boolean isRunning() {
            return rxThread.isAlive();
        }

        @Override
        public void run() {
            InputStream rxStream;           //obiekt przychodzącego strumienia
            String message = "";            //zmienna do przechowania przychodzącego ciągu znaków
            try {                           //obsługa wyjątków i błędów
                rxStream = bluetoothSocket.getInputStream();//pobranie przychodzącego strumienia
                while (!halt) {
                    byte[] buffer = new byte[256];//bufor danych
                    byte crc = 0, crcFinal; //zmienne wykożystywane do sprawdzania sumy kontrolnej
                    int crcPoint = 0;
                    if (rxStream.available() > 0) {
                        rxStream.read(buffer);//przepisanie strumienia do bufora
                        int i;
                        for (i = 0; (i < buffer.length) && (0 != buffer[i]); i++) {
                            if (i < (buffer.length - 1)) {  //pętla obliczająca sumę kontrolną
                                crc = Command.crc8_ccitt_update(crc, buffer[i]);
                                crcPoint++;
                            }
                        }
                        crcFinal = buffer[crcPoint];    //odczyt odebranej sumy kontrolnej
                        buffer[crcPoint - 1] = 0x00;    //usunięcie z danych pola sumy kontrolnej
                        final String rxText = new String(buffer, 0, i);
                        if (crc == crcFinal)    //sprawdzenie poprawności sumy kontrolnej
                            EventBus.getDefault().post(new BluetoothReceiveEvent(rxText));
                        else
                            EventBus.getDefault().post(new BluetoothReceiveEvent("CRC Error"));
                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        public void stop() {
            halt = true;
        }
    }

}
