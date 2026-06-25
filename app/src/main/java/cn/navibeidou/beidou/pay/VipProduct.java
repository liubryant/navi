package cn.navibeidou.beidou.pay;

import org.json.JSONObject;

public final class VipProduct {
    public final String id;
    public final String name;
    public final String price;
    public final String description;

    VipProduct(JSONObject json) {
        id = json.optString("id", json.optString("productId", ""));
        name = json.optString("name", "导航会员");
        price = json.optString("price", "");
        description = json.optString("description", "");
    }
}
