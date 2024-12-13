package cn.navibeidou.beidou.navi;

import android.os.Bundle;

import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.NaviLatLng;

import cn.navibeidou.beidou.R;


public class RideRouteCalculateActivity extends BaseActivity {
    private double currentLat = 39.917337;
    private double currentLon = 116.397056;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_navi);
        currentLat = getIntent().getDoubleExtra("currentLat", 39.917337);
        currentLon = getIntent().getDoubleExtra("currentLon", 116.397056);
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
    }

    @Override
    public void onInitNaviSuccess() {
        super.onInitNaviSuccess();
        mAMapNavi.calculateRideRoute(new NaviLatLng(currentLat, currentLon), new NaviLatLng(22.674227, 113.811147));
    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {
        super.onCalculateRouteSuccess(aMapCalcRouteResult);
        mAMapNavi.startNavi(NaviType.GPS);
    }
}
