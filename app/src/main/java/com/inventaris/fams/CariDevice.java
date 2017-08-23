package com.inventaris.fams;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inventaris.fams.Adapter.AdapterPairedDevices;
import com.inventaris.fams.Model.PairedDevice;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CariDevice extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter = null;
    private final static int REQUEST_ENABLE_BT = 1;
    private AdapterPairedDevices adapter;
    private ArrayList<PairedDevice> dataDevice = new ArrayList<>();

    @BindView(R.id.btn_turnOn)
    Button turnOn;
    @BindView(R.id.listDevice)
    ListView list;
    @BindView(R.id.txtPaired)
    TextView textPaired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_device);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle("Find Device");

        turnOn.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);
        textPaired.setVisibility(View.GONE);

        adapter = new AdapterPairedDevices(dataDevice, getApplicationContext());
        list.setAdapter(adapter);

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
    }

    @OnClick(R.id.btn_turnOn)
    void turningOnBluetooth() {
        bluetoothState();
    }

    private void bluetoothState() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            turnOn.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            textPaired.setVisibility(View.VISIBLE);
            getPairedDevices();
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                kembali();
                break;
        }
        return true;
    }

    private void kembali() {
        this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                turnOn.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                textPaired.setVisibility(View.VISIBLE);
                getPairedDevices();
            } else {
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
                    turnOn.setVisibility(View.VISIBLE);
                    list.setVisibility(View.GONE);
                }
            }
        }
    }
}
