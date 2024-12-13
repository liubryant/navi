package cn.navibeidou.beidou.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;

import cn.navibeidou.beidou.R;

public class ListAdapter extends BaseAdapter {


    private LinkedList<ListItem> mData;
    private Context mContext;

    public ListAdapter(LinkedList<ListItem> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        if (mData.get(position).getStyle() == 0) {
            return false;
        }
        return super.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mData.get(position).getStyle() == 0) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.itemtag_list_option, parent, false);
            TextView item_tag = (TextView) convertView.findViewById(R.id.item_tag);
            item_tag.setText(mData.get(position).getTitle());
            return convertView;
        } else {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_option, parent, false);
            //ImageView item_icon = (ImageView) convertView.findViewById(R.id.item_icon);
            TextView item_title = (TextView) convertView.findViewById(R.id.item_title);
            TextView item_subTitle = (TextView) convertView.findViewById(R.id.item_subTitle);
            //item_icon.setBackgroundResource(mData.get(position).getIcon());
            item_title.setText(mData.get(position).getTitle());
            item_subTitle.setText(mData.get(position).getSubTitle());
            return convertView;
        }

        //return null;
    }
}
