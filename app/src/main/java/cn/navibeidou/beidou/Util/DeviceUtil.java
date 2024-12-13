package cn.navibeidou.beidou.Util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.UUID;

public class DeviceUtil {
    private static final String TAG = "DeviceUtil";
    private static final String KEY_NAME = "android_unique_device_id";

    @SuppressLint("NewApi")
    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;

        PackageManager pm = context.getPackageManager();
        if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            result = true;
        }

        return result;
    }
}
