package com.siakad.services;

import com.google.gson.JsonObject;
import com.siakad.utils.Config;

public class NilaiService {

    public static JsonObject getMataKuliah(String semester, String search) throws Exception {
        StringBuilder url = new StringBuilder(Config.NILAI_URL + "/mata-kuliah");
        url.append("?q=1");
        if (semester != null && !semester.isEmpty()) {
            url.append("&semester=").append(ApiService.encodeQueryParam(semester));
        }
        if (search != null && !search.isEmpty()) {
            url.append("&search=").append(ApiService.encodeQueryParam(search));
        }
        return ApiService.get(url.toString());
    }

    public static JsonObject getInputList(String kodeMk, String tahunAjaran, String search) throws Exception {
        return getInputList(kodeMk, tahunAjaran, search, null);
    }

    public static JsonObject getInputList(String kodeMk, String tahunAjaran, String search, String jurusan) throws Exception {
        StringBuilder url = new StringBuilder(Config.NILAI_URL + "/input-list");
        url.append("?kode_mk=").append(ApiService.encodeQueryParam(kodeMk));
        url.append("&tahun_ajaran=").append(ApiService.encodeQueryParam(tahunAjaran));
        if (search != null && !search.isEmpty()) {
            url.append("&search=").append(ApiService.encodeQueryParam(search));
        }
        if (jurusan != null && !jurusan.isEmpty()) {
            url.append("&jurusan=").append(ApiService.encodeQueryParam(jurusan));
        }
        return ApiService.get(url.toString());
    }

    public static JsonObject getRekap(String tahunAjaran, String kodeMk, String search, String jurusan) throws Exception {
        StringBuilder url = new StringBuilder(Config.NILAI_URL + "/rekap");
        url.append("?tahun_ajaran=").append(ApiService.encodeQueryParam(tahunAjaran));
        if (kodeMk != null && !kodeMk.isEmpty()) {
            url.append("&kode_mk=").append(ApiService.encodeQueryParam(kodeMk));
        }
        if (search != null && !search.isEmpty()) {
            url.append("&search=").append(ApiService.encodeQueryParam(search));
        }
        if (jurusan != null && !jurusan.isEmpty()) {
            url.append("&jurusan=").append(ApiService.encodeQueryParam(jurusan));
        }
        return ApiService.get(url.toString());
    }

    public static JsonObject bulkSave(JsonObject body) throws Exception {
        return ApiService.post(Config.NILAI_URL + "/bulk", body);
    }

    public static JsonObject delete(JsonObject body) throws Exception {
        return ApiService.deleteWithBody(Config.NILAI_URL, body);
    }

    public static JsonObject getMyNilai() throws Exception {
        return ApiService.get(Config.NILAI_URL + "/saya");
    }
}
