package cn.navibeidou.beidou.pay;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.alipay.sdk.app.PayTask;

import java.util.Map;

public final class AlipayManager {
    private static final String RESULT_SUCCESS = "9000";
    private static final String RESULT_PROCESSING = "8000";
    private static final String RESULT_CANCELLED = "6001";

    public interface PayResultCallback {
        void onSuccess();
        void onProcessing();
        void onCancelled();
        void onFailed(String message);
    }

    private AlipayManager() {
    }

    public static void pay(final Activity activity, final String orderString, final PayResultCallback callback) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Map<String, String> result = new PayTask(activity).payV2(orderString, true);
                final String status = result.get("resultStatus");
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (RESULT_SUCCESS.equals(status)) {
                            callback.onSuccess();
                        } else if (RESULT_PROCESSING.equals(status)) {
                            callback.onProcessing();
                        } else if (RESULT_CANCELLED.equals(status)) {
                            callback.onCancelled();
                        } else {
                            String memo = result.get("memo");
                            callback.onFailed(memo == null || memo.isEmpty() ? "支付失败" : memo);
                        }
                    }
                });
            }
        }).start();
    }
}
