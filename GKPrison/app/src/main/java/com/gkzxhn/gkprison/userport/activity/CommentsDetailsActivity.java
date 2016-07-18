package com.gkzxhn.gkprison.userport.activity;

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
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.Utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * created by huangzhengneng on 2016.6.27
 * 评论详情页面
 */
public class CommentsDetailsActivity extends BaseActivity {

    private static final java.lang.String TAG = "CommentsDetailsActivity";
    private TextView tv_wonderful_comments;
    private ListView lv_wonderful_comments;
    private TextView tv_newest_comments;
    private ListView lv_newest_comments;
    private SwipeRefreshLayout srl_refresh;
    private int id;

    private CommentListAdapter wonderfulAdapter;
    private CommentListAdapter newestAdapter;
    private SwipeRefreshLayout.OnRefreshListener onListener;

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
        id = getIntent().getIntExtra("news_id", -1);
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
        onListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (srl_refresh.isRefreshing()) {
                    srl_refresh.setRefreshing(false);
                }
            }
        };
        srl_refresh.setOnRefreshListener(onListener);
        Utils.setListViewHeightBasedOnChildren(lv_wonderful_comments);
        Utils.setListViewHeightBasedOnChildren(lv_newest_comments);

        // getAllComments
        getAllComments();
    }

    /**
     * 获取所有评论
     */
    private void getAllComments() {
        srl_refresh.post(new Runnable() {
            @Override
            public void run() {
                if (srl_refresh.isRefreshing()) {
                    srl_refresh.setRefreshing(true);
                }
            }
        });
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Constants.URL_HEAD + "news/" + id + "/comments?access_token=" + SPUtil.get(this, "token", ""))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        srl_refresh.post(new Runnable() {
                            @Override
                            public void run() {
                                if (srl_refresh.isRefreshing()) {
                                    srl_refresh.setRefreshing(false);
                                }
                                showToastMsgShort("刷新失败，请稍后再试！");
                            }
                        });
                    }
                });
            }

            /**
             * [
             {
             "content": "哭啦咯哦了",
             "created_at": "2016-07-18T03:52:28.000Z",
             "family_id": 13,
             "id": 1,
             "news_id": 92,
             "updated_at": "2016-07-18T03:52:28.000Z"
             }
             ]
             */

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.i(TAG, result);
                parseResult(result);
            }
        });
    }

    /**
     * 解析
     * @param result
     */
    private void parseResult(String result) {

    }

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
