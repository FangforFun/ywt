package com.gkzxhn.gkprison.activity;

import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.gkzxhn.gkprison.R;

import java.util.List;

public class FamilyServiceActivity extends BaseActivity{
    private ExpandableListView el_messge;
    private int[] img_messge = {R.drawable.sentence,R.drawable.consumption,R.drawable.buy};

    @Override
    protected View initView() {
        View view = View.inflate(mContext,R.layout.activity_family_service,null);
        el_messge = (ExpandableListView)view.findViewById(R.id.el_messge);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("家属服务");
        setBackVisibility(View.VISIBLE);
        setTextVisibility(View.VISIBLE);

    }

    private class MyAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            return img_messge.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
