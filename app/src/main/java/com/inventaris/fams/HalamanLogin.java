package com.inventaris.fams;

import android.content.ContextWrapper;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

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
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class HalamanLogin extends AppCompatActivity {
    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_login);

        //bind view
        ButterKnife.bind(this);

        //initialize shared pref
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        username = "satria@btp.or.id";
        password = "satria";
    }

    @OnClick(R.id.btn_login)
    void doLogin() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.URL_GENERATE_TOKEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject data = new JSONObject(response);
                            String token = data.getString("token_type") + " " + data.getString("access_token");
//                            Toast.makeText(HalamanLogin.this, token, Toast.LENGTH_SHORT).show();
                            Prefs.putString(Config.TOKEN_SHARED_PREF, token);
                            startActivity(new Intent(getApplicationContext(), HalamanUtama.class));
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(HalamanLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    Toast.makeText(HalamanLogin.this, "Terjadi kesalah pada koneksi intenrnet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put("grant_type", Config.GRANT_TYPE);
                    params.put("client_id", Config.CLIENT_ID);
                    params.put("client_secret", Config.CLIENT_SECRET);
                    params.put("username", username);
                    params.put("password", password);
                    return params;
                } catch (Exception e) {
                    Toast.makeText(HalamanLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return params;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
