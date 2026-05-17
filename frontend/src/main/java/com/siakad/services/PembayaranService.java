package com.siakad.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.siakad.utils.Config;

/**
 * PembayaranService - CRUD Pembayaran + Dashboard Stats
 * Mengambil data mahasiswa dari API Kelompok 1 via nim
 */
public class PembayaranService {

    /**
     * Ambil semua pembayaran dengan filter
     */
    public static JsonObject getAll(int page, int limit, String search, String status, String tahunAjaran) throws Exception {
        StringBuilder url = new StringBuilder(Config.PEMBAYARAN_URL);
        url.append("?page=").append(page).append("&limit=").append(limit);
        if (search != null && !search.isEmpty()) url.append("&search=").append(ApiService.encodeQueryParam(search));
        if (status != null && !status.isEmpty()) url.append("&status=").append(ApiService.encodeQueryParam(status));
        if (tahunAjaran != null && !tahunAjaran.isEmpty()) url.append("&tahun_ajaran=").append(ApiService.encodeQueryParam(tahunAjaran));
        return ApiService.get(url.toString());
    }

    /**
     * Detail pembayaran by ID
     */
    public static JsonObject getById(int id) throws Exception {
        return ApiService.get(Config.PEMBAYARAN_URL + "/" + id);
    }

    /**
     * History pembayaran per mahasiswa (mengambil data mhs dari Kelompok 1)
     */
    public static JsonObject getByNim(String nim) throws Exception {
        return ApiService.get(Config.PEMBAYARAN_URL + "/mahasiswa/" + nim);
    }

    /**
     * Input pembayaran baru
     */
    public static JsonObject create(JsonObject data) throws Exception {
        return ApiService.post(Config.PEMBAYARAN_URL, data);
    }

    /**
     * Update pembayaran
     */
    public static JsonObject update(int id, JsonObject data) throws Exception {
        return ApiService.put(Config.PEMBAYARAN_URL + "/" + id, data);
    }

    /**
     * Update status pembayaran (verifikasi)
     */
    public static JsonObject updateStatus(int id, String status) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("status", status);
        return ApiService.put(Config.PEMBAYARAN_URL + "/" + id + "/status", body);
    }

    /**
     * Hapus pembayaran
     */
    public static JsonObject delete(int id) throws Exception {
        return ApiService.delete(Config.PEMBAYARAN_URL + "/" + id);
    }

    /**
     * Cek status pembayaran mahasiswa
     */
    public static JsonObject cekStatus(String nim, int semester, String tahunAjaran) throws Exception {
        String encodedTahunAjaran = tahunAjaran == null ? "" : ApiService.encodeQueryParam(tahunAjaran);
        String url = Config.PEMBAYARAN_URL + "/status/" + nim + "?semester=" + semester
                + "&tahun_ajaran=" + encodedTahunAjaran;
        return ApiService.get(url);
    }

    /**
     * Dashboard statistik
     */
    public static JsonObject getDashboardStats(String tahunAjaran) throws Exception {
        String url = Config.PEMBAYARAN_URL + "/dashboard/stats";
        if (tahunAjaran != null && !tahunAjaran.isEmpty()) url += "?tahun_ajaran=" + ApiService.encodeQueryParam(tahunAjaran);
        return ApiService.get(url);
    }

    /**
     * Ambil list tahun ajaran
     */
    public static JsonObject getTahunAjaran() throws Exception {
        return ApiService.get(Config.PEMBAYARAN_URL + "/tahun-ajaran");
    }
}
