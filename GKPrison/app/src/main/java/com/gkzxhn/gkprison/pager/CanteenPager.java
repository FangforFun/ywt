package com.gkzxhn.gkprison.pager;

import android.app.Fragment;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.activity.BaseActivity;
import com.gkzxhn.gkprison.fragment.AllClassificationFragment;
import com.gkzxhn.gkprison.fragment.IntellingentSortingFragment;
import com.gkzxhn.gkprison.fragment.InterractiveMailboxFragment;
import com.gkzxhn.gkprison.fragment.ReplyPublicityFragment;
import com.gkzxhn.gkprison.fragment.SalesPriorityFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzn on 2015/12/3.
 */
public class CanteenPager extends BasePager{
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public CanteenPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        view = View.inflate(context, R.layout.pager_canteen, null);
        tabLayout = (TabLayout)view.findViewById(R.id.tabs1);
        viewPager = (ViewPager)view.findViewById(R.id.viewpage);
        return view;
    }

    @Override
    public void initData() {
        if (viewPager != null){
            setupViewPager(viewPager);
        }
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager){
        MyPagerAdapter adapter = new MyPagerAdapter(((BaseActivity)context).getSupportFragmentManager());
        adapter.addFragment(new AllClassificationFragment(), "全部分类");
        adapter.addFragment(new SalesPriorityFragment(), "销量优先");
        adapter.addFragment(new IntellingentSortingFragment(),"智能排序");
        viewPager.setAdapter(adapter);
    }
    class MyPagerAdapter extends FragmentPagerAdapter {
        //List放置Fragment
        private final List<android.support.v4.app.Fragment> mFragments = new ArrayList<>();
        //List放置标题
        private final List<String> mFragmentTitles = new ArrayList<>();

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        //添加碎片的方法
        public void addFragment(android.support.v4.app.Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
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
