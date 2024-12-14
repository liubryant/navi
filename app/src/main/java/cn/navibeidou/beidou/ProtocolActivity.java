package cn.navibeidou.beidou;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProtocolActivity extends AppCompatActivity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol);
        mContext = this;
        TextView tv_protocol = findViewById(R.id.tv_protocol);
        boolean service = getIntent().getBooleanExtra("service", false);
        boolean yinsi = getIntent().getBooleanExtra("yinsi", false);
        WebView webView = findViewById(R.id.webview);
        if (service) {
            tv_protocol.setText(getResources().getString(R.string.xieyi));
            tv_protocol.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        } else if (yinsi) {
//            tv_protocol.setText(getResources().getString(R.string.yinsi));
            tv_protocol.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 是否支持Javascript，默认false
        webSettings.setSupportMultipleWindows(false);// 是否支持多窗口，默认false
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);// 是否可用Javascript(window.open)打开窗口，默认false
        webSettings.setDomStorageEnabled(true);
        //访问网页
        webView.loadUrl("http://cjym123.cn/privacy_navi.html");
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
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                SharedPreferences sharedPreferences = getSharedPreferences("FirstRun", 0);
                Boolean first_run = sharedPreferences.getBoolean("First", true);
                if (first_run) {
                    Log.e("navi","first_run value  "+first_run);
                    finish();
                    Intent intent = new Intent(ProtocolActivity.this, InitActivity.class);
                    startActivity(intent);
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
