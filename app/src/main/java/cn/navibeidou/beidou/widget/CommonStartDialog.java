package cn.navibeidou.beidou.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import cn.navibeidou.beidou.ProtocolActivity;
import cn.navibeidou.beidou.R;
import cn.navibeidou.beidou.SplashActivity;
import cn.navibeidou.beidou.toutiao.config.TTAdManagerHolder;

/**
 * author : Stanley
 * e-mail : bryant_liu24@126.com
 * date   : 2020/3/24 15:01
 * desc   : SerialApp
 * version: 1.0
 */

public class CommonStartDialog {
    private Dialog dlg;
    private Context context;
    private Boolean firstRun;
    private TextView tv_policy;
    private Button btn_agree, btn_disagree;

    public CommonStartDialog(Context context, Boolean firstRun) {
        this.context = context;
        this.firstRun = firstRun;
    }


    public Dialog showSheet() {
        dlg = new Dialog(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(
                R.layout.common_start, null);
        tv_policy = (TextView) layout.findViewById(R.id.tv_policy);
        btn_agree = (Button) layout.findViewById(R.id.btn_agree);
        btn_disagree = (Button) layout.findViewById(R.id.btn_disagree);

        btn_agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
                Log.d("navi", "初始化ttAd");
                TTAdManagerHolder.init(context);
                //初始化组件化基础库, 所有友盟业务SDK都必须调用此初始化接口。
                UMConfigure.init(context, "6013dda26a2a470e8f97901a", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
                MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
                //tbs
                //GDTADManager.getInstance().initWith(mContext, Constants.APPID);
                //全景初始化


                if (firstRun) {
//                    SharedPreferences sharedPreferences = context.getSharedPreferences("FirstRun", 0);
//                    sharedPreferences.edit().putBoolean("First", false).commit();
                    Intent intent = new Intent(context, SplashActivity.class);
                    context.startActivity(intent);
                }
            }
        });
        btn_disagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
                SharedPreferences sharedPreferences = context.getSharedPreferences("FirstRun", 0);
                sharedPreferences.edit().putBoolean("First", true).commit();
                System.exit(0);
            }
        });
        //
        SpannableString spannableString = new SpannableString(context.getString(R.string.yinsipolicy));

        // 设置用户协议的高亮颜色
//        ForegroundColorSpan userAgreementSpan = new ClickableSpan(Color.BLUE); // 例如，蓝色高亮
        int userAgreementStartIndex = 18; // “用户协议”的起始位置
        int userAgreementEndIndex = 24; // “用户协议”的结束位置

        // 设置隐私政策的高亮颜色
//        ForegroundColorSpan privacyPolicySpan = new ForegroundColorSpan(Color.RED); // 例如，红色高亮
        int privacyPolicyStartIndex = 25; // “隐私政策”的起始位置，注意这里是示例，实际位置可能不同
        int privacyPolicyEndIndex = 31; // “隐私政策”的结束位置

        // 将设置好的SpannableString设置到TextView上
        ClickableSpan userAgreementSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                close();
                Intent intentservice = new Intent(context, ProtocolActivity.class);
                intentservice.putExtra("service", true);
                context.startActivity(intentservice);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE); // 设置高亮背景颜色
                ds.setUnderlineText(false); // 去除下划线
            }
        };
        ClickableSpan privacyPolicySpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // 用户点击了“用户协议”时触发的操作
                close();
                SharedPreferences sharedPreferences = context.getSharedPreferences("FirstRun", 0);
                sharedPreferences.edit().putBoolean("First", true).commit();
                Intent intent = new Intent(context, ProtocolActivity.class);
                intent.putExtra("yinsi", true);
                context.startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE); // 设置高亮背景颜色
                ds.setUnderlineText(false); // 去除下划线
            }
        };
        spannableString.setSpan(userAgreementSpan, userAgreementStartIndex, userAgreementEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacyPolicySpan, privacyPolicyStartIndex, privacyPolicyEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //
        tv_policy.setText(spannableString);
        tv_policy.setMovementMethod(LinkMovementMethod.getInstance());


        dlg.setCanceledOnTouchOutside(false);
        dlg.setContentView(layout);
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.i("navi", "city  " + true);

                    return true;
                } else {
                    Log.i("navi", "city  " + false);

                    return false; // 默认返回 false
                }
            }
        });
        dlg.show();
        return dlg;
    }

    private void close() {
        if (dlg != null) {
            Log.i("navi", "city  " + "close");
            dlg.cancel();
            dlg = null;
        }
    }

}
