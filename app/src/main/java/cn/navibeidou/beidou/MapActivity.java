package cn.navibeidou.beidou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.RouteSearch;
import com.baidu.lbsapi.BMapManager;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.DislikeInfo;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.PersonalizationPrompt;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.navibeidou.beidou.Util.Constants;
import cn.navibeidou.beidou.Util.TimeStringUtil;
import cn.navibeidou.beidou.toutiao.DislikeDialog;
import cn.navibeidou.beidou.toutiao.config.TTAdManagerHolder;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;

public class MapActivity extends AppCompatActivity implements AMapLocationListener, GeocodeSearch.OnGeocodeSearchListener, View.OnClickListener {
    public BMapManager mBMapManager = null;
    private boolean isHadOpenGame = false;
    private int count = 0;
    private boolean isInteraction;
    private long startTime = 0;
    private boolean mHasShowDownloadActive = false;
    final Timer timer = new Timer();
    private String mVerticalCodeId;
    DrawerLayout drawerLayout;
    private AdLoadListener mAdLoadListener;
    ActionBarDrawerToggle toggle;
    private TTNativeExpressAd mTTAd;
    //    private FrameLayout mBannerContainer;
    private FrameLayout mExpressContainer;
    private TTAdNative mTTAdNative;
    private Button mCreativeButton;
    private OnFragmentInteractionListener mListener;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private Context mContext;
    MapView mMapView = null;
    LinearLayout ll_service, ll_yinsi, ll_feedback, ll_normal, ll_satellite, ll_bus, ll_quanjian, ll_weather_left;
    AMap aMap;
    RouteSearch routeSearch;
    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;

    private String carAddress;
    private LatLng carGaodeLat;
    private LatLng userLatLng;
    GeocodeSearch geocodeSearch;

    private ImageView iv_normal, iv_satellite, iv_bus;
    private TextView input_edittext, tv_traffic, tv_poi, tv_weather, tv_vedio, tv_title, tv_metro, tv_north, tv_quanjin, tv_current_location, tv_game;
    private boolean trafficVisible = false;
    private boolean isEn = false;
    float bearing = 0.0f;  // 地图默认方向
    private List<Double> latLngList = new ArrayList<>();

    private double currentLat, currentLon;
    private String city;
    private Toolbar toolbar;
    private boolean firstMove = true;
    CameraPosition cameraPosition1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(cameraPosition1.target, cameraPosition1.zoom, cameraPosition1.tilt, bearing)));
                    break;
                case 99:
                    showAd();
                    break;
                case 200:
