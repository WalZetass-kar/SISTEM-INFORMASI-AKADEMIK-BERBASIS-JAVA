package com.siakad.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.siakad.utils.JwtHelper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * ApiService - HTTP Client untuk komunikasi dengan REST API
 * Menggunakan java.net.HttpURLConnection (built-in)
 */
public class ApiService {
    private static final Gson gson = new Gson();
    private static final int TIMEOUT = 10000; // 10 detik

    public static String encodeQueryParam(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * HTTP GET Request
     */
    public static JsonObject get(String urlString) throws Exception {
        HttpURLConnection conn = createConnection(urlString, "GET");
        return readResponse(conn);
    }

    /**
     * HTTP POST Request
     */
    public static JsonObject post(String urlString, Object body) throws Exception {
        HttpURLConnection conn = createConnection(urlString, "POST");
        writeBody(conn, body);
        return readResponse(conn);
    }

    /**
     * HTTP PUT Request
     */
    public static JsonObject put(String urlString, Object body) throws Exception {
        HttpURLConnection conn = createConnection(urlString, "PUT");
        writeBody(conn, body);
        return readResponse(conn);
    }

    /**
     * HTTP DELETE Request
     */
    public static JsonObject delete(String urlString) throws Exception {
        HttpURLConnection conn = createConnection(urlString, "DELETE");
        return readResponse(conn);
    }

    public static JsonObject deleteWithBody(String urlString, Object body) throws Exception {
        HttpURLConnection conn = createConnection(urlString, "DELETE");
        conn.setDoOutput(true);
        writeBody(conn, body);
        return readResponse(conn);
    }

    public static JsonObject uploadFile(String urlString, File file, String fieldName) throws Exception {
        String boundary = "----SiakadBoundary" + System.currentTimeMillis();
        HttpURLConnection conn = createRawConnection(urlString, "POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true);
             FileInputStream input = new FileInputStream(file)) {
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"")
                    .append(fieldName)
                    .append("\"; filename=\"")
                    .append(file.getName())
                    .append("\"\r\n");
            writer.append("Content-Type: application/octet-stream\r\n\r\n");
            writer.flush();

            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();

            writer.append("\r\n");
            writer.append("--").append(boundary).append("--\r\n");
            writer.flush();
        }

        return readResponse(conn);
    }

    public static void downloadToFile(String urlString, File targetFile) throws Exception {
        HttpURLConnection conn = createRawConnection(urlString, "GET");
        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            JsonObject error = readResponse(conn);
            String message = error.has("message") ? error.get("message").getAsString()
                    : "Download gagal (code: " + responseCode + ")";
            throw new IOException(message);
        }

        try (InputStream is = conn.getInputStream();
             FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Buat koneksi HTTP dengan header yang diperlukan
     */
    private static HttpURLConnection createConnection(String urlString, String method) throws Exception {
        HttpURLConnection conn = createRawConnection(urlString, method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        if ("POST".equals(method) || "PUT".equals(method)) {
            conn.setDoOutput(true);
        }

        return conn;
    }

    private static HttpURLConnection createRawConnection(String urlString, String method) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        // Tambahkan JWT token jika sudah login
        JwtHelper jwt = JwtHelper.getInstance();
        if (jwt.isLoggedIn()) {
            conn.setRequestProperty("Authorization", jwt.getAuthHeader());
        }

        return conn;
    }

    /**
     * Tulis body request (JSON)
     */
    private static void writeBody(HttpURLConnection conn, Object body) throws Exception {
        String jsonBody = gson.toJson(body);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    /**
     * Baca response dari server
     */
    private static JsonObject readResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        InputStream is;

        if (responseCode >= 200 && responseCode < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }

        if (is == null) {
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "Tidak ada response dari server (code: " + responseCode + ")");
            return error;
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        conn.disconnect();

        String responseBody = response.toString();
        if (responseBody.isBlank()) {
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "Response server kosong (code: " + responseCode + ")");
            return error;
        }

        try {
            return JsonParser.parseString(responseBody).getAsJsonObject();
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "Response server tidak valid (code: " + responseCode + ")");
            return error;
        }
    }
}
