package cn.navibeidou.beidou;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.CameraPosition;
import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.model.BaiduPanoData;
import com.baidu.lbsapi.model.BaiduPoiPanoData;
import com.baidu.lbsapi.panoramaview.ImageMarker;
import com.baidu.lbsapi.panoramaview.OnTabMarkListener;
import com.baidu.lbsapi.panoramaview.PanoramaRequest;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.baidu.lbsapi.panoramaview.TextMarker;
import com.baidu.lbsapi.tools.CoordinateConverter;
import com.baidu.lbsapi.tools.Point;
import com.baidu.pano.platform.plugin.indooralbum.IndoorAlbumCallback;
import com.baidu.pano.platform.plugin.indooralbum.IndoorAlbumPlugin;

import cn.navibeidou.beidou.indoor.AlbumContainer;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;

/**
 * 全景Demo主Activity
 */
public class QuanJingActivity extends Activity {

    private static final String LTAG = "liu";

    private PanoramaView mPanoView;
    private TextView textTitle;
    private Button btnImageMarker, btnTextMarker;// 添加移除marker测试
    private Button btnIsShowArrow, btnArrowStyle01, btnArrowStyle02;// 全景其他功能测试
    private Button btnIsShowInoorAblum;

    private View seekPitchLayout, seekHeadingLayout, seekLevelLayout;
    private SeekBar seekPitch, seekHeading, seekLevel;// 俯仰角,偏航角,全景图缩放测试

    private boolean isAddImageMarker = false;
    private boolean isAddTextMarker = false;
    private boolean isShowArrow = false;
    private boolean isShowAblum = true;
    private double latitude, longitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        // 先初始化BMapManager
        initBMapManager();
        setContentView(R.layout.panodemo_main);

        initView();

