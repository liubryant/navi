package cn.navibeidou.beidou.account;

import android.content.Context;
import android.content.SharedPreferences;

public final class UserSession {
    private static final String PREFS = "member_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private final SharedPreferences preferences;

    public UserSession(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGGED_IN, false) && !getPhone().isEmpty();
    }

    public String getPhone() {
        return preferences.getString(KEY_PHONE, "");
    }

    public String getAccessToken() {
        return preferences.getString(KEY_ACCESS_TOKEN, "");
    }

    public boolean canPay() {
        return isLoggedIn() && !getAccessToken().isEmpty();
    }

    public void login(String phone, String accessToken) {
        preferences.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_PHONE, phone)
                .putString(KEY_ACCESS_TOKEN, accessToken == null ? "" : accessToken)
                .apply();
    }

    public void logout() {
        preferences.edit().clear().apply();
    }
}
