package cn.navibeidou.beidou;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Poi;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;

import java.util.ArrayList;
import java.util.List;

import cn.navibeidou.beidou.Util.Constants;
import cn.navibeidou.beidou.Util.ToastUtil;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;
import cn.navibeidou.beidou.toutiao.config.TTAdManagerHolder;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;

/**
 * 介绍poi周边搜索功能
 */
public class PoiAroundSearchActivity extends Activity implements OnClickListener,
        OnMapClickListener, OnInfoWindowClickListener, InfoWindowAdapter, OnMarkerClickListener,
        OnPoiSearchListener {
    private static final String ERROR_TAG = "navierror";
    private MapView mapview;
    private AMap mAMap;

    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    //    private LatLonPoint lp = new LatLonPoint(39.993743, 116.472995);// 116.472995,39.993743
    private LatLonPoint lp = new LatLonPoint(22.586241, 113.861147);// 116.472995,39.993743

    private Marker locationMarker; // 选择的点
    private Marker detailMarker;
    private Marker mlastMarker;
    private PoiSearch poiSearch;
    private myPoiOverlay poiOverlay;// poi图层
    private List<PoiItem> poiItems;// poi数据

    private RelativeLayout mPoiDetail;
    private TextView mPoiName, mPoiAddress;
    private String keyWord = "";
    private EditText mSearchText;
    private double latitude, longitude;
    private String city;
    private PoiItem selectedPoiItem;
    private TTAdNative mTTAdNative;
    private TTRewardVideoAd mRewardVideoAd;
    private String mRewardVideoCodeId = "982599527";
    private boolean pendingSearchAfterAd = false;
    private boolean searchExecutedForCurrentAd = false;
    private boolean rewardVideoSkipped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poiaroundsearch_activity);
        latitude = (double) getIntent().getDoubleExtra("latitude", 22.586241);
        longitude = (double) getIntent().getDoubleExtra("longitude", 113.861147);
        if (!isValidLatLng(latitude, longitude)) {
            Log.e(ERROR_TAG, "PoiAroundSearchActivity received invalid location lat="
                    + latitude + ", lon=" + longitude + ", use default fallback");
            latitude = 22.586241;
            longitude = 113.861147;
        }
        lp = new LatLonPoint(latitude, longitude);
        city = getIntent().getStringExtra("city");
        Log.e("navi", "city  " + city + "  latitude  " + latitude + "  longitude  " + longitude);
        Log.e(ERROR_TAG, "PoiAroundSearchActivity onCreate city=" + city
                + ", lat=" + latitude + ", lon=" + longitude);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        privacyCompliance();
        mapview = (MapView) findViewById(R.id.mapView);
        mapview.onCreate(savedInstanceState);
        init();
    }


    /**
     * 初始化AMap对象
     */
    private void init() {
        if (mAMap == null) {
            mAMap = mapview.getMap();
            mAMap.setOnMapClickListener(this);
            mAMap.setOnMarkerClickListener(this);
            mAMap.setOnInfoWindowClickListener(this);
            mAMap.setInfoWindowAdapter(this);
            mAMap.getUiSettings().setLogoBottomMargin(-50);//隐藏logo

            TextView searchButton = (TextView) findViewById(R.id.btn_search);
            searchButton.setOnClickListener(this);
            locationMarker = mAMap.addMarker(new MarkerOptions()
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory
                            .fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point4)))
                    .position(new LatLng(lp.getLatitude(), lp.getLongitude())));
            locationMarker.showInfoWindow();

        }
        setup();
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
    }

    private void setup() {
        mPoiDetail = (RelativeLayout) findViewById(R.id.poi_detail);
        mPoiDetail.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startDefaultDriveNavi(selectedPoiItem);
            }
        });
        mPoiName = (TextView) findViewById(R.id.poi_name);
        mPoiAddress = (TextView) findViewById(R.id.poi_address);
        mSearchText = (EditText) findViewById(R.id.input_edittext);

        // 初始化广告SDK
        if (!TTAdManagerHolder.isInit()) {
            Log.w("naviad", "TTAdSdk 未初始化，跳过POI页广告加载");
        } else if (mTTAdNative == null) {
            mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
            Log.d("naviad", "广告SDK初始化完成");
        }

        // 预加载激励视频广告
        if (mTTAdNative != null) {
            loadRewardVideoAd();
        }
    }
    /**
     * 开始进行poi搜索
     */
    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        if (keyWord == null || keyWord.length() == 0) {
            keyWord = mSearchText.getText().toString().trim();
        }
        Log.e(ERROR_TAG, "doSearchQuery keyword=" + keyWord
                + ", lp=" + (lp == null ? "null" : lp.getLatitude() + "," + lp.getLongitude())
                + ", city=" + city);
        if (keyWord.length() == 0) {
            Log.e(ERROR_TAG, "doSearchQuery blocked: empty keyword");
            ToastUtil.show(PoiAroundSearchActivity.this, "请输入搜索关键字");
            return;
        }
        currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        if (lp != null) {
            try {
                Log.e(ERROR_TAG, "start PoiSearch keyword=" + keyWord
                        + ", lat=" + lp.getLatitude()
                        + ", lon=" + lp.getLongitude()
                        + ", radius=5000");
                poiSearch = new PoiSearch(this, query);
                poiSearch.setOnPoiSearchListener(this);
                poiSearch.setBound(new SearchBound(lp, 5000, true));//
                // 设置搜索区域为以lp点为圆心，其周围5000米范围
                poiSearch.searchPOIAsyn();// 异步搜索
            } catch (AMapException e) {
                Log.e(ERROR_TAG, "PoiSearch create/search failed keyword=" + keyWord, e);
                throw new RuntimeException(e);
            }

        } else {
            Log.e(ERROR_TAG, "doSearchQuery blocked: lp is null, keyword=" + keyWord);
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapview.onResume();
        whetherToShowDetailInfo(false);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapview.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapview.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapview.onDestroy();
    }

    @Override
    public void onPoiItemSearched(PoiItem arg0, int arg1) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onPoiSearched(PoiResult result, int rcode) {
        Log.e(ERROR_TAG, "onPoiSearched rcode=" + rcode
                + ", resultNull=" + (result == null)
                + ", currentKeyword=" + keyWord);
        if (rcode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    Log.e(ERROR_TAG, "onPoiSearched matched query keyword=" + keyWord
                            + ", count=" + (poiItems == null ? -1 : poiItems.size())
                            + ", pageCount=" + poiResult.getPageCount());
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems != null && poiItems.size() > 0) {
                        PoiItem firstPoi = poiItems.get(0);
                        Log.e(ERROR_TAG, "first poi title=" + firstPoi.getTitle()
                                + ", point=" + (firstPoi.getLatLonPoint() == null ? "null"
                                : firstPoi.getLatLonPoint().getLatitude() + "," + firstPoi.getLatLonPoint().getLongitude())
                                + ", address=" + firstPoi.getSnippet());
                        //清除POI信息显示
                        whetherToShowDetailInfo(false);
                        selectedPoiItem = null;
                        //并还原点击marker样式
                        if (mlastMarker != null) {
                            resetlastmarker();
                        }
                        //清理之前搜索结果的marker
                        if (poiOverlay != null) {
                            poiOverlay.removeFromMap();
                        }
                        mAMap.clear();
                        poiOverlay = new myPoiOverlay(mAMap, poiItems);
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                        showPoiDetail(poiItems.get(0));

                        mAMap.addMarker(new MarkerOptions()
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory
                                        .fromBitmap(BitmapFactory.decodeResource(
                                                getResources(), R.drawable.point4)))
                                .position(new LatLng(lp.getLatitude(), lp.getLongitude())));

                        mAMap.addCircle(new CircleOptions()
                                .center(new LatLng(lp.getLatitude(),
                                        lp.getLongitude())).radius(5000)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.argb(50, 1, 1, 1))
                                .strokeWidth(2));

                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        Log.e(ERROR_TAG, "onPoiSearched no pois, suggestionCityCount=" + suggestionCities.size());
                        showSuggestCity(suggestionCities);
                    } else {
                        Log.e(ERROR_TAG, "onPoiSearched no result keyword=" + keyWord
                                + ", lat=" + lp.getLatitude() + ", lon=" + lp.getLongitude());
                        ToastUtil.show(PoiAroundSearchActivity.this,
                                R.string.no_result);
                    }
                } else {
                    Log.e(ERROR_TAG, "onPoiSearched ignored: query mismatch");
                }
            } else {
                Log.e(ERROR_TAG, "onPoiSearched no result object/query, keyword=" + keyWord);
                ToastUtil
                        .show(PoiAroundSearchActivity.this, R.string.no_result);
            }
        } else {
            Log.e(ERROR_TAG, "onPoiSearched error rcode=" + rcode + ", keyword=" + keyWord);
            ToastUtil.showerror(this.getApplicationContext(), rcode);
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(ERROR_TAG, "onMarkerClick hasObject=" + (marker != null && marker.getObject() != null));

        if (marker.getObject() != null) {
            whetherToShowDetailInfo(true);
            try {
                PoiItem mCurrentPoi = (PoiItem) marker.getObject();
                Log.e(ERROR_TAG, "marker poi selected title=" + mCurrentPoi.getTitle()
                        + ", point=" + (mCurrentPoi.getLatLonPoint() == null ? "null"
                        : mCurrentPoi.getLatLonPoint().getLatitude() + "," + mCurrentPoi.getLatLonPoint().getLongitude()));
                selectedPoiItem = mCurrentPoi;
                if (mlastMarker == null) {
                    mlastMarker = marker;
                } else {
                    // 将之前被点击的marker置为原来的状态
                    resetlastmarker();
                    mlastMarker = marker;
                }
                detailMarker = marker;
                detailMarker.setIcon(BitmapDescriptorFactory
                        .fromBitmap(BitmapFactory.decodeResource(
                                getResources(),
                                R.drawable.poi_marker_pressed)));

                showPoiDetail(mCurrentPoi);
            } catch (Exception e) {
                Log.e(ERROR_TAG, "onMarkerClick failed", e);
            }
        } else {
            whetherToShowDetailInfo(false);
            selectedPoiItem = null;
            if (mlastMarker != null) {
                resetlastmarker();
            }
        }


        return true;
    }

    // 将之前被点击的marker置为原来的状态
    private void resetlastmarker() {
        if (poiOverlay == null || mlastMarker == null) {
            return;
        }
        int index = poiOverlay.getPoiIndex(mlastMarker);
        if (index < 0) {
            mlastMarker = null;
            return;
        }
        if (index < 10) {
            mlastMarker.setIcon(BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(
                            getResources(),
                            markers[index])));
        } else {
            mlastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(getResources(), R.drawable.marker_other_highlight)));
        }
        mlastMarker = null;

    }


    private void setPoiItemDisplayContent(final PoiItem mCurrentPoi) {
        mPoiName.setText(mCurrentPoi.getTitle());
        mPoiAddress.setText(mCurrentPoi.getSnippet() + mCurrentPoi.getDistance());
    }

    private void showPoiDetail(PoiItem poiItem) {
        if (poiItem == null) {
            return;
        }
        Log.e(ERROR_TAG, "showPoiDetail title=" + poiItem.getTitle()
                + ", point=" + (poiItem.getLatLonPoint() == null ? "null"
                : poiItem.getLatLonPoint().getLatitude() + "," + poiItem.getLatLonPoint().getLongitude())
                + ", address=" + poiItem.getSnippet());
        selectedPoiItem = poiItem;
        setPoiItemDisplayContent(poiItem);
        whetherToShowDetailInfo(true);
    }


    @Override
    public View getInfoContents(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public View getInfoWindow(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void onInfoWindowClick(Marker arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                keyWord = mSearchText.getText().toString().trim();
                Log.e(ERROR_TAG, "search button clicked keyword=" + keyWord
                        + ", lat=" + latitude + ", lon=" + longitude
                        + ", rewardAdLoaded=" + (mRewardVideoAd != null));
                if (keyWord.length() == 0) {
                    Log.e(ERROR_TAG, "search button blocked: empty keyword");
                    ToastUtil.show(PoiAroundSearchActivity.this, "请输入搜索关键字");
                    break;
                }
                selectedPoiItem = null;
                pendingSearchAfterAd = true;
                searchExecutedForCurrentAd = false;
                rewardVideoSkipped = false;
                // 点击搜索按钮，先播放激励视频
                Log.d("naviad", "点击搜索按钮，准备播放激励视频");
                showRewardVideoAd();
                // 隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),
                            0);
                }
                break;

            default:
                break;
        }

    }

    private int[] markers = {R.drawable.poi_marker_1,
            R.drawable.poi_marker_2,
            R.drawable.poi_marker_3,
            R.drawable.poi_marker_4,
            R.drawable.poi_marker_5,
            R.drawable.poi_marker_6,
            R.drawable.poi_marker_7,
            R.drawable.poi_marker_8,
            R.drawable.poi_marker_9,
            R.drawable.poi_marker_10
    };

    private void whetherToShowDetailInfo(boolean isToShow) {
        if (isToShow) {
            mPoiDetail.setVisibility(View.VISIBLE);

        } else {
            mPoiDetail.setVisibility(View.GONE);

        }
    }

    private void startDefaultDriveNavi(PoiItem poiItem) {
        if (poiItem == null || poiItem.getLatLonPoint() == null) {
            Log.e(ERROR_TAG, "startDefaultDriveNavi blocked: poiItem invalid");
            ToastUtil.show(PoiAroundSearchActivity.this, "请选择要导航的位置");
            return;
        }
        LatLonPoint endPoint = poiItem.getLatLonPoint();
        Poi start = null;
        if (isValidLatLng(latitude, longitude)) {
            start = new Poi("我的位置", new LatLng(latitude, longitude), "");
        }
        Poi end = new Poi(poiItem.getTitle(),
                new LatLng(endPoint.getLatitude(), endPoint.getLongitude()),
                poiItem.getPoiId());
        AmapNaviParams params = new AmapNaviParams(start, null, end, AmapNaviType.DRIVER, AmapPageType.NAVI);
        params.setUseInnerVoice(true);
        params.setNeedCalculateRouteWhenPresent(true);
        privacyCompliance();
        Log.e(ERROR_TAG, "start drive navi from=" + (start == null ? "null" : start.getCoordinate())
                + ", to=" + (end == null ? "null" : end.getCoordinate())
                + ", current=" + latitude + "," + longitude
                + " to " + endPoint.getLatitude() + "," + endPoint.getLongitude()
                + " name=" + poiItem.getTitle());
        AmapNaviPage.getInstance().showRouteActivity(PoiAroundSearchActivity.this, params, null);
    }

    private boolean isValidLatLng(double lat, double lon) {
        return lat >= -90d && lat <= 90d
                && lon >= -180d && lon <= 180d
                && !(lat == 0d && lon == 0d);
    }

    private void privacyCompliance() {
        MapsInitializer.updatePrivacyShow(PoiAroundSearchActivity.this, true, true);
        MapsInitializer.updatePrivacyAgree(PoiAroundSearchActivity.this, true);
        AMapLocationClient.updatePrivacyShow(PoiAroundSearchActivity.this, true, true);
        AMapLocationClient.updatePrivacyAgree(PoiAroundSearchActivity.this, true);
        ServiceSettings.updatePrivacyShow(PoiAroundSearchActivity.this, true, true);
        ServiceSettings.updatePrivacyAgree(PoiAroundSearchActivity.this, true);
    }


    @Override
    public void onMapClick(LatLng arg0) {
        whetherToShowDetailInfo(false);
        selectedPoiItem = null;
        if (mlastMarker != null) {
            resetlastmarker();
        }
    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(this, infomation);

    }


    /**
     * 自定义PoiOverlay
     */

    private class myPoiOverlay {
        private AMap mamap;
        private List<PoiItem> mPois;
        private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();

        public myPoiOverlay(AMap amap, List<PoiItem> pois) {
            mamap = amap;
            mPois = pois;
        }

        /**
         * 添加Marker到地图中。
         *
         * @since V2.1.0
         */
        public void addToMap() {
            if (mPois != null) {
                int size = mPois.size();
                for (int i = 0; i < size; i++) {
                    Marker marker = mamap.addMarker(getMarkerOptions(i));
                    PoiItem item = mPois.get(i);
                    marker.setObject(item);
                    mPoiMarks.add(marker);
                }
            }
        }

        /**
         * 去掉PoiOverlay上所有的Marker。
         *
         * @since V2.1.0
         */
        public void removeFromMap() {
            for (Marker mark : mPoiMarks) {
                mark.remove();
            }
        }

        /**
         * 移动镜头到当前的视角。
         *
         * @since V2.1.0
         */
        public void zoomToSpan() {
            if (mPois != null && mPois.size() > 0) {
                if (mamap == null)
                    return;
                LatLngBounds bounds = getLatLngBounds();
                mamap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        }

        private LatLngBounds getLatLngBounds() {
            LatLngBounds.Builder b = LatLngBounds.builder();
            if (mPois != null) {
                int size = mPois.size();
                for (int i = 0; i < size; i++) {
                    b.include(new LatLng(mPois.get(i).getLatLonPoint().getLatitude(),
                            mPois.get(i).getLatLonPoint().getLongitude()));
                }
            }
            return b.build();
        }

        private MarkerOptions getMarkerOptions(int index) {
            return new MarkerOptions()
                    .position(
                            new LatLng(mPois.get(index).getLatLonPoint()
                                    .getLatitude(), mPois.get(index)
                                    .getLatLonPoint().getLongitude()))
                    .title(getTitle(index)).snippet(getSnippet(index))
                    .icon(getBitmapDescriptor(index));
        }

        protected String getTitle(int index) {
            return mPois.get(index).getTitle();
        }

        protected String getSnippet(int index) {
            return mPois.get(index).getSnippet();
        }

        /**
         * 从marker中得到poi在list的位置。
         *
         * @param marker 一个标记的对象。
         * @return 返回该marker对应的poi在list的位置。
         * @since V2.1.0
         */
        public int getPoiIndex(Marker marker) {
            for (int i = 0; i < mPoiMarks.size(); i++) {
                if (mPoiMarks.get(i).equals(marker)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 返回第index的poi的信息。
         *
         * @param index 第几个poi。
         * @return poi的信息。poi对象详见搜索服务模块的基础核心包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/PoiItem.html" title="com.amap.api.services.core中的类">PoiItem</a></strong>。
         * @since V2.1.0
         */
        public PoiItem getPoiItem(int index) {
            if (index < 0 || index >= mPois.size()) {
                return null;
            }
            return mPois.get(index);
        }

        protected BitmapDescriptor getBitmapDescriptor(int arg0) {
            if (arg0 < 10) {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(getResources(), markers[arg0]));
                return icon;
            } else {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(getResources(), R.drawable.marker_other_highlight));
                return icon;
            }
        }
    }

    /**
     * 加载激励视频广告
     */
    private void loadRewardVideoAd() {
        if (Constants.isCloseAd) {
            Log.d("naviad", "广告已关闭，不加载激励视频");
            Log.e(ERROR_TAG, "loadRewardVideoAd skipped: Constants.isCloseAd=true");
            return;
        }

        Log.d("naviad", "开始加载激励视频广告, codeId: " + mRewardVideoCodeId);
        Log.e(ERROR_TAG, "loadRewardVideoAd start codeId=" + mRewardVideoCodeId);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mRewardVideoCodeId)
                .setSupportDeepLink(true)
                .setRewardName("金币") // 奖励名称
                .setRewardAmount(10)   // 奖励数量
                .setUserID("user_id_" + System.currentTimeMillis()) // 用户id
                .setMediaExtra("media_extra") // 附加参数
                .setOrientation(TTAdConstant.VERTICAL) // 竖屏
                .build();

        if (mTTAdNative != null) {
            mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
                @Override
                public void onError(int code, String message) {
                    Log.e("naviad", "激励视频加载失败 code: " + code + ", message: " + message);
                    Log.e(ERROR_TAG, "reward ad load failed code=" + code + ", message=" + message);
                    mRewardVideoAd = null;
                }

                @Override
                public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                    Log.d("naviad", "激励视频加载成功");
                    Log.e(ERROR_TAG, "reward ad loaded");
                    mRewardVideoAd = ad;
                    // 设置视频广告的监听
                    mRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {
                        @Override
                        public void onAdShow() {
                            Log.d("naviad", "激励视频开始播放");
                            Log.e(ERROR_TAG, "reward ad show");
                        }

                        @Override
                        public void onAdVideoBarClick() {
                            Log.d("naviad", "激励视频被点击");
                        }

                        @Override
                        public void onAdClose() {
                            Log.d("naviad", "激励视频关闭");
                            Log.e(ERROR_TAG, "reward ad close");
                            if (!rewardVideoSkipped) {
                                executePendingSearch("adClose");
                            }
                            // 视频关闭，重新加载下一次使用
                            loadRewardVideoAd();
                        }

                        @Override
                        public void onVideoComplete() {
                            Log.d("naviad", "激励视频播放完成");
                            Log.e(ERROR_TAG, "reward ad video complete");
                            executePendingSearch("videoComplete");
                        }

                        @Override
                        public void onVideoError() {
                            Log.e("naviad", "激励视频播放出错");
                            Log.e(ERROR_TAG, "reward ad video error, execute search");
                            // 播放出错，执行搜索
                            executePendingSearch("videoError");
                        }

                        @Override
                        public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
                            Log.d("naviad", "激励视频奖励验证 rewardVerify: " + rewardVerify +
                                    ", rewardAmount: " + rewardAmount + ", rewardName: " + rewardName);
                            Log.e(ERROR_TAG, "reward verify rewardVerify=" + rewardVerify
                                    + ", rewardAmount=" + rewardAmount
                                    + ", rewardName=" + rewardName
                                    + ", errorCode=" + errorCode
                                    + ", errorMsg=" + errorMsg);
                            if (rewardVerify) {
                                // 奖励验证成功，视频播放完整，执行搜索
                                Log.d("naviad", "激励视频播放完整，执行POI搜索");
                                executePendingSearch("rewardVerify");
                            } else {
                                Log.w("naviad", "激励视频未完整播放，errorCode: " + errorCode + ", errorMsg: " + errorMsg);
                            }
                        }

                        @Override
                        public void onSkippedVideo() {
                            Log.d("naviad", "激励视频被跳过");
                            Log.e(ERROR_TAG, "reward ad skipped");
                            rewardVideoSkipped = true;
                            pendingSearchAfterAd = false;
                        }
                    });
                }

                @Override
                public void onRewardVideoCached() {
                    Log.d("naviad", "激励视频缓存成功");
                    Log.e(ERROR_TAG, "reward ad cached");
                }
            });
        } else {
            Log.e("naviad", "mTTAdNative为空，无法加载激励视频");
            Log.e(ERROR_TAG, "loadRewardVideoAd failed: mTTAdNative is null");
        }
    }

    /**
     * 播放激励视频广告
     */
    private void showRewardVideoAd() {
        if (mRewardVideoAd != null) {
            Log.d("naviad", "播放激励视频广告");
            Log.e(ERROR_TAG, "showRewardVideoAd show loaded ad");
            mRewardVideoAd.showRewardVideoAd(PoiAroundSearchActivity.this);
        } else {
            Log.w("naviad", "激励视频未加载或加载失败，直接执行搜索");
            Log.e(ERROR_TAG, "showRewardVideoAd no ad, execute search directly");
            executePendingSearch("noAd");
        }
    }

    private void executePendingSearch(String reason) {
        Log.e(ERROR_TAG, "executePendingSearch reason=" + reason
                + ", pending=" + pendingSearchAfterAd
                + ", executed=" + searchExecutedForCurrentAd
                + ", skipped=" + rewardVideoSkipped
                + ", keyword=" + keyWord);
        if (!pendingSearchAfterAd || searchExecutedForCurrentAd || rewardVideoSkipped) {
            return;
        }
        searchExecutedForCurrentAd = true;
        pendingSearchAfterAd = false;
        doSearchQuery();
    }
}
