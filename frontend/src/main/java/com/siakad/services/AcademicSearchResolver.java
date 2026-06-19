package com.siakad.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Helper untuk mencari mahasiswa unik dari filter dan mengambil mata kuliah KRS-nya.
 * Dipakai oleh panel akademik ketika pencarian mahasiswa tidak muncul di filter awal.
 */
public final class AcademicSearchResolver {
    private AcademicSearchResolver() {
    }

    public record Resolution(String nim, String nama, String jurusan, String kodeMk, String tahunAjaran) {
    }

    public static Resolution resolveSingleStudentCourse(String search, String tahunAjaran) throws Exception {
        String normalizedSearch = normalize(search);
        String normalizedTahunAjaran = normalize(tahunAjaran);
        if (normalizedSearch.isBlank() || normalizedTahunAjaran.isBlank()) {
            return null;
        }

        JsonObject mahasiswaResponse = MahasiswaService.getAll(1, 1000, normalizedSearch);
        if (!isSuccessful(mahasiswaResponse)) {
            return null;
        }

        JsonArray mahasiswaData = mahasiswaResponse.has("data") && mahasiswaResponse.get("data").isJsonArray()
                ? mahasiswaResponse.getAsJsonArray("data")
                : null;
        if (mahasiswaData == null || mahasiswaData.size() != 1) {
            return null;
        }

        JsonObject mahasiswa = mahasiswaData.get(0).getAsJsonObject();
        String nim = getString(mahasiswa, "nim");
        String nama = getString(mahasiswa, "nama");
        String jurusan = getString(mahasiswa, "jurusan");
        String status = getString(mahasiswa, "status");
        if (nim.isBlank() || !"aktif".equalsIgnoreCase(status)) {
            return null;
        }

        JsonObject krsResponse = AkademikService.getKrs(nim, normalizedTahunAjaran, null);
        if (!isSuccessful(krsResponse)) {
            return null;
        }

        JsonArray krsData = krsResponse.has("data") && krsResponse.get("data").isJsonArray()
                ? krsResponse.getAsJsonArray("data")
                : null;
        if (krsData == null || krsData.size() == 0) {
            return null;
        }

        String kodeMk = "";
        for (JsonElement element : krsData) {
            JsonObject row = element.getAsJsonObject();
            String candidate = getString(row, "kode_mk");
            if (!candidate.isBlank()) {
                kodeMk = candidate;
                break;
            }
        }

        if (kodeMk.isBlank()) {
            return null;
        }

        return new Resolution(nim, nama, jurusan, kodeMk, normalizedTahunAjaran);
    }

    private static boolean isSuccessful(JsonObject response) {
        return response != null
                && response.has("success")
                && !response.get("success").isJsonNull()
                && response.get("success").getAsBoolean();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String getString(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }
        return object.get(key).getAsString().trim();
    }
}
