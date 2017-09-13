package com.inventaris.fams.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.inventaris.fams.Model.ScannedCode;
import com.inventaris.fams.R;

import java.util.ArrayList;

/**
 * Created by mwildani on 07/09/2017.
 */

public class AdapterScannedCode extends BaseAdapter {
    private ArrayList<ScannedCode> dataCode = new ArrayList<>();
    private ArrayList<ScannedCode> dataCodeOrigin = new ArrayList<>();
    private Context context;

    public AdapterScannedCode(Context context) {
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
            TextView code = (TextView) view.findViewById(R.id.txtKode);
            TextView tahun = (TextView) view.findViewById(R.id.txtYear);
            TextView lokasi = (TextView) view.findViewById(R.id.txtLocation);
            TextView tipeAset = (TextView) view.findViewById(R.id.txtTipeAset);

            code.setText("ID : " + getItem(position).getCode());
            tahun.setText("Tahun : " + getItem(position).getTahun());
            lokasi.setText("Lokasi : " + getItem(position).getLokasi().getName());
            tipeAset.setText("Klasifikasi : " + getItem(position).getTipeAset().getTipegeneral() + ", Nama : " + getItem(position).getTipeAset().getName());
        }

        return view;
    }

    public void filterbyCode(String kode) {
        dataCode.clear();
        for (ScannedCode dataAsli : dataCodeOrigin) {
            if (dataAsli.getCode().toLowerCase().contains(kode.toLowerCase())) {
                dataCode.add(dataAsli);
            }
        }
        notifyDataSetChanged();
    }

    public void filterbyklasifikasi(String klasifikasi) {
        dataCode.clear();
        for (ScannedCode dataAsli : dataCodeOrigin) {
            if (!dataAsli.isNewData()) {
                if (dataAsli.getTipeAset().getTipegeneral().equals(klasifikasi)) {
                    dataCode.add(dataAsli);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void refreshDataOrigin() {
        dataCode.clear();
        dataCodeOrigin.clear();
        notifyDataSetChanged();
    }

    public void refreshList() {
        dataCode.clear();
        for (ScannedCode dataAsli : dataCodeOrigin) {
            dataCode.add(dataAsli);

        }
        notifyDataSetChanged();
    }

    public ArrayList<ScannedCode> getDataCode() {
        return dataCode;
    }

    public ArrayList<ScannedCode> getDataCodeOrigin() {
        return dataCodeOrigin;
    }

    public void addData(ScannedCode scannedCode) {
        dataCode.add(scannedCode);
        dataCodeOrigin.add(scannedCode);
        notifyDataSetChanged();
    }

}
