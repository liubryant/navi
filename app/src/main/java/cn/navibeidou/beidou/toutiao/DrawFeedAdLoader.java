package cn.navibeidou.beidou.toutiao;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTDrawFeedAd;

import java.util.List;

import cn.navibeidou.beidou.Util.Constants;
import cn.navibeidou.beidou.toutiao.config.TTAdManagerHolder;

public class DrawFeedAdLoader {

    private static final String TAG = "naviad";

    public interface Callback {
        void onLoaded(TTDrawFeedAd ad, View adView);
        void onFailed();
    }

    public static void load(Activity activity, final Callback callback) {
        Log.d(TAG, "DrawFeedAdLoader.load() 调用, codeId=" + Constants.DRAW_FEED_ID + " activity=" + activity.getClass().getSimpleName());
        if (!TTAdManagerHolder.isInit()) {
            Log.w(TAG, "TTAdSdk未初始化，跳过draw信息流广告加载");
            callback.onFailed();
            return;
        }
        TTAdNative adNative = TTAdManagerHolder.get().createAdNative(activity);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(Constants.DRAW_FEED_ID)
                .setAdCount(1)
                .setImageAcceptedSize(1080, 1920)
                .build();
        Log.d(TAG, "开始请求draw信息流广告(自渲染) codeId=" + Constants.DRAW_FEED_ID);
        adNative.loadDrawFeedAd(adSlot, new TTAdNative.DrawFeedAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.w(TAG, "draw信息流广告加载失败 code=" + code + " msg=" + message);
                callback.onFailed();
            }

            @Override
            public void onDrawFeedAdLoad(List<TTDrawFeedAd> ads) {
                Log.d(TAG, "onDrawFeedAdLoad回调, 广告数量=" + (ads == null ? 0 : ads.size()));
                if (ads == null || ads.isEmpty()) {
                    callback.onFailed();
                    return;
                }
                TTDrawFeedAd ad = ads.get(0);
                View adView = ad.getAdView();
                Log.d(TAG, "draw广告getAdView()=" + (adView == null ? "null" : adView.getClass().getSimpleName()));
                if (adView == null) {
                    callback.onFailed();
                    return;
                }
                callback.onLoaded(ad, adView);
            }
        });
    }
}