//                    showGame();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        privacyCompliance();
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_map);
        getExtraInfo();
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        initSlide();
        //获取地图控件引用
        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        initView();
        initEngineManager(getApplicationContext());
        Log.i("navi", "mapac  onCreateView");

        if (mContext instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) mContext;
        }
        if (TTAdManagerHolder.isInit()) {
            execAd();
        } else {
            Log.d("navi", "TTAdSdk 未初始化，跳过广告SDK请求与加载");
        }
    }

    private void privacyCompliance() {
        MapsInitializer.updatePrivacyShow(MapActivity.this, true, true);
        MapsInitializer.updatePrivacyAgree(MapActivity.this, true);
//        SpannableStringBuilder spannable = new SpannableStringBuilder("\"亲，感谢您对XXX一直以来的信任！我们依据最新的监管要求更新了XXX《隐私权政策》，特向您说明如下\n1.为向您提供交易相关基本功能，我们会收集、使用必要的信息；\n2.基于您的明示授权，我们可能会获取您的位置（为您提供附近的商品、店铺及优惠资讯等）等信息，您有权拒绝或取消授权；\n3.我们会采取业界先进的安全措施保护您的信息安全；\n4.未经您同意，我们不会从第三方处获取、共享或向提供您的信息；\n");
//        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 35, 42, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        new AlertDialog.Builder(this)
//                .setTitle("温馨提示(隐私合规示例)")
//                .setMessage(spannable)
//                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        MapsInitializer.updatePrivacyAgree(MapActivity.this,true);
//                    }
//                })
//                .setNegativeButton("不同意", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        MapsInitializer.updatePrivacyAgree(MapActivity.this,false);
//                    }
//                })
//                .show();
    }

    private void execAd() {
        if (Constants.isCloseAd) {
            Log.d("navi", "close banner ad " + Constants.isCloseAd);
            Constants.BANNER_ID = "888888888";
            Constants.INTERACTION_ID = "888888888";
            tv_game.setVisibility(View.GONE);

        } else {
            //模板渲染bannerexpress
            //进来先加载interaction ad
            loadExpressAd(Constants.BANNER_ID, 300, 250);
            Log.d("navi", "Constants.isCloseAd " + Constants.isCloseAd + " Constants.APPID " + Constants.APPID + " Constants.INTERACTION_ID "
                    + Constants.INTERACTION_ID + " Constants.BANNER_ID " + Constants.BANNER_ID + " Constants.STREAM_ID " + Constants.STREAM_ID
                    + " Constants.OPEN_ID " + Constants.OPEN_ID +
                    " Constants.count " + Constants.count + " Constants.time " + Constants.time);
        }


        if (mAdLoadListener == null || Constants.INTERACTION_ID.equals("888888888")) {
            TimerTask task = new TimerTask() {
                public void run() {
                    count++;
                    Log.d("navi", "loading success interaction " + Constants.isCloseAd + " count " + count);
                    loadAd(mVerticalCodeId);
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(99, 1000);
                    }
                    if (count == Constants.count) {
                        timer.cancel();
                    }
                }
            };
            timer.schedule(task, 0, Constants.time); // 定时器每隔3秒钟执行一次任务
        }

    }

    private void getExtraInfo() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mVerticalCodeId = intent.getStringExtra("vertical_rit");
        isInteraction = intent.getBooleanExtra("is_interaction", false);
    }

    private void showAd() {
        if (mAdLoadListener == null || Constants.INTERACTION_ID.equals("888888888")) {
            Log.d("navi", "loading fail interaction " + Constants.isCloseAd);
            return;
        }
        mAdLoadListener.showAd(TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
    }


    private void loadAd(final String codeId) {
        //step5:创建ad请求参数AdSlot
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) // ad代码位Id
//                .setAdLoadType(TTAdLoadType.LOAD) // 本次ad用途：TTAdLoadType.LOAD实时；TTAdLoadType.PRELOAD预请求
                .build();
        //step6:注册ad加载生命周期监听
        mAdLoadListener = new AdLoadListener(this);
        if (mTTAdNative != null) {
            mTTAdNative.loadFullScreenVideoAd(adSlot, mAdLoadListener);
        }
    }

    private void initView() {
        input_edittext = findViewById(R.id.input_edittext);
        tv_current_location = findViewById(R.id.tv_current_location);
        ll_service = findViewById(R.id.ll_service);
        ll_yinsi = findViewById(R.id.ll_yinsi);
        ll_feedback = findViewById(R.id.ll_feedback);
        ll_normal = findViewById(R.id.ll_normal);
        ll_satellite = findViewById(R.id.ll_satellite);
        ll_bus = findViewById(R.id.ll_bus);
        ll_quanjian = findViewById(R.id.ll_quanjian);
        ll_weather_left = findViewById(R.id.ll_weather_left);
        iv_normal = findViewById(R.id.iv_normal);
        iv_satellite = findViewById(R.id.iv_satellite);
        iv_bus = findViewById(R.id.iv_bus);
        ll_normal.setOnClickListener(this);
        ll_satellite.setOnClickListener(this);
        ll_bus.setOnClickListener(this);
        ll_quanjian.setOnClickListener(this);
        ll_weather_left.setOnClickListener(this);
        tv_traffic = findViewById(R.id.tv_traffic);
        tv_weather = findViewById(R.id.tv_weather);
        tv_metro = findViewById(R.id.tv_metro);
        tv_poi = findViewById(R.id.tv_poi);
        tv_vedio = findViewById(R.id.tv_vedio);
        tv_title = findViewById(R.id.tv_title);
        tv_north = findViewById(R.id.tv_north);
        tv_quanjin = findViewById(R.id.tv_quanjin);
        tv_game = findViewById(R.id.tv_game);
        tv_title.setText(getString(R.string.app_name) + "V" + getVersionName(mContext));
        input_edittext.setOnClickListener(this);
        tv_traffic.setOnClickListener(this);
        tv_poi.setOnClickListener(this);
        tv_weather.setOnClickListener(this);
        tv_metro.setOnClickListener(this);
        tv_north.setOnClickListener(this);
        tv_quanjin.setOnClickListener(this);
        tv_game.setOnClickListener(this);
        tv_vedio.setOnClickListener(this);
        ll_service.setOnClickListener(this);
        ll_yinsi.setOnClickListener(this);
        ll_feedback.setOnClickListener(this);
    }

    private void initSlide() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setOnClickListener(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);//将toolbar与ActionBar关联
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);//初始化状态
        toggle.syncState();

        //蒙层颜色
        drawerLayout.setScrimColor(getResources().getColor(R.color.trans));
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                View mContent = drawerLayout.getChildAt(0);
                View mMenu = drawerView;
