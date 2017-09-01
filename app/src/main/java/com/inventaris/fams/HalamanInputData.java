package com.inventaris.fams;

import android.content.ContextWrapper;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.inventaris.fams.Model.Lokasi;
import com.inventaris.fams.Model.TipeAset;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HalamanInputData extends AppCompatActivity implements LabelledSpinner.OnItemChosenListener {
    String sumber[] = {"Hibah", "Logistik"};
//    String tempat[] = {"Ruang Pimpinan", "Ruang Administrasi", "Ruang Pelatihan", "Ruang Riset"};
//    String tipe[] = {"Lemari", "Meja Kerja", "Meja Layanan", "AC", "Komputer", "Printer", "Bangku Kuliah"};

    ArrayList<Lokasi> dataLokasi = new ArrayList<>();
    ArrayList<TipeAset> dataAset = new ArrayList<>();

    String asal, lokasi, type, jenis, idLokasi, idTipeAset, kode;

    MaterialDialog dialog;

    @BindView(R.id.txtEpc)
    TextView txtepc;
    @BindView(R.id.spinnerSource)
    LabelledSpinner spinnerSource;
    @BindView(R.id.spinnerLocation)
    LabelledSpinner spinnerLocation;
    @BindView(R.id.spinnerType)
    LabelledSpinner spinnerType;
    @BindView(R.id.edTahun)
    TextView tahun;

    Config config = new Config();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_input_data);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        spinnerSource.setItemsArray(sumber);
//        spinnerLocation.setItemsArray(tempat);
//        spinnerType.setItemsArray(tipe);

        spinnerLocation.setOnItemChosenListener(this);
        spinnerSource.setOnItemChosenListener(this);
        spinnerType.setOnItemChosenListener(this);

        Bundle b = getIntent().getExtras();
        txtepc.setText(b.getString("epcCode"));
        if (b.getString("epcCode").startsWith("EP")) {
            jenis = "RFID";
            kode = b.getString("epcCode").substring(5);
        } else {
            jenis = "BC";
            kode = b.getString("epcCode").substring(4);
        }

        getDataLocation();

    }

    @OnClick(R.id.btnSubmit)
    void submit() {
        if (!tahun.getText().toString().equals("")) {
            if (jenis.equals("RFID")) {
                createAset(config.url_create_asset_withRFID(kode,
                        asal.substring(0, 1),
                        tahun.getText().toString(),
                        idLokasi, idTipeAset));
            } else {
                createAset(config.url_create_asset_withBarcode(kode,
                        asal.substring(0, 1),
                        tahun.getText().toString(),
                        idLokasi, idTipeAset));
            }
        } else {
            Toast.makeText(this, "Tolong isi tahun terlebih dahulu !", Toast.LENGTH_SHORT).show();
            tahun.requestFocus();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                break;
        }
        return true;
    }

    @Override
    public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
        switch (labelledSpinner.getId()) {
            case R.id.spinnerSource:
                asal = adapterView.getItemAtPosition(position).toString();
                break;
            case R.id.spinnerLocation:
                lokasi = adapterView.getItemAtPosition(position).toString();
                for (Lokasi lokasi1 : dataLokasi) {
                    if (lokasi1.getName().equals(lokasi)) {
                        idLokasi = lokasi1.getId();
                        break;
                    }
                }
                break;
            case R.id.spinnerType:
                type = adapterView.getItemAtPosition(position).toString();
                for (TipeAset tipeAset : dataAset) {
                    if (tipeAset.getName().equals(type)) {
                        idTipeAset = tipeAset.getId();
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

    private void createAset(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(HalamanInputData.this, "created", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissDialog();
                    Toast.makeText(HalamanInputData.this, "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getDataLocation() {
        showDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_GET_LOCATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject dataJson = new JSONObject(response);
                            JSONArray data = dataJson.getJSONArray("data");
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
                            spinnerLocation.setItemsArray(nama);
                            getDataTipeAset();
                        } catch (Exception e) {
                            dismissDialog();
                            Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissDialog();
                    Toast.makeText(HalamanInputData.this, "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getDataTipeAset() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_GET_ASSET,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject dataJson = new JSONObject(response);
                            JSONArray data = dataJson.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject isi = data.getJSONObject(i);
                                TipeAset tipeAset = new TipeAset(isi.getString("id"), isi.getString("name"),
                                        isi.getString("type_general"));
                                dataAset.add(tipeAset);
                            }
                            ArrayList<String> nama = new ArrayList<>();
                            for (TipeAset aset : dataAset) {
                                nama.add(aset.getName());
                            }
                            spinnerType.setItemsArray(nama);
                            dismissDialog();
                        } catch (Exception e) {
                            dismissDialog();
                            Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissDialog();
                    Toast.makeText(HalamanInputData.this, "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(HalamanInputData.this)
                .title("Mengambil Data dari server")
                .progress(true, 0)
                .content("Mohon tunggu !");

        dialog = builder.build();
        dialog.show();
    }

    private void dismissDialog() {
        dialog.dismiss();
    }
}
