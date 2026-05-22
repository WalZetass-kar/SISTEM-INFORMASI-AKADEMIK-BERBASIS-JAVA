package com.siakad.services;

import com.google.gson.JsonObject;
import com.siakad.utils.Config;

/**
 * AkademikService - Komunikasi API untuk modul KRS & Jadwal Kuliah
 */
public class AkademikService {

    public static JsonObject getMatakuliah(String search, Integer semester) throws Exception {
        StringBuilder url = new StringBuilder(Config.MATAKULIAH_URL);
        boolean hasQuery = false;

        if (search != null && !search.isBlank()) {
            url.append(hasQuery ? "&" : "?")
                    .append("search=")
                    .append(ApiService.encodeQueryParam(search));
            hasQuery = true;
        }
        if (semester != null) {
            url.append(hasQuery ? "&" : "?").append("semester=").append(semester);
        }

        return ApiService.get(url.toString());
    }

    public static JsonObject createMatakuliah(JsonObject data) throws Exception {
        return ApiService.post(Config.MATAKULIAH_URL, data);
    }

    public static JsonObject getKrs(String nim, String tahunAjaran, String kodeMk) throws Exception {
        StringBuilder url = new StringBuilder(Config.KRS_URL);
        boolean hasQuery = false;

        if (nim != null && !nim.isBlank()) {
            url.append(hasQuery ? "&" : "?")
                    .append("nim=")
                    .append(ApiService.encodeQueryParam(nim));
            hasQuery = true;
        }
        if (tahunAjaran != null && !tahunAjaran.isBlank()) {
            url.append(hasQuery ? "&" : "?")
                    .append("tahun_ajaran=")
                    .append(ApiService.encodeQueryParam(tahunAjaran));
            hasQuery = true;
        }
        if (kodeMk != null && !kodeMk.isBlank()) {
            url.append(hasQuery ? "&" : "?")
                    .append("kode_mk=")
                    .append(ApiService.encodeQueryParam(kodeMk));
        }

        return ApiService.get(url.toString());
    }

    public static JsonObject createKrs(JsonObject data) throws Exception {
        return ApiService.post(Config.KRS_URL, data);
    }

    public static JsonObject getJadwal(String kodeMk, String hari, Integer semester) throws Exception {
        StringBuilder url = new StringBuilder(Config.JADWAL_URL);
        boolean hasQuery = false;

        if (kodeMk != null && !kodeMk.isBlank()) {
            url.append(hasQuery ? "&" : "?")
                    .append("kode_mk=")
                    .append(ApiService.encodeQueryParam(kodeMk));
            hasQuery = true;
        }
        if (hari != null && !hari.isBlank()) {
            url.append(hasQuery ? "&" : "?")
                    .append("hari=")
                    .append(ApiService.encodeQueryParam(hari));
            hasQuery = true;
        }
        if (semester != null) {
            url.append(hasQuery ? "&" : "?").append("semester=").append(semester);
        }

        return ApiService.get(url.toString());
    }

    public static JsonObject createJadwal(JsonObject data) throws Exception {
        return ApiService.post(Config.JADWAL_URL, data);
    }
}
