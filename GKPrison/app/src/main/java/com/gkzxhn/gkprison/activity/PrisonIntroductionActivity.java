package com.gkzxhn.gkprison.activity;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

/**
 * 监狱简介
 */
public class PrisonIntroductionActivity extends BaseActivity {

    private ViewPager vp_carousel;
    private RadioGroup rg_carousel; // 轮播图底部小圆圈
    private RadioButton rb_carousel_01;
    private RadioButton rb_carousel_02;
    private RadioButton rb_carousel_03;
    private RadioButton rb_carousel_04;
    private final int[] CAROUSEL_IVS = {R.drawable.img1, R.drawable.img2, R.drawable.img3, R.drawable.img3};
    private TextView tv_prison_introduction;
    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.activity_prison_introduction, null);
        vp_carousel = (ViewPager) view.findViewById(R.id.vp_carousel);
        rg_carousel = (RadioGroup) view.findViewById(R.id.rg_carousel);
        rb_carousel_01 = (RadioButton) view.findViewById(R.id.rb_carousel_01);
        rb_carousel_02 = (RadioButton) view.findViewById(R.id.rb_carousel_02);
        rb_carousel_03 = (RadioButton) view.findViewById(R.id.rb_carousel_03);
        rb_carousel_04 = (RadioButton) view.findViewById(R.id.rb_carousel_04);
        tv_prison_introduction = (TextView) view.findViewById(R.id.tv_prison_introduction);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("监狱简介");
        setBackVisibility(View.VISIBLE);
        vp_carousel.setAdapter(new MyCarouselAdapter());
        vp_carousel.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        rg_carousel.check(R.id.rb_carousel_01);
                        break;
                    case 1:
                        rg_carousel.check(R.id.rb_carousel_02);
                        break;
                    case 2:
                        rg_carousel.check(R.id.rb_carousel_03);
                        break;
                    case 3:
                        rg_carousel.check(R.id.rb_carousel_04);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vp_carousel.setCurrentItem(0);// 默认选中第0个
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    private class MyCarouselAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return CAROUSEL_IVS.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = View.inflate(mContext, R.layout.carousel_item, null);
            ImageView iv_carousel = (ImageView) view.findViewById(R.id.iv_carousel);
            iv_carousel.setImageResource(CAROUSEL_IVS[position]);
            container.addView(view);
            view.setOnTouchListener(new View.OnTouchListener() {
                private int downX;
                private int upX;
                private long downTime;
                private long upTime;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downX = (int) event.getX();
                            downTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_UP:
                            upX = (int) event.getX();
                            upTime = System.currentTimeMillis();
                            if (downX == upX && upTime - downTime < 500) {
                                showToastMsgShort("点我干嘛呀...");
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            break;
                    }
                    return true;
                }
            });
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
