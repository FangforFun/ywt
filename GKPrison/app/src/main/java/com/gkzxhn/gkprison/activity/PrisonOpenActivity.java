package com.gkzxhn.gkprison.activity;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

/**
 * 狱务公开页面
 */
public class PrisonOpenActivity extends BaseActivity {

    private ViewPager vp_carousel;
    private RadioGroup rg_carousel; // 轮播图底部小圆圈
    private RadioButton rb_carousel_01;
    private RadioButton rb_carousel_02;
    private RadioButton rb_carousel_03;
    private RadioButton rb_carousel_04;
    private final int[] CAROUSEL_IVS = {R.drawable.img1, R.drawable.img2, R.drawable.img3, R.drawable.img3};
    private final String[] NEWS_TITLES = {"【刑罚执行】昭通市食药监局到昭通监狱开展警示教育", "【狱政管理】第二届换届选举张全蛋公选为监狱长", "【教育改造】第二届换届选举张全蛋公选为监狱长", "【劳动改造】第二届换届选举张全蛋公选为监狱长"};
    private final String[] NEWS_CONTENTS = {"    为深入践行“三严三实”和“忠诚干净担当”专题教育实践活动，加强系统党风廉政建设，提高干部拒腐防变能力，党员干部到昭通监狱接手廉政...", "2015年12月15日，广东监狱举办第二届换届选举，张全蛋以最多票数被选为监狱长。", "2015年12月15日，广东监狱举办第二届换届选举，张全蛋以最多票数被选为监狱长。", "2015年12月15日，广东监狱举办第二届换届选举，张全蛋以最多票数被选为监狱长。"};
    private ListView lv_prison_open;

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.activity_prison_open, null);
        vp_carousel = (ViewPager) view.findViewById(R.id.vp_carousel);
        rg_carousel = (RadioGroup) view.findViewById(R.id.rg_carousel);
        rb_carousel_01 = (RadioButton) view.findViewById(R.id.rb_carousel_01);
        rb_carousel_02 = (RadioButton) view.findViewById(R.id.rb_carousel_02);
        rb_carousel_03 = (RadioButton) view.findViewById(R.id.rb_carousel_03);
        rb_carousel_04 = (RadioButton) view.findViewById(R.id.rb_carousel_04);
        lv_prison_open = (ListView) view.findViewById(R.id.lv_prison_open);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("狱务公开");
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
        lv_prison_open.setAdapter(new MyAdapter());
        lv_prison_open.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showToastMsgShort("position++" + NEWS_TITLES[position]);
            }
        });
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
                convertView = View.inflate(mContext, R.layout.prison_open_item, null);
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
