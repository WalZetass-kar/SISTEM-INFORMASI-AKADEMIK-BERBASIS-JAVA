package com.siakad.utils;

/**
 * JWT Helper - Menyimpan dan mengelola JWT token
 * Singleton pattern untuk global access
 */
public class JwtHelper {
    private static JwtHelper instance;
    private String token;
    private String username;
    private String role;
    private String nim;
    private int userId;

    private JwtHelper() {}

    public static synchronized JwtHelper getInstance() {
        if (instance == null) {
            instance = new JwtHelper();
        }
        return instance;
    }

    public void setToken(String token) { this.token = token; }
    public String getToken() { return token; }

    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }

    public void setRole(String role) { this.role = role; }
    public String getRole() { return role; }

    public void setNim(String nim) { this.nim = nim; }
    public String getNim() { return nim; }

    public void setUserId(int userId) { this.userId = userId; }
    public int getUserId() { return userId; }

    public boolean isAdmin() { return "admin".equals(role); }
    public boolean isMahasiswa() { return "mahasiswa".equals(role); }
    public boolean isLoggedIn() { return token != null && !token.isEmpty(); }

    public String getAuthHeader() { return "Bearer " + token; }

    public void clear() {
        token = null;
        username = null;
        role = null;
        nim = null;
        userId = 0;
    }
}