//                ViewHelper.setTranslationX(mContent, mMenu.getMeasuredWidth() * slideOffset);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                onClickShowBanner();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                loadExpressAd(Constants.BANNER_ID, 300, 250);
            }
        });

        //穿山甲bannerAd
        mExpressContainer = (FrameLayout) findViewById(R.id.banner_container);
//        mBannerContainer = (FrameLayout) findViewById(R.id.banner_container);
        //step2:创建TTAdNative对象
        if (mTTAdNative != null) {
            mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
        }
        // 不在启动页或首页初始化阶段主动申请电话权限。
        // 广告 SDK 在缺少 READ_PHONE_STATE 时仍可工作，只是可能影响部分广告填充，
        // 因此权限申请应延后到用户实际触发相关业务功能时再进行。
    }

    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    private void initMap() {
        //初始化地图控制器对象
        try {
            mlocationClient = new AMapLocationClient(mContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(this);
        //设置定位模式为 Hight_Accuracy 高精度模式，Battery_Saving 为低功耗模式，Device_Sensors 是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();
        Log.d("navi", "initMap startLocation ");
        if (aMap == null) {
            aMap = mMapView.getMap();
//            aMap.setTrafficEnabled(true);//显示交通
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);//卫星地图模式
//            myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
//            myLocationStyle.interval(6000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
//            aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
            setupLocationStyle();
            //右上角定位按钮
//            aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
            //罗盘
            aMap.getUiSettings().setCompassEnabled(true);
            aMap.getUiSettings().setAllGesturesEnabled(true);
            aMap.getUiSettings().setScaleControlsEnabled(true);
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.getUiSettings().setLogoBottomMargin(-50);//隐藏logo
            aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
            //初始化位置

            //只定位一次。
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            myLocationStyle.showMyLocation(true);
            customLocationIcon(null);
            try {
                geocodeSearch = new GeocodeSearch(MapActivity.this);
            } catch (AMapException e) {
                throw new RuntimeException(e);
            }
            geocodeSearch.setOnGeocodeSearchListener(this);
        }
    }


    //设置不自动移动到中心圆点
    public void customLocationIcon(View view) {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_cam_camera);
        myLocationStyle.myLocationIcon(bitmapDescriptor);
        aMap.setMyLocationStyle(myLocationStyle);
    }

    private MyLocationStyle myLocationStyle;

    /**
     * 设置自定义定位蓝点
     */
    private void setupLocationStyle() {
        // 自定义系统定位蓝点
        myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.drawable.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(STROKE_COLOR);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
        //只定位一次。
//      myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
//      myLocationStyle.showMyLocation(true);
    }

