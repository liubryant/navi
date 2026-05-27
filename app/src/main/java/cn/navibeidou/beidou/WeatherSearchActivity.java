package cn.navibeidou.beidou;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearch.OnWeatherSearchListener;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.DislikeInfo;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.PersonalizationPrompt;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;

import cn.navibeidou.beidou.Util.Constants;
import cn.navibeidou.beidou.toutiao.DislikeDialog;
import cn.navibeidou.beidou.toutiao.config.TTAdManagerHolder;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;

/**
 * 天气查询 示例查询
 */
public class WeatherSearchActivity extends Activity implements OnWeatherSearchListener {
    private TextView forecasttv;
    private TextView reporttime1;
    private TextView reporttime2;
    private TextView weather;
    private TextView Temperature;
    private TextView wind;
    private TextView humidity;
    private WeatherSearchQuery mquery;
    private WeatherSearch mweathersearch;
    private LocalWeatherLive weatherlive;
    private LocalWeatherForecast weatherforecast;
    private List<LocalDayWeatherForecast> forecastlist = null;
    private String cityname = "深圳市";//天气搜索的城市，可以写名称或adcode；
    private double latitude, longitude;
    private String cityIntent;
    private FrameLayout mExpressContainer;
    //    private ViewGroup container;
    //    private EditText editTextWidth, editTextHeight; // 编辑框输入的宽高
    private int adWidth, adHeight; // Ad宽高
    //    private CheckBox checkBoxFullWidth, checkBoxAutoHeight;
    private boolean isAdFullWidth, isAdAutoHeight; // 是否采用了ADSize.FULL_WIDTH，ADSize.AUTO_HEIGHT
    private boolean isPreloadVideo;
    private TTAdNative mTTAdNative;
    private TTNativeExpressAd mTTAd;
    private Context mContext;
    private AudioManager mAudioManager;
    private boolean mAdAudioMuted;

//        private String mCodeId = "901121253";//测试code
    private String mCodeId = "945690844";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        mContext = this;
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
//        setTitleBar();
        cityIntent = getIntent().getStringExtra("city");
        Log.i("navi", "city  " + cityIntent);
//        latitude = (double) getIntent().getDoubleExtra("latitude", 22.586241);
//        longitude = (double) getIntent().getDoubleExtra("longitude", 113.861147);
        init();
        if (Constants.isCloseAd) {
            Log.d("navi", "close weather ad");
            mCodeId = "888888888";
        }
        loadExpressAd(mCodeId);
//        loadExpressAd("901121253");
        //天气
        searchliveweather();
        searchforcastsweather();
        //加载横屏Ad
        isPreloadVideo = true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mTTAd != null) {
            muteAdAudio();
        }
    }

    @Override
    protected void onStop() {
        restoreAdAudio();
        super.onStop();
    }

    private void init() {
        mExpressContainer = (FrameLayout) findViewById(R.id.express_container);
        TextView city = (TextView) findViewById(R.id.city);
        city.setText(cityIntent);
        forecasttv = (TextView) findViewById(R.id.forecast);
        reporttime1 = (TextView) findViewById(R.id.reporttime1);
        reporttime2 = (TextView) findViewById(R.id.reporttime2);
        weather = (TextView) findViewById(R.id.weather);
        Temperature = (TextView) findViewById(R.id.temp);
        wind = (TextView) findViewById(R.id.wind);
        humidity = (TextView) findViewById(R.id.humidity);
//        container = (ViewGroup) findViewById(R.id.container);
    }

    private boolean isLargeScreen() {
        boolean result = getPackageManager().hasSystemFeature("oplus.feature.largescreen");
        Log.d("navi", "isLargeScreen  " + result);
        return result;
//        return true;
    }

    /**
     * 预报天气查询
     */
    private void searchforcastsweather() {
        mquery = new WeatherSearchQuery(cityname, WeatherSearchQuery.WEATHER_TYPE_FORECAST);//检索参数为城市和天气类型，实时天气为1、天气预报为2
        try {
            mweathersearch = new WeatherSearch(this);
            mweathersearch.setOnWeatherSearchListener(this);
            mweathersearch.setQuery(mquery);
            mweathersearch.searchWeatherAsyn(); //异步搜索
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 实时天气查询
     */
    private void searchliveweather() {
        mquery = new WeatherSearchQuery(cityname, WeatherSearchQuery.WEATHER_TYPE_LIVE);//检索参数为城市和天气类型，实时天气为1、天气预报为2
        try {
            mweathersearch = new WeatherSearch(this);
            mweathersearch.setOnWeatherSearchListener(this);
            mweathersearch.setQuery(mquery);
            mweathersearch.searchWeatherAsyn(); //异步搜索
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 实时天气查询回调
     */
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (weatherLiveResult != null && weatherLiveResult.getLiveResult() != null) {
                weatherlive = weatherLiveResult.getLiveResult();
                reporttime1.setText(weatherlive.getReportTime() + "发布");
                weather.setText(weatherlive.getWeather());
                Temperature.setText(weatherlive.getTemperature() + "°");
                wind.setText(weatherlive.getWindDirection() + "风     " + weatherlive.getWindPower() + "级");
                humidity.setText("湿度         " + weatherlive.getHumidity() + "%");
            } else {
//                ToastUtil.show(WeatherSearchActivity.this, R.string.no_result);
            }
        } else {
//            ToastUtil.showerror(WeatherSearchActivity.this, rCode);
            Log.d("navi", rCode + "");
        }
    }

    /**
     * 天气预报查询结果回调
     */
    @Override
    public void onWeatherForecastSearched(
            LocalWeatherForecastResult weatherForecastResult, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (weatherForecastResult != null && weatherForecastResult.getForecastResult() != null
                    && weatherForecastResult.getForecastResult().getWeatherForecast() != null
                    && weatherForecastResult.getForecastResult().getWeatherForecast().size() > 0) {
                weatherforecast = weatherForecastResult.getForecastResult();
                forecastlist = weatherforecast.getWeatherForecast();
                fillforecast();

            } else {
//                ToastUtil.show(WeatherSearchActivity.this, R.string.no_result);
            }
        } else {
//            ToastUtil.showerror(WeatherSearchActivity.this, rCode);
        }
    }

    private void fillforecast() {
        reporttime2.setText(weatherforecast.getReportTime() + "发布");
        String forecast = "";
        for (int i = 0; i < forecastlist.size(); i++) {
            LocalDayWeatherForecast localdayweatherforecast = forecastlist.get(i);
            String week = null;
            switch (Integer.valueOf(localdayweatherforecast.getWeek())) {
                case 1:
                    week = "周一";
                    break;
                case 2:
                    week = "周二";
                    break;
                case 3:
                    week = "周三";
                    break;
                case 4:
                    week = "周四";
                    break;
                case 5:
                    week = "周五";
                    break;
                case 6:
                    week = "周六";
                    break;
                case 7:
                    week = "周日";
                    break;
                default:
                    break;
            }
            String temp = String.format("%-3s/%3s",
                    localdayweatherforecast.getDayTemp() + "°",
                    localdayweatherforecast.getNightTemp() + "°");
            String date = localdayweatherforecast.getDate();
            forecast += date + "  " + week + "                       " + temp + "\n\n";
        }
        forecasttv.setText(forecast);
    }

    private String getPosId() {
        return getIntent().getStringExtra(Constants.POS_ID);
    }

    private int getMinVideoDuration() {
        return getIntent().getIntExtra(Constants.MIN_VIDEO_DURATION, 0);
    }

    private int getMaxVideoDuration() {
        return getIntent().getIntExtra(Constants.MAX_VIDEO_DURATION, 0);
    }

    private void loadExpressAd(String codeId) {
        mExpressContainer.removeAllViews();
        float expressViewWidth = 350;
        float expressViewHeight = 350;
        if (isLargeScreen()) {
            expressViewWidth = 350;
            expressViewHeight = 200;
        }
/*        try {
            expressViewWidth = Float.parseFloat(mEtWidth.getText().toString());
            expressViewHeight = Float.parseFloat(mEtHeight.getText().toString());
        } catch (Exception e) {
            expressViewHeight = 0; //高度设置为0,则高度会自适应
        }*/
        //step4:创建Ad请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //Ad位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求Ad数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板Adview的size,单位dp
                .build();
        //step5:请求Ad，对请求回调的Ad作渲染处理
        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
//                TToast.show(getActivity(), "load error : " + code + ", " + message);
                mExpressContainer.removeAllViews();
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    return;
                }
                mTTAd = ads.get(0);
                muteAdAudio();
                bindAdListener(mTTAd);
                startTime = System.currentTimeMillis();
                mTTAd.render();
            }
        });
    }

    private long startTime = 0;

    private boolean mHasShowDownloadActive = false;

    private void muteAdAudio() {
        if (mAudioManager == null || mAdAudioMuted) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            } else {
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
            mAdAudioMuted = true;
            Log.d("naviad", "广告音频已静音");
        } catch (Throwable throwable) {
            Log.w("naviad", "muteAdAudio fail", throwable);
        }
    }

    private void restoreAdAudio() {
        if (mAudioManager == null || !mAdAudioMuted) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            } else {
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            }
            Log.d("naviad", "广告音频已恢复");
        } catch (Throwable throwable) {
            Log.w("naviad", "restoreAdAudio fail", throwable);
        } finally {
            mAdAudioMuted = false;
        }
    }

    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
