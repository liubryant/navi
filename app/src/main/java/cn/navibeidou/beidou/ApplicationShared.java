package cn.navibeidou.beidou;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;

public class ApplicationShared extends Application {
    public BMapManager mBMapManager = null;
    private static ApplicationShared mInstance;

    private static Context mContext;


    private static String version = "1.0";

    // 单例模式中获取唯一的ExitApplication 实例
    public static ApplicationShared getInstance() {
        if (null == mInstance) {
            mInstance = new ApplicationShared();
        }
        return mInstance;
    }

    public void onCreate() {
        super.onCreate();
        //获取context
        mInstance = this;
        //tbs
//        GDTADManager.getInstance().initWith(mContext, Constants.APPID);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    //创建一个静态的方法，以便获取context对象
    public static Context getContext() {
        return mContext;
    }

    public static String getVersion() {
        return version;
    }

    public static void setVersion(String ver) {
        version = ver;
    }

    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(new MyGeneralListener())) {
            Toast.makeText(ApplicationShared.getInstance().getApplicationContext(), "BMapManager  初始化错误!",
                    Toast.LENGTH_LONG).show();
        }
        Log.d("navi", "initEngineManager");
    }


    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    static class MyGeneralListener implements MKGeneralListener {

        @Override
        public void onGetPermissionState(int iError) {
            // 非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
//                Toast.makeText(ApplicationShared.getInstance().getApplicationContext(),
//                        "请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: " + iError, Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(ApplicationShared.getInstance().getApplicationContext(), "key认证成功", Toast.LENGTH_LONG)
//                        .show();
            }
        }
    }
}
