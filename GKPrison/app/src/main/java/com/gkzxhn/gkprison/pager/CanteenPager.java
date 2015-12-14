package com.gkzxhn.gkprison.pager;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.activity.BaseActivity;
import com.gkzxhn.gkprison.fragment.AllClassificationFragment;
import com.gkzxhn.gkprison.fragment.BaseFragment;
import com.gkzxhn.gkprison.fragment.IntellingentSortingFragment;
import com.gkzxhn.gkprison.fragment.SalesPriorityFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzn on 2015/12/3.
 */
public class CanteenPager extends BasePager{
    private TabHost tab_host;
    private FrameLayout fl_content_01;
    private FrameLayout fl_content_02;
    private FrameLayout fl_content_03;
    private BaseFragment baseFragment;

    public CanteenPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        view = View.inflate(context, R.layout.pager_canteen, null);
        tab_host = (TabHost) view.findViewById(R.id.tab_host);
        fl_content_01 = (FrameLayout) view.findViewById(R.id.fl_content_01);
        fl_content_02 = (FrameLayout) view.findViewById(R.id.fl_content_02);
        fl_content_03 = (FrameLayout) view.findViewById(R.id.fl_content_03);
        tab_host.setup();
        tab_host.addTab(tab_host.newTabSpec("tab1").setIndicator("全部分类", null).setContent(R.id.tab1));
        tab_host.addTab(tab_host.newTabSpec("tab2").setIndicator("销量优先", null).setContent(R.id.tab2));
        tab_host.addTab(tab_host.newTabSpec("tab3").setIndicator("智能排序", null).setContent(R.id.tab3));
        return view;
    }

    @Override
    public void initData() {

        TabWidget tabWidget = tab_host.getTabWidget();
        for (int i =0; i < tabWidget.getChildCount(); i++) {
            //修改Tabhost高度和宽度
//            tabWidget.getChildAt(i).getLayoutParams().height = 30;
//            tabWidget.getChildAt(i).getLayoutParams().width = 65;
            //修改显示字体大小
            TextView tv = (TextView) tabWidget.getChildAt(i).findViewById(android.R.id.title);
            tv.setTextSize(14);
            tv.setTextColor(context.getResources().getColor(R.color.tv_mid));
        }
        tab_host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                switch (tabId) {
                    case "tab1":
                        baseFragment = new AllClassificationFragment();
                        ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_content_01, baseFragment).commit();
                        break;
                    case "tab2":
                        baseFragment = new SalesPriorityFragment();
                        ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_content_02, baseFragment).commit();
                        break;
                    case "tab3":
                        baseFragment = new IntellingentSortingFragment();
                        ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_content_03, baseFragment).commit();
                        break;
                }
            }
        });
        tab_host.setCurrentTab(0);
        baseFragment = new AllClassificationFragment();
        ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_content_01, baseFragment).commit();
    }
}
