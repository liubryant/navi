package cn.navibeidou.beidou;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import cn.navibeidou.beidou.Util.ToastUtil;

/**
 * author : Stanley
 * e-mail : bryant_liu24@126.com
 * date   : 2020/12/10 9:48
 * desc   : ZouZou project
 * version: 1.0
 */
public class WebActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        setContentView(R.layout.activity_web);
//        StatusNavUtils.setStatusBarColor(this, 0x00000000);
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        WebView webView = findViewById(R.id.webview);
        RelativeLayout ll_feedback = findViewById(R.id.ll_feedback);
        Button btn_confirm = findViewById(R.id.btn_confirm);
        final EditText edit = findViewById(R.id.edit);
        //声明WebSettings子类
        WebSettings webSettings = webView.getSettings();
        //插件支持设置
        webSettings.setJavaScriptEnabled(true); // 是否支持Javascript，默认false
        webSettings.setSupportMultipleWindows(false);// 是否支持多窗口，默认false
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);// 是否可用Javascript(window.open)打开窗口，默认false
        //加上这一句 这个用来保存值 供js调用
        webSettings.setDomStorageEnabled(true);
        //支持flash
        String temp = "<html><body bgcolor=\"" + "black"
                + "\"> <br/><embed src=\"" + "https://map.baidu.com/mobile/webapp/subway/show/city=shenzhen" + "\" width=\"" + "100%"
                + "\" height=\"" + "90%" + "\" scale=\"" + "noscale"
                + "\" type=\"" + "application/x-shockwave-flash"
                + "\"> </embed></body></html>";
        String mimeType = "text/html";
        String encoding = "utf-8";
        webView.loadDataWithBaseURL("null", temp, mimeType, encoding, "");

        //访问网页
        String url = getIntent().getStringExtra("url");
        boolean feedback = getIntent().getBooleanExtra("feedback", false);
        if (feedback) {
            ll_feedback.setVisibility(View.VISIBLE);
            webView.loadUrl("http://cjym123.cn");
//            webView.loadUrl("https://map.baidu.com/mobile/webapp/subway/show/city=shenzhen");
        } else if (url != null && url.length() > 0) {
            webView.loadUrl(url);
        } else {
            webView.loadUrl("https://blog.csdn.net/qq_37519849/article/details/107434101");
        }
        //系统默认会通过手机浏览器打开网页，为了能够直接通过WebView显示网页，则必须设置
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //使用WebView加载显示url
                view.loadUrl(url);
                //返回true
                return true;
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.setText("");
                ToastUtil.show(WebActivity.this, R.string.thankyou);
            }
        });
    }
}
