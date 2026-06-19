package cn.navibeidou.beidou;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import cn.navibeidou.beidou.translucentparent.StatusNavUtils;
import cn.navibeidou.beidou.world.WorldPanoramaPlace;

public class WorldPanoramaDetailActivity extends Activity {
    private static final String BAIDU_AK = "dh2V6BNKX8GMaCmkwXyG8RW6ecii83cG";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_panorama_detail);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);

        String name = getIntent().getStringExtra("name");
        String region = getIntent().getStringExtra("region");
        String province = getIntent().getStringExtra("province");
        String city = getIntent().getStringExtra("city");
        double latitude = getIntent().getDoubleExtra("latitude", 39.9042);
        double longitude = getIntent().getDoubleExtra("longitude", 116.4074);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.tv_title)).setText(name);
        ((TextView) findViewById(R.id.tv_summary)).setText(name + " · " + region + " · " + province + " · " + city + "\n" + WorldPanoramaPlace.SUMMARY);
        ((TextView) findViewById(R.id.tv_coordinate)).setText(String.format("纬度 %.4f · 经度 %.4f", latitude, longitude));

        WebView webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");
        webView.setWebViewClient(new WebViewClient());
        webView.loadDataWithBaseURL("https://api.map.baidu.com", panoramaHtml(name, province, city, latitude, longitude), "text/html", "utf-8", null);
    }

    private String panoramaHtml(String name, String province, String city, double latitude, double longitude) {
        String keyword = escapeJs(name + " " + province + " " + city);
        return "<!DOCTYPE html><html><head><meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no'>"
                + "<style>html,body,#pano{width:100%;height:100%;margin:0;padding:0;overflow:hidden;background:#f2f3f5}"
                + "#tip{position:absolute;left:16px;right:16px;top:50%;transform:translateY(-50%);text-align:center;color:#777;font-size:14px;line-height:1.6}</style>"
                + "<script src='https://api.map.baidu.com/api?v=3.0&ak=" + BAIDU_AK + "'></script></head><body>"
                + "<div id='pano'></div><div id='tip'>全景加载中...</div><script>"
                + "var keyword='" + keyword + "';var loaded=false;"
                + "function showEmpty(){document.getElementById('tip').innerText='该景区附近暂无可用百度全景数据';document.getElementById('tip').style.display='block';}"
                + "function hideTip(){document.getElementById('tip').style.display='none';}"
                + "function loadByPoint(point){try{var pano=new BMap.Panorama('pano');var service=new BMap.PanoramaService();"
                + "service.getPanoramaByLocation(point,5000,function(data){if(data&&data.id){hideTip();pano.setId(data.id);pano.setPov({heading:0,pitch:0});}"
                + "else{hideTip();pano.setPosition(point);pano.setPov({heading:0,pitch:0});pano.addEventListener('emptyposition',showEmpty);setTimeout(showEmpty,2500);}});}"
                + "catch(e){showEmpty();}}"
                + "function init(){var fallback=new BMap.Point(" + longitude + "," + latitude + ");var local=new BMap.LocalSearch('',{onSearchComplete:function(results){"
                + "if(loaded)return;loaded=true;if(local.getStatus()==BMAP_STATUS_SUCCESS&&results&&results.getCurrentNumPois()>0){loadByPoint(results.getPoi(0).point);}else{loadByPoint(fallback);}}});"
                + "local.search(keyword);setTimeout(function(){if(!loaded){loaded=true;loadByPoint(fallback);}},1800);}window.onload=init;"
                + "</script></body></html>";
    }

    private String escapeJs(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
