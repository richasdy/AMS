package com.inventaris.fams.Fragment;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.inventaris.fams.CariDevice;
import com.inventaris.fams.Config;
import com.inventaris.fams.Model.PairedDevice;
import com.inventaris.fams.R;
import com.inventaris.fams.Utils.BluetoothConnector;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddData extends Fragment {
    private BluetoothDevice device = null;
    final int handlerState = 0;
    private BluetoothConnector.BluetoothSocketWrapper mSocket = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    BluetoothConnector bluetoothConnector;
    private Handler bluetoothIn;
    private PairedDevice dev;
    private ConnectedThread mConnectedThread;

    @BindView(R.id.txtStatus)
    TextView status;
    @BindView(R.id.btn_cari)
    Button cari;

    public AddData() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_data, container, false);
        ButterKnife.bind(this, view);

        status.setText("Status : Not Connected !");
        cari.setText("Find");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Bundle b = getActivity().getIntent().getExtras();
        if (b != null) {
            PairedDevice paireddevice = b.getParcelable("dataDevice");
            dev = paireddevice;
            device = mBluetoothAdapter.getRemoteDevice(paireddevice.getDeviceHardwareAddress());
            connectToDevice(device);
        }

        return view;
    }

    @OnClick(R.id.btn_cari)
    void cari() {
        if (cari.getText().toString().equals("Scan")) {
            Log.i("scan", "scan");
            mConnectedThread.write(".iv" + "\r\n");
        } else {
            startActivity(new Intent(AddData.this.getContext(), CariDevice.class));
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        status.setText("Status : Not Connected !");
        Bundle b = getActivity().getIntent().getExtras();
        if (b != null) {
            PairedDevice paireddevice = b.getParcelable("dataDevice");
            dev.getDeviceName();
            device = mBluetoothAdapter.getRemoteDevice(paireddevice.getDeviceHardwareAddress());
            connectToDevice(device);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            Toast.makeText(AddData.this.getContext(), "Failed to close Socket because " +
                    e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        bluetoothConnector = new BluetoothConnector(device, true, mBluetoothAdapter, null);
        try {
            mSocket = bluetoothConnector.connect();
            mConnectedThread = new ConnectedThread(mSocket);
            mConnectedThread.start();
            status.setText("Status : connected to " + mSocket.getRemoteDeviceName());
            cari.setText("Scan");
            bluetoothIn = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    if (msg.what == handlerState) {                                        //if message is what we want
                        String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                        //keep appending to string until ~
                        Log.i("response", readMessage);
                        Toast.makeText(AddData.this.getContext(), readMessage + "\r\n", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothConnector.BluetoothSocketWrapper socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(AddData.this.getContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                cari.setText("Find");
            }
        }
    }
}
