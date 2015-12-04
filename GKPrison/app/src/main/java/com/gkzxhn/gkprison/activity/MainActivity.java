package com.gkzxhn.gkprison.activity;

import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.pager.BasePager;
import com.gkzxhn.gkprison.pager.CanteenPager;
import com.gkzxhn.gkprison.pager.HomePager;
import com.gkzxhn.gkprison.pager.VisitPager;
import com.gkzxhn.gkprison.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 主activity
 */
public class MainActivity extends BaseActivity {

    private ViewPager home_viewPager;
    private RadioGroup rg_bottom_guide;
    private List<BasePager> pagerList;
    private RadioButton rb_bottom_guide_home;
    private RadioButton rb_bottom_guide_visit;
    private RadioButton rb_bottom_guide_canteen;
    private long mExitTime;//add by hzn 退出按键时间间隔

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.activity_main, null);
        home_viewPager = (ViewPager) view.findViewById(R.id.layout_content);
        rg_bottom_guide = (RadioGroup) view.findViewById(R.id.rg_bottom_guide);
        rb_bottom_guide_home = (RadioButton) view.findViewById(R.id.rb_bottom_guide_home);
        rb_bottom_guide_visit = (RadioButton) view.findViewById(R.id.rb_bottom_guide_visit);
        rb_bottom_guide_canteen = (RadioButton) view.findViewById(R.id.rb_bottom_guide_canteen);
//        tv_title_bar_title = (TextView) view.findViewById(R.id.tv_title_bar_title);
        Drawable[] drawables = rb_bottom_guide_home.getCompoundDrawables();
        drawables[1].setBounds(0, DensityUtil.dip2px(mContext, 5), 70, 85);
        rb_bottom_guide_home.setCompoundDrawables(drawables[0],drawables[1],drawables[2],drawables[3]);
        Drawable[] drawables2 = rb_bottom_guide_visit.getCompoundDrawables();
        drawables2[1].setBounds(0, DensityUtil.dip2px(mContext, 5), 70, 85);
        rb_bottom_guide_visit.setCompoundDrawables(drawables2[0],drawables2[1],drawables2[2],drawables2[3]);
        Drawable[] drawables3 = rb_bottom_guide_canteen.getCompoundDrawables();
        drawables3[1].setBounds(0, DensityUtil.dip2px(mContext, 5), 70, 85);
        rb_bottom_guide_canteen.setCompoundDrawables(drawables3[0],drawables3[1],drawables3[2],drawables3[3]);
        return view;
    }

    @Override
    protected void initData() {
        pagerList = new ArrayList<>();
        pagerList.clear();
        pagerList.add(new HomePager(mContext));
        pagerList.add(new VisitPager(mContext));
        pagerList.add(new CanteenPager(mContext));

        home_viewPager.setAdapter(new MyPagerAdapter());
        rg_bottom_guide.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_bottom_guide_home: // 首页
                        home_viewPager.setCurrentItem(0);
                        setTitle("首页");
                        break;
                    case R.id.rb_bottom_guide_visit: // 远程会见
                        home_viewPager.setCurrentItem(1);
                        setTitle("远程会见");
                        break;
                    case R.id.rb_bottom_guide_canteen: // 小卖部
                        home_viewPager.setCurrentItem(2);
                        setTitle("小卖部");
                        break;
                    default:
                        break;
                }
            }
        });
        rg_bottom_guide.check(R.id.rb_bottom_guide_home); // 默认选择首页
        home_viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    rg_bottom_guide.check(R.id.rb_bottom_guide_home);
                    setTitle("首页");
                } else if (i == 1) {
                    rg_bottom_guide.check(R.id.rb_bottom_guide_visit);
                    setTitle("远程会见");
                } else if (i == 2) {
                    rg_bottom_guide.check(R.id.rb_bottom_guide_canteen);
                    setTitle("小卖部");
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pagerList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return (view == o);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(pagerList.get(position).getView());
            pagerList.get(position).initData();
            return pagerList.get(position).getView();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    /**
     * 按兩次返回退出程序   add by hzn
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
