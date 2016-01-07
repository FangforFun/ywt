package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.userport.fragment.InterractiveMailboxFragment;
import com.gkzxhn.gkprison.userport.fragment.ReplyPublicityFragment;
import com.gkzxhn.gkprison.userport.view.RollViewPager;

import java.util.ArrayList;
import java.util.List;

public class PrisonWardenActivity extends FragmentActivity implements View.OnClickListener{

    private TextView tv_title;
    private ViewPager viewPager;
    private RelativeLayout rl_back;
    private RelativeLayout rl_write_message;
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
    private final int[] CAROUSEL_IVS = {R.drawable.img1, R.drawable.img2, R.drawable.img3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_prison_warden);
        rl_back = (RelativeLayout)findViewById(R.id.rl_back);
        rl_write_message = (RelativeLayout) findViewById(R.id.rl_write_message);
        rl_write_message.setVisibility(View.VISIBLE);
        rl_write_message.setOnClickListener(this);
        rl_carousel = (RelativeLayout) findViewById(R.id.rl_carousel);
        layout_roll_view = View.inflate(this, R.layout.layout_roll_view, null);
        dots_ll = (LinearLayout) layout_roll_view.findViewById(R.id.dots_ll);
        top_news_title = (TextView) layout_roll_view.findViewById(R.id.top_news_title);
        top_news_viewpager = (LinearLayout) layout_roll_view.findViewById(R.id.top_news_viewpager);
        rl_carousel.addView(layout_roll_view);
        tv_title = (TextView) findViewById(R.id.tv_title);
        rl_back.setVisibility(View.VISIBLE);
        rl_back.setOnClickListener(this);
        viewPager = (ViewPager)findViewById(R.id.viewpage);
        if (viewPager != null){
            setupViewPager(viewPager);
        }
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs1);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tv_title.setText("监狱长信箱");
        initDot();// 初始化轮播图底部小圆圈
        vp_carousel = new RollViewPager(this, dotList, CAROUSEL_IVS, new RollViewPager.OnViewClickListener() {
            @Override
            public void viewClick(int position) {
//                Toast.makeText(this, list_news_title.get(position), Toast.LENGTH_SHORT).show();
            }
        });
        list_news_title.clear();
        list_news_title.add("我狱杨晓红干警被评为“最美警花1”");
        list_news_title.add("我狱杨晓红干警被评为“最美警花2”");
        list_news_title.add("我狱杨晓红干警被评为“最美警花3”");
        vp_carousel.initTitle(list_news_title, top_news_title);
        vp_carousel.initImgUrl(list_news_title.size());
        vp_carousel.startRoll();
        top_news_viewpager.removeAllViews();
        top_news_viewpager.addView(vp_carousel);
    }

    private void initDot() {
        dotList.clear();
        dots_ll.removeAllViews();
        for (int i = 0; i < 3; i++) {
            View view = new View(this);
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
    private void setupViewPager(ViewPager viewPager){
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ReplyPublicityFragment(), "答复公示");
        adapter.addFragment(new InterractiveMailboxFragment(), "互动信箱");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.rl_back:
                finish();
                break;
            case R.id.rl_write_message:
                intent = new Intent(this, WriteMessageActivity.class);
                startActivity(intent);
                break;
        }

        
    }

    class MyPagerAdapter extends FragmentPagerAdapter{
        //List放置Fragment
        private final List<Fragment> mFragments = new ArrayList<>();
        //List放置标题
        private final List<String> mFragmentTitles = new ArrayList<>();

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        //添加碎片的方法
        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return  mFragmentTitles.get(position);
        }
    }

}
