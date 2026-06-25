package cn.navibeidou.beidou.pay;

import android.os.Handler;
import android.os.Looper;

import cn.navibeidou.beidou.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class PaymentApi {
    private static final String BASE_URL = BuildConfig.API_BASE_URL + "/im/bot/navi/vip";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ResultCallback<T> {
        void onSuccess(T value);
        void onError(String message);
    }

    public static final class OrderResult {
        public final String orderId;
        public final String payChannel;
        public final WeChatPayParams payParams;
        public final String aliPayOrderString;
        public final boolean mock;

        OrderResult(String orderId, String payChannel, WeChatPayParams payParams, String aliPayOrderString, boolean mock) {
            this.orderId = orderId;
            this.payChannel = payChannel;
            this.payParams = payParams;
            this.aliPayOrderString = aliPayOrderString;
            this.mock = mock;
        }
    }

    public void getProducts(final ResultCallback<List<VipProduct>> callback) {
        request(new Request.Builder().url(BASE_URL + "/products").get().build(), new ResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                JSONArray items = data == null ? null : data.optJSONArray("items");
                List<VipProduct> products = new ArrayList<>();
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.optJSONObject(i);
                        if (item != null) {
                            VipProduct product = new VipProduct(item);
                            if (!product.id.isEmpty() && !product.price.isEmpty()) {
                                products.add(product);
                            }
                        }
                    }
                }
                callback.onSuccess(products);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void createOrder(String accessToken, String productId, String payChannel, final ResultCallback<OrderResult> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("productId", productId);
            body.put("appid", WeChatPayManager.APP_ID);
            body.put("payChannel", payChannel);
        } catch (Exception e) {
            callback.onError("订单参数错误");
            return;
        }
        Request request = new Request.Builder().url(BASE_URL + "/orders")
                .post(RequestBody.create(JSON, body.toString()))
                .header("Authorization", "Bearer " + accessToken)
                .build();
        request(request, new ResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                String orderId = data == null ? "" : data.optString("orderId", "");
                boolean mock = data != null && data.optBoolean("mock", false);
                String resolvedChannel = data == null ? "wechat" : data.optString("payChannel", "wechat");
                if (mock && !BuildConfig.PAYMENT_MOCK_ALLOWED) {
                    callback.onError("正式版本禁止模拟支付");
                    return;
                }
                if (orderId.isEmpty()) {
                    callback.onError("支付参数校验失败");
                    return;
                }
                if (mock) {
                    callback.onSuccess(new OrderResult(orderId, resolvedChannel, null, null, true));
                    return;
                }
                if ("alipay".equals(resolvedChannel)) {
                    String aliPayOrderString = data.optString("aliPayOrderString", "");
                    if (aliPayOrderString.isEmpty()) {
                        callback.onError("支付参数校验失败");
                        return;
                    }
                    callback.onSuccess(new OrderResult(orderId, resolvedChannel, null, aliPayOrderString, false));
                    return;
                }
                WeChatPayParams params = WeChatPayParams.from(data.optJSONObject("payParams"));
                if (params == null || !params.isValid()) {
                    callback.onError("支付参数校验失败");
                    return;
                }
                callback.onSuccess(new OrderResult(orderId, resolvedChannel, params, null, false));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public static final class MembershipStatus {
        public final boolean active;
        public final String expiresAt;

        MembershipStatus(boolean active, String expiresAt) {
            this.active = active;
            this.expiresAt = expiresAt;
        }
    }

    public void getMembership(String accessToken, final ResultCallback<MembershipStatus> callback) {
        request(new Request.Builder().url(BASE_URL + "/membership").get()
                .header("Authorization", "Bearer " + accessToken).build(), new ResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                boolean active = isMembershipActive(data);
                String expiresAt = data == null ? null : firstText(data,
                        "expiresAt", "expiredAt", "expireAt", "vipExpireAt", "vip_expired_at", "expired_timestamp");
                callback.onSuccess(new MembershipStatus(active, expiresAt));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private static boolean isMembershipActive(JSONObject data) {
        if (data == null) {
            return false;
        }
        if (data.optBoolean("active", false)
                || data.optBoolean("isVip", false)
                || data.optBoolean("is_vip", false)
                || data.optBoolean("vip", false)) {
            return true;
        }
        String status = firstText(data, "status", "vipStatus", "vip_status", "memberStatus", "member_status");
        return "1".equals(status)
                || "true".equalsIgnoreCase(status)
                || "active".equalsIgnoreCase(status)
                || "vip".equalsIgnoreCase(status)
                || "is_vip".equalsIgnoreCase(status)
                || "paid".equalsIgnoreCase(status)
                || "success".equalsIgnoreCase(status);
    }

    private static String firstText(JSONObject data, String... keys) {
        for (String key : keys) {
            String value = data.optString(key, "");
            if (value != null && !value.isEmpty() && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return null;
    }

    public void queryOrder(String accessToken, String orderId, final ResultCallback<String> callback) {
        String url = BASE_URL + "/orders/" + orderId;
        request(new Request.Builder().url(url).get()
                .header("Authorization", "Bearer " + accessToken).build(), new ResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                callback.onSuccess(data == null ? "UNKNOWN" : data.optString("status", "UNKNOWN"));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void request(Request request, final ResultCallback<JSONObject> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                error(callback, "网络连接失败，请稍后重试");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body() == null ? "{}" : response.body().string());
                    Object code = json.opt("code");
                    if (!response.isSuccessful() || code == null || !"0".equals(String.valueOf(code))) {
                        error(callback, json.optString("msg", "服务暂不可用"));
                        return;
                    }
                    final JSONObject data = json.optJSONObject("data");
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(data);
                        }
                    });
                } catch (Exception e) {
                    error(callback, "服务器响应异常");
                } finally {
                    response.close();
                }
            }
        });
    }

    private void error(final ResultCallback<?> callback, final String message) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(message == null || message.isEmpty() ? "服务暂不可用" : message);
            }
        });
    }
}
