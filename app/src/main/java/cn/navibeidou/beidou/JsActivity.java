package cn.navibeidou.beidou;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import cn.navibeidou.beidou.translucentparent.StatusNavUtils;

public class JsActivity extends Activity {
    // 网页加载完整地铁城市列表前的默认展示，加载完成后会被 metro.html 回传的完整列表替换
    private static final String[] DEFAULT_SUBWAY_CITIES = {
            "深圳", "北京", "上海", "广州", "成都", "武汉", "杭州", "南京", "重庆", "天津",
            "西安", "苏州", "郑州", "长沙", "青岛", "宁波", "无锡", "沈阳", "大连", "厦门",
            "福州", "昆明", "南宁", "合肥", "南昌", "石家庄", "哈尔滨", "长春", "贵阳", "佛山"
    };
    private final List<String> cityNames = new ArrayList<>(Arrays.asList(DEFAULT_SUBWAY_CITIES));
    private Spinner citySpinner;
    private ArrayAdapter<String> cityAdapter;
    private WebView webView;
    private boolean metroPageLoaded = false;
    private String message;
    private JsInterface jsInterface;
    private String[] params = null;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityjs);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setupCitySpinner();
        webView = findViewById(R.id.webview);
        webView.requestFocus();
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                JsActivity.this.setTitle("Loading...");
                JsActivity.this.setProgress(progress);
                if (progress >= 80) {
                    JsActivity.this.setTitle("JsAndroid Test");
                    metroPageLoaded = true;
                }
            }
        });


        jsInterface = new JsInterface();
        params = new String[2];
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDefaultTextEncodingName("utf-8");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
//        webView.addJavascriptInterface(new JSInterface(), "adcode");
//        webView.addJavascriptInterface(jsInterface, "jsObj");
//        webView.loadUrl("http://cocos-games.fir.show/games/0000079-basketball-2.4.4/index.html");
        webView.addJavascriptInterface(new CityListBridge(), "AndroidCityBridge");
        loadMetroPage();
        settings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
    }

    private void setupCitySpinner() {
        citySpinner = findViewById(R.id.sp_city);
        cityAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_city, cityNames);
        cityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_city);
        citySpinner.setAdapter(cityAdapter);
        citySpinner.setSelection(0);
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (webView == null || position < 0 || position >= cityNames.size()) return;
                String city = cityNames.get(position);
                webView.evaluateJavascript("window.setSubwayCityByName && window.setSubwayCityByName('" + city + "')", null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * metro.html 中地铁城市列表 (BMapSub.SubwayCitiesList) 加载完成后，通过此接口把完整列表回传给原生 Spinner，
     * 避免原生侧固定城市数组比网页端实际可选城市更少。
     */
    private void applyFullCityList(String namesJson) {
        try {
            JSONArray array = new JSONArray(namesJson);
            List<String> names = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                String name = array.optString(i, null);
                if (name != null && !name.isEmpty()) {
                    names.add(name);
                }
            }
            if (names.isEmpty() || cityAdapter == null || citySpinner == null) return;

            int previousPosition = citySpinner.getSelectedItemPosition();
            String previouslySelected = previousPosition >= 0 && previousPosition < cityNames.size()
                    ? cityNames.get(previousPosition) : null;

            cityNames.clear();
            cityNames.addAll(names);
            cityAdapter.notifyDataSetChanged();

            int newPosition = previouslySelected != null ? names.indexOf(previouslySelected) : -1;
            citySpinner.setSelection(newPosition >= 0 ? newPosition : 0);
        } catch (JSONException e) {
            Log.w("navi", "parse subway city list failed", e);
        }
    }

    private class CityListBridge {
        @JavascriptInterface
        public void onCitiesLoaded(final String namesJson) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    applyFullCityList(namesJson);
                }
            });
        }
    }

    private void loadMetroPage() {
        try {
            webView.loadDataWithBaseURL("https://api.map.baidu.com/", readAssetText("metro.html"), "text/html", "UTF-8", null);
        } catch (IOException e) {
            webView.loadUrl("file:///android_asset/metro.html");
        }
    }

    private String readAssetText(String fileName) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStream inputStream = getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
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
