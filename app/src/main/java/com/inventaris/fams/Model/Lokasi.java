package com.inventaris.fams.Model;

/**
 * Created by mwildani on 29/08/2017.
 */

public class Lokasi {
    private String id, name,idGedung ;

    public Lokasi(String id, String name, String idGedung) {
        this.id = id;
        this.name = name;
        this.idGedung = idGedung;
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

    public String getIdGedung() {
        return idGedung;
    }

    public void setIdGedung(String idGedung) {
        this.idGedung = idGedung;
    }
}
