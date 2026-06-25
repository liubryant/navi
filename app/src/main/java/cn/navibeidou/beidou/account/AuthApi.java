package cn.navibeidou.beidou.account;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import cn.navibeidou.beidou.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class AuthApi {
    private static final String BASE_URL = BuildConfig.API_BASE_URL;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public static final class LoginResult {
        public final String phone;
        public final String accessToken;

        LoginResult(String phone, String accessToken) {
            this.phone = phone;
            this.accessToken = accessToken;
        }
    }

    public void sendSmsCode(String phone, ResultCallback<Void> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("phone", phone);
        } catch (Exception e) {
            callback.onError("请求参数错误");
            return;
        }
        post("/im/bot/login-code", body, new ResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void loginByCode(String phone, String code, ResultCallback<LoginResult> callback) {
        JSONObject body = commonLoginBody(phone);
        try {
            body.put("code", code);
        } catch (Exception e) {
            callback.onError("请求参数错误");
            return;
        }
        login("/im/bot/login-by-code", phone, body, callback);
    }

    public void loginByPassword(String phone, String password, ResultCallback<LoginResult> callback) {
        JSONObject body = commonLoginBody(phone);
        try {
            body.put("password", password);
        } catch (Exception e) {
            callback.onError("请求参数错误");
            return;
        }
        login("/im/bot/login-by-password", phone, body, callback);
    }

    public void setPassword(String phone, String code, String password, ResultCallback<Void> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("phone", phone);
            body.put("code", code);
            body.put("password", password);
        } catch (Exception e) {
            callback.onError("请求参数错误");
            return;
        }
        post("/im/bot/set-password", body, new ResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void deleteAccount(String phone, String code, ResultCallback<Void> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("phone", phone);
            body.put("code", code);
        } catch (Exception e) {
            callback.onError("请求参数错误");
            return;
        }
        post("/im/bot/remove_account", body, new ResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private JSONObject commonLoginBody(String phone) {
        JSONObject body = new JSONObject();
        try {
            body.put("phone", phone);
            body.put("deviceModel", Build.MODEL);
            body.put("osVersion", Build.VERSION.RELEASE);
            body.put("appName", "卫星导航地图");
        } catch (Exception ignored) {
        }
        return body;
    }

    private void login(String path, final String fallbackPhone, JSONObject body, final ResultCallback<LoginResult> callback) {
        post(path, body, new ResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                String phone = data == null ? "" : data.optString("phone", "");
                String token = data == null ? "" : data.optString("accessToken", data.optString("token", ""));
                if (token.isEmpty()) {
                    callback.onError("登录服务未返回安全令牌，请重启或检查服务端配置");
                    return;
                }
                callback.onSuccess(new LoginResult(phone.isEmpty() ? fallbackPhone : phone, token));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void post(String path, JSONObject json, final ResultCallback<JSONObject> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .post(RequestBody.create(JSON, json.toString()))
                .header("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                deliverError(callback, "网络连接失败，请稍后重试");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String text = response.body() == null ? "{}" : response.body().string();
                    JSONObject result = new JSONObject(text);
                    Object code = result.opt("code");
                    boolean success = response.isSuccessful() && code != null && "0".equals(String.valueOf(code));
                    if (!success) {
                        String message = result.optString("msg", "请求失败，请稍后重试");
                        deliverError(callback, message.isEmpty() ? "请求失败，请稍后重试" : message);
                        return;
                    }
                    final JSONObject data = result.optJSONObject("data");
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(data);
                        }
                    });
                } catch (Exception e) {
                    deliverError(callback, "服务器响应异常");
                } finally {
                    response.close();
                }
            }
        });
    }

    private void deliverError(final ResultCallback<?> callback, final String message) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(message);
            }
        });
    }
}
