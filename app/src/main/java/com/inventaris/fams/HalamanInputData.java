package com.inventaris.fams;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.farbod.labelledspinner.LabelledSpinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HalamanInputData extends AppCompatActivity implements LabelledSpinner.OnItemChosenListener {
    String sumber[] = {"Hibah", "Logistik"};
    String tempat[] = {"Ruang Pimpinan", "Ruang Administrasi", "Ruang Pelatihan", "Ruang Riset"};
    String tipe[] = {"Lemari", "Meja Kerja", "Meja Layanan", "AC", "Komputer", "Printer", "Bangku Kuliah"};

    String asal, lokasi, type, kodeEpc;

//    ArrayAdapter<String> arraySumber = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sumber);
//    ArrayAdapter<String> arrayTempat = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tempat);
//    ArrayAdapter<String> arrayTipe = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tipe);

    @BindView(R.id.txtEpc)
    TextView txtepc;
    @BindView(R.id.spinnerSource)
    LabelledSpinner spinnerSource;
    @BindView(R.id.spinnerLocation)
    LabelledSpinner spinnerLocation;
    @BindView(R.id.spinnerType)
    LabelledSpinner spinnerType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_input_data);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        spinnerSource.setItemsArray(sumber);
        spinnerLocation.setItemsArray(tempat);
        spinnerType.setItemsArray(tipe);

        spinnerLocation.setOnItemChosenListener(this);
        spinnerSource.setOnItemChosenListener(this);
        spinnerType.setOnItemChosenListener(this);

        Bundle b = getIntent().getExtras();
        kodeEpc = b.getString("epcCode");
        txtepc.setText("EPC : " + b.getString("epcCode"));
    }

    @OnClick(R.id.btnSubmit)
    void submit() {
        Toast.makeText(this, asal, Toast.LENGTH_SHORT).show();
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
                break;
            case R.id.spinnerType:
                type = adapterView.getItemAtPosition(position).toString();
                break;
            // If you have multiple LabelledSpinners, you can add more cases here
        }
    }

    @Override
    public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

    }
}
