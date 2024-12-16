package cn.navibeidou.beidou;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.lbsapi.BMapManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import org.json.JSONObject;

import cn.navibeidou.beidou.Util.CommonUtil;
import cn.navibeidou.beidou.Util.Constants;
import cn.navibeidou.beidou.Util.SpUtil;
import cn.navibeidou.beidou.Util.UrlUtil;
import cn.navibeidou.beidou.okhttp.OkHttpUtils;
import cn.navibeidou.beidou.okhttp.callback.StringCallback;
import cn.navibeidou.beidou.okhttp.request.RequestCall;
import cn.navibeidou.beidou.toutiao.config.TTAdManagerHolder;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;
import cn.navibeidou.beidou.widget.CommonStartDialog;
import okhttp3.Call;


public class InitActivity extends AppCompatActivity {
    private int lauch_ad_status;
    private Context mContext;
    private RequestCall getCall;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        mContext = this;
        StatusNavUtils.setStatusBarColor(this, 0x00000000);
        lauch_ad_status = (int) SpUtil.get(this, "launch_ad", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // huawei market loadServerData close goToMainActivity
        // other close loadServerData打开goToMainActivity
        //oppo vivo xiaomi huawei other
        loadServerData();
//        firstRun();
//        goToMainActivity(lauch_ad_status);
    }

    private void goToMainActivity(int status) {
        if (true) {
            SpUtil.put(this, "launch_ad", 1);
            Log.d("navi", "初始化ttAd");

            TTAdManagerHolder.init(mContext);
            Intent intent = new Intent(InitActivity.this, SplashActivity.class);
            startActivity(intent);
            this.finish();
        } else {
//            SpUtil.put(this, "launch_ad", 0);
//            Intent intent = new Intent(InitActivity.this, SplashTbsActivity.class);
//            startActivity(intent);
//            this.finish();
        }
    }


    private void loadServerData() {
        getCall = OkHttpUtils.get().url(UrlUtil.URLPERMISSIONADOPEN).build();
        getCall.execute(getCallback);
    }

    private StringCallback getCallback = new StringCallback() {
        @Override
        public void onResponse(String response, int id) {
            try {
                String brand = Build.BRAND.toLowerCase();
                Log.d("navi", "brand " + brand);
                JSONObject object = new JSONObject(response);
                JSONObject data = object.getJSONObject("data");
                boolean isPermissionReceiveAd = data.getBoolean("isPermissionReceiveAd");
                //true打开Ad
//                isPermissionReceiveAd = true;
                int versionCodeRedis = data.getInt("versionCode");
                String adCloseType = data.getString("adCloseType");
                int currentVersion = CommonUtil.getAppVersionCode(mContext);
                Log.d("navi", "isPermissionReceiveAd  " + isPermissionReceiveAd
                        + "  versionCodeRedis  " + versionCodeRedis + "  currentVersion  " + currentVersion+ "  adCloseType  " + adCloseType);
                if (currentVersion >= versionCodeRedis) {//本地版本号大于等于服务器版本号
                    if (!isPermissionReceiveAd || adCloseType.contains(brand)) {
                        Constants.isCloseAd = true;//close ad
                        Log.d("navi", "close ad  Constants.isCloseAd  " + Constants.isCloseAd);
                    } else {
                        Constants.isCloseAd = false;
                        Log.d("navi", "openad " + Constants.isCloseAd);
                    }
                } else {
                    Constants.isCloseAd = false;
                    Log.d("navi", "openad 222 " + Constants.isCloseAd);
                }
                firstRun();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("navi", "openad  exception" + e);
                firstRun();

            }
        }

        @Override
        public void onError(Call call, Exception e, int id) {
//            goToMainActivity(lauch_ad_status);
            firstRun();
            Log.d("navi", "close ad onErrorNoConnect  " + Constants.isCloseAd);
            e.printStackTrace();
        }
    };


    private void firstRun() {
        SharedPreferences sharedPreferences = getSharedPreferences("FirstRun", 0);
        boolean first_run = sharedPreferences.getBoolean("First", true);
//        first_run = true;
        if (first_run) {
            CommonStartDialog commonNum = new CommonStartDialog(mContext, true);
            commonNum.showSheet();
        } else {
            TTAdManagerHolder.init(mContext);
            Log.d("navi", "初始化ttAd");
            //初始化组件化基础库, 所有友盟业务SDK都必须调用此初始化接口。
            UMConfigure.init(mContext, "6013dda26a2a470e8f97901a", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);

            Intent intent = new Intent(InitActivity.this, SplashActivity.class);
            startActivity(intent);
            this.finish();
        }
    }
}
