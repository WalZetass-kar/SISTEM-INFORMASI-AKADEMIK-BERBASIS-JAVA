package com.siakad.services;

import com.google.gson.JsonObject;
import com.siakad.utils.Config;

/**
 * AkademikService - Komunikasi API untuk modul Akademik, KRS & Jadwal Kuliah
 */
public class AkademikService {

    // --- Modul Pengaturan Akademik & Tahun Ajaran ---
    
    public static JsonObject getSettings() throws Exception {
        return ApiService.get(Config.AKADEMIK_URL + "/settings");
    }

    public static JsonObject createTahunAjaran(JsonObject body) throws Exception {
        return ApiService.post(Config.AKADEMIK_URL + "/tahun-ajaran", body);
    }

    public static JsonObject updateTahunAjaran(int id, JsonObject body) throws Exception {
        return ApiService.put(Config.AKADEMIK_URL + "/tahun-ajaran/" + id, body);
    }

    public static JsonObject deleteTahunAjaran(int id) throws Exception {
        return ApiService.delete(Config.AKADEMIK_URL + "/tahun-ajaran/" + id);
    }

    public static JsonObject createMataKuliah(JsonObject body) throws Exception {
        return ApiService.post(Config.AKADEMIK_URL + "/mata-kuliah", body);
    }

    public static JsonObject updateMataKuliah(String kodeMk, JsonObject body) throws Exception {
        return ApiService.put(Config.AKADEMIK_URL + "/mata-kuliah/" + ApiService.encodeQueryParam(kodeMk), body);
    }

    public static JsonObject deleteMataKuliah(String kodeMk) throws Exception {
        return ApiService.delete(Config.AKADEMIK_URL + "/mata-kuliah/" + ApiService.encodeQueryParam(kodeMk));
    }

    public static JsonObject updateBobotNilai(JsonObject body) throws Exception {
        return ApiService.put(Config.AKADEMIK_URL + "/bobot-nilai", body);
    }

    // --- Modul KRS & Jadwal Kuliah (Legacy/Compatibility) ---

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
