package cn.navibeidou.beidou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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
        boolean empty = result.isEmpty();
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private class PlaceAdapter extends BaseAdapter {
        private final List<WorldPanoramaPlace> items = new ArrayList<WorldPanoramaPlace>();

        void setItems(List<WorldPanoramaPlace> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @Override public int getCount() { return items.size(); }
        @Override public WorldPanoramaPlace getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
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
            WorldPanoramaPlace place = getItem(position);
            holder.image.setVisibility(View.VISIBLE);
            holder.image.setImageResource(place.coverResId);
            holder.thumb.setVisibility(View.GONE);
            holder.name.setText(place.name);
            holder.region.setText(place.subtitle());
            holder.summary.setText(WorldPanoramaPlace.SUMMARY);
            return convertView;
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
