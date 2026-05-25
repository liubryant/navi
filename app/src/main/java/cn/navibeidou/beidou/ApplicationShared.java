package cn.navibeidou.beidou;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;

import java.util.WeakHashMap;

import cn.navibeidou.beidou.translucentparent.StatusNavUtils;

public class ApplicationShared extends Application {
    private static final String AMAP_ROUTE_ACTIVITY = "com.amap.api.navi.AmapRouteActivity";
    private static final long AMAP_ROUTE_STATUS_BAR_GUARD_INTERVAL_MS = 300L;

    public BMapManager mBMapManager = null;
    private static ApplicationShared mInstance;
    private static Context mContext;
    private static String version = "1.0";
    private final WeakHashMap<Activity, Runnable> aMapRouteStatusBarGuards = new WeakHashMap<>();

    public static ApplicationShared getInstance() {
        if (mInstance == null) {
            mInstance = new ApplicationShared();
        }
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mContext = getApplicationContext();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                applyAmapRouteImmersive(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                applyAmapRouteImmersive(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                stopAmapRouteStatusBarGuard(activity);
            }

            @Override
            public void onActivityStopped(Activity activity) {
                stopAmapRouteStatusBarGuard(activity);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                stopAmapRouteStatusBarGuard(activity);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

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
            Toast.makeText(ApplicationShared.getInstance().getApplicationContext(), "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
        }
        Log.d("navi", "initEngineManager");
    }

    private void applyAmapRouteImmersive(Activity activity) {
        if (activity == null || !AMAP_ROUTE_ACTIVITY.equals(activity.getClass().getName())) {
            return;
        }
        installAmapRouteStatusBarGuard(activity);
        forceAmapRouteStatusBarVisible(activity);
    }

    private void installAmapRouteStatusBarGuard(final Activity activity) {
        final Window window = activity.getWindow();
        if (window == null) {
            return;
        }
        final View decorView = window.getDecorView();
        stopAmapRouteStatusBarGuard(activity);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                decorView.post(new Runnable() {
                    @Override
                    public void run() {
                        forceAmapRouteStatusBarVisible(activity);
                    }
                });
            }
        });
        Runnable guard = new Runnable() {
            @Override
            public void run() {
                if (activity.isFinishing()) {
                    return;
                }
                forceAmapRouteStatusBarVisible(activity);
                decorView.postDelayed(this, AMAP_ROUTE_STATUS_BAR_GUARD_INTERVAL_MS);
            }
        };
        aMapRouteStatusBarGuards.put(activity, guard);
        decorView.post(guard);
    }

    private void stopAmapRouteStatusBarGuard(Activity activity) {
        if (activity == null) {
            return;
        }
        Runnable guard = aMapRouteStatusBarGuards.remove(activity);
        Window window = activity.getWindow();
        if (guard != null && window != null) {
            window.getDecorView().removeCallbacks(guard);
        }
    }

    private void forceAmapRouteStatusBarVisible(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Window window = activity.getWindow();
        if (window == null) {
            return;
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        StatusNavUtils.setStatusBarColor(activity, Color.WHITE);
        View decorView = window.getDecorView();
        int systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(systemUiVisibility);
        View content = decorView.findViewById(android.R.id.content);
        if (content != null) {
            content.setFitsSystemWindows(true);
            if (content instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) content;
                for (int i = 0; i < group.getChildCount(); i++) {
                    group.getChildAt(i).setFitsSystemWindows(true);
                }
            }
        }
    }

    static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetPermissionState(int iError) {
        }
    }
}
