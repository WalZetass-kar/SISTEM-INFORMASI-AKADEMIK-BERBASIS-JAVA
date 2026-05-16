package com.siakad.services;

import com.google.gson.JsonObject;
import com.siakad.utils.Config;
import com.siakad.utils.JwtHelper;

/**
 * AuthService - Login, Logout, Profile
 */
public class AuthService {

    /**
     * Login ke sistem
     * @return true jika berhasil
     */
    public static boolean login(String username, String password) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);

        JsonObject response = ApiService.post(Config.LOGIN_URL, body);

        if (response.get("success").getAsBoolean()) {
            JsonObject data = response.getAsJsonObject("data");
            String token = data.get("token").getAsString();
            JsonObject user = data.getAsJsonObject("user");

            JwtHelper jwt = JwtHelper.getInstance();
            jwt.setToken(token);
            jwt.setUserId(user.get("id").getAsInt());
            jwt.setUsername(user.get("username").getAsString());
            jwt.setRole(user.get("role").getAsString());
            if (user.has("nim") && !user.get("nim").isJsonNull()) {
                jwt.setNim(user.get("nim").getAsString());
            }
            return true;
        }
        return false;
    }

    /**
     * Logout dari sistem
     */
    public static void logout() {
        try {
            ApiService.post(Config.LOGOUT_URL, new JsonObject());
        } catch (Exception ignored) {
        }
        JwtHelper.getInstance().clear();
    }

    /**
     * Ambil response login (untuk error message)
     */
    public static String loginWithMessage(String username, String password) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);

        JsonObject response = ApiService.post(Config.LOGIN_URL, body);

        if (response.get("success").getAsBoolean()) {
            JsonObject data = response.getAsJsonObject("data");
            JwtHelper jwt = JwtHelper.getInstance();
            jwt.setToken(data.get("token").getAsString());
            JsonObject user = data.getAsJsonObject("user");
            jwt.setUserId(user.get("id").getAsInt());
            jwt.setUsername(user.get("username").getAsString());
            jwt.setRole(user.get("role").getAsString());
            if (user.has("nim") && !user.get("nim").isJsonNull()) {
                jwt.setNim(user.get("nim").getAsString());
            }
            return "success";
        }

        return response.get("message").getAsString();
    }
}
