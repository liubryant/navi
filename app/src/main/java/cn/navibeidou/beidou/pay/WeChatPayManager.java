package cn.navibeidou.beidou.pay;

import android.content.Context;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public final class WeChatPayManager {
    public static final String APP_ID = "wx4ac470d6bef3de2f";
    public static final String MERCHANT_ID = "1645114257";

    private static volatile WeChatPayManager instance;
    private final IWXAPI api;

    private WeChatPayManager(Context context) {
        api = WXAPIFactory.createWXAPI(context.getApplicationContext(), APP_ID, true);
        api.registerApp(APP_ID);
    }

    public static WeChatPayManager get(Context context) {
        if (instance == null) {
            synchronized (WeChatPayManager.class) {
                if (instance == null) {
                    instance = new WeChatPayManager(context);
                }
            }
        }
        return instance;
    }

    public boolean isWeChatInstalled() {
        return api.isWXAppInstalled();
    }

    public boolean pay(WeChatPayParams params) {
        if (params == null || !params.isValid()) {
            return false;
        }
        PayReq request = new PayReq();
        request.appId = params.appId;
        request.partnerId = params.partnerId;
        request.prepayId = params.prepayId;
        request.packageValue = params.packageValue;
        request.nonceStr = params.nonceStr;
        request.timeStamp = params.timeStamp;
        request.sign = params.sign;
        request.signType = params.signType;
        return api.sendReq(request);
    }
}
