package com.inventaris.fams.Model;

/**
 * Created by mwildani on 07/09/2017.
 */

public class ScannedCode {
    private String tahun, code;
    private Lokasi lokasi;
    private TipeAset tipeAset;
    private boolean isNewData = false;

    public ScannedCode(String code, String tahun, Lokasi lokasi, TipeAset tipeAset) {
        this.code = code;
        this.tahun = tahun;
        this.lokasi = lokasi;
        this.tipeAset = tipeAset;
        isNewData = false;
    }

    public ScannedCode(String code) {
        this.code = code;
        isNewData = true;
    }

    public String getTahun() {
        return tahun;
    }

    public void setTahun(String tahun) {
        this.tahun = tahun;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Lokasi getLokasi() {
        return lokasi;
    }

    public void setLokasi(Lokasi lokasi) {
        this.lokasi = lokasi;
    }

    public TipeAset getTipeAset() {
        return tipeAset;
    }

    public void setTipeAset(TipeAset tipeAset) {
        this.tipeAset = tipeAset;
    }

    public boolean isNewData() {
        return isNewData;
    }

    public void setNewData(boolean newData) {
        isNewData = newData;
    }
}
