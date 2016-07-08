package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.utils.Utils;


/**
 * created by huangzhengneng on 2016.6.27
 * 评论详情页面
 */
public class CommentsDetailsActivity extends BaseActivity {

    private TextView tv_wonderful_comments;
    private ListView lv_wonderful_comments;
    private TextView tv_newest_comments;
    private ListView lv_newest_comments;
    private SwipeRefreshLayout srl_refresh;

    private CommentListAdapter wonderfulAdapter;
    private CommentListAdapter newestAdapter;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_commens_details, null);
        tv_wonderful_comments = (TextView) view.findViewById(R.id.tv_wonderful_comments);
        lv_wonderful_comments = (ListView) view.findViewById(R.id.lv_wonderful_comments);
        tv_newest_comments = (TextView) view.findViewById(R.id.tv_newest_comments);
        lv_newest_comments = (ListView) view.findViewById(R.id.lv_newest_comments);
        srl_refresh = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("");
        setBackVisibility(View.VISIBLE);
        if(wonderfulAdapter == null){
            wonderfulAdapter = new CommentListAdapter(3);
            lv_wonderful_comments.setAdapter(wonderfulAdapter);
        }else {
            wonderfulAdapter.notifyDataSetChanged();
        }

        if(newestAdapter == null){
            newestAdapter = new CommentListAdapter(10);
            lv_newest_comments.setAdapter(newestAdapter);
        }else {
            newestAdapter.notifyDataSetChanged();
        }
        srl_refresh.setColorSchemeColors(R.color.theme);
        srl_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        srl_refresh.setRefreshing(false);
                    }
                }, 1500);
            }
        });
        Utils.setListViewHeightBasedOnChildren(lv_wonderful_comments);
        Utils.setListViewHeightBasedOnChildren(lv_newest_comments);
    }

    private Handler handler = new Handler();

    /**
     * 评论列表适配器
     */
    private class CommentListAdapter extends BaseAdapter{

        private int count;

        public CommentListAdapter(int commentCount){
            this.count = commentCount;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final CommentListHolder holder;
            if(convertView == null){
                convertView = View.inflate(CommentsDetailsActivity.this, R.layout.comments_item, null);
                holder = new CommentListHolder();
                holder.civ_commenter_icon = (ImageView) convertView.findViewById(R.id.civ_commenter_icon);
                holder.tv_commenter_name = (TextView) convertView.findViewById(R.id.tv_commenter_name);
                holder.tv_comment_time = (TextView) convertView.findViewById(R.id.tv_comment_time);
                holder.tv_zan_count = (TextView) convertView.findViewById(R.id.tv_zan_count);
                holder.iv_wonderful = (ImageView) convertView.findViewById(R.id.iv_wonderful);
                holder.tv_comment_content = (TextView) convertView.findViewById(R.id.tv_comment_content);
                convertView.setTag(holder);
            }else {
                holder = (CommentListHolder) convertView.getTag();
            }
            holder.iv_wonderful.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int count = Integer.parseInt(holder.tv_zan_count.getText().toString());
                    holder.iv_wonderful.setImageResource(R.drawable.zanzan);
                    count++;
                    holder.tv_zan_count.setText(count + "");
                }
            });
            int iiii= 0;
            return convertView;
        }
    }

    private static class CommentListHolder{
        ImageView civ_commenter_icon;
        TextView tv_commenter_name;
        TextView tv_comment_time;
        ImageView iv_wonderful;
        TextView tv_zan_count;
        TextView tv_comment_content;
    }
}
