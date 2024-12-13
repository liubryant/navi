package cn.navibeidou.beidou.Util;

import android.widget.Toast;

//import com.inet.ankeyopen.base.BaseApplication;


public class ToastUtils {

    private static Toast mToast;

    /**
     * 显示Toast
     */
    public static void showToast( final CharSequence text) {

//        BaseApplication.getInstance().uiHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (mToast == null) {
//                    mToast = Toast.makeText(BaseApplication.getInstance(), text, Toast.LENGTH_SHORT);
//                } else {
                    mToast.setText(text);
//                }
                mToast.show();
//            }
//        });

    }


}
