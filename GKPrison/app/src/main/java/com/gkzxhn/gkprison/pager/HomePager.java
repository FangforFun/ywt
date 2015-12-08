package com.gkzxhn.gkprison.pager;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.activity.LawsRegulationsActivity;
import com.gkzxhn.gkprison.activity.PrisonIntroductionActivity;
import com.gkzxhn.gkprison.activity.PrisonWardenActivity;
import com.gkzxhn.gkprison.activity.VisitingServiceActivity;

/**
 * Created by hzn on 2015/12/3.
 */
public class HomePager extends BasePager{

    private ViewPager vp_carousel;
    private TextView tv_carousel_title; // 轮播图标题
    private RadioGroup rg_carousel; // 轮播图底部小圆圈
    private RadioButton rb_carousel_01;
    private RadioButton rb_carousel_02;
    private RadioButton rb_carousel_03;
    private GridView gv_home_options;
    private TextView tv_focus_attention; // 焦点关注
    private LinearLayout ll_home_news;
    private ImageView iv_home_news_icon;
    private TextView tv_home_news_title;
    private TextView tv_home_news_content;
    private final int[] CAROUSEL_IVS = {R.drawable.img1, R.drawable.img2, R.drawable.img3};
    private final int[] OPTIONS_IVS_PRESS = {R.drawable.prison_introduction_press, R.drawable.laws_press, R.drawable.prison_open_press, R.drawable.visit_service_press, R.drawable.sms_press, R.drawable.family_service_press};
    private final int[] OPTIONS_IVS = {R.drawable.prison_introduction, R.drawable.laws, R.drawable.prison_open, R.drawable.visit_service, R.drawable.sms, R.drawable.family_service};
    private final String[] OPTIONS_TVS = {"监狱简介", "法律法规", "狱务公开", "探监服务", "监狱长信箱", "家属服务"};

    public HomePager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        view = View.inflate(context, R.layout.pager_home, null);
        vp_carousel = (ViewPager) view.findViewById(R.id.vp_carousel);
        tv_carousel_title = (TextView) view.findViewById(R.id.tv_carousel_title);
        rg_carousel = (RadioGroup) view.findViewById(R.id.rg_carousel);
        rb_carousel_01 = (RadioButton) view.findViewById(R.id.rb_carousel_01);
        rb_carousel_02 = (RadioButton) view.findViewById(R.id.rb_carousel_02);
        rb_carousel_03 = (RadioButton) view.findViewById(R.id.rb_carousel_03);
        gv_home_options = (GridView) view.findViewById(R.id.gv_home_options);
        tv_focus_attention = (TextView) view.findViewById(R.id.tv_focus_attention);
        ll_home_news = (LinearLayout) view.findViewById(R.id.ll_home_news);
        iv_home_news_icon = (ImageView) view.findViewById(R.id.iv_home_news_icon);
        tv_home_news_title = (TextView) view.findViewById(R.id.tv_home_news_title);
        tv_home_news_content = (TextView) view.findViewById(R.id.tv_home_news_content);
        return view;
    }

    @Override
    public void initData() {
        Drawable[] drawables = tv_focus_attention.getCompoundDrawables();
        drawables[0].setBounds(0, 0, 40, 40);
        tv_focus_attention.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
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
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vp_carousel.setCurrentItem(0);// 默认选中第0个
        gv_home_options.setAdapter(new MyOptionsAdapter());
        ll_home_news.setOnClickListener(this);
    }

    private class MyCarouselAdapter extends PagerAdapter{

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
            View view = View.inflate(context, R.layout.carousel_item, null);
            ImageView iv_carousel = (ImageView) view.findViewById(R.id.iv_carousel);
            iv_carousel.setImageResource(CAROUSEL_IVS[position]);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private class MyOptionsAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return OPTIONS_IVS.length;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            final OptionsViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(context, R.layout.home_options_item, null);
                holder = new OptionsViewHolder();
                holder.iv_home_options = (ImageView) convertView.findViewById(R.id.iv_home_options);
                holder.tv_home_options = (TextView) convertView.findViewById(R.id.tv_home_options);
                convertView.setTag(holder);
            }else {
                holder = (OptionsViewHolder) convertView.getTag();
            }
            holder.iv_home_options.setImageResource(OPTIONS_IVS[position]);
            holder.tv_home_options.setText(OPTIONS_TVS[position]);
            final View finalConvertView = convertView;
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Intent intent;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            finalConvertView.setBackgroundColor(context.getResources().getColor(R.color.theme));
                            holder.tv_home_options.setTextColor(context.getResources().getColor(R.color.white));
                            holder.iv_home_options.setImageResource(OPTIONS_IVS_PRESS[position]);
                            break;
                        case MotionEvent.ACTION_MOVE:

                            break;
                        case MotionEvent.ACTION_UP:
                            finalConvertView.setBackgroundColor(context.getResources().getColor(R.color.white));
                            holder.tv_home_options.setTextColor(context.getResources().getColor(R.color.tv_bg));
                            holder.iv_home_options.setImageResource(OPTIONS_IVS[position]);
                            showToastMsgShort("hehehe" + position);
                            switch (position) {
                                case 0:
                                    intent = new Intent(context, PrisonIntroductionActivity.class);
                                    context.startActivity(intent);
                                    break;
                                case 1:
                                    intent = new Intent(context, LawsRegulationsActivity.class);
                                    context.startActivity(intent);
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    intent = new Intent(context, VisitingServiceActivity.class);
                                    context.startActivity(intent);
                                    break;
                                case 4:
                                    intent = new Intent(context, PrisonWardenActivity.class);
                                    context.startActivity(intent);
                                    break;
                                case 5:
                                    
                                    break;

                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            finalConvertView.setBackgroundColor(context.getResources().getColor(R.color.white));
                            holder.tv_home_options.setTextColor(context.getResources().getColor(R.color.tv_bg));
                            holder.iv_home_options.setImageResource(OPTIONS_IVS[position]);
                            break;
                    }
                    return true;
                }
            });
            return convertView;
        }
    }

    private static class OptionsViewHolder{
        ImageView iv_home_options;
        TextView tv_home_options;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_home_news:
                showToastMsgShort(tv_home_news_title.getText().toString().trim());
                break;
        }
        super.onClick(v);
    }
}
