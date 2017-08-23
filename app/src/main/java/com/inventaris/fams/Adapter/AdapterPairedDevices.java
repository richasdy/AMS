package com.inventaris.fams.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inventaris.fams.Model.PairedDevice;
import com.inventaris.fams.R;

import java.util.ArrayList;

/**
 * Created by mwildani on 23/08/2017.
 */

public class AdapterPairedDevices extends BaseAdapter {
    private ArrayList<PairedDevice> dataDevice = new ArrayList<>();
    private Context context;

    public AdapterPairedDevices(ArrayList<PairedDevice> dataDevice, Context context) {
        this.dataDevice = dataDevice;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dataDevice.size();
    }

    @Override
    public PairedDevice getItem(int position) {
        return dataDevice.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_paireddevices, parent, false);

        TextView deviceName = (TextView) view.findViewById(R.id.txtDeviceName);
        TextView deviceAddress = (TextView) view.findViewById(R.id.txtDeviceAddress);

        deviceName.setText("Device name : " + dataDevice.get(position).getDeviceName());
        deviceAddress.setText("Device Address : " +dataDevice.get(position).getDeviceHardwareAddress());

        return view;
    }
}
