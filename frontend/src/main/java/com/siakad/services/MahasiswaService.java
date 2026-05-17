package com.siakad.services;

import com.google.gson.JsonObject;
import com.siakad.utils.Config;

/**
 * MahasiswaService - Mengambil data mahasiswa dari API Kelompok 1
 */
public class MahasiswaService {

    public static JsonObject getAll(int page, int limit, String search) throws Exception {
        StringBuilder url = new StringBuilder(Config.MAHASISWA_URL);
        url.append("?page=").append(page).append("&limit=").append(limit);
        if (search != null && !search.isEmpty()) url.append("&search=").append(ApiService.encodeQueryParam(search));
        return ApiService.get(url.toString());
    }

    public static JsonObject getByNim(String nim) throws Exception {
        return ApiService.get(Config.MAHASISWA_URL + "/" + nim);
    }

    public static JsonObject create(JsonObject data) throws Exception {
        return ApiService.post(Config.MAHASISWA_URL, data);
    }

    public static JsonObject update(String nim, JsonObject data) throws Exception {
        return ApiService.put(Config.MAHASISWA_URL + "/" + nim, data);
    }

    public static JsonObject delete(String nim) throws Exception {
        return ApiService.delete(Config.MAHASISWA_URL + "/" + nim);
    }
}
