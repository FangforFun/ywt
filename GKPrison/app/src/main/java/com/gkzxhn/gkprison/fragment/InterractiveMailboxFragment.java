package com.gkzxhn.gkprison.fragment;


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
 * 监狱长信箱 --> 互动信箱
 */
public class InterractiveMailboxFragment extends Fragment {

    private ExpandableListView elv_my_mailbox_list;
    private List<String> my_mailbox_list_title = new ArrayList<String>(){
        {
            add("回复：关于监狱用餐问题的建议");
            add("关于住宿问题的建议");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interractive_mailbox,null);
        elv_my_mailbox_list = (ExpandableListView) view.findViewById(R.id.elv_my_mailbox_list);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        elv_my_mailbox_list.setAdapter(new MyAdapter());
    }

    private class MyAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return my_mailbox_list_title.size();
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
            holder.tv_reply_item_title.setText(my_mailbox_list_title.get(groupPosition));
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
                convertView = View.inflate(getActivity(), R.layout.interactive_mailbox_child, null);
                holder = new ChildViewHolder();
                holder.tv_reply_content = (TextView) convertView.findViewById(R.id.tv_reply_content);
                holder.tv_warden_signature = (TextView) convertView.findViewById(R.id.tv_warden_signature);
                holder.tv_message_time = (TextView) convertView.findViewById(R.id.tv_message_time);
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
        TextView tv_reply_content;
        TextView tv_warden_signature;
        TextView tv_message_time;
    }
}
