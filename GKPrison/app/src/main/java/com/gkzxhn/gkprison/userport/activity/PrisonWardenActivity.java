package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.userport.fragment.InterractiveMailboxFragment;
import com.gkzxhn.gkprison.userport.fragment.ReplyPublicityFragment;
import com.keda.sky.app.PcAppStackManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 投诉建议
 */
public class PrisonWardenActivity extends FragmentActivity implements View.OnClickListener {

    private TextView tv_title;
    private ViewPager viewPager;
    private RelativeLayout rl_back;
    private RelativeLayout rl_write_message;
    private String url = "";
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PcAppStackManager.Instance().pushActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_prison_warden);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        rl_back = (RelativeLayout) findViewById(R.id.rl_back);
        rl_write_message = (RelativeLayout) findViewById(R.id.rl_write_message);
        rl_write_message.setVisibility(View.VISIBLE);
        rl_write_message.setOnClickListener(this);
        tv_title = (TextView) findViewById(R.id.tv_title);
        rl_back.setVisibility(View.VISIBLE);
        rl_back.setOnClickListener(this);
        viewPager = (ViewPager) findViewById(R.id.viewpage);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs1);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tv_title.setText("投诉建议");
    }

    private void setupViewPager(ViewPager viewPager) {
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ReplyPublicityFragment(), "公示信息");
        adapter.addFragment(new InterractiveMailboxFragment(), "投诉反馈");
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.rl_write_message:
                intent = new Intent(this, WriteMessageActivity.class);
                startActivity(intent);
                break;
        }
    }

    class MyPagerAdapter extends FragmentPagerAdapter {
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
            return mFragmentTitles.get(position);
        }
    }

}
