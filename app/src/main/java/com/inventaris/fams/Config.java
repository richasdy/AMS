package com.inventaris.fams;

import java.util.UUID;

/**
 * Created by mwildani on 23/08/2017.
 */

public class Config {
    public final static String CLIENT_SECRET = "krrUUmbDtyxGVew2d1qaoAVaBPEZH2tguhRIN06o";
    public final static String TOKEN_SHARED_PREF = "token";
    public final static String URL_GENERATE_TOKEN = "http://128.199.115.183:8002/oauth/token";
    public final static String GRANT_TYPE = "password";
    public final static String CLIENT_ID = "4";
    public static final UUID RFIDUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String URL_GET_LOCATION = "http://128.199.115.183:8002/api/index-location";
    public static final String URL_GET_ASSET = "http://128.199.115.183:8002/api/index-type-detail";

    public Config() {
    }

    public String url_create_asset_withRFID(String epc, String asset_origin, String year, String idLokasi, String idTipeAset) {
        return "http://128.199.115.183:8002/api/create-asset?tag_rfid=" + epc
                + "&barcode=&asset_origin=" + asset_origin
                + "&year=" + year
                + "&id_location=" + idLokasi
                + "&id_asset_type_detail=" + idTipeAset;
    }

    public String url_create_asset_withBarcode(String barcode, String asset_origin, String year, String idLokasi, String idTipeAset) {
        return "http://128.199.115.183:8002/api/create-asset?tag_rfid=&barcode=" + barcode
                + "&asset_origin=" + asset_origin
                + "&year=" + year
                + "&id_location=" + idLokasi
                + "&id_asset_type_detail=" + idTipeAset;
    }
}