//                TToast.show(mContext, "Ad被点击");
            }

            @Override
            public void onAdShow(View view, int type) {
//                TToast.show(mContext, "Ad展示");
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.d("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
//                TToast.show(mContext, msg + " code:" + code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.d("ExpressView", "render suc:" + (System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
//                TToast.show(mContext, "渲染成功");
                mExpressContainer.removeAllViews();
                mExpressContainer.addView(view);
            }
        });
        //dislike设置
        bindDislike(ad, false);
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
//                TToast.show(getActivity(), "点击开始下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
//                    TToast.show(getActivity(), "下载中，点击暂停", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
//                TToast.show(getActivity(), "下载暂停，点击继续", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
//                TToast.show(getActivity(), "下载失败，点击重新下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled(String fileName, String appName) {
//                TToast.show(getActivity(), "安装完成，点击图片打开", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
//                TToast.show(getActivity(), "点击安装", Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * 设置Ad的不喜欢，注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板Ad中的 dislike区域不响应dislike事件。
     *
     * @param ad
     * @param customStyle 是否自定义样式，true:样式自定义
     */
    private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {


        if (!customStyle) {
            //使用自定义样式
            DislikeInfo dislikeInfo = ad.getDislikeInfo();
            if (dislikeInfo == null || dislikeInfo.getFilterWords() == null || dislikeInfo.getFilterWords().isEmpty()) {
                return;
            }
            final DislikeDialog dislikeDialog = new DislikeDialog(this, dislikeInfo);
            dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
                @Override
                public void onItemClick(FilterWord filterWord) {
                    //屏蔽Ad
//                    TToast.show(mContext, "点击 " + filterWord.getName());
                    //用户选择不喜欢原因后，移除Ad展示
                    mExpressContainer.removeAllViews();
                }
            });
            dislikeDialog.setOnPersonalizationPromptClick(new DislikeDialog.OnPersonalizationPromptClick() {
                @Override
                public void onClick(PersonalizationPrompt personalizationPrompt) {
//                    TToast.show(mContext, "点击了为什么看到此Ad");
                }
            });
            ad.setDislikeDialog(dislikeDialog);
            return;
        }
        //使用默认模板中默认dislike弹出样式
       /* ad.setDislikeCallback(WeatherSearchActivity.this, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }

            @Override
            public void onSelected(int i, String s) {
                //用户选择不喜欢原因后，移除Ad展示
                mExpressContainer.removeAllViews();
//                if (enforce) {
//                    TToast.show(mContext, "NativeExpressActivity 模版信息流 sdk强制移除View ");
//                }
            }


            @Override
            public void onCancel() {
                TToast.show(mContext, "点击取消 ");
            }

            @Override
            public void onRefuse() {

            }

        });*/
    }

    @Override
    public void onDestroy() {
        restoreAdAudio();
        super.onDestroy();
        if (mTTAd != null) {
            mTTAd.destroy();
        }
    }
}
