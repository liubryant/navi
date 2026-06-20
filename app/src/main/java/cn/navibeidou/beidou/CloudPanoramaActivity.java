package cn.navibeidou.beidou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bytedance.sdk.openadsdk.TTDrawFeedAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;

import java.util.ArrayList;
import java.util.List;

import cn.navibeidou.beidou.toutiao.DrawFeedAdLoader;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;
import cn.navibeidou.beidou.world.CloudPanoramaItem;

public class CloudPanoramaActivity extends Activity {
    private CloudPanoramaItem[] items;
    private final List<TTDrawFeedAd> loadedDrawAds = new ArrayList<TTDrawFeedAd>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_panorama);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        items = CloudPanoramaItem.all();
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        ListView listView = findViewById(R.id.list_cloud);
        CloudAdapter adapter = new CloudAdapter();
        listView.setAdapter(adapter);
        Log.d("naviad", "CloudPanorama items.length=" + items.length + " adapter.getCount()=" + adapter.getCount());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (TTDrawFeedAd ad : loadedDrawAds) {
            ad.destroy();
        }
        loadedDrawAds.clear();
    }

    private void openItem(CloudPanoramaItem item) {
        Intent intent = new Intent(CloudPanoramaActivity.this, CloudWebActivity.class);
        intent.putExtra("title", item.title);
        intent.putExtra("url", item.url);
        startActivity(intent);
    }

    private class CloudAdapter extends BaseAdapter {
        private static final int AD_INTERVAL = 6;
        private static final int PAIR_ROWS_PER_BLOCK = AD_INTERVAL / 2;
        private static final int BLOCK_SIZE = PAIR_ROWS_PER_BLOCK + 1;

        private boolean isAdRow(int position) {
            return position % BLOCK_SIZE == PAIR_ROWS_PER_BLOCK;
        }

        private int pairRowIndexForPosition(int position) {
            int block = position / BLOCK_SIZE;
            int offset = position % BLOCK_SIZE;
            return block * PAIR_ROWS_PER_BLOCK + offset;
        }

        private int adSlotIndexForPosition(int position) {
            return position / BLOCK_SIZE;
        }

        @Override public int getViewTypeCount() { return 2; }
        @Override public int getItemViewType(int position) {
            return isAdRow(position) ? 1 : 0;
        }

        @Override
        public int getCount() {
            int n = items.length;
            int pairRows = (n + 1) / 2;
            int adRows = n / AD_INTERVAL;
            return pairRows + adRows;
        }

        @Override public Object getItem(int position) { return null; }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (isAdRow(position)) {
                return getAdRowView(position, convertView, parent);
            }
            return getPairRowView(position, convertView, parent);
        }

        private View getPairRowView(int position, View convertView, ViewGroup parent) {
            View row = convertView != null
                    ? convertView
                    : LayoutInflater.from(CloudPanoramaActivity.this)
                            .inflate(R.layout.item_cloud_panorama_row, parent, false);
            int pairRowIndex = pairRowIndexForPosition(position);
            int leftIndex = pairRowIndex * 2;
            int rightIndex = leftIndex + 1;
            bindSlot(row, R.id.slot_left, R.id.iv_cover_left, R.id.tv_title_left, leftIndex);
            bindSlot(row, R.id.slot_right, R.id.iv_cover_right, R.id.tv_title_right, rightIndex);
            return row;
        }

        private void bindSlot(View row, int slotId, int imageId, int titleId, final int itemIndex) {
            View slot = row.findViewById(slotId);
            if (itemIndex >= items.length) {
                slot.setVisibility(View.INVISIBLE);
                slot.setOnClickListener(null);
                return;
            }
            slot.setVisibility(View.VISIBLE);
            final CloudPanoramaItem item = items[itemIndex];
            ((ImageView) row.findViewById(imageId)).setImageResource(item.coverResId);
            ((TextView) row.findViewById(titleId)).setText(item.title);
            slot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openItem(item);
                }
            });
        }

        private View getAdRowView(int position, View convertView, ViewGroup parent) {
            final FrameLayout container = convertView != null
                    ? (FrameLayout) convertView
                    : (FrameLayout) LayoutInflater.from(CloudPanoramaActivity.this)
                            .inflate(R.layout.item_draw_feed_ad_grid, parent, false);
            final int slotIndex = adSlotIndexForPosition(position);
            final View progress = container.findViewById(R.id.draw_ad_progress);
            Object tag = container.getTag();
            Log.d("naviad", "CloudPanorama getAdRowView position=" + position + " slotIndex=" + slotIndex + " 已有tag=" + tag + " 子View数=" + container.getChildCount());
            if (tag instanceof Integer && (Integer) tag == slotIndex && container.getChildCount() > 1) {
                Log.d("naviad", "CloudPanorama 复用已加载的广告 slotIndex=" + slotIndex);
                progress.setVisibility(View.GONE);
                return container;
            }
            container.setTag(slotIndex);
            if (container.getChildCount() > 1) {
                container.removeViewAt(1);
            }
            setAdContainerCollapsed(container, false);
            container.setBackgroundColor(0xFF000000);
            progress.setVisibility(View.VISIBLE);
            Log.d("naviad", "CloudPanorama 开始为slotIndex=" + slotIndex + " 加载新广告");
            DrawFeedAdLoader.load(CloudPanoramaActivity.this, new DrawFeedAdLoader.Callback() {
                @Override
                public void onLoaded(TTDrawFeedAd ad, View adView) {
                    Log.d("naviad", "CloudPanorama onLoaded slotIndex=" + slotIndex + " 当前容器tag=" + container.getTag());
                    Object currentTag = container.getTag();
                    if (!(currentTag instanceof Integer) || (Integer) currentTag != slotIndex) {
                        Log.w("naviad", "CloudPanorama 广告加载完成但容器已复用给别的slot，放弃 slotIndex=" + slotIndex);
                        ad.destroy();
                        return;
                    }
                    if (container.getChildCount() > 1) {
                        container.removeViewAt(1);
                    }
                    container.addView(adView, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    container.setBackground(null);
                    progress.setVisibility(View.GONE);
                    ad.registerViewForInteraction(container, adView, new TTNativeAd.AdInteractionListener() {
                        @Override public void onAdClicked(View view, TTNativeAd nativeAd) {}
                        @Override public void onAdCreativeClick(View view, TTNativeAd nativeAd) {}
                        @Override public void onAdShow(TTNativeAd nativeAd) {}
                    });
                    loadedDrawAds.add(ad);
                    Log.d("naviad", "CloudPanorama 广告View已添加到容器 slotIndex=" + slotIndex);
                }

                @Override
                public void onFailed() {
                    Log.w("naviad", "CloudPanorama 广告加载失败，隐藏占位 slotIndex=" + slotIndex);
                    container.setBackground(null);
                    progress.setVisibility(View.GONE);
                    setAdContainerCollapsed(container, true);
                }
            });
            return container;
        }

        private void setAdContainerCollapsed(FrameLayout container, boolean collapsed) {
            ViewGroup.LayoutParams lp = container.getLayoutParams();
            lp.height = collapsed ? 0 : dp(170);
            container.setLayoutParams(lp);
            container.setVisibility(collapsed ? View.GONE : View.VISIBLE);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
