package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.DemoCache;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.News;
import com.gkzxhn.gkprison.userport.view.RollViewPager;
import com.gkzxhn.gkprison.utils.Utils;
import com.squareup.picasso.Picasso;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作动态
 */
public class VisitingServiceActivity extends BaseActivity {
    private final int[] CAROUSEL_IVS = {R.drawable.img1, R.drawable.img2, R.drawable.img3, R.drawable.img3};
    private final String[] NEWS_TITLES = {"【刑罚执行】昭通市食药监局到昭通监狱开展警示教育", "【狱政管理】第二届换届选举张全蛋公选为监狱长", "【教育改造】第二届换届选举张全蛋公选为监狱长", "【劳动改造】第二届换届选举张全蛋公选为监狱长"};
    private final String[] NEWS_CONTENTS = {"    为深入践行“三严三实”和“忠诚干净担当”专题教育实践活动，加强系统党风廉政建设，提高干部拒腐防变能力，党员干部到昭通监狱接手廉政...", "2015年12月15日，广东监狱举办第二届换届选举，张全蛋以最多票数被选为监狱长。", "2015年12月15日，广东监狱举办第二届换届选举，张全蛋以最多票数被选为监狱长。", "2015年12月15日，广东监狱举办第二届换届选举，张全蛋以最多票数被选为监狱长。"};
    private ListView lv_prison_open;
    private RelativeLayout rl_carousel;
    private RollViewPager vp_carousel;
    private View layout_roll_view;
    private LinearLayout dots_ll;
    private TextView top_news_title;
    private LinearLayout top_news_viewpager;
    private final List<String> list_news_title = new ArrayList<>();
    private String url = "";
    private SharedPreferences sp;
    private String token;
    private List<News> newsList = new ArrayList<>();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String tag = (String)msg.obj;
                    if (tag.equals("success")){
                        Bundle bundle = msg.getData();
                        String result = bundle.getString("result");
                        newsList = analysisNews(result);
                        lv_prison_open.setAdapter(new MyAdapter());
                    }else if (tag.equals("error")){
                        Toast.makeText(getApplicationContext(), "同步数据失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    /**
     * 轮播图导航点集合
     */
    private List<View> dotList = new ArrayList<>();

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_visiting_service,null);
        lv_prison_open = (ListView) view.findViewById(R.id.lv_prison_open);
        rl_carousel = (RelativeLayout) view.findViewById(R.id.rl_carousel);
        layout_roll_view = View.inflate(getApplicationContext(), R.layout.layout_roll_view, null);
        dots_ll = (LinearLayout) layout_roll_view.findViewById(R.id.dots_ll);
        top_news_title = (TextView) layout_roll_view.findViewById(R.id.top_news_title);
        top_news_viewpager = (LinearLayout) layout_roll_view.findViewById(R.id.top_news_viewpager);
        rl_carousel.addView(layout_roll_view);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("工作动态");
        setBackVisibility(View.VISIBLE);
        initDot();// 初始化轮播图底部小圆圈
        sp = getSharedPreferences("config", MODE_PRIVATE);
        token = sp.getString("token", "");
        url = Constants.URL_HEAD + "news?jail_id=1" ;
        getNews();
        vp_carousel = new RollViewPager(getApplicationContext(), dotList, CAROUSEL_IVS, new RollViewPager.OnViewClickListener() {
            @Override
            public void viewClick(int position) {
                showToastMsgShort(list_news_title.get(position));
                Intent intent = new Intent(VisitingServiceActivity.this, NewsDetailActivity.class);
                startActivity(intent);
            }
        });
        list_news_title.clear();
        list_news_title.add("我狱杨晓红干警被评为“最美警花1”");
        list_news_title.add("我狱杨晓红干警被评为“最美警花2”");
        list_news_title.add("我狱杨晓红干警被评为“最美警花3”");
        list_news_title.add("我狱杨晓红干警被评为“最美警花4”");
        vp_carousel.initTitle(list_news_title, top_news_title);
        vp_carousel.initImgUrl(list_news_title.size());
        vp_carousel.startRoll();
        top_news_viewpager.removeAllViews();
        top_news_viewpager.addView(vp_carousel);
        lv_prison_open.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i = newsList.get(position).getId();
                Intent intent = new Intent(VisitingServiceActivity.this, NewsDetailActivity.class);
                intent.putExtra("id", i);
                startActivity(intent);
            }
        });

    }
    /**
     * 获取新闻
     */
    private void getNews(){
        if(Utils.isNetworkAvailable()) {
            new Thread() {
                @Override
                public void run() {
                    Message msg = handler.obtainMessage();
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet Get = new HttpGet(url);
                    try {
                        HttpResponse response = httpClient.execute(Get);
                        if (response.getStatusLine().getStatusCode() == 200) {
                            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                            msg.obj = "success";
                            Bundle bundle = new Bundle();
                            bundle.putString("result", result);
                            msg.setData(bundle);
                            msg.what = 1;
                            handler.sendMessage(msg);
                        } else {
                            msg.obj = "error";
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }else {
            showToastMsgShort("没有网络");
        }
    }

    private List<News> analysisNews(String s){
        List<News> newses = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(s);
            for (int i = 0;i < jsonArray.length();i++){
                News news = new News();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                news.setId(jsonObject.getInt("id"));
                news.setTitle(jsonObject.getString("title"));
                news.setContents(jsonObject.getString("contents"));
                news.setJail_id(jsonObject.getInt("jail_id"));
                news.setImage_url(jsonObject.getString("image_url"));
                newses.add(news);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newses;
    }
    /**
     * 初始化轮播小圆点
     */
    private void initDot() {
        dotList.clear();
        dots_ll.removeAllViews();
        for (int i = 0; i < 4; i++) {
            View view = new View(getApplicationContext());
            if (i == 0) {
                view.setBackgroundResource(R.drawable.rb_shape_blue);
            } else {
                view.setBackgroundResource(R.drawable.rb_shape_gray);
            }
            // 指定点的大小
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.dot_radius), getResources().getDimensionPixelSize(R.dimen.dot_radius));
            // 间距
            layoutParams.setMargins(10, 0, 10, 0);
            dots_ll.addView(view, layoutParams);
            dotList.add(view);
        }
    }
    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return newsList.size();
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
            ViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(getApplicationContext(), R.layout.prison_open_item, null);
                holder = new ViewHolder();
                holder.iv_home_news_icon = (ImageView) convertView.findViewById(R.id.iv_home_news_icon);
                holder.tv_home_news_title = (TextView) convertView.findViewById(R.id.tv_home_news_title);
                holder.tv_home_news_content = (TextView) convertView.findViewById(R.id.tv_home_news_content);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            String t = Constants.RESOURSE_HEAD+newsList.get(position).getImage_url();
            Picasso.with(holder.iv_home_news_icon.getContext()).load(t).into(holder.iv_home_news_icon);
            holder.tv_home_news_title.setText(newsList.get(position).getTitle());
            holder.tv_home_news_content.setText(newsList.get(position).getContents());
            return convertView;
        }
    }

    private static class ViewHolder{
        ImageView iv_home_news_icon;
        TextView tv_home_news_title;
        TextView tv_home_news_content;
        ImageView iv_home_news_go;
    }

}
