package cn.navibeidou.beidou;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
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

import java.util.ArrayList;
import java.util.List;

import cn.navibeidou.beidou.Util.Constants;
import cn.navibeidou.beidou.Util.SpUtil;
import cn.navibeidou.beidou.Util.TimeStringUtil;

public class MapFragment extends Fragment implements AMapLocationListener, GeocodeSearch.OnGeocodeSearchListener {
    private static final String KEY_LAST_LAT = "map_last_lat";
    private static final String KEY_LAST_LON = "map_last_lon";
    private static final String KEY_LAST_CITY = "map_last_city";
    private OnFragmentInteractionListener mListener;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private Context mContext;
    private Context homeContext;
    MapView mMapView = null;
    AMap aMap;
    RouteSearch routeSearch;


    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;

    private String carAddress;
    private LatLng carGaodeLat;
    private LatLng userLatLng;

    private TextView text1;
    private TextView text2;
    private TextView input_edittext, tv_traffic, tv_poi, tv_weather;
    private boolean trafficVisible = false;
    float bearing = 0.0f;  // 地图默认方向

    public MapFragment() {
    }

    public static Fragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("navi", "fragment_map  onCreate");
    }

    GeocodeSearch geocodeSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        homeContext = this.getActivity();
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mContext = view.getContext();

        //获取地图控件引用
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        input_edittext = (TextView) view.findViewById(R.id.input_edittext);
        tv_traffic = (TextView) view.findViewById(R.id.tv_traffic);
        tv_weather = (TextView) view.findViewById(R.id.tv_weather);
        tv_poi = (TextView) view.findViewById(R.id.tv_poi);
        input_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PoiKeywordSearchActivity.class);
                intent.putExtra("latitude", currentLat);
                intent.putExtra("longitude", currentLon);
                intent.putExtra("city", city);
                startActivity(intent);
            }
        });
        tv_traffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!trafficVisible) {
                    trafficVisible = true;
                    aMap.setTrafficEnabled(true);//显示交通
                } else {
                    trafficVisible = false;
                    aMap.setTrafficEnabled(false);//不显示交通
                }
                CameraPosition cameraPosition = aMap.getCameraPosition();
                aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(cameraPosition.target, cameraPosition.zoom, cameraPosition.tilt, bearing)));

            }
        });
        tv_poi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PoiAroundSearchActivity.class);
                intent.putExtra("latitude", currentLat);
                intent.putExtra("longitude", currentLon);
                intent.putExtra("city", city);
                startActivity(intent);
            }
        });
        tv_weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WeatherSearchActivity.class);
                intent.putExtra("city", city);
                startActivity(intent);
            }
        });
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        initMap();
        Log.i("navi", "fragment_map  onCreateView");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving 为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(6000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();


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
//            aMap.getUiSettings().setCompassEnabled(true);
            aMap.getUiSettings().setAllGesturesEnabled(true);
            aMap.getUiSettings().setScaleControlsEnabled(true);
            aMap.getUiSettings().setZoomControlsEnabled(false);
//            aMap.getUiSettings().setMyLocationButtonEnabled(true);
            aMap.getUiSettings().setLogoBottomMargin(-50);//隐藏logo
            aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
            //初始化位置

            //只定位一次。
            applyCachedLocation();
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            myLocationStyle.showMyLocation(true);
            customLocationIcon(null);
            try {
                geocodeSearch = new GeocodeSearch(getActivity());
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
    private void applyCachedLocation() {
        if (mContext == null || aMap == null) {
            return;
        }
        float cachedLat = (float) SpUtil.get(mContext, KEY_LAST_LAT, 0f);
        float cachedLon = (float) SpUtil.get(mContext, KEY_LAST_LON, 0f);
        String cachedCity = (String) SpUtil.get(mContext, KEY_LAST_CITY, "");
        if (cachedLat == 0f || cachedLon == 0f) {
            return;
        }
        currentLat = cachedLat;
        currentLon = cachedLon;
        city = cachedCity;
        Constants.city = cachedCity;
        userLatLng = new LatLng(cachedLat, cachedLon);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17));
    }

    private void cacheLatestLocation(double lat, double lon, String currentCity) {
        if (mContext == null) {
            return;
        }
        SpUtil.put(mContext, KEY_LAST_LAT, (float) lat);
        SpUtil.put(mContext, KEY_LAST_LON, (float) lon);
        SpUtil.put(mContext, KEY_LAST_CITY, currentCity == null ? "" : currentCity);
    }

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
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
//        myLocationStyle.showMyLocation(true);
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

    private double currentLat, currentLon;
    private String city;
    private boolean firstMove = true;

    //车主定位
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                currentLat = amapLocation.getLatitude();//获取纬度
                currentLon = amapLocation.getLongitude();//获取经度
                city = amapLocation.getCity();
                Constants.city = city;
                amapLocation.getAccuracy();//获取精度信息
                userLatLng = new LatLng(currentLat, currentLon);
                cacheLatestLocation(currentLat, currentLon, city);
                if (firstMove) {
                    firstMove = false;
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17));
                }
                String time = TimeStringUtil.longToDate(amapLocation.getTime());
                Log.i("navi", "city  " + city + "  currentLat  " + currentLat + "  currentLon  " + currentLon + "  time  " + time);
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }


    private View getCustomView(String title) {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        try {
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            text1 = new TextView(getActivity());
            text1.setGravity(Gravity.CENTER);
            text1.setHeight(90);
            text1.setMinWidth(300);
            text1.setText(title);

            text2 = new TextView(getActivity());
            text2.setGravity(Gravity.CENTER);
            text1.setHeight(90);
            text2.setMinWidth(300);
            text2.setText(title);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.addView(text1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(text2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.height = 100;
            linearLayout.setLayoutParams(params);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return linearLayout;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

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

    private List<Double> latLngList = new ArrayList<>();

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
}
