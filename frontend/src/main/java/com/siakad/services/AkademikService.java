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

    public static JsonObject getSemester() throws Exception {
        return ApiService.get(Config.AKADEMIK_URL + "/semester");
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

    public static JsonObject createSemester(JsonObject body) throws Exception {
        return ApiService.post(Config.AKADEMIK_URL + "/semester", body);
    }

    public static JsonObject updateSemester(int id, JsonObject body) throws Exception {
        return ApiService.put(Config.AKADEMIK_URL + "/semester/" + id, body);
    }

    public static JsonObject deleteSemester(int id) throws Exception {
        return ApiService.delete(Config.AKADEMIK_URL + "/semester/" + id);
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

    public static JsonObject createJurusan(JsonObject body) throws Exception {
        return ApiService.post(Config.AKADEMIK_URL + "/jurusan", body);
    }

    public static JsonObject updateJurusan(int id, JsonObject body) throws Exception {
        return ApiService.put(Config.AKADEMIK_URL + "/jurusan/" + id, body);
    }

    public static JsonObject deleteJurusan(int id) throws Exception {
        return ApiService.delete(Config.AKADEMIK_URL + "/jurusan/" + id);
    }

    public static JsonObject updateBobotNilai(JsonObject body) throws Exception {
        return ApiService.put(Config.AKADEMIK_URL + "/bobot-nilai", body);
    }

    public static JsonObject updateJumlahPertemuan(JsonObject body) throws Exception {
        return ApiService.put(Config.AKADEMIK_URL + "/jumlah-pertemuan", body);
    }

    public static JsonObject updateJumlahPertemuanJurusan(JsonObject body) throws Exception {
        return ApiService.put(Config.AKADEMIK_URL + "/jumlah-pertemuan-jurusan", body);
    }

    public static JsonObject getKehadiranInputList(String kodeMk, String tahunAjaran, String tanggal, String search, String jurusan) throws Exception {
        StringBuilder url = new StringBuilder(Config.AKADEMIK_URL + "/kehadiran/input-list");
        url.append("?kode_mk=").append(ApiService.encodeQueryParam(kodeMk));
        url.append("&tahun_ajaran=").append(ApiService.encodeQueryParam(tahunAjaran));
        url.append("&tanggal=").append(ApiService.encodeQueryParam(tanggal));
        if (search != null && !search.isEmpty()) {
            url.append("&search=").append(ApiService.encodeQueryParam(search));
        }
        if (jurusan != null && !jurusan.isEmpty()) {
            url.append("&jurusan=").append(ApiService.encodeQueryParam(jurusan));
        }
        return ApiService.get(url.toString());
    }

    public static JsonObject bulkSaveKehadiran(JsonObject body) throws Exception {
        return ApiService.post(Config.AKADEMIK_URL + "/kehadiran/bulk", body);
    }

    public static JsonObject getKehadiranRekap(String tahunAjaran, String kodeMk, String tanggalMulai, String tanggalSelesai, String search, String jurusan) throws Exception {
        StringBuilder url = new StringBuilder(Config.AKADEMIK_URL + "/kehadiran/rekap");
        url.append("?tahun_ajaran=").append(ApiService.encodeQueryParam(tahunAjaran));
        if (kodeMk != null && !kodeMk.isEmpty()) {
            url.append("&kode_mk=").append(ApiService.encodeQueryParam(kodeMk));
        }
        if (tanggalMulai != null && !tanggalMulai.isEmpty()) {
            url.append("&tanggal_mulai=").append(ApiService.encodeQueryParam(tanggalMulai));
        }
        if (tanggalSelesai != null && !tanggalSelesai.isEmpty()) {
            url.append("&tanggal_selesai=").append(ApiService.encodeQueryParam(tanggalSelesai));
        }
        if (search != null && !search.isEmpty()) {
            url.append("&search=").append(ApiService.encodeQueryParam(search));
        }
        if (jurusan != null && !jurusan.isEmpty()) {
            url.append("&jurusan=").append(ApiService.encodeQueryParam(jurusan));
        }
        return ApiService.get(url.toString());
    }

    public static JsonObject getKehadiranSaya() throws Exception {
        return ApiService.get(Config.AKADEMIK_URL + "/kehadiran/saya");
    }

    public static JsonObject getInfoAkademikSaya() throws Exception {
        return ApiService.get(Config.AKADEMIK_URL + "/info-saya");
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

    public static JsonObject updateMatakuliah(String kodeMk, JsonObject data) throws Exception {
        return ApiService.put(Config.MATAKULIAH_URL + "/" + ApiService.encodeQueryParam(kodeMk), data);
    }

    public static JsonObject deleteMatakuliah(String kodeMk) throws Exception {
        return ApiService.delete(Config.MATAKULIAH_URL + "/" + ApiService.encodeQueryParam(kodeMk));
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

    public static JsonObject updateJadwal(String id, JsonObject data) throws Exception {
        return ApiService.put(Config.JADWAL_URL + "/" + ApiService.encodeQueryParam(id), data);
    }

    public static JsonObject deleteJadwal(String id) throws Exception {
        return ApiService.delete(Config.JADWAL_URL + "/" + ApiService.encodeQueryParam(id));
    }
}
