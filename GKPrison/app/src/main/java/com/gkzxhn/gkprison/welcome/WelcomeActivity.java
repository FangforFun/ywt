package com.gkzxhn.gkprison.welcome;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.login.LoadingActivity;

/**
 * created by hzn 2015/12/24
 * 欢迎页
 */
public class WelcomeActivity extends BaseActivity {

    private ViewPager vp_welcome;
    private final int[] WELCOME_IMGS = {R.drawable.welcome01, R.drawable.welcome02, R.drawable.welcome03};
    private MyAdapter welcome_adapter;
    private int currentPosition = -1;
    private Handler handler = new Handler();

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_welcome, null);
        vp_welcome = (ViewPager) view.findViewById(R.id.vp_welcome);
        return view;
    }

    @Override
    protected void initData() {
        setActionBarGone(View.GONE);
        welcome_adapter = new MyAdapter();
        vp_welcome.setAdapter(welcome_adapter);
        vp_welcome.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == WELCOME_IMGS.length - 1){
                    handler.postDelayed(moveToNextTask, 1000);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private Runnable moveToNextTask = new Runnable() {
        @Override
        public void run() {
//            if(currentPosition < WELCOME_IMGS.length - 1){
//                vp_welcome.setCurrentItem(currentPosition + 1);
//            }else {
                Intent intent = new Intent(WelcomeActivity.this, LoadingActivity.class);
            startActivity(intent);
                WelcomeActivity.this.finish();
//            }
        }
    };

    private class MyAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return WELCOME_IMGS.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = View.inflate(WelcomeActivity.this, R.layout.welcome_item, null);
            ImageView iv_welcome = (ImageView) view.findViewById(R.id.iv_welcome);
            iv_welcome.setImageResource(WELCOME_IMGS[position]);
            container.addView(view);
//            view.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    long downTime = 0;
//                    switch (event.getAction()){
//                        case MotionEvent.ACTION_DOWN:
//                            downTime = System.currentTimeMillis();
//                            break;
//                        case MotionEvent.ACTION_UP:
//                        case MotionEvent.ACTION_CANCEL:
//                            long upTime = System.currentTimeMillis();
//                                if (upTime - downTime > 0) {
//                                    currentPosition = position;
//                                    handler.postDelayed(moveToNextTask, 500);
//                                }
//                            break;
//                    }
//                    return true;
//                }
//            });
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            super.destroyItem(container, position, object);
            container.removeView((View) object);
        }
    }
}
