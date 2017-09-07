package com.inventaris.fams.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.inventaris.fams.Config;
import com.inventaris.fams.HalamanInputData;
import com.inventaris.fams.Model.Lokasi;
import com.inventaris.fams.Model.TipeAset;
import com.inventaris.fams.R;
import com.pixplicity.easyprefs.library.Prefs;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchData extends Fragment {
    @BindView(R.id.edCari)
    MaterialEditText edCari;
    @BindView(R.id.cardODP)
    CardView card;
    @BindView(R.id.txtYear)
    TextView tahun;
    @BindView(R.id.txtLocation)
    TextView lokasii;
    @BindView(R.id.txtTipeAset)
    TextView tipeAset;
    @BindView(R.id.btnCari)
    Button cari;

    String dataTahun;
    Lokasi datalokasi;
    TipeAset dataTipeAset;

    MaterialDialog dialog;

    public SearchData() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search_data, container, false);
        ButterKnife.bind(this, v);

        card.setVisibility(View.GONE);
        cari.setEnabled(false);

        edCari.addTextChangedListener(cariListener);


        return v;
    }

    TextWatcher cariListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (edCari.getText().toString().length() > 0) {
                cari.setEnabled(true);
            } else {
                cari.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @OnClick(R.id.btnCari)
    void cari() {
        cariData(edCari.getText().toString());
    }

    private void cariData(String epcbc) {
        showDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_SEARCH_ASSET + epcbc,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject dataJson = new JSONObject(response);
                            if (dataJson.getString("status").equals("success")) {
                                JSONObject data = dataJson.getJSONObject("data");
                                dataTahun = data.getString("year");
                                JSONObject lokasi = data.getJSONObject("location");
                                datalokasi = new Lokasi(lokasi.getString("id"), lokasi.getString("name")
                                        , lokasi.getString("id_gedung"));
                                JSONObject tipeaset = data.getJSONObject("type_detail");
                                dataTipeAset = new TipeAset(tipeaset.getString("id"), tipeaset.getString("name"),
                                        "");
                                tahun.setText("Tahun : " + dataTahun);
                                lokasii.setText("Lokasi : " + datalokasi.getName());
                                tipeAset.setText("Tipe : " + dataTipeAset.getName());
                                card.setVisibility(View.VISIBLE);
                                dismissDialog();
                            } else {
                                dismissDialog();
                                card.setVisibility(View.GONE);
                                Toast.makeText(getContext(), dataJson.getString("message"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            dismissDialog();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissDialog();
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
                    dismissDialog();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

    private void showDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(SearchData.this.getContext())
                .title("Mencari Data dari server")
                .progress(true, 0)
                .content("Mohon tunggu !");

        dialog = builder.build();
        dialog.show();
    }

    private void dismissDialog() {
        dialog.dismiss();
    }
}
