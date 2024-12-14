package cn.navibeidou.beidou;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.StringTokenizer;

import cn.navibeidou.beidou.translucentparent.StatusNavUtils;

public class JsActivity extends Activity {
    private WebView webView;
    private String message;
    private JsInterface jsInterface;
    private String[] params = null;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityjs);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        webView = findViewById(R.id.webview);
        webView.requestFocus();
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                JsActivity.this.setTitle("Loading...");
                JsActivity.this.setProgress(progress);
                if (progress >= 80) {
                    JsActivity.this.setTitle("JsAndroid Test");
                }
            }
        });


        jsInterface = new JsInterface();
        params = new String[2];
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
//        webView.addJavascriptInterface(new JSInterface(), "adcode");
//        webView.addJavascriptInterface(jsInterface, "jsObj");
//        webView.loadUrl("http://cocos-games.fir.show/games/0000079-basketball-2.4.4/index.html");
        webView.loadUrl("file:///android_asset/metro.html");
        webView.evaluateJavascript("javascript:theRequest('" + 3100 + "'')", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d("navi", "onReceiveValue  " + value);
            }
        });
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
    }

    private class JsInterface {
        /**
         * js中通过window.jsObj.HtmlcallJava2("参数") 可以调用此方法并且把js中input中的值作为参数传入，
         * 但这是在点击js中的按钮得到的，若实现点击java中的按钮得到，需要方法 clickView(View v)
         *
         * @param param
         */
        public void HtmlcallJava2(final String param) {
            message = param;
            String str = message;
            StringTokenizer st = new StringTokenizer(str, ",;");
            while (st.hasMoreTokens()) {
                for (int i = 0; i < params.length; i++) {
                    params[i] = st.nextToken();
                    System.out.println(params[i]);
                }
            }
            Toast.makeText(JsActivity.this, param, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * button的点击事件
     *
     * @param v
     */
    public void clickView(View v) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /**
                 * 调用js中的方法实现点击java中的按钮得到js中input的值
                 */
                webView.loadUrl("javascript: showHtmlcallJava2()");
//                [INFO:CONSOLE(49)] "Uncaught TypeError: Cannot read property 'value' of null", source: file:///android_asset/metro.html (49)


//                [INFO:CONSOLE(51)] "null", source: file:///android_asset/metro.html (51)
//                2021-04-15 15:08:34.954 20910-20910/cn.navibeidou.beidou I/chromium: [INFO:CONSOLE(53)]
//                "Uncaught TypeError: window.jsObj.HtmlcallJava2 is not a function", source: file:///android_asset/metro.html (53)
            }
        });
    }
}
