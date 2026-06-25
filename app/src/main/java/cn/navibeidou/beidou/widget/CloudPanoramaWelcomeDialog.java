package cn.navibeidou.beidou.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import cn.navibeidou.beidou.CloudPanoramaActivity;
import cn.navibeidou.beidou.CloudWebActivity;
import cn.navibeidou.beidou.R;

/**
 * 首次进入首页5秒后弹出的720云景区推荐弹窗，与iOS端 CloudPanoramaWelcomeViewController 保持一致。
 */
public class CloudPanoramaWelcomeDialog {
    private static final String FEATURED_TITLE = "珠穆朗玛纳木措";
    private static final String FEATURED_URL = "https://www.720yun.com/t/7a9jvztkeO5?scene_id=20321714";
    private static final String FEATURED_COVER = "file:///android_asset/720yun/93_珠穆朗玛纳木措.jpg";

    public static void show(final Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CardView layout = (CardView) activity.getLayoutInflater()
                .inflate(R.layout.dialog_cloud_panorama_welcome, null);
        dialog.setContentView(layout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(true);

        ImageView cover = layout.findViewById(R.id.iv_cloud_welcome_cover);
        Glide.with(activity).load(FEATURED_COVER).centerCrop().into(cover);
        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openFeatured(activity);
            }
        });

        layout.findViewById(R.id.iv_cloud_welcome_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        layout.findViewById(R.id.btn_cloud_welcome_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activity.startActivity(new Intent(activity, CloudPanoramaActivity.class));
            }
        });

        dialog.show();
    }

    private static void openFeatured(Activity activity) {
        Intent intent = new Intent(activity, CloudWebActivity.class);
        intent.putExtra("title", FEATURED_TITLE);
        intent.putExtra("url", FEATURED_URL);
        activity.startActivity(intent);
    }
}
