package com.inventaris.fams.Fragment;


import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.inventaris.fams.Adapter.AdapterScannedCode;
import com.inventaris.fams.Config;
import com.inventaris.fams.FamsModel;
import com.inventaris.fams.HalamanUtama;
import com.inventaris.fams.Model.Lokasi;
import com.inventaris.fams.Model.ScannedCode;
import com.inventaris.fams.Model.TipeAset;
import com.inventaris.fams.R;
import com.inventaris.fams.Utils.ModelBase;
import com.inventaris.fams.Utils.WeakHandler;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanData extends Fragment {
    @BindView(R.id.btn_scan)
    Button cari;
    @BindView(R.id.txtStatus)
    TextView status;
    @BindView(R.id.listScanner)
    ListView list;

    AdapterScannedCode adapter;
    private ArrayList<ScannedCode> dataCode = new ArrayList<>();

    MaterialDialog dialog;


    public ScanData() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scan_data, container, false);
        ButterKnife.bind(this, v);

        new Prefs.Builder()
                .setContext(ScanData.this.getContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getActivity().getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        adapter = new AdapterScannedCode(dataCode, ScanData.this.getContext());
        list.setAdapter(adapter);

        status.setText(Prefs.getString("status", ""));
        boolean reader = Prefs.getBoolean("reader", false);
        if (reader) {
            showScanButton();
        } else {
            hideScanButton();
        }


        return v;
    }

    public void setStatus(String pesan) {
        status.setText(pesan);
    }

    @OnClick(R.id.btn_scan)
    void scanDong() {
        ((HalamanUtama) getActivity()).doScan();
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

    public void showScanButton() {
        cari.setVisibility(View.VISIBLE);
    }

    public void hideScanButton() {
        cari.setVisibility(View.GONE);
    }

    public void onNewData(String kode) {
        if (kode.startsWith("EPC")) {
            cariData(kode.substring(5));
        } else if (kode.startsWith("BC")) {
            cariData(kode.substring(4));
        }
    }

    private void cariData(final String epcbc) {
//        showDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_SEARCH_ASSET + epcbc,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject dataJson = new JSONObject(response);
                            if (dataJson.getString("status").equals("success")) {
                                JSONObject data = dataJson.getJSONObject("data");
                                String dataTahun = data.getString("year");
                                JSONObject lokasi = data.getJSONObject("location");
                                Lokasi datalokasi = new Lokasi(lokasi.getString("id"), lokasi.getString("name")
                                        , lokasi.getString("id_gedung"));
                                JSONObject tipeaset = data.getJSONObject("type_detail");
                                TipeAset dataTipeAset = new TipeAset(tipeaset.getString("id"), tipeaset.getString("name"),
                                        "");
                                ScannedCode scannedCode = new ScannedCode(epcbc, dataTahun, datalokasi, dataTipeAset);
                                addData(epcbc, scannedCode);
                                adapter.notifyDataSetChanged();
                                scrollListViewToBottom();
//                                dismissDialog();
                            } else {
                                if (dataJson.getString("message").equals("Asset Not Found")) {
                                    ScannedCode scannedCode = new ScannedCode(epcbc);
                                    addData(epcbc, scannedCode);
                                    adapter.notifyDataSetChanged();
                                    scrollListViewToBottom();
                                }
//                                dismissDialog();
                            }

                        } catch (Exception e) {
//                            dismissDialog();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
//                    dismissDialog();
                    Toast.makeText(getContext(), "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap();
                try {
                    headers.put("Authorization", Prefs.getString(Config.TOKEN_SHARED_PREF, ""));
                    return headers;
                } catch (Exception e) {
//                    dismissDialog();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

    private void showDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(ScanData.this.getContext())
                .title("Mencari Data dari server")
                .progress(true, 0)
                .content("Mohon tunggu !");

        dialog = builder.build();
        dialog.show();
    }

    private void dismissDialog() {
        dialog.dismiss();
    }

    private void scrollListViewToBottom() {
        list.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                list.setSelection(adapter.getCount() - 1);
            }
        });
    }

    private void addData(String kode, ScannedCode data) {
        if (dataCode.size() == 0) {
            dataCode.add(data);
        } else {
            boolean ada = false;
            for (ScannedCode data1 : dataCode) {
                if (data1.getCode().equals(kode)) {
                    ada = true;
                    break;
                }
            }
            if (!ada) {
                dataCode.add(data);
            }
        }
    }

}
