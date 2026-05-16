package com.siakad.utils;

/**
 * Konfigurasi aplikasi - URL API dan settings
 */
public class Config {
    // Base URL API Backend
    public static final String API_BASE_URL = "http://localhost:3000/api";

    // Endpoints
    public static final String LOGIN_URL = API_BASE_URL + "/login";
    public static final String LOGOUT_URL = API_BASE_URL + "/logout";
    public static final String PROFILE_URL = API_BASE_URL + "/profile";
    public static final String MAHASISWA_URL = API_BASE_URL + "/mahasiswa";
    public static final String PEMBAYARAN_URL = API_BASE_URL + "/pembayaran";
    public static final String LAPORAN_URL = API_BASE_URL + "/laporan";

    // App Info
    public static final String APP_NAME = "Sistem Informasi Akademik";
    public static final String APP_VERSION = "1.0.0";

    // UI Settings
    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 800;
}
