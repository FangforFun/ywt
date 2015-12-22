package com.gkzxhn.gkprison.userport.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReplyPublicityFragment extends Fragment {
    private ExpandableListView elv_reply_list;
    private MyAdapter adapter;
    private List<String> openmessge = new ArrayList<String>(){
        {
            add("2015年9月份局长信箱答复情况公示");
            add("2015年8月份局长信箱答复情况公示");
            add("2015年7月份局长信箱答复情况公示");
            add("2015年6月份局长信箱答复情况公示");
            add("2015年5月份局长信箱答复情况公示");
            add("2015年4月份局长信箱答复情况公示");
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View  view = inflater.inflate(R.layout.fragment_reply_publicity,null);
        elv_reply_list = (ExpandableListView)view.findViewById(R.id.elv_reply_list);
        adapter = new MyAdapter();
        elv_reply_list.setAdapter(adapter);
        return view;
    }

    private class MyAdapter extends BaseExpandableListAdapter{


        @Override
        public int getGroupCount() {
            return openmessge.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
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
            GroupViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(getActivity(), R.layout.prison_warden_item, null);
                holder = new GroupViewHolder();
                holder.tv_reply_item_title = (TextView) convertView.findViewById(R.id.tv_reply_item_title);
                holder.iv_reply_item = (ImageView) convertView.findViewById(R.id.iv_reply_item);
                convertView.setTag(holder);
            }else {
                holder = (GroupViewHolder) convertView.getTag();
            }
            holder.tv_reply_item_title.setText(openmessge.get(groupPosition));
            if (isExpanded){
                holder.iv_reply_item.setImageResource(R.drawable.up_gray);
            }else {
                holder.iv_reply_item.setImageResource(R.drawable.down_gray);
            }
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(getActivity(), R.layout.prison_warden_item_child, null);
                holder = new ChildViewHolder();
                holder.tv_warden_message_public = (TextView) convertView.findViewById(R.id.tv_warden_message_public);
                convertView.setTag(holder);
            }else {
                holder = (ChildViewHolder) convertView.getTag();
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private static class GroupViewHolder{
        TextView tv_reply_item_title;
        ImageView iv_reply_item;
    }

    private static class ChildViewHolder{
        TextView tv_warden_message_public;
    }
}
