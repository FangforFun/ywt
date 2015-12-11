package com.gkzxhn.gkprison.activity;

import android.opengl.Visibility;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.fragment.InterractiveMailboxFragment;
import com.gkzxhn.gkprison.fragment.ReplyPublicityFragment;

import java.util.ArrayList;
import java.util.List;

public class PrisonWardenActivity extends FragmentActivity{
    private ViewPager viewPager;
    private ImageView back;
    private ImageView message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }
        setContentView(R.layout.activity_prison_warden);
        back = (ImageView)findViewById(R.id.iv_back);
        back.setVisibility(View.VISIBLE);
        message = (ImageView)findViewById(R.id.iv_messge);
        message.setVisibility(View.VISIBLE);
       viewPager = (ViewPager)findViewById(R.id.viewpage);
        if (viewPager != null){
            setupViewPager(viewPager);
        }
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs1);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
    }
    private void setupViewPager(ViewPager viewPager){
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ReplyPublicityFragment(), "答复公示");
        adapter.addFragment(new InterractiveMailboxFragment(), "互动信箱");
        viewPager.setAdapter(adapter);
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