//    LatLng p6 = new LatLng(22.586241, 113.861147);//侨鸿盛

    private LatLng coordinate(LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter(mContext);
        // CoordType.GPS 待转换坐标类型
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标点 LatLng类型
        converter.coord(sourceLatLng);
        // 执行转换操作
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                currentLat = amapLocation.getLatitude();//获取纬度
                currentLon = amapLocation.getLongitude();//获取经度
                String address = amapLocation.getAddress();
                city = amapLocation.getCity();
                toolbar.setTitle(city);
                amapLocation.getAccuracy();//获取精度信息
                userLatLng = new LatLng(currentLat, currentLon);
                if (firstMove) {
                    firstMove = false;
                    Log.i("navi", "moveCamera");
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18));
                }
                String time = TimeStringUtil.longToDate(amapLocation.getTime());
                Log.i("navi", "city  " + city + "  currentLat  " + currentLat + "  currentLon  " + currentLon + "  time  " + time);
                tv_current_location.setText("经度: " + currentLat + "    纬度: " + currentLon + "\n位置: " + address);
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("navi AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
//        destroyAd();
        mListener = null;
        if (mCreativeButton != null) {
            mCreativeButton = null;
        }
        if (mTTAd != null) {
            mTTAd.destroy();
        }
        if (mAdLoadListener != null) {
            mAdLoadListener = null;
        }
        if (mTTAdNative != null) {
            mTTAdNative = null;
        }
        if (mHandler != null) {
            mHandler = null;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        initMap();
    }

    /*private void showPopupWindow() {
        // 创建弹出窗口的布局
        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);

        // 创建 PopupWindow 对象并设置相关属性
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true); // 设置焦点，使弹出窗口可以响应触摸事件
        popupWindow.setOutsideTouchable(true); // 点击弹出窗口外部可关闭弹出窗口

        // 显示弹出窗口在底部
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
    }*/

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    private void getAddressByLatlng(LatLng latLng) {
        //逆地理编码查询条件：逆地理编码查询的地理坐标点、查询范围、坐标类型。
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 500f, GeocodeSearch.AMAP);
        //异步查询
        geocodeSearch.getFromLocationAsyn(query);
    }

    //得到逆地理编码异步查询结果
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
        String formatAddress = regeocodeAddress.getFormatAddress();
        String simpleAddress = formatAddress.substring(9);
        Log.i("liuzheng  ", "simpleAddress  " + simpleAddress);
        carAddress = formatAddress;
