/**
 *
 */
package cn.navibeidou.beidou.Util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 继承了Activity，实现Android6.0的运行时权限检测
 * 需要进行运行时权限检测的Activity可以继承这个类
 *
 * @author hongming.wang
 * @创建时间：2016年5月27日 下午3:01:31
 * @项目名称： AMapLocationDemo
 * @文件名称：PermissionsChecker.java
 * @类型名称：PermissionsChecker
 * @since 2.5.0
 */
public class CheckPermissionsActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int PERMISSON_REQUESTCODE = 0;
    private boolean hasRequestedInThisPage = false;

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedCheck && !hasRequestedInThisPage) {
            checkPermissions(needPermissions);
        }
    }

    /**
     * @since 2.5.0
     */
    private void checkPermissions(String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(permissions);
        if (null != needRequestPermissonList
                && needRequestPermissonList.size() > 0) {
            showPermissionPurposeDialog(needRequestPermissonList);
        } else {
            isNeedCheck = false;
        }
    }

    private void showPermissionPurposeDialog(final List<String> needRequestPermissonList) {
        hasRequestedInThisPage = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("权限申请说明");
        builder.setMessage("为提供定位、路线规划与附近搜索服务，我们需要申请位置信息权限。\n\n"
                + "权限仅用于导航相关功能，您可以拒绝或后续在系统设置中开启。"
                + "拒绝后仍可继续使用应用的其他非定位功能。");
        builder.setNegativeButton("暂不开启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isNeedCheck = false;
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("继续授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(CheckPermissionsActivity.this,
                        needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]),
                        PERMISSON_REQUESTCODE);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, perm)) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }

    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
            }
            isNeedCheck = false;
        }
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("未开启定位权限，导航与附近搜索功能可能不可用。\\n\\n"
                + "您可以稍后在“设置-应用权限”中手动开启。\n"
                + "应用不会因您拒绝授权而自动退出。");

        builder.setNegativeButton("我知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     * 启动应用的设置
     *
     * @since 2.5.0
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            Log.d("navi","退出permission index");
//            Intent intent = new Intent(CheckPermissionsActivity.this, MapActivity.class);
//            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
