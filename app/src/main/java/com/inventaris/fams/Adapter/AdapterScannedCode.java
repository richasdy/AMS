package com.inventaris.fams.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inventaris.fams.Model.ScannedCode;
import com.inventaris.fams.R;

import java.util.ArrayList;

/**
 * Created by mwildani on 07/09/2017.
 */

public class AdapterScannedCode extends BaseAdapter {
    private ArrayList<ScannedCode> dataCode = new ArrayList<>();
    private Context context;

    public AdapterScannedCode(ArrayList<ScannedCode> dataCode, Context context) {
        this.dataCode = dataCode;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dataCode.size();
    }

    @Override
    public ScannedCode getItem(int position) {
        return dataCode.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = null;

        if (getItem(position).isNewData()) {
            view = inflater.inflate(R.layout.list_row_newasset, parent, false);
            TextView code = (TextView) view.findViewById(R.id.txtCode);
            code.setText(getItem(position).getCode());
        } else {
            view = inflater.inflate(R.layout.list_row_scanneddevice, parent, false);
            TextView code = (TextView) view.findViewById(R.id.txtCode);
            TextView tahun = (TextView) view.findViewById(R.id.txtYear);
            TextView lokasi = (TextView) view.findViewById(R.id.txtLocation);
            TextView tipeAset = (TextView) view.findViewById(R.id.txtTipeAset);

            code.setText(getItem(position).getCode());
            tahun.setText("Tahun : " + getItem(position).getTahun());
            lokasi.setText("Lokasi : " + getItem(position).getLokasi().getName());
            tipeAset.setText("Tipe Aset : " + getItem(position).getTipeAset().getName());
        }

        return view;
    }
}
