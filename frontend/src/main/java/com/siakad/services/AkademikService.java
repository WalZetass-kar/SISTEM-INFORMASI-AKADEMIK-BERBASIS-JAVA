package com.siakad.services;

import com.google.gson.JsonObject;
import com.siakad.utils.Config;

public class AkademikService {
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
}
