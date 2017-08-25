package com.inventaris.fams;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ExpandedMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inventaris.fams.Adapter.AdapterPairedDevices;
import com.inventaris.fams.Model.PairedDevice;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.pixplicity.easyprefs.library.Prefs;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.jar.Manifest;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CariDevice extends AppCompatActivity {
    private BluetoothSocket mSocket = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private final static int REQUEST_ENABLE_BT = 1;
    private AdapterPairedDevices adapter, adapterAvailable;
    private ArrayList<PairedDevice> dataDevice = new ArrayList<>();
    private ArrayList<PairedDevice> dataDeviceAvailable = new ArrayList<>();

    @BindView(R.id.btn_turnOn)
    Button turnOn;
    @BindView(R.id.listDevice)
    ListView list;
    @BindView(R.id.txtPaired)
    TextView textPaired;
    @BindView(R.id.listAvailableDevice)
    ListView listAvailable;
    @BindView(R.id.txtAvailableDevices)
    TextView textAvailable;
    @BindView(R.id.txtSearching)
    TextView cari;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_device);

        ButterKnife.bind(this);

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle("Find Device");

        turnOn.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        textPaired.setVisibility(View.GONE);
        listAvailable.setVisibility(View.GONE);
        textAvailable.setVisibility(View.GONE);
        cari.setVisibility(View.GONE);

        list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        listAvailable.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        adapter = new AdapterPairedDevices(dataDevice, getApplicationContext());
        list.setAdapter(adapter);
        adapterAvailable = new AdapterPairedDevices(dataDeviceAvailable, getApplicationContext());
        listAvailable.setAdapter(adapterAvailable);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "This Device does not support Bluetooth, exiting,,,", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    kembali();
                }
            }, 800);
        } else {
            bluetoothState();
        }

        listAvailable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), HalamanUtama.class);
                intent.putExtra("dataDevice", dataDeviceAvailable.get(position));
                startActivity(intent);
                finish();
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                Intent intent = new Intent(getApplicationContext(), HalamanUtama.class);
                intent.putExtra("dataDevice", dataDevice.get(position));
                startActivity(intent);
                finish();
            }
        });
    }

    @OnClick(R.id.btn_turnOn)
    void turningOnBluetooth() {
        bluetoothState();
    }

    @OnClick(R.id.txtSearching)
    void scanning() {
        if (dataDeviceAvailable.isEmpty() && !mBluetoothAdapter.isDiscovering()) {
            discoverDevice();
        }
    }


    private void bluetoothState() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            turnOn.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            textPaired.setVisibility(View.VISIBLE);
            listAvailable.setVisibility(View.VISIBLE);
            textAvailable.setVisibility(View.VISIBLE);
            getPairedDevices();
            discoverDevice();
        }
    }

    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                dataDevice.add(new PairedDevice(device.getName(), device.getAddress()));
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void discoverDevice() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Dexter.withActivity(this)
                    .withPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
//                            Toast.makeText(CariDevice.this, "permission granted !", Toast.LENGTH_SHORT).show();
                            mBluetoothAdapter.startDiscovery();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        }
                    }).check();
        } else {
            mBluetoothAdapter.startDiscovery();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                dataDeviceAvailable.clear();
                cari.setText("Searching Device...");
                cari.setVisibility(View.VISIBLE);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (dataDeviceAvailable.isEmpty()) {
                    cari.setText("No Device Found, make sure the other device is discoverable ! click here to scan again !");
                    cari.setVisibility(View.VISIBLE);
                } else {
                    cari.setVisibility(View.GONE);
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String deviceName = device.getName();
                    boolean isNew = true;
                    for (int i = 0; i < dataDevice.size(); i++) {
                        if (dataDevice.get(i).getDeviceName().equals(deviceName)) {
                            isNew = false;
                            break;
                        }
                    }
                    if (isNew) {
                        cari.setVisibility(View.GONE);
                        Log.d("Action Found", deviceName);
                        listAvailable.setVisibility(View.VISIBLE);
                        dataDeviceAvailable.add(new PairedDevice(device.getName(), device.getAddress()));
                        adapterAvailable.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                kembali();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed()
    {
       kembali();
    }

    private void kembali() {
        Intent intent = new Intent(getApplicationContext(), HalamanUtama.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                turnOn.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                textPaired.setVisibility(View.VISIBLE);
                listAvailable.setVisibility(View.VISIBLE);
                textAvailable.setVisibility(View.VISIBLE);
                getPairedDevices();
                discoverDevice();
            } else {
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
                    turnOn.setVisibility(View.VISIBLE);
                    list.setVisibility(View.GONE);
                    textPaired.setVisibility(View.GONE);
                    listAvailable.setVisibility(View.GONE);
                    textAvailable.setVisibility(View.GONE);
                }
            }
        }
    }


}
