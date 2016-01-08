package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.userport.view.RollViewPager;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.List;

/**
 * 狱务公开页面
 */
public class PrisonOpenActivity extends BaseActivity {

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
    /**
     * 轮播图导航点集合
     */
    private List<View> dotList = new ArrayList<>();

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_prison_open, null);
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
        setTitle("狱务公开");
        setBackVisibility(View.VISIBLE);
        initDot();// 初始化轮播图底部小圆圈
        getNews();
        vp_carousel = new RollViewPager(getApplicationContext(), dotList, CAROUSEL_IVS, new RollViewPager.OnViewClickListener() {
            @Override
            public void viewClick(int position) {
                showToastMsgShort(list_news_title.get(position));
                Intent intent = new Intent(PrisonOpenActivity.this, NewsDetailActivity.class);
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
        lv_prison_open.setAdapter(new MyAdapter());
        lv_prison_open.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PrisonOpenActivity.this, NewsDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getNews(){
        new Thread(){
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet  get = new HttpGet();

        }.start();
    }

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
                    30, 30);
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

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return CAROUSEL_IVS.length;
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
            holder.iv_home_news_icon.setImageResource(CAROUSEL_IVS[position]);
            holder.tv_home_news_title.setText(NEWS_TITLES[position]);
            holder.tv_home_news_content.setText(NEWS_CONTENTS[position]);
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
