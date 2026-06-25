package cn.navibeidou.beidou.pay;

import org.json.JSONObject;

public final class WeChatPayParams {
    public final String appId;
    public final String partnerId;
    public final String prepayId;
    public final String packageValue;
    public final String nonceStr;
    public final String timeStamp;
    public final String sign;
    public final String signType;

    private WeChatPayParams(JSONObject json) {
        appId = first(json, "appId", "appid");
        partnerId = first(json, "partnerId", "partnerid");
        prepayId = first(json, "prepayId", "prepayid");
        packageValue = first(json, "packageValue", "package");
        nonceStr = first(json, "nonceStr", "noncestr");
        timeStamp = first(json, "timeStamp", "timestamp");
        sign = json.optString("sign", "");
        signType = first(json, "signType", "sign_type");
    }

    public static WeChatPayParams from(JSONObject json) {
        return json == null ? null : new WeChatPayParams(json);
    }

    public boolean isValid() {
        return WeChatPayManager.APP_ID.equals(appId)
                && WeChatPayManager.MERCHANT_ID.equals(partnerId)
                && !prepayId.isEmpty()
                && "Sign=WXPay".equals(packageValue)
                && !nonceStr.isEmpty()
                && timeStamp.matches("^\\d{10}$")
                && !sign.isEmpty()
                && !signType.isEmpty();
    }

    private static String first(JSONObject json, String first, String second) {
        String value = json.optString(first, "");
        return value.isEmpty() ? json.optString(second, "") : value;
    }
}
