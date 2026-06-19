package com.siakad.services;

import com.google.gson.JsonObject;
import com.siakad.utils.Config;

/**
 * LaporanService - Generate & Cetak Laporan
 */
public class LaporanService {

    public static JsonObject getAll(int page, int limit, String jenis) throws Exception {
        return getAll(page, limit, jenis, null, null, null);
    }

    public static JsonObject getAll(int page, int limit, String jenis, String status, String tahunAjaran, String search) throws Exception {
        StringBuilder url = new StringBuilder(Config.LAPORAN_URL);
        url.append("?page=").append(page).append("&limit=").append(limit);
        if (jenis != null && !jenis.isEmpty()) url.append("&jenis=").append(ApiService.encodeQueryParam(jenis));
        if (status != null && !status.isEmpty()) url.append("&status=").append(ApiService.encodeQueryParam(status));
        if (tahunAjaran != null && !tahunAjaran.isEmpty()) url.append("&tahun_ajaran=").append(ApiService.encodeQueryParam(tahunAjaran));
        if (search != null && !search.isEmpty()) url.append("&search=").append(ApiService.encodeQueryParam(search));
        return ApiService.get(url.toString());
    }

    public static JsonObject getById(int id) throws Exception {
        return ApiService.get(Config.LAPORAN_URL + "/" + id);
    }

    public static JsonObject generatePembayaran(String periodeMulai, String periodeSelesai, String tahunAjaran) throws Exception {
        JsonObject body = new JsonObject();
        if (periodeMulai != null) body.addProperty("periode_mulai", periodeMulai);
        if (periodeSelesai != null) body.addProperty("periode_selesai", periodeSelesai);
        if (tahunAjaran != null) body.addProperty("tahun_ajaran", tahunAjaran);
        return ApiService.post(Config.LAPORAN_URL + "/generate/pembayaran", body);
    }

    public static JsonObject generateMahasiswa() throws Exception {
        return ApiService.post(Config.LAPORAN_URL + "/generate/mahasiswa", new JsonObject());
    }

    public static JsonObject generateKeuangan(String tahunAjaran) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("tahun_ajaran", tahunAjaran);
        return ApiService.post(Config.LAPORAN_URL + "/generate/keuangan", body);
    }

    public static JsonObject delete(int id) throws Exception {
        return ApiService.delete(Config.LAPORAN_URL + "/" + id);
    }
}
