package cn.navibeidou.beidou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.amap.api.navi.INaviInfoCallback;
import com.amap.api.navi.model.AMapNaviLocation;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.DislikeInfo;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.PersonalizationPrompt;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.ArrayList;
import java.util.List;

import cn.navibeidou.beidou.Util.CheckPermissionsActivity;
import cn.navibeidou.beidou.Util.Constants;
import cn.navibeidou.beidou.navi.DriverListActivity;
import cn.navibeidou.beidou.navi.FeatureView;
import cn.navibeidou.beidou.navi.RideRouteCalculateActivity;
import cn.navibeidou.beidou.navi.TruckRouteCalculateActivity;
import cn.navibeidou.beidou.navi.WalkRouteCalculateActivity;
import cn.navibeidou.beidou.toutiao.DislikeDialog;
import cn.navibeidou.beidou.toutiao.config.TTAdManagerHolder;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;

/**
 * Created by shixin on 16/8/23.
 * <p>
 * bug反馈QQ:1438734562
 */
public class IndexActivity extends CheckPermissionsActivity implements INaviInfoCallback, View.OnClickListener {
    private LatLng p1 = new LatLng(39.993266, 116.473193);//首开广场
    private LatLng p2 = new LatLng(39.917337, 116.397056);//故宫博物院
    private LatLng p3 = new LatLng(39.904556, 116.427231);//北京站
    private LatLng p4 = new LatLng(39.773801, 116.368984);//新三余公园(南5环)
    private LatLng p5 = new LatLng(40.041986, 116.414496);//立水桥(北5环)
    private boolean isMap = false;
    private double currentLat = 39.917337;
    private double currentLon = 116.397056;
    private int naviType = 1;
    private LinearLayout ll_drive;
    private LinearLayout ll_walk;
    private LinearLayout ll_bike;
    private LinearLayout ll_truck;
    private Button btn_drive;
    private Button btn_walk;
    private Button btn_bike;
    private Button btn_truck;
    private Button btn_navi;
    private Button btn_near;
    private ImageView iv_arrow;
    private ImageView plus;
    private ImageView change;
    //    private EditText et_gps;
    private EditText et_destination;
    private LinearLayout ll_destination;
    private TextView text1;
    private ListAdapter adapter;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_drive:
            case R.id.et_destination:
                naviType = 1;
                updateModeButtons();
                AmapNaviParams params = new AmapNaviParams(null, null, null, AmapNaviType.DRIVER);
                params.setUseInnerVoice(true);
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params, IndexActivity.this);
                break;
            case R.id.btn_walk:
                naviType = 2;
                updateModeButtons();
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(),
                        new AmapNaviParams(null, null, new Poi("故宫博物院", null, ""),
                                AmapNaviType.WALK), IndexActivity.this);
                break;
            case R.id.btn_bike:
                naviType = 3;
                updateModeButtons();
                AmapNaviParams params3 = new AmapNaviParams(null, null, null, AmapNaviType.RIDE);
                params3.setUseInnerVoice(true);
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params3, IndexActivity.this);
                break;
            case R.id.btn_truck:
                naviType = 4;
                updateModeButtons();
                AmapNaviParams params4 = new AmapNaviParams(null, null, null, AmapNaviType.DRIVER);
                params4.setUseInnerVoice(true);
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params4, IndexActivity.this);
                break;
            case R.id.iv_arrow:
                finish();
                break;
            case R.id.btn_navi:
                naviType = 1;
                AmapNaviParams params5 = new AmapNaviParams(null, null, null, AmapNaviType.DRIVER);
                params5.setUseInnerVoice(true);
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params5, IndexActivity.this);
                break;
            case R.id.btn_near:
                //街景
                Intent intentquan = new Intent(this, QuanJingActivity.class);
                intentquan.putExtra("type", 1);
                intentquan.putExtra("latitude", currentLat);
                intentquan.putExtra("longitude", currentLon);
                Log.e("navi", "latitude  " + currentLat + "  longitude  " + currentLon);
                startActivity(intentquan);
                break;
            default:
                break;
        }
    }

    private static class DemoDetails {
        private final int titleId;
        private final int descriptionId;
        private final Class<? extends Activity> activityClass;

        public DemoDetails(int titleId, int descriptionId,
                           Class<? extends Activity> activityClass) {
            super();
            this.titleId = titleId;
            this.descriptionId = descriptionId;
            this.activityClass = activityClass;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        isMap = getIntent().getBooleanExtra("isMap", false);
        currentLat = getIntent().getDoubleExtra("currentLat", 39.917337);
        currentLon = getIntent().getDoubleExtra("currentLon", 116.397056);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        initView();
        privacyCompliance();
    }

    private void privacyCompliance() {
        /*MapsInitializer.updatePrivacyShow(IndexActivity.this,true,true);
        SpannableStringBuilder spannable = new SpannableStringBuilder("\"亲，感谢您对XXX一直以来的信任！我们依据最新的监管要求更新了XXX《隐私权政策》，特向您说明如下\n1.为向您提供交易相关基本功能，我们会收集、使用必要的信息；\n2.基于您的明示授权，我们可能会获取您的位置（为您提供附近的商品、店铺及优惠资讯等）等信息，您有权拒绝或取消授权；\n3.我们会采取业界先进的安全措施保护您的信息安全；\n4.未经您同意，我们不会从第三方处获取、共享或向提供您的信息；\n");
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 35, 42, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        new AlertDialog.Builder(this)
                .setTitle("温馨提示(隐私合规示例)")
                .setMessage(spannable)
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MapsInitializer.updatePrivacyAgree(IndexActivity.this,true);
                    }
                })
                .setNegativeButton("不同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MapsInitializer.updatePrivacyAgree(IndexActivity.this,false);
                    }
                })
                .show();*/
    }

    private void initView() {
        ll_drive = findViewById(R.id.ll_drive);
        ll_walk = findViewById(R.id.ll_walk);
        ll_bike = findViewById(R.id.ll_bike);
        ll_truck = findViewById(R.id.ll_truck);
        btn_drive = findViewById(R.id.btn_drive);
        btn_walk = findViewById(R.id.btn_walk);
        btn_bike = findViewById(R.id.btn_bike);
        btn_truck = findViewById(R.id.btn_truck);
        btn_navi = findViewById(R.id.btn_navi);
        btn_near = findViewById(R.id.btn_near);
        iv_arrow = findViewById(R.id.iv_arrow);
        plus = findViewById(R.id.plus);
        change = findViewById(R.id.change);
//        et_gps = findViewById(R.id.et_gps);
        et_destination = findViewById(R.id.et_destination);
        ll_drive.setOnClickListener(this);
        ll_walk.setOnClickListener(this);
        ll_bike.setOnClickListener(this);
        ll_truck.setOnClickListener(this);
        btn_drive.setOnClickListener(this);
        btn_bike.setOnClickListener(this);
        btn_walk.setOnClickListener(this);
        btn_truck.setOnClickListener(this);
        btn_navi.setOnClickListener(this);
        btn_near.setOnClickListener(this);
        iv_arrow.setOnClickListener(this);
        change.setOnClickListener(this);
        plus.setOnClickListener(this);
        et_destination.setOnClickListener(this);
        ListView listView = findViewById(R.id.list);
        setTitle("导航SDK " + AMapNavi.getVersion());

        adapter = new CustomArrayAdapter(
                this.getApplicationContext(), DEMOS);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(mItemClickListener);
        mExpressContainer = (FrameLayout) findViewById(R.id.express_container);
        if (mTTAdNative != null) {
            mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
        }
        if (Constants.isCloseAd) {
            Log.d("navi", "close weather ad");
            Constants.STREAM_ID = "888888888";
        }
        updateModeButtons();
        loadExpressAd(Constants.STREAM_ID);
    }

    private void updateModeButtons() {
        updateModeButton(btn_drive, naviType == 1);
        updateModeButton(btn_walk, naviType == 2);
        updateModeButton(btn_bike, naviType == 3);
        updateModeButton(btn_truck, naviType == 4);
    }

    private void updateModeButton(Button button, boolean selected) {
        if (button == null) {
            return;
        }
        button.setBackgroundResource(selected ? R.drawable.button_backgroud : R.drawable.button_backgroud_grey);
        button.setTextColor(selected ? Color.WHITE : Color.parseColor("#4D6486"));
    }

    private TTAdNative mTTAdNative;
    private TTNativeExpressAd mTTAd;
    private FrameLayout mExpressContainer;

    private boolean isLargeScreen() {
        boolean result = getPackageManager().hasSystemFeature("oplus.feature.largescreen");
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //直接进导航
        /*if (isMap) {
            isMap = false;
            AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(),
                    new AmapNaviParams(null, null, new Poi("故宫博物院", null, ""),
                            AmapNaviType.DRIVER), IndexActivity.this);
        } else {//回到地图页面
            isMap = true;
            finish();
        }*/
    }

    private static class CustomArrayAdapter extends ArrayAdapter<DemoDetails> {
        public CustomArrayAdapter(Context context, DemoDetails[] demos) {
            super(context, R.layout.feature, R.id.title, demos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FeatureView featureView;
            if (convertView instanceof FeatureView) {
                featureView = (FeatureView) convertView;
            } else {
                featureView = new FeatureView(getContext());
            }
            DemoDetails demo = getItem(position);
            featureView.setTitleId(demo.titleId, demo.activityClass != null);
            return featureView;
        }

    }

    private static final DemoDetails[] DEMOS = {
// 导航组件
            new DemoDetails(R.string.blank, R.string.blank, null),
            new DemoDetails(R.string.navi_ui, R.string.blank, null),
// 组件起终点算路
            new DemoDetails(R.string.navi_start_end_poi_calculate_title, R.string.navi_start_end_poi_calculate_desc, IndexActivity.class),
// 组件起终点算路
            new DemoDetails(R.string.navi_end_poi_calculate_title, R.string.navi_end_poi_calculate_desc, IndexActivity.class),
// 驾车路径规划
            new DemoDetails(R.string.navi_route_driver_title, R.string.navi_route_driver_desc, DriverListActivity.class),
// 步行路径规划
            new DemoDetails(R.string.navi_route_walk_title, R.string.navi_route_walk_desc, WalkRouteCalculateActivity.class),
// 骑行路径规划
            new DemoDetails(R.string.navi_route_ride_title, R.string.navi_route_ride_desc, RideRouteCalculateActivity.class),
// 货车路径规划导航
            new DemoDetails(R.string.navi_route_truck_title, R.string.navi_route_truck_title, TruckRouteCalculateActivity.class),
/*// 组件起终点算路
new DemoDetails(R.string.navi_bywayof_poi_calculate_title, R.string.navi_bywayof_poi_calculate_desc, com.amap.navi.demo.activity.IndexActivity.class),
// 直接导航
new DemoDetails(R.string.navi_ui_navi_title, R.string.navi_ui_navi_desc, com.amap.navi.demo.activity.IndexActivity.class),
// 组件起终点算路（白色主题）
new DemoDetails(R.string.navi_ui_custom_activity, R.string.navi_ui_custom_activity, com.amap.navi.demo.activity.IndexActivity.class),
     // 路径规划
     new DemoDetails(R.string.navi_route_line, R.string.blank, null),
     // 驾车路径规划
     new DemoDetails(R.string.navi_route_driver_title, R.string.navi_route_driver_desc, DriverListActivity.class),
     // 步行路径规划
     new DemoDetails(R.string.navi_route_walk_title, R.string.navi_route_walk_desc, WalkRouteCalculateActivity.class),
     // 骑行路径规划
     new DemoDetails(R.string.navi_route_ride_title, R.string.navi_route_ride_desc, RideRouteCalculateActivity.class),
     // 货车路径规划导航
     new DemoDetails(R.string.navi_route_truck_title, R.string.navi_route_truck_title, TruckRouteCalculateActivity.class),
     // 独立路线规划
     new DemoDetails(R.string.navi_route_independent_title, R.string.navi_route_independent_title, IndependentRouteCalculateActivity.class),
     // 导航类型
     new DemoDetails(R.string.navi_type, R.string.blank, null),
     // 内置语音导航
     new DemoDetails(R.string.navi_type_inner_voice, R.string.blank, EmulatorActivity.class),
     // 实时导航
     new DemoDetails(R.string.navi_type_gps_title, R.string.navi_type_gps_desc, GPSNaviActivity.class),
     // 模拟导航
     new DemoDetails(R.string.navi_type_emu_title, R.string.navi_type_emu_desc, EmulatorActivity.class),
     // 货车导航
     new DemoDetails(R.string.navi_type_truck, R.string.blank,SetTruckParamsActivity.class),
     // 智能巡航
     new DemoDetails(R.string.navi_type_intelligent_title, R.string.navi_type_intelligent_desc, IntelligentBroadcastActivity.class),
     // HUD导航
     new DemoDetails(R.string.navi_type_hud_title, R.string.navi_type_hud_desc, HudDisplayActivity.class),

     // 导航UI在自定义
     new DemoDetails(R.string.navi_ui_custom, R.string.blank, null),
     // 自定义车标
     new DemoDetails(R.string.navi_ui_custom_car_icon, R.string.blank, CustomCarActivity.class),
     // 自定义路线UI
     new DemoDetails(R.string.navi_ui_custom_route, R.string.blank, CustomRouteActivity.class),
     // 自定义路线纹理
     new DemoDetails(R.string.navi_ui_custom_route_style, R.string.blank, CustomRouteTextureInAMapNaviViewActivity.class),
     // 自定义路口转向提示
     new DemoDetails(R.string.navi_ui_custom_trun_across_tip, R.string.blank, CustomNextTurnTipViewActivity.class),
     // 正被漠视
     new DemoDetails(R.string.navi_ui_custom_northmode, R.string.blank, NorthModeActivity.class),
     // 自定义全览模式
     new DemoDetails(R.string.navi_ui_custom_wholescan_button, R.string.blank, OverviewModeActivity.class),
     // 自定义指南针
     new DemoDetails(R.string.navi_ui_custom_compass, R.string.blank, CustomDirectionViewActivity.class),
     // 自定义路况按钮
     new DemoDetails(R.string.navi_ui_custom_traffic_button, R.string.blank, CustomTrafficButtonViewActivity.class),
     // 自定义放大缩小按钮
     new DemoDetails(R.string.navi_ui_custom_zoom_button, R.string.blank, CustomZoomButtonViewActivity.class),
     // 自定义路口放大图
     new DemoDetails(R.string.navi_ui_custom_across_overlay, R.string.blank, CustomZoomInIntersectionViewActivity.class),
     // 自定义导航光柱(New)
     new DemoDetails(R.string.navi_ui_custom_traffic_bar_new, R.string.blank, CustomTrafficProgressBarActivity.class),
     // 自定义道路选择
     new DemoDetails(R.string.navi_ui_custom_route_select, R.string.blank, CustomDriveWayViewActivity.class),

     // 导航完全自定义示例
     new DemoDetails(R.string.navi_custom_all, R.string.blank, null),
     // 完全自定义自车位置和绘制路线
     new DemoDetails(R.string.navi_custom_car_route, R.string.blank, AllCustomCarRouteActivity.class),
     // 完全自定义路名、距离、下一路口图标
     new DemoDetails(R.string.navi_custom_road_distance_nexttip, R.string.blank, AllCustomNextRoadInfoActivity.class),
     // 完全自定义路况导航条
     new DemoDetails(R.string.navi_custom_traffic_bar, R.string.blank, AllCustomTrafficBarActivity.class),
     // 完全自定义车道信息
     new DemoDetails(R.string.navi_custom_route_way, R.string.blank, AllCustomDriveWayActivity.class),
     // 完全自定义路口放大图
     new DemoDetails(R.string.navi_custom_crossing, R.string.blank, AllCustomCrossingActivity.class),
     // 完全自定义摄像头违章摄影
     new DemoDetails(R.string.navi_custom_camera, R.string.blank, AllCustomCameraActivity.class),
     // 完全自定义导航
     new DemoDetails(R.string.navi_custom_navi, R.string.blank, AllCustomNaviActivity.class),


     // 导航扩展
     new DemoDetails(R.string.navi_expand, R.string.blank, null),
     // 传入GPS数据导航
     new DemoDetails(R.string.navi_expand_set_gps_data, R.string.blank, UseExtraGpsDataActivity.class),
     // 展示导航路径详情
     new DemoDetails(R.string.navi_expand_route_detail, R.string.blank, GetNaviStepsAndLinksActivity.class),
     // 主辅路切换
     new DemoDetails(R.string.navi_expand_switch_road, R.string.blank, SwitchMasterRoadNaviActivity.class),
     // 科大讯飞语音集成播报
     new DemoDetails(R.string.navi_expand_iflyt_voice, R.string.blank, IflyVoiceActivity.class),*/
    };

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 1) {
                AmapNaviParams params = new AmapNaviParams(new Poi("北京站", p3, ""), null, new Poi("故宫博物院", p2, ""), AmapNaviType.DRIVER);
                params.setUseInnerVoice(true);
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params, IndexActivity.this);
            } else if (position == 2) {
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), new AmapNaviParams(null, null, new Poi("故宫博物院", p2, ""), AmapNaviType.DRIVER), IndexActivity.this);
            } else if (position == 3) {
                List<Poi> poiList = new ArrayList();
                poiList.add(new Poi("首开广场", p1, ""));
                poiList.add(new Poi("故宫博物院", p2, ""));
                poiList.add(new Poi("北京站", p3, ""));

                AmapNaviParams params = new AmapNaviParams(new Poi("立水桥(北5环)", p5, ""), poiList, new Poi("新三余公园(南5环)", p4, ""), AmapNaviType.DRIVER);
                params.setUseInnerVoice(true);
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params, IndexActivity.this);
            } else if (position == 4) {
                //起点
                Poi start = new Poi("立水桥(北5环)", p5, "");
                //途经点
                List<Poi> poiList = new ArrayList();
                poiList.add(new Poi("首开广场", p1, ""));
                poiList.add(new Poi("故宫博物院", p2, ""));
                poiList.add(new Poi("北京站", p3, ""));
                //终点
                Poi end = new Poi("新三余公园(南5环)", p4, "");
                AmapNaviParams amapNaviParams = new AmapNaviParams(start, poiList, end, AmapNaviType.DRIVER, AmapPageType.NAVI);
                amapNaviParams.setUseInnerVoice(true);
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), amapNaviParams, IndexActivity.this);
            } else {
                /*DemoDetails demo = (DemoDetails) adapter.getItem(position);
                if (demo.activityClass != null) {
                    Intent intent = new Intent(IndexActivity.this, demo.activityClass);
                    intent.putExtra("currentLat", currentLat);
                    intent.putExtra("currentLon", currentLon);
                    startActivity(intent);
                }*/
            }
        }
    };

    /**
     * 返回键处理事件
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
//            System.exit(0);// 退出程序
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onInitNaviFailure() {
        Log.d("navi", "onInitNaviFailure ");

    }

    @Override
    public void onGetNavigationText(String s) {
    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
    }

    @Override
    public void onArriveDestination(boolean b) {
        Log.d("navi", "onArriveDestination ");

    }

    @Override
    public void onStartNavi(int i) {
        Log.d("navi", "onStartNavi ");

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        Log.d("navi", "onCalculateRouteSuccess ");

    }

    @Override
    public void onCalculateRouteFailure(int i) {
        Log.d("navi", "onCalculateRouteFailure ");

    }

    @Override
    public void onStopSpeaking() {
    }

    @Override
    public void onReCalculateRoute(int i) {
        Log.d("navi", "onReCalculateRoute ");

    }

    @Override
    public void onExitPage(int i) {
        Log.d("navi", "onExitPage fail: 退出页面");

    }

    @Override
    public void onStrategyChanged(int i) {
    }

    @Override
    public View getCustomNaviBottomView() {//返回null则不显示自定义区域
        return getCustomView("卫星导航地图");
    }

    @Override
    public View getCustomNaviView() {
        //返回null则不显示自定义区域
        return null;
    }

    @Override
    public void onArrivedWayPoint(int i) {
    }

    @Override
    public void onMapTypeChanged(int i) {
    }

    @Override
    public View getCustomMiddleView() {
        return null;
    }

    @Override
    public void onNaviDirectionChanged(int i) {
    }

    @Override
    public void onDayAndNightModeChanged(int i) {
    }

    @Override
    public void onBroadcastModeChanged(int i) {
        Log.d("navi", "onBroadcastModeChanged ");

    }

    @Override
    public void onScaleAutoChanged(boolean b) {
    }

    private View getCustomView(String title) {
        LinearLayout linearLayout = new LinearLayout(this);
        try {
            if (naviType == 1) {
                title = title + "驾车导航";
            } else if (naviType == 2) {
                title = title + "步行导航";
            } else if (naviType == 3) {
                title = title + "骑行导航";
            } else {
                title = title + "货车导航";
            }
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            text1 = new TextView(this);
            text1.setGravity(Gravity.CENTER);
            text1.setHeight(90);
            text1.setMinWidth(300);
            text1.setText(title);
            text1.setTextColor(Color.BLACK);
            text1.setTextSize(12);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.addView(text1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.height = 100;
            linearLayout.setLayoutParams(params);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return linearLayout;
    }

    private void loadExpressAd(String codeId) {
        mExpressContainer.removeAllViews();
        float expressViewWidth = 350;
        float expressViewHeight = 350;
        if (isLargeScreen()) {
            expressViewHeight = 300;
        }
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //Ad位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求Ad数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板Adview的size,单位dp
                .build();
        //step5:请求Ad，对请求回调的Ad作渲染处理
        if (mTTAdNative != null) {
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
                    bindAdListener(mTTAd);
                    startTime = System.currentTimeMillis();
                    mTTAd.render();
                }
            });
        }
    }

    private long startTime = 0;

    private boolean mHasShowDownloadActive = false;

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTTAd != null) {
            mTTAd.destroy();
        }
    }
}
