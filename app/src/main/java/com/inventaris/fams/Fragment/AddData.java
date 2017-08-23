package com.inventaris.fams.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inventaris.fams.CariDevice;
import com.inventaris.fams.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddData extends Fragment {


    public AddData() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_data, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @OnClick(R.id.btn_cari)
    void cari() {
        startActivity(new Intent(AddData.this.getContext(), CariDevice.class));
    }

}
