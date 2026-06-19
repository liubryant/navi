package cn.navibeidou.beidou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import cn.navibeidou.beidou.translucentparent.StatusNavUtils;
import cn.navibeidou.beidou.world.CloudPanoramaItem;

public class CloudPanoramaActivity extends Activity {
    private CloudPanoramaItem[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_panorama);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        items = CloudPanoramaItem.all();
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        GridView gridView = findViewById(R.id.list_cloud);
        gridView.setAdapter(new CloudAdapter());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CloudPanoramaItem item = items[position];
                Intent intent = new Intent(CloudPanoramaActivity.this, CloudWebActivity.class);
                intent.putExtra("title", item.title);
                intent.putExtra("url", item.url);
                startActivity(intent);
            }
        });
    }

    private class CloudAdapter extends BaseAdapter {
        @Override public int getCount() { return items.length; }
        @Override public Object getItem(int position) { return items[position]; }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(CloudPanoramaActivity.this)
                        .inflate(R.layout.item_cloud_panorama, parent, false);
            }
            CloudPanoramaItem item = items[position];
            ImageView imageView = convertView.findViewById(R.id.iv_cover);
            imageView.setImageResource(item.coverResId);
            ((TextView) convertView.findViewById(R.id.tv_title)).setText(item.title);
            return convertView;
        }
    }
}
