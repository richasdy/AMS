package com.inventaris.fams.Fragment;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inventaris.fams.BuildConfig;
import com.inventaris.fams.CariDevice;
import com.inventaris.fams.Config;
import com.inventaris.fams.FamsModel;
import com.inventaris.fams.HalamanInputData;
import com.inventaris.fams.HalamanUtama;
import com.inventaris.fams.Model.PairedDevice;
import com.inventaris.fams.R;
import com.inventaris.fams.Utils.BluetoothConnector;
import com.inventaris.fams.Utils.ModelBase;
import com.inventaris.fams.Utils.TSLBluetoothDeviceApplication;
import com.inventaris.fams.Utils.WeakHandler;
import com.pixplicity.easyprefs.library.Prefs;
import com.uk.tsl.rfid.DeviceListActivity;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;

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
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;

    @BindView(R.id.txtStatus)
    TextView status;
    @BindView(R.id.btn_cari)
    Button cari;

    // Debug control
    private static final boolean D = BuildConfig.DEBUG;

    // The list of results from actions
    private ArrayAdapter<String> mResultsArrayAdapter;
    private ListView mResultsListView;
    private ArrayAdapter<String> mBarcodeResultsArrayAdapter;
    private ListView mBarcodeResultsListView;

    // All of the reader inventory tasks are handled by this class
    private FamsModel mModel;

    public AddData() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_data, container, false);
        ButterKnife.bind(this, view);

        mResultsArrayAdapter = new ArrayAdapter<String>(AddData.this.getContext(), android.R.layout.simple_list_item_1);
        mBarcodeResultsArrayAdapter = new ArrayAdapter<String>(AddData.this.getContext(), android.R.layout.simple_list_item_1);

        mResultsListView = (ListView) view.findViewById(R.id.resultListView);
        mResultsListView.setAdapter(mResultsArrayAdapter);
        mResultsListView.setFastScrollEnabled(true);

        mBarcodeResultsListView = (ListView) view.findViewById(R.id.barcodeListView);
        mBarcodeResultsListView.setAdapter(mBarcodeResultsArrayAdapter);
        mBarcodeResultsListView.setFastScrollEnabled(true);

        mModel = ((HalamanUtama) getActivity()).getmModel();

        mModel.setHandler(mGenericModelHandler);

        status.setText("Status : Not Connected !");
        cari.setVisibility(View.GONE);

        mResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AddData.this.getContext(), HalamanInputData.class)
                        .putExtra("epcCode", mResultsArrayAdapter.getItem(position));
                startActivity(intent);
            }
        });

        mBarcodeResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AddData.this.getContext(), HalamanInputData.class)
                        .putExtra("epcCode", mBarcodeResultsArrayAdapter.getItem(position));
                startActivity(intent);
            }
        });

        return view;
    }

    private final WeakHandler<AddData> mGenericModelHandler = new WeakHandler<AddData>(this) {

        @Override
        public void handleMessage(Message msg, AddData thisActivity) {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        //TODO: process change in model busy state
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        // Examine the message for prefix
                        String message = (String) msg.obj;
                        if (message.startsWith("ER:")) {
                            Toast.makeText(AddData.this.getContext(), message.substring(3), Toast.LENGTH_SHORT).show();
                        } else if (message.startsWith("BC:")) {
                            mBarcodeResultsArrayAdapter.add(message);
                            scrollBarcodeListViewToBottom();
                        } else {
                            mResultsArrayAdapter.add(message);
                            scrollResultsListViewToBottom();
                        }
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    };

    private void scrollResultsListViewToBottom() {
        mResultsListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mResultsListView.setSelection(mResultsArrayAdapter.getCount() - 1);
            }
        });
    }

    private void scrollBarcodeListViewToBottom() {
        mBarcodeResultsListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mBarcodeResultsListView.setSelection(mBarcodeResultsArrayAdapter.getCount() - 1);
            }
        });
    }

    private BroadcastReceiver mCommanderMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) {
                Log.d(getClass().getName(), "AsciiCommander state changed - isConnected: " + ((HalamanUtama) getActivity()).getCommander().isConnected());
            }

            String connectionStateMsg = intent.getStringExtra(AsciiCommander.REASON_KEY);
            Toast.makeText(context, connectionStateMsg, Toast.LENGTH_SHORT).show();

            displayReaderState();
            if (((HalamanUtama) getActivity()).getCommander().isConnected()) {
//                // Update for any change in power limits
//                setPowerBarLimits();
//                // This may have changed the current power level setting if the new range is smaller than the old range
//                // so update the model's inventory command for the new power value
//                mModel.getCommand().setOutputPower(mPowerLevel);

                mModel.resetDevice();
                mModel.updateConfiguration();
            }
        }
    };

    private void displayReaderState() {

        String connectionMsg = "Reader ";
        switch (((HalamanUtama) getActivity()).getCommander().getConnectionState()) {
            case CONNECTED:
                cari.setVisibility(View.VISIBLE);
                cari.setText("Scan");
                connectionMsg += ((HalamanUtama) getActivity()).getCommander().getConnectedDeviceName();
                break;
            case CONNECTING:
                cari.setVisibility(View.GONE);
                connectionMsg += "Connecting...";
                break;
            default:
                connectionMsg += "Disconnected";
                cari.setVisibility(View.GONE);
        }
        status.setText("Status : " + connectionMsg);
    }

    @OnClick(R.id.btn_cari)
    void cari() {
        if (cari.getText().toString().equals("Scan")) {
            Log.i("scan", "scan");
            mModel.scan();
        } else {
            ((HalamanUtama) getActivity()).finDevice();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mModel.setEnabled(true);

        // Register to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(AddData.this.getContext()).registerReceiver(mCommanderMessageReceiver,
                new IntentFilter(AsciiCommander.STATE_CHANGED_NOTIFICATION));

        displayReaderState();
    }

    @Override
    public void onPause() {
        super.onPause();
        mModel.setEnabled(false);

        // Unregister to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(AddData.this.getContext()).unregisterReceiver(mCommanderMessageReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        mModel.setEnabled(false);

        // Unregister to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(AddData.this.getContext()).unregisterReceiver(mCommanderMessageReceiver);
    }


}
