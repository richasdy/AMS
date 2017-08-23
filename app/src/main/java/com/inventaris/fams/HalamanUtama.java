package com.inventaris.fams;

import android.content.ContextWrapper;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pixplicity.easyprefs.library.Prefs;

public class HalamanUtama extends AppCompatActivity {
    private String stat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_utama);

        //initialize shared pref
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        stat = Prefs.getString(Config.TOKEN_SHARED_PREF, "null");

        if (stat.equals("null")) {
            startActivity(new Intent(getApplicationContext(), HalamanLogin.class));
            finish();
        }
    }
}
