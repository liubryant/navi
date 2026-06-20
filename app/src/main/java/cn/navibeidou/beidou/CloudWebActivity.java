package cn.navibeidou.beidou;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.navibeidou.beidou.translucentparent.StatusNavUtils;

public class CloudWebActivity extends Activity {
    // 实测发现页面顶部那条带"广告"角标、5 秒后才出现 X 的横幅，是 React 组件
    // Ads（class名形如 Ads_ads__xxxxx，hash 每次发版可能变化，不能硬编码）渲染的，
    // 图片来自 window.advertisement 数组，图片域名固定是 official-t.720static.com/home/
    // （场景缩略图走的是 ssl-thumb.720static.com/pano|tour/，不会被误伤）。
    // 之前只盯着另一个全屏弹层（zIndex 10000002）所以没把这条横幅去掉；这次两个
    // 一起处理：按图片域名反查到 Ads 组件的外层容器隐藏掉，找不到就退化成只隐藏图片本身。
    private static final String DISABLE_AD_POPUP_JS =
            "(function(){" +
            "  function hideAdsWrapper(img){" +
            "    var target = img, hops = 0;" +
            "    while (target && hops < 10) {" +
            "      if (typeof target.className === 'string' && /ads/i.test(target.className)) {" +
            "        target.style.display = 'none';" +
            "        return;" +
            "      }" +
            "      target = target.parentElement;" +
            "      hops++;" +
            "    }" +
            "    img.style.display = 'none';" +
            "  }" +
            "  function killAd(){" +
            "    var bannerImgs = document.querySelectorAll('img[src*=\"720static.com/home/\"]');" +
            "    for (var i = 0; i < bannerImgs.length; i++) { hideAdsWrapper(bannerImgs[i]); }" +
            "    var candidates = document.querySelectorAll('[style]');" +
            "    for (var j = 0; j < candidates.length; j++) {" +
            "      var el = candidates[j];" +
            "      if (el.style && el.style.zIndex === '10000002') {" +
            "        el.style.display = 'none';" +
            "      }" +
            "    }" +
            "  }" +
            "  if (!window.__cloudAdKillerStarted) {" +
            "    window.__cloudAdKillerStarted = true;" +
            "    setInterval(killAd, 100);" +
            "  }" +
            "  killAd();" +
            "})();";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_web);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        String url = getIntent().getStringExtra("url");
        WebView webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                view.evaluateJavascript(DISABLE_AD_POPUP_JS, null);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.evaluateJavascript(DISABLE_AD_POPUP_JS, null);
            }
        });
        webView.loadUrl(url == null ? "https://www.720yun.com" : url);
    }
}