//        tvChoseAddress.setText("查询经纬度对应详细地址：\n" + simpleAddress);
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    public synchronized String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*private void showGame() {
        // 设置Activity为全透明
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // 创建对话框
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setCancelable(false);

        // 设置对话框布局
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_layout, null);
        dialogBuilder.setView(dialogView);

        // 设置对话框背景为透明
        dialogView.setBackgroundColor(Color.TRANSPARENT);

        // 设置对话框图片
        ImageView imageView = dialogView.findViewById(R.id.dialog_image);
        imageView.setImageResource(R.mipmap.vip); // 替换为你的图片资源

        // 设置按钮点击事件
        ImageView button = dialogView.findViewById(R.id.dialog_button);

        // 创建对话框并显示
        final AlertDialog dialog = dialogBuilder.create();

        // 设置对话框背景为透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intentGame = new Intent(MapActivity.this, GameActivity.class);
                startActivity(intentGame);
            }
        });
        // 显示对话框
        dialog.show();
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_normal:
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                iv_normal.setImageResource(R.mipmap.normal_select);
                iv_satellite.setImageResource(R.mipmap.map_mode_satellite);
                iv_bus.setImageResource(R.mipmap.map_mode_bus);
                break;
            case R.id.ll_satellite:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                iv_normal.setImageResource(R.mipmap.map_mode_normal);
                iv_satellite.setImageResource(R.mipmap.satellite_select);
                iv_bus.setImageResource(R.mipmap.map_mode_bus);
                break;
            case R.id.ll_bus:
                aMap.setMapType(AMap.MAP_TYPE_BUS);
                iv_normal.setImageResource(R.mipmap.map_mode_normal);
                iv_satellite.setImageResource(R.mipmap.map_mode_satellite);
                iv_bus.setImageResource(R.mipmap.bus_select);
                break;

            case R.id.input_edittext:
                Intent intent = new Intent(MapActivity.this, IndexActivity.class);
                intent.putExtra("isMap", true);
                intent.putExtra("currentLat", currentLat);
                intent.putExtra("currentLon", currentLon);
                startActivity(intent);
                if (timer != null) {
                    timer.cancel();
                }
                break;
            case R.id.tv_traffic:
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                if (!trafficVisible) {
                    trafficVisible = true;
                    aMap.setTrafficEnabled(true);//显示交通
                } else {
                    trafficVisible = false;
                    aMap.setTrafficEnabled(false);//不显示交通
                }
                cameraPosition1 = aMap.getCameraPosition();
                aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(cameraPosition1.target, cameraPosition1.zoom, cameraPosition1.tilt, -15f)));
                mHandler.sendEmptyMessageDelayed(0, 200);
                break;

            case R.id.tv_poi:
                Intent intentpoi = new Intent(MapActivity.this, PoiAroundSearchActivity.class);
                intentpoi.putExtra("latitude", currentLat);
                intentpoi.putExtra("longitude", currentLon);
                intentpoi.putExtra("city", city);
                startActivity(intentpoi);
                break;
            case R.id.tv_weather:
            case R.id.ll_weather_left:
                Intent intentweather = new Intent(MapActivity.this, WeatherSearchActivity.class);
                intentweather.putExtra("city", city);
                startActivity(intentweather);
                break;
            case R.id.tv_metro:
                Intent intentmetro = new Intent(MapActivity.this, JsActivity.class);
                startActivity(intentmetro);
                break;
            case R.id.toolbar:
                changeSlide();
                break;
            case R.id.tv_north:
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                cameraPosition1 = aMap.getCameraPosition();
                aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(cameraPosition1.target, cameraPosition1.zoom, cameraPosition1.tilt, -15f)));
                mHandler.sendEmptyMessageDelayed(0, 200);
                break;
            case R.id.tv_quanjin:
            case R.id.ll_quanjian:
                Intent intentquan = new Intent(MapActivity.this, QuanJingActivity.class);
                intentquan.putExtra("type", 1);
                intentquan.putExtra("latitude", currentLat);
                intentquan.putExtra("longitude", currentLon);
                Log.e("navi", "latitude  " + currentLat + "  longitude  " + currentLon);
                startActivity(intentquan);
                break;
            case R.id.tv_game:
                //进来第二次播放一次全屏ad
                Intent intentGame = new Intent(MapActivity.this, GameActivity.class);
                startActivity(intentGame);
                break;
            case R.id.tv_vedio:
                if (!isEn) {
                    isEn = true;
                    aMap.setMapLanguage("en");
                    iv_normal.setImageResource(R.mipmap.normal_select);
                    iv_satellite.setImageResource(R.mipmap.map_mode_satellite);
                } else {
                    isEn = false;
                    aMap.setMapLanguage("zh_cn");
                    aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                    iv_normal.setImageResource(R.mipmap.map_mode_normal);
                    iv_satellite.setImageResource(R.mipmap.satellite_select);
                }
                iv_bus.setImageResource(R.mipmap.map_mode_bus);
                break;
            case R.id.ll_service:
                Intent intentservice = new Intent(mContext, ProtocolActivity.class);
                intentservice.putExtra("service", true);
                mContext.startActivity(intentservice);
                break;
            case R.id.ll_yinsi:
                Intent intentyinsi = new Intent(mContext, ProtocolActivity.class);
                intentyinsi.putExtra("yinsi", true);
                mContext.startActivity(intentyinsi);
                break;
            case R.id.ll_feedback:
                Intent intentFeed = new Intent(mContext, WebActivity.class);
                intentFeed.putExtra("feedback", true);
                mContext.startActivity(intentFeed);
                break;
            default:
                break;
        }
    }

    void changeSlide() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void loadExpressAd(String codeId, int expressViewWidth, int expressViewHeight) {
        mExpressContainer.removeAllViews();
        //step4:创建Ad请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //Ad位id
                .setAdCount(1) //请求Ad数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板Adview的size,单位dp
                .build();
        //step5:请求Ad，对请求回调的Ad作渲染处理
        if (mTTAdNative != null) {
            mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
                @Override
                public void onError(int code, String message) {
                    Log.d("navi", "load error : " + code + ", " + message);
                    mExpressContainer.removeAllViews();
                }

                @Override
                public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                    if (ads == null || ads.size() == 0) {
                        return;
                    }
                    mTTAd = ads.get(0);
                    mTTAd.setSlideIntervalTime(30 * 1000);
                    bindAdListener(mTTAd);
                    startTime = System.currentTimeMillis();
                    Log.d("navi", "load success!");
                    //加载Ad
                    onClickShowBanner();
                }
            });
        }
    }

    public void onClickShowBanner() {
        if (mTTAd != null) {
            mTTAd.render();
        } else {
            Log.d("liu", "load Ad..");
        }
    }


    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
            }

            @Override
            public void onAdShow(View view, int type) {
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.d("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.d("ExpressView", "render suc:" + (System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
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
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
            }

            @Override
            public void onInstalled(String fileName, String appName) {
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
            }
        });
    }

    /**
     * 设置Ad的不喜欢, 注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板Ad中的 dislike区域不响应dislike事件。
     *
     * @param ad
     * @param customStyle 是否自定义样式，true:样式自定义
     */
    private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
        if (!customStyle) {
            //使用自定义样式
            final DislikeInfo dislikeInfo = ad.getDislikeInfo();
            if (dislikeInfo == null || dislikeInfo.getFilterWords() == null || dislikeInfo.getFilterWords().isEmpty()) {
                return;
            }
            final DislikeDialog dislikeDialog = new DislikeDialog(this, dislikeInfo);
            dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
                @Override
                public void onItemClick(FilterWord filterWord) {
//                    TToast.show(mContext, "点击 " + filterWord.getName());
                    mExpressContainer.removeAllViews();
                }
            });
            dislikeDialog.setOnPersonalizationPromptClick(new DislikeDialog.OnPersonalizationPromptClick() {
                @Override
                public void onClick(PersonalizationPrompt personalizationPrompt) {
//                    TToast.show(mContext, "why Ad");
                }
            });
            ad.setDislikeDialog(dislikeDialog);
            return;
        }
        //使用默认模板中默认dislike弹出样式
        /*ad.setDislikeCallback(MapActivity.this, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }

            @Override
            public void onSelected(int position, String value, boolean enforce) {
                TToast.show(mContext, "点击 " + value);
                mExpressContainer.removeAllViews();
                //用户选择不喜欢原因后，移除Ad展示
                if (enforce) {
                    TToast.show(mContext, "模版Banner 穿山甲sdk强制将view关闭了");
                }
            }

            @Override
            public void onCancel() {
                TToast.show(mContext, "点击取消 ");
            }

        });*/
    }

    public static class AdSizeModel {
        public AdSizeModel(String adSizeName, int width, int height, String codeId) {
            this.adSizeName = adSizeName;
            this.width = width;
            this.height = height;
            this.codeId = codeId;
        }

        public String adSizeName;
        public int width;
        public int height;
        public String codeId;
    }

    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }
        if (!mBMapManager.init(new ApplicationShared.MyGeneralListener())) {
            Log.d("navi", "BMapManager  初始化错误!");
        }
    }

    private class AdLoadListener implements TTAdNative.FullScreenVideoAdListener {

        private static final String TAG = "MapActivity";
        private final Activity mActivity;

        private TTFullScreenVideoAd mAd;

        public AdLoadListener(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void onError(int code, String message) {
            Log.d(TAG, "Callback --> onError: " + code + ", " + message);
        }

        @Override

        public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
            Log.d(TAG, "Callback --> onFullScreenVideoAdLoad");
            handleAd(ad);
        }

        @Override

        public void onFullScreenVideoCached() {
            // 已废弃 请使用 onRewardVideoCached(TTRewardVideoAd ad) 方法
        }

        public void handleAd(TTFullScreenVideoAd ad) {
            if (mAd != null) {
                return;
            }
            mAd = ad;
            //【必须】ad展示时的生命周期监听

            mAd.setFullScreenVideoAdInteractionListener(new AdLifeListener(mActivity));
            //【可选】监听下载状态
            mAd.setDownloadListener(new DownloadStatusListener());
        }

        public void showAd(TTAdConstant.RitScenes ritScenes, String scenes) {
            if (mAd == null) {
                return;
            }
            mAd.showFullScreenVideoAd(mActivity, ritScenes, scenes);
            mAd = null;
        }
    }

    /**
     * 【必须】ad生命状态监听器
     */
    private class AdLifeListener implements TTFullScreenVideoAd.FullScreenVideoAdInteractionListener {

        private static final String TAG = "MapActivity";
        private final WeakReference<Context> mContextRef;

        public AdLifeListener(Context context) {
            mContextRef = new WeakReference<>(context);
        }

        @Override

        public void onAdShow() {
            Log.d(TAG, "Callback --> FullVideoAd show");

//            TToast.show(mContextRef.get(), "FullVideoAd show");
        }

        @Override

        public void onAdVideoBarClick() {
            Log.d(TAG, "Callback --> FullVideoAd bar click");

//            TToast.show(mContextRef.get(), "FullVideoAd bar click");
        }

        @Override

        public void onAdClose() {
            Log.d(TAG, "Callback --> FullVideoAd close");
//            TToast.show(mContextRef.get(), "FullVideoAd close");
            if (!isHadOpenGame) {
                isHadOpenGame = true;
                mHandler.sendEmptyMessageDelayed(200, 500);
            }
        }

        @Override
        public void onVideoComplete() {
            Log.d(TAG, "Callback --> FullVideoAd complete");

//            TToast.show(mContextRef.get(), "FullVideoAd complete");
        }

        @Override
        public void onSkippedVideo() {
            Log.d(TAG, "Callback --> FullVideoAd skipped");

//            TToast.show(mContextRef.get(), "FullVideoAd skipped");
        }
    }

    private static class DownloadStatusListener implements TTAppDownloadListener {

        @Override
        public void onIdle() {
        }

        @Override
        public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
            Log.d("DML", "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
        }

        @Override
        public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
            Log.d("DML", "onDownloadPaused===totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
        }

        @Override
        public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
            Log.d("DML", "onDownloadFailed==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
        }

        @Override
        public void onDownloadFinished(long totalBytes, String fileName, String appName) {
            Log.d("DML", "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
        }

        @Override
        public void onInstalled(String fileName, String appName) {
            Log.d("DML", "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
        }
    }

    private static String getAdType(int type) {
        switch (type) {

            case TTAdConstant.AD_TYPE_COMMON_VIDEO:
                return "普通全屏视频，type=" + type;

            case TTAdConstant.AD_TYPE_PLAYABLE_VIDEO:
                return "Playable全屏视频，type=" + type;

            case TTAdConstant.AD_TYPE_PLAYABLE:
                return "纯Playable，type=" + type;

        /*case TTAdConstant.AD_TYPE_LIVE:
            return "直播流，type=" + type;*/
        }

        return "未知类型+type=" + type;
    }
}
