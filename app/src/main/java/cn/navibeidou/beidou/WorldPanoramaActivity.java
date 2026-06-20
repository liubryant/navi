package cn.navibeidou.beidou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bytedance.sdk.openadsdk.TTDrawFeedAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;

import java.util.ArrayList;
import java.util.List;

import cn.navibeidou.beidou.toutiao.DrawFeedAdLoader;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;
import cn.navibeidou.beidou.world.WorldPanoramaPlace;

public class WorldPanoramaActivity extends Activity {
    private LinearLayout categoryContainer;
    private EditText searchInput;
    private ListView listView;
    private TextView emptyView;
    private PlaceAdapter adapter;
    private String selectedCategory = WorldPanoramaPlace.CATEGORIES[0];
    private final List<TextView> categoryButtons = new ArrayList<TextView>();
    private final List<TTDrawFeedAd> loadedDrawAds = new ArrayList<TTDrawFeedAd>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_panorama);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        categoryContainer = findViewById(R.id.category_container);
        searchInput = findViewById(R.id.et_search);
        listView = findViewById(R.id.list_places);
        emptyView = findViewById(R.id.tv_empty);
        adapter = new PlaceAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getItemViewType(position) != PlaceAdapter.VIEW_TYPE_PLACE) {
                    return;
                }
                WorldPanoramaPlace place = adapter.getItem(position);
                Intent intent = new Intent(WorldPanoramaActivity.this, WorldPanoramaDetailActivity.class);
                intent.putExtra("name", place.name);
                intent.putExtra("region", place.region);
                intent.putExtra("province", place.province);
                intent.putExtra("city", place.city);
                intent.putExtra("latitude", place.latitude);
                intent.putExtra("longitude", place.longitude);
                startActivity(intent);
            }
        });

        buildCategories();
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        applyFilter();
    }

    private void buildCategories() {
        for (int i = 0; i < WorldPanoramaPlace.CATEGORIES.length; i++) {
            final String category = WorldPanoramaPlace.CATEGORIES[i];
            TextView button = new TextView(this);
            button.setText(category);
            button.setTextSize(14);
            button.setGravity(android.view.Gravity.CENTER);
            button.setPadding(dp(14), dp(7), dp(14), dp(7));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) lp.leftMargin = dp(8);
            categoryContainer.addView(button, lp);
            categoryButtons.add(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedCategory = category;
                    updateCategoryButtons();
                    applyFilter();
                    listView.setSelection(0);
                }
            });
        }
        updateCategoryButtons();
    }

    private void updateCategoryButtons() {
        for (TextView button : categoryButtons) {
            boolean selected = selectedCategory.equals(button.getText().toString());
            button.setTextColor(selected ? 0xFFFFFFFF : 0xFF222222);
            button.setBackgroundResource(selected ? R.drawable.bg_world_category_selected : R.drawable.bg_world_category);
        }
    }

    private void applyFilter() {
        String query = searchInput.getText() == null ? "" : searchInput.getText().toString();
        List<WorldPanoramaPlace> source = WorldPanoramaPlace.placesIn(selectedCategory);
        List<WorldPanoramaPlace> result = new ArrayList<WorldPanoramaPlace>();
        for (WorldPanoramaPlace place : source) {
            if (place.matches(query)) result.add(place);
        }
        adapter.setItems(result);
        Log.d("naviad", "WorldPanorama result.size()=" + result.size() + " adapter.getCount()=" + adapter.getCount());
        boolean empty = result.isEmpty();
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (TTDrawFeedAd ad : loadedDrawAds) {
            ad.destroy();
        }
        loadedDrawAds.clear();
    }

    private class PlaceAdapter extends BaseAdapter {
        static final int VIEW_TYPE_PLACE = 0;
        static final int VIEW_TYPE_AD = 1;
        private static final int AD_INTERVAL = 4;

        private final List<WorldPanoramaPlace> items = new ArrayList<WorldPanoramaPlace>();

        void setItems(List<WorldPanoramaPlace> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        private boolean isAdPosition(int position) {
            return position % (AD_INTERVAL + 1) == AD_INTERVAL;
        }

        private int realIndexForPosition(int position) {
            int block = position / (AD_INTERVAL + 1);
            int offset = position % (AD_INTERVAL + 1);
            return block * AD_INTERVAL + offset;
        }

        private int adSlotIndexForPosition(int position) {
            return position / (AD_INTERVAL + 1);
        }

        @Override public int getViewTypeCount() { return 2; }
        @Override public int getItemViewType(int position) {
            return isAdPosition(position) ? VIEW_TYPE_AD : VIEW_TYPE_PLACE;
        }

        @Override
        public int getCount() {
            int n = items.size();
            return n + n / AD_INTERVAL;
        }

        @Override
        public WorldPanoramaPlace getItem(int position) {
            return isAdPosition(position) ? null : items.get(realIndexForPosition(position));
        }

        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (isAdPosition(position)) {
                return getAdView(position, convertView, parent);
            }
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(WorldPanoramaActivity.this)
                        .inflate(R.layout.item_world_panorama_place, parent, false);
                holder = new ViewHolder();
                holder.image = convertView.findViewById(R.id.iv_thumb);
                holder.thumb = convertView.findViewById(R.id.tv_thumb);
                holder.name = convertView.findViewById(R.id.tv_name);
                holder.region = convertView.findViewById(R.id.tv_region);
                holder.summary = convertView.findViewById(R.id.tv_summary);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            WorldPanoramaPlace place = items.get(realIndexForPosition(position));
            holder.image.setVisibility(View.VISIBLE);
            holder.image.setImageResource(place.coverResId);
            holder.thumb.setVisibility(View.GONE);
            holder.name.setText(place.name);
            holder.region.setText(place.subtitle());
            holder.summary.setText(WorldPanoramaPlace.SUMMARY);
            return convertView;
        }

        private View getAdView(int position, View convertView, ViewGroup parent) {
            final FrameLayout container = convertView != null
                    ? (FrameLayout) convertView
                    : (FrameLayout) LayoutInflater.from(WorldPanoramaActivity.this)
                            .inflate(R.layout.item_draw_feed_ad, parent, false);
            final int slotIndex = adSlotIndexForPosition(position);
            final View progress = container.findViewById(R.id.draw_ad_progress);
            Object tag = container.getTag();
            Log.d("naviad", "WorldPanorama getAdView position=" + position + " slotIndex=" + slotIndex + " 已有tag=" + tag + " 子View数=" + container.getChildCount());
            if (tag instanceof Integer && (Integer) tag == slotIndex && container.getChildCount() > 1) {
                Log.d("naviad", "WorldPanorama 复用已加载的广告 slotIndex=" + slotIndex);
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
            Log.d("naviad", "WorldPanorama 开始为slotIndex=" + slotIndex + " 加载新广告");
            DrawFeedAdLoader.load(WorldPanoramaActivity.this, new DrawFeedAdLoader.Callback() {
                @Override
                public void onLoaded(TTDrawFeedAd ad, View adView) {
                    Log.d("naviad", "WorldPanorama onLoaded slotIndex=" + slotIndex + " 当前容器tag=" + container.getTag());
                    Object currentTag = container.getTag();
                    if (!(currentTag instanceof Integer) || (Integer) currentTag != slotIndex) {
                        Log.w("naviad", "WorldPanorama 广告加载完成但容器已复用给别的slot，放弃 slotIndex=" + slotIndex);
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
                    Log.d("naviad", "WorldPanorama 广告View已添加到容器 slotIndex=" + slotIndex);
                }

                @Override
                public void onFailed() {
                    Log.w("naviad", "WorldPanorama 广告加载失败，隐藏占位 slotIndex=" + slotIndex);
                    container.setBackground(null);
                    progress.setVisibility(View.GONE);
                    setAdContainerCollapsed(container, true);
                }
            });
            return container;
        }

        private void setAdContainerCollapsed(FrameLayout container, boolean collapsed) {
            ViewGroup.LayoutParams lp = container.getLayoutParams();
            lp.height = collapsed ? 0 : dp(192);
            container.setLayoutParams(lp);
            container.setVisibility(collapsed ? View.GONE : View.VISIBLE);
        }
    }

    private static class ViewHolder {
        ImageView image;
        TextView thumb;
        TextView name;
        TextView region;
        TextView summary;
    }
}
