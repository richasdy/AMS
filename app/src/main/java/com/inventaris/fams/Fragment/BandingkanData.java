package com.inventaris.fams.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.inventaris.fams.HalamanInputData;
import com.inventaris.fams.Model.Lokasi;
import com.inventaris.fams.Model.ScannedCode;
import com.inventaris.fams.Model.TipeAset;
import com.inventaris.fams.R;
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
public class BandingkanData extends Fragment implements LabelledSpinner.OnItemChosenListener {
    @BindView(R.id.ListMasterData)
    ListView listMaster;
    @BindView(R.id.ListScanner)
    ListView listScanner;
    @BindView(R.id.spinnerLokasi)
    LabelledSpinner spinnerLokasi;
    @BindView(R.id.txtTotalMasterData)
    TextView totalMaster;
    @BindView(R.id.txtTotalScanData)
    TextView totalScan;

    String lokasi = "";
    String idLokasi = "";

    ArrayList<Lokasi> dataLokasi = new ArrayList<>();

    private MaterialDialog dialog;
    private AdapterScannedCode adapter, adapter1;

    public BandingkanData() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bandingkan_data, container, false);
        ButterKnife.bind(this, view);

        adapter = new AdapterScannedCode(BandingkanData.this.getContext());
        adapter1 = new AdapterScannedCode(BandingkanData.this.getContext());
        listMaster.setAdapter(adapter);
        listScanner.setAdapter(adapter1);

        spinnerLokasi.setOnItemChosenListener(this);
        getDataLocation();

        return view;
    }

    @OnClick(R.id.btnLihatData)
    void lihatData() {
        if (!lokasi.equals("")) {
            adapter.refreshDataOrigin();
            ambilDataMasterbyLokasi();
        } else {
            Toast.makeText(BandingkanData.this.getContext(), "Tolong pilih Lokasi dahulu !", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
        switch (labelledSpinner.getId()) {
            case R.id.spinnerLokasi:
                lokasi = adapterView.getItemAtPosition(position).toString();
                for (Lokasi lokasi1 : dataLokasi) {
                    if (lokasi1.getName().equals(lokasi)) {
                        idLokasi = lokasi1.getId();
                        break;
                    }
                }
                break;
            // If you have multiple LabelledSpinners, you can add more cases here
        }
    }

    @Override
    public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

    }

    private void showDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(BandingkanData.this.getContext())
                .title("Mengambil Data dari server")
                .progress(true, 0)
                .content("Mohon tunggu !");

        dialog = builder.build();
        dialog.show();
    }

    private void dismissDialog() {
        dialog.dismiss();
    }

    private void getDataLocation() {
        showDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_GET_LOCATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray data = new JSONArray(response);
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject isi = data.getJSONObject(i);
                                Lokasi lokasi = new Lokasi(isi.getString("id"),
                                        isi.getString("name"), isi.getString("id_gedung"));
                                dataLokasi.add(lokasi);
                            }
                            ArrayList<String> nama = new ArrayList<>();
                            for (Lokasi lokasi : dataLokasi) {
                                nama.add(lokasi.getName());
                            }
                            spinnerLokasi.setItemsArray(nama);
                            dismissDialog();
                        } catch (Exception e) {
                            dismissDialog();
                            Toast.makeText(BandingkanData.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissDialog();
                    Toast.makeText(BandingkanData.this.getContext(), "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(BandingkanData.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(BandingkanData.this.getContext());
        requestQueue.add(stringRequest);
    }

    private void ambilDataMasterbyLokasi() {
        showDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_GET_ALL_ASSET,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject dataJson = new JSONObject(response);
                            String status = dataJson.getString("status");
                            if (status.equals("success")) {
                                JSONArray data = dataJson.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject isi = data.getJSONObject(i);
                                    String kode = isi.getString("id");
                                    if (isi.getString("id_location").equals(idLokasi)) {
                                        cariDataMaster(kode);
                                    }
                                }
                                ArrayList<String> nama = new ArrayList<>();
                                for (Lokasi lokasi : dataLokasi) {
                                    nama.add(lokasi.getName());
                                }
                                spinnerLokasi.setItemsArray(nama);
                            }
                            dismissDialog();
                        } catch (Exception e) {
                            dismissDialog();
                            Toast.makeText(BandingkanData.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissDialog();
                    Toast.makeText(BandingkanData.this.getContext(), "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(BandingkanData.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(BandingkanData.this.getContext());
        requestQueue.add(stringRequest);
    }

    private void cariDataMaster(final String kode) {
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
                                totalMaster.setText("Total Item : " + Integer.toString(adapter.getCount()));
                                scrollListViewToBottom();
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
                                addDataScan(data.getString("id"), scannedCode);
                                totalScan.setText("Total Item : " + Integer.toString(adapter1.getCount()));
                                scrollListViewToBottom1();
//                                dismissDialog();
                            } else {
                                if (dataJson.getString("message").equals("Asset Not Found")) {
                                    ScannedCode scannedCode = new ScannedCode(epcbc);
                                    addDataScan(epcbc, scannedCode);
                                    totalScan.setText("Total Item : " + Integer.toString(adapter1.getCount()));
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

    public void onNewData(String kode) {
        cariData(kode);
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

    private void addDataScan(String kode, ScannedCode data) {
        if (adapter1.getDataCodeOrigin().size() == 0) {
            adapter1.addData(data);
        } else {
            boolean ada = false;
            for (ScannedCode data1 : adapter1.getDataCodeOrigin()) {
                if (data1.getCode().equals(kode)) {
                    ada = true;
                    break;
                }
            }
            if (!ada) {
                adapter1.addData(data);
            }
        }
    }

    private void scrollListViewToBottom() {
        listMaster.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listMaster.setSelection(adapter.getCount() - 1);
            }
        });
    }

    private void scrollListViewToBottom1() {
        listScanner.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listScanner.setSelection(adapter1.getCount() - 1);
            }
        });
    }
}
