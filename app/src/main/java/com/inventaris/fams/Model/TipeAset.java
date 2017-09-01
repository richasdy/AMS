package com.inventaris.fams.Model;

/**
 * Created by mwildani on 29/08/2017.
 */

public class TipeAset {
    private String id, name, tipegeneral;

    public TipeAset(String id, String name, String tipegeneral) {
        this.id = id;
        this.name = name;
        this.tipegeneral = tipegeneral;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTipegeneral() {
        return tipegeneral;
    }

    public void setTipegeneral(String tipegeneral) {
        this.tipegeneral = tipegeneral;
    }
}