        Intent intent = getIntent();
        if (intent != null) {
            double currentLat = intent.getDoubleExtra("latitude", 0);
            double currentLon = intent.getDoubleExtra("longitude", 0);
            Log.i("navi", "quanjing  currentLat  " + currentLat + "  currentLon  " + currentLon);
            // 原始点经纬度
            Point sourcePoint =
                    new Point(currentLat, currentLon);
            Point resultPointLL = CoordinateConverter.converter(CoordinateConverter.COOR_TYPE.COOR_TYPE_GCJ02, sourcePoint);
            latitude = resultPointLL.y;
            longitude = resultPointLL.x;
            //测试经纬度
//            latitude = 23.0012;
//            longitude = 123.0012;
            Log.e("navi", "quanjing  latitude  " + latitude + "  longitude  " + longitude);
            testPanoByType(intent.getIntExtra("type", -1));
        }
    }

    private void initBMapManager() {
        ApplicationShared app = (ApplicationShared) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(app);
            app.mBMapManager.init(new ApplicationShared.MyGeneralListener());
        }
    }

    private void initView() {
        textTitle = (TextView) findViewById(R.id.panodemo_main_title);
        mPanoView = (PanoramaView) findViewById(R.id.panorama);
        btnImageMarker = (Button) findViewById(R.id.panodemo_main_btn_imagemarker);
        btnTextMarker = (Button) findViewById(R.id.panodemo_main_btn_textmarker);
        btnIsShowArrow = (Button) findViewById(R.id.panodemo_main_btn_showarrow);
        btnArrowStyle01 = (Button) findViewById(R.id.panodemo_main_btn_arrowstyle_01);
        btnArrowStyle02 = (Button) findViewById(R.id.panodemo_main_btn_arrowstyle_02);
        btnIsShowInoorAblum = (Button) findViewById(R.id.panodemo_main_btn_indoor_album);

        btnImageMarker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isAddImageMarker) {
                    addImageMarker();
                    btnImageMarker.setText("删除图片标注");
                } else {
                    removeImageMarker();
                    btnImageMarker.setText("添加图片标注");
                }
                isAddImageMarker = !isAddImageMarker;
            }
        });

        btnTextMarker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isAddTextMarker) {
                    addTextMarker();
                    btnTextMarker.setText("删除文字标注");
                } else {
                    removeTextMarker();
                    btnTextMarker.setText("添加文字标注");
                }
                isAddTextMarker = !isAddTextMarker;
            }
        });

        seekPitchLayout = findViewById(R.id.seekpitch_ly);
        seekHeadingLayout = findViewById(R.id.seekheading_ly);
        seekLevelLayout = findViewById(R.id.seeklevel_ly);
        seekPitch = (SeekBar) findViewById(R.id.seekpitch);
        seekLevel = (SeekBar) findViewById(R.id.seeklevel);
        seekHeading = (SeekBar) findViewById(R.id.seekheading);

        seekPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPanoView.setPanoramaPitch(progress - 90);
            }
        });
        seekHeading.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPanoView.setPanoramaHeading(progress);
            }
        });
        seekLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPanoView.setPanoramaZoomLevel(progress + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public static final int PID = 0;// PID方式
    public static final int GEO = 1;// 经纬度方式
    public static final int MERCATOR = 2;// 墨卡托方式
    public static final int UID_STREET = 3;// UID方式展示外景
    public static final int UID_INTERIOR = 4;// UID方式展示内景
    public static final int UID_STREET_CUSTOMALBUM = 5;// UID方式展示外景(使用自定义相册)
    public static final int MARKER = 6;// 标注
    public static final int OTHER = 7;// 其他测试
    public static final int COORDINATE_CONVERTER = 8;// 坐标转换测试
    public static final int WGS84 = 9;// 大地坐标系方式
    public static final int GCJ02 = 10;// 国测局加密方式

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast toast = Toast.makeText(QuanJingActivity.this, "该经纬度位置暂无卫星街景信息！", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                default:
                    break;
            }
        }
    };
    private void testPanoByType(int type) {
        mPanoView.setShowTopoLink(true);
        hideMarkerButton();
        hideSeekLayout();
        hideOtherLayout();
        hideIndoorAblumLayout();

        // 测试回调函数,需要注意的是回调函数要在setPanorama()之前调用，否则回调函数可能执行异常
        mPanoView.setPanoramaViewListener(new PanoramaViewListener() {

            @Override
            public void onLoadPanoramaBegin() {
                Log.i(LTAG, "onLoadPanoramaStart...");
//                ng: quanjing  latitude  23.0012  longitude  123.0012
//                2021-04-29 14:17:48.734 8744-8744/cn.navibeidou.beidou E/liu: 进入卫星地图坐标全景lat123.0012  lon  23.0012
//                2021-04-29 14:17:48.735 8744-9092/cn.navibeidou.beidou I/liu: onLoadPanoramaStart...
//                2021-04-29 14:17:49.001 8744-9092/cn.navibeidou.beidou I/liu: onLoadPanoramaError : {"code":102,"error":"描述信息加载失败"}
            }

            @Override
            public void onLoadPanoramaEnd(String json) {
                Log.i(LTAG, "onLoadPanoramaEnd : " + json);
            }

            @Override
            public void onLoadPanoramaError(String error) {
                Log.i(LTAG, "onLoadPanoramaError : " + error);
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onDescriptionLoadEnd(String json) {

            }

            @Override
            public void onMessage(String msgName, int msgType) {

            }

            @Override
            public void onCustomMarkerClick(String key) {

            }

            @Override
            public void onMoveStart() {

            }

            @Override
            public void onMoveEnd() {

            }
        });

        if (type == QuanJingActivity.PID) {
            textTitle.setText(R.string.demo_desc_pid);

            mPanoView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionHigh);
            String pid = "0900220000141205144547300IN";
            mPanoView.setPanorama(pid);
        } else if (type == QuanJingActivity.WGS84) {
            textTitle.setText(R.string.demo_desc_wgs84);
            mPanoView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
//            double lat = 39.906283536127169;
//            double lon = 116.39129554889048;
            double lon = latitude;
            double lat = longitude;
            mPanoView.setPanorama(lon, lat, PanoramaView.COORDTYPE_WGS84);
        } else if (type == QuanJingActivity.GCJ02) {
            textTitle.setText(R.string.demo_desc_gcj02);
            mPanoView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
//            double lat = 39.907687;
//            double lon = 116.397539;
            double lon = latitude;
            double lat = longitude;
            mPanoView.setPanorama(lon, lat, PanoramaView.COORDTYPE_GCJ02);
        } else if (type == QuanJingActivity.GEO) {
            textTitle.setText(R.string.demo_desc_geo);
            mPanoView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
            double baidulon = latitude;
            double baidulat = longitude;
//            double lat = 39.91403075654526;
//            double lon = 116.40391285827147;
            //侨鸿盛位置
            Log.d("liu", "进入卫星地图坐标全景lat" + baidulat + "  lon  " + baidulon);
            double lat = 22.59244047344863;
//            double lon = 113.86765681704155;
            double lon = 130.86765681704155;
//            mPanoView.setPanorama(lon, lat, PanoramaView.COORDTYPE_BD09LL);
            mPanoView.setPanorama(baidulon, baidulat, PanoramaView.COORDTYPE_BD09LL);
        } else if (type == QuanJingActivity.MERCATOR) {
            textTitle.setText(R.string.demo_desc_mercator);
            mPanoView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
            int mcX = 12958165;
            int mcY = 4825783;
            mPanoView.setPanorama(mcX, mcY, PanoramaView.COORDTYPE_BD09MC);
        } else if (type == QuanJingActivity.UID_STREET) {
            // 默认相册
            IndoorAlbumPlugin.getInstance().init();
            mPanoView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
            textTitle.setText(R.string.demo_desc_uid_street);
            mPanoView.setPanoramaZoomLevel(5);
            mPanoView.setArrowTextureByUrl("http://d.lanrentuku.com/down/png/0907/system-cd-disk/arrow-up.png");
            String uid = "7aea43b75f0ee3e17c29bd71";
            mPanoView.setPanoramaByUid(uid, PanoramaView.PANOTYPE_STREET);
        } else if (type == QuanJingActivity.UID_INTERIOR) {
            // 默认相册
            IndoorAlbumPlugin.getInstance().init();
            mPanoView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionHigh); // 设置清晰度
            IndoorAlbumCallback.EntryInfo info = new IndoorAlbumCallback.EntryInfo();
            info.setEnterPid("0900220000141205144547300IN");
            IndoorAlbumPlugin.getInstance().loadAlbumView(mPanoView, info);

            textTitle.setText(R.string.demo_desc_uid_interior);
            showIndoorAblumLayout();

            mPanoView.setPanoramaByUid("7c5e480b109e67adacb22aae", PanoramaView.PANOTYPE_INTERIOR);

            btnIsShowInoorAblum.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!isShowAblum) {
                        btnIsShowInoorAblum.setText("隐藏内景相册");
                        mPanoView.setIndoorAlbumVisible();
                    } else {
                        btnIsShowInoorAblum.setText("显示内景相册");
                        mPanoView.setIndoorAlbumGone();
                    }
                    isShowAblum = !isShowAblum;
                }
            });
        } else if (type == QuanJingActivity.UID_STREET_CUSTOMALBUM) {
            // 自定义相册
            IndoorAlbumPlugin.getInstance().init(new IndoorAlbumCallback() {

                @Override
                public View loadAlbumView(PanoramaView panoramaView, EntryInfo info) {
                    if (panoramaView != null && info != null) {
                        View albumView = LayoutInflater.from(panoramaView.getContext())
                                .inflate(R.layout.baidupano_photoalbum_container, null);
                        if (albumView != null) {
                            AlbumContainer mAlbumContainer =
                                    (AlbumContainer) albumView.findViewById(R.id.page_pano_album_view);
                            TextView mTvAddress = (TextView) albumView.findViewById(R.id.page_pano_album_address);
                            mAlbumContainer.setControlView(panoramaView, mTvAddress);
                        }
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) albumView.getLayoutParams();
                        if (lp == null) {
                            lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                        }
                        lp.gravity = Gravity.BOTTOM;
                        albumView.setLayoutParams(lp);
                        AlbumContainer albumContainer =
                                (AlbumContainer) albumView.findViewById(R.id.page_pano_album_view);
                        albumContainer.startLoad(panoramaView.getContext(), info);
                        return albumView;
                    } else {
                        return null;
                    }
                }
            });

            textTitle.setText(R.string.demo_desc_uid_street_customalbum);

            mPanoView.setPanoramaZoomLevel(5);
            mPanoView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionHigh);
            String uid = "7aea43b75f0ee3e17c29bd71";
            mPanoView.setPanoramaByUid(uid, PanoramaView.PANOTYPE_STREET);
        } else if (type == QuanJingActivity.MARKER) {
            textTitle.setText(R.string.demo_desc_marker);
            mPanoView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle); // 设置清晰度
            showMarkerButton();
            mPanoView.setPanorama("0900220001150514054806738T5");
            mPanoView.setShowTopoLink(false);
        } else if (type == QuanJingActivity.OTHER) {
            textTitle.setText(R.string.demo_desc_other);

            showSeekLayout();
            showOtherLayout();

            mPanoView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
            String pid = "0900220001150514054806738T5";
            mPanoView.setPanorama(pid);

            // 测试获取内景的相册描述信息和服务推荐描述信息
            testPanoramaRequest();

            btnIsShowArrow.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!isShowArrow) {
                        mPanoView.setShowTopoLink(false);
                        btnIsShowArrow.setText("显示全景箭头");
                    } else {
                        mPanoView.setShowTopoLink(true);
                        btnIsShowArrow.setText("隐藏全景箭头");
                    }
                    isShowArrow = !isShowArrow;
                }
            });

            btnArrowStyle01.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mPanoView.setArrowTextureByUrl("http://d.lanrentuku.com/down/png/0907/system-cd-disk/arrow-up.png");
                }
            });

            btnArrowStyle02.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.street_arrow);
                    mPanoView.setArrowTextureByBitmap(bitmap);
                }
            });
        }

    }

    private void testPanoramaRequest() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                PanoramaRequest panoramaRequest = PanoramaRequest.getInstance(QuanJingActivity.this);

                String pid = "01002200001307201550572285B";
                Log.d(LTAG, "PanoramaRecommendInfo");
                Log.i(LTAG, panoramaRequest.getPanoramaRecommendInfo(pid).toString());

                String iid = "978602fdf6c5856bddee8b62";
                Log.d(LTAG, "PanoramaByIIdWithJson");
                Log.i(LTAG, panoramaRequest.getPanoramaByIIdWithJson(iid).toString());

                // 通过卫星地图经纬度坐标获取当前位置相关全景信息，包括是否有外景，外景PID，外景名称等
                double lat = 40.029233;
                double lon = 116.32085;
                BaiduPanoData mPanoDataWithLatLon = panoramaRequest.getPanoramaInfoByLatLon(lon, lat);
                Log.d(LTAG, "PanoDataWithLatLon");
                Log.i(LTAG, mPanoDataWithLatLon.getDescription());

                // 通过卫星地图墨卡托坐标获取当前位置相关全景信息，包括是否有外景，外景PID，外景名称等
                int x = 12948920;
                int y = 4842480;
                BaiduPanoData mPanoDataWithXy = panoramaRequest.getPanoramaInfoByMercator(x, y);

                Log.d(LTAG, "PanoDataWithXy");
                Log.i(LTAG, mPanoDataWithXy.getDescription());

                // 通过卫星地图地图uid获取该poi下的全景描述信息，以此来判断此UID下是否有内景及外景
                String uid = "bff8fa7deabc06b9c9213da4";
                BaiduPoiPanoData poiPanoData = panoramaRequest.getPanoramaInfoByUid(uid);
                Log.d(LTAG, "poiPanoData");
                Log.i(LTAG, poiPanoData.getDescription());
            }
        }).start();

    }

    // 隐藏添加删除标注按钮
    private void hideMarkerButton() {
        btnImageMarker.setVisibility(View.GONE);
        btnTextMarker.setVisibility(View.GONE);
    }

    // 显示添加删除标注按钮
    private void showMarkerButton() {
        btnImageMarker.setVisibility(View.VISIBLE);
        btnTextMarker.setVisibility(View.VISIBLE);
    }

    // 隐藏设置俯仰角偏航角SeekBar
    private void hideSeekLayout() {
        seekPitchLayout.setVisibility(View.GONE);
        seekHeadingLayout.setVisibility(View.GONE);
        seekLevelLayout.setVisibility(View.GONE);
    }

    // 显示设置俯仰角偏航角SeekBar
    private void showSeekLayout() {
        seekPitchLayout.setVisibility(View.VISIBLE);
        seekHeadingLayout.setVisibility(View.VISIBLE);
        seekLevelLayout.setVisibility(View.VISIBLE);
    }

    // 隐藏其他功能测试
    private void hideOtherLayout() {
        btnIsShowArrow.setVisibility(View.GONE);
        btnArrowStyle01.setVisibility(View.GONE);
        btnArrowStyle02.setVisibility(View.GONE);
    }

    // 显示其他功能测试
    private void showOtherLayout() {
        btnIsShowArrow.setVisibility(View.VISIBLE);
        btnArrowStyle01.setVisibility(View.VISIBLE);
        btnArrowStyle02.setVisibility(View.VISIBLE);
    }

    // 隐藏内景相册测试
    private void hideIndoorAblumLayout() {
        btnIsShowInoorAblum.setVisibility(View.GONE);
    }

    // 显示内景相册测试
    private void showIndoorAblumLayout() {
        btnIsShowInoorAblum.setVisibility(View.VISIBLE);
    }

    private ImageMarker marker1;
    private ImageMarker marker2;

    /**
     * 添加图片标注
     */
    private void addImageMarker() {
        // 天安门西南方向
        marker1 = new ImageMarker();
        marker1.setMarkerPosition(new Point(116.356329, 39.890534));
        marker1.setMarkerHeight(2.3f);
        marker1.setMarker(getResources().getDrawable(R.drawable.icon_marka));
        marker1.setOnTabMarkListener(new OnTabMarkListener() {

            @Override
            public void onTab() {
                Toast.makeText(QuanJingActivity.this, "图片MarkerA标注已被点击", Toast.LENGTH_SHORT).show();
            }
        });
        // 天安门东北方向
        marker2 = new ImageMarker();
        marker2.setMarkerPosition(new Point(116.427116, 39.929718));
        marker2.setMarker("https://www.baidu.com/img/baidu_resultlogo@2.png");
        marker2.setMarkerHeight(7);
        marker2.setOnTabMarkListener(new OnTabMarkListener() {

            @Override
            public void onTab() {
                Toast.makeText(QuanJingActivity.this, "图片MarkerB标注已被点击", Toast.LENGTH_SHORT).show();
            }
        });
        mPanoView.addMarker(marker1);
        mPanoView.addMarker(marker2);
    }

    /**
     * 删除图片标注
     */
    private void removeImageMarker() {
        mPanoView.removeMarker(marker1);
        mPanoView.removeMarker(marker2);
    }

    private TextMarker textMark1;
    private TextMarker textMark2;

    /**
     * 添加文本标注
     */
    private void addTextMarker() {
        // 天安门西北方向
        textMark1 = new TextMarker();
        textMark1.setMarkerPosition(new Point(116.399562, 39.916789));
        textMark1.setFontColor(0xFFFF0000);
        textMark1.setText("卫星地图全景卫星地图全景\nmap pano\n你好marker");
        textMark1.setFontSize(12);
        textMark1.setBgColor(0xFFFFFFFF);
        textMark1.setPadding(10, 20, 15, 25);
        textMark1.setMarkerHeight(20.3f);
        textMark1.setOnTabMarkListener(new OnTabMarkListener() {

            @Override
            public void onTab() {
                Toast.makeText(QuanJingActivity.this, "textMark1标注已被点击", Toast.LENGTH_SHORT).show();
            }
        });
        // 天安门东南方向
        textMark2 = new TextMarker();
        textMark2.setMarkerPosition(new Point(116.409766, 39.911808));
        textMark2.setFontColor(Color.RED);
        textMark2.setText("你好marker");
        textMark2.setFontSize(12);
        textMark2.setBgColor(Color.BLUE);
        textMark2.setPadding(10, 20, 15, 25);
        textMark2.setMarkerHeight(10);
        textMark2.setOnTabMarkListener(new OnTabMarkListener() {

            @Override
            public void onTab() {
                Toast.makeText(QuanJingActivity.this, "textMark2标注已被点击", Toast.LENGTH_SHORT).show();
            }
        });
        mPanoView.addMarker(textMark1);
        mPanoView.addMarker(textMark2);
    }

    /**
     * 删除文本标注
     */
    private void removeTextMarker() {
        mPanoView.removeMarker(textMark1);
        mPanoView.removeMarker(textMark2);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mPanoView.destroy();
        super.onDestroy();
    }

}
