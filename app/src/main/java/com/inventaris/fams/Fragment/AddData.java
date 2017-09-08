package com.inventaris.fams.Fragment;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.inventaris.fams.HalamanInputData;
import com.inventaris.fams.HalamanUtama;
import com.inventaris.fams.Model.PairedDevice;
import com.inventaris.fams.R;
import com.inventaris.fams.Utils.BluetoothConnector;
import com.pixplicity.easyprefs.library.Prefs;

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


    // The list of results from actions
    private ArrayAdapter<String> mResultsArrayAdapter;
    private ListView mResultsListView;
    private ArrayAdapter<String> mBarcodeResultsArrayAdapter;
    private ListView mBarcodeResultsListView;


    public AddData() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_data, container, false);
        ButterKnife.bind(this, view);

        new Prefs.Builder()
                .setContext(AddData.this.getContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getActivity().getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        mResultsArrayAdapter = new ArrayAdapter<String>(AddData.this.getContext(), android.R.layout.simple_list_item_1);
        mBarcodeResultsArrayAdapter = new ArrayAdapter<String>(AddData.this.getContext(), android.R.layout.simple_list_item_1);

        mResultsListView = (ListView) view.findViewById(R.id.resultListView);
        mResultsListView.setAdapter(mResultsArrayAdapter);
        mResultsListView.setFastScrollEnabled(true);

        mBarcodeResultsListView = (ListView) view.findViewById(R.id.barcodeListView);
        mBarcodeResultsListView.setAdapter(mBarcodeResultsArrayAdapter);
        mBarcodeResultsListView.setFastScrollEnabled(true);

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

        status.setText(Prefs.getString("status", ""));

        boolean reader = Prefs.getBoolean("reader", false);
        if (reader) {
            showScanButton();
        } else {
            hideScanButton();
        }

        return view;
    }


    public void addBarcodeData(String pesan) {
        mBarcodeResultsArrayAdapter.add(pesan);
        scrollBarcodeListViewToBottom();
    }

    public void addRFIDData(String pesan) {
        mResultsArrayAdapter.add(pesan);
        scrollResultsListViewToBottom();
    }

    public void setStatus(String pesan) {
        status.setText(pesan);
    }

    public void showScanButton() {
        cari.setVisibility(View.VISIBLE);
    }

    public void hideScanButton() {
        cari.setVisibility(View.GONE);
    }

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


    @OnClick(R.id.btn_cari)
    void cari() {
        if (cari.getText().toString().equals("Scan")) {
            Log.i("scan", "scan");
            ((HalamanUtama) getActivity()).doScan();
        } else {
            ((HalamanUtama) getActivity()).finDevice();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        status.setText(Prefs.getString("status", ""));
        boolean reader = Prefs.getBoolean("reader", false);
        if (reader) {
            showScanButton();
        } else {
            hideScanButton();
        }
    }

}
