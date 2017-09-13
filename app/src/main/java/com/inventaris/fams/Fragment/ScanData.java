package com.inventaris.fams.Fragment;


import android.content.ContextWrapper;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
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
import com.farbod.labelledspinner.LabelledSpinner;
import com.inventaris.fams.Adapter.AdapterScannedCode;
import com.inventaris.fams.Config;
import com.inventaris.fams.FamsModel;
import com.inventaris.fams.HalamanInputData;
import com.inventaris.fams.HalamanUtama;
import com.inventaris.fams.Model.Lokasi;
import com.inventaris.fams.Model.ScannedCode;
import com.inventaris.fams.Model.TipeAset;
import com.inventaris.fams.R;
import com.inventaris.fams.Utils.ModelBase;
import com.inventaris.fams.Utils.WeakHandler;
import com.pixplicity.easyprefs.library.Prefs;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
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
public class ScanData extends Fragment implements LabelledSpinner.OnItemChosenListener {
    @BindView(R.id.btn_scan)
    Button cari;
    @BindView(R.id.txtStatus)
    TextView status;
    @BindView(R.id.listScanner)
    ListView list;
    @BindView(R.id.txtJumlahItem)
    TextView jumItem;
    @BindView(R.id.spinnerKategori)
    LabelledSpinner spinnerKategori;
    @BindView(R.id.edCari)
    MaterialEditText edCari;
    @BindView(R.id.layoutFilter)
    View layout;
    @BindView(R.id.layoutIcon)
    View layoutIcon;
    @BindView(R.id.layoututama)
    View layoututama;
    @BindView(R.id.showFilter)
    ImageView showfilter;

    AdapterScannedCode adapter;

    MaterialDialog dialog;

    boolean show = false;

    ArrayList<String> dataKlasifikasi = new ArrayList<>();

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

        adapter = new AdapterScannedCode(ScanData.this.getContext());
        list.setAdapter(adapter);
        jumItem.setVisibility(View.GONE);
        layoutIcon.setVisibility(View.GONE);
        spinnerKategori.setVisibility(View.GONE);
        edCari.setVisibility(View.GONE);
        layout.setVisibility(View.GONE);
        showfilter.setImageResource(R.drawable.ic_showarrow);

        status.setText(Prefs.getString("status", ""));
        getDataKlasifikasi();
        boolean reader = Prefs.getBoolean("reader", false);
        spinnerKategori.setOnItemChosenListener(this);
        if (reader) {
            showScanButton();
        } else {
            hideScanButton();
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getItem(position).isNewData()) {
                    Intent intent = new Intent(ScanData.this.getContext(), HalamanInputData.class)
                            .putExtra("epcCode", adapter.getItem(position).getCode());
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Halaman Edit", Toast.LENGTH_SHORT).show();
                }
            }
        });
        edCari.addTextChangedListener(cariListener);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                jumItem.setText("Total Item : " + Integer.toString(adapter.getCount()));
                jumItem.setVisibility(View.VISIBLE);
                super.onChanged();
            }
        });
        return v;
    }

    TextWatcher cariListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.filterbyCode(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void setStatus(String pesan) {
        status.setText(pesan);
    }

    @OnClick(R.id.layoututama)
    void clickarrow() {
        if (!show) {
            show = true;
            showfilter.setImageResource(R.drawable.ic_hidearrow);
            layoutIcon.setVisibility(View.VISIBLE);
            edCari.setVisibility(View.VISIBLE);
        } else {
            show = false;
            showfilter.setImageResource(R.drawable.ic_showarrow);
            layoutIcon.setVisibility(View.GONE);
            edCari.setVisibility(View.GONE);
            spinnerKategori.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.btn_scan)
    void scanDong() {
        ((HalamanUtama) getActivity()).doScan();
    }

    @OnClick(R.id.btnCari)
    void cariClick() {
        adapter.refreshList();
        edCari.setVisibility(View.VISIBLE);
        spinnerKategori.setVisibility(View.GONE);
    }


    @OnClick(R.id.btnKategori)
    void kategoriClick() {
        spinnerKategori.setVisibility(View.VISIBLE);
        edCari.setVisibility(View.GONE);
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
        layout.setVisibility(View.VISIBLE);
    }

    public void hideScanButton() {
        cari.setVisibility(View.GONE);
        layout.setVisibility(View.GONE);
    }

    public void onNewData(String kode) {
        cariData(kode);
    }

    private void cariData(final String epcbc) {
        String kode = "";
        if (epcbc.startsWith("EPC")) {
            kode = epcbc.substring(5);
        } else if (epcbc.startsWith("BC")) {
            kode = epcbc.substring(4);
        }
//        showDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_SEARCH_ASSET + kode,
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
                                JSONObject tipeGeneral = tipeaset.getJSONObject("type_parent");
                                TipeAset dataTipeAset = new TipeAset(tipeaset.getString("id"), tipeaset.getString("name"),
                                        tipeGeneral.getString("name"));
                                ScannedCode scannedCode = new ScannedCode(data.getString("id"), dataTahun, datalokasi, dataTipeAset);
                                addData(data.getString("id"), scannedCode);
                                jumItem.setText("Total Item : " + Integer.toString(adapter.getCount()));
                                jumItem.setVisibility(View.VISIBLE);
                                scrollListViewToBottom();
//                                dismissDialog();
                            } else {
                                if (dataJson.getString("message").equals("Asset Not Found")) {
                                    ScannedCode scannedCode = new ScannedCode(epcbc);
                                    addData(epcbc, scannedCode);
                                    jumItem.setText("Total Item : " + Integer.toString(adapter.getCount()));
                                    jumItem.setVisibility(View.VISIBLE);
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

    private void showDialog(String title) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(ScanData.this.getContext())
                .title(title)
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
        if (adapter.getDataCodeOrigin().size() == 0) {
            adapter.addData(data);
        } else {
            boolean ada = false;
            for (ScannedCode data1 : adapter.getDataCodeOrigin()) {
                if (data1.getCode().equals(kode)) {
                    ada = true;
                    break;
                }
            }
            if (!ada) {
                adapter.addData(data);
            }
        }
    }

    @Override
    public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
        switch (labelledSpinner.getId()) {
            case R.id.spinnerKategori:
                adapter.filterbyklasifikasi(adapterView.getItemAtPosition(position).toString());
                break;
            // If you have multiple LabelledSpinners, you can add more cases here
        }
    }

    @Override
    public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

    }

    private void getDataKlasifikasi() {
        showDialog("Mengambil Data");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_GET_KLASIFIKASI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject dataJson = new JSONObject(response);
                            JSONArray data = dataJson.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject isi = data.getJSONObject(i);
                                dataKlasifikasi.add(isi.getString("name"));
                            }
                            spinnerKategori.setItemsArray(dataKlasifikasi);
                            dismissDialog();
                        } catch (Exception e) {
                            dismissDialog();
                            Toast.makeText(ScanData.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissDialog();
                    Toast.makeText(ScanData.this.getContext(), "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
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
                    dismissDialog();
                    Toast.makeText(ScanData.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ScanData.this.getContext());
        requestQueue.add(stringRequest);
    }
}
