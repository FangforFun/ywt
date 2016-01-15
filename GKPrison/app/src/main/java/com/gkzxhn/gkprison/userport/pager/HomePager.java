package com.gkzxhn.gkprison.userport.pager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BasePager;
import com.gkzxhn.gkprison.userport.activity.FamilyServiceActivity;
import com.gkzxhn.gkprison.userport.activity.LawsRegulationsActivity;
import com.gkzxhn.gkprison.userport.activity.NewsDetailActivity;
import com.gkzxhn.gkprison.userport.activity.PrisonIntroductionActivity;
import com.gkzxhn.gkprison.userport.activity.PrisonOpenActivity;
import com.gkzxhn.gkprison.userport.activity.PrisonWardenActivity;
import com.gkzxhn.gkprison.userport.activity.VisitingServiceActivity;
import com.gkzxhn.gkprison.userport.view.RollViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzn on 2015/12/3.
 */
public class HomePager extends BasePager {

    private RelativeLayout rl_carousel;
    //    private ViewPager vp_carousel;
    private RollViewPager vp_carousel;
    private View layout_roll_view;
    private LinearLayout dots_ll;
    private TextView top_news_title;
    private LinearLayout top_news_viewpager;
    private GridView gv_home_options;
    private TextView tv_focus_attention; // 焦点关注
    private LinearLayout ll_home_news1;
    private LinearLayout ll_home_news2;
    private LinearLayout ll_home_news3;
//    private ImageView iv_home_news_icon;
//    private TextView tv_home_news_title;
//    private TextView tv_home_news_content;
    private final int[] CAROUSEL_IVS = {R.drawable.img1, R.drawable.img2, R.drawable.img3};
    private final int[] OPTIONS_IVS_PRESS = {R.drawable.prison_introduction_press, R.drawable.laws_press, R.drawable.prison_open_press, R.drawable.visit_service_press, R.drawable.family_service_press, R.drawable.sms_press};
    private final int[] OPTIONS_IVS = {R.drawable.prison_introduction, R.drawable.laws, R.drawable.prison_open, R.drawable.visit_service, R.drawable.family_service, R.drawable.sms};
    private final String[] OPTIONS_TVS = {"监狱简介", "法律法规", "狱务公开", "工作动态", "家属服务", "投诉建议"};
    private final List<String> list_news_title = new ArrayList<>();
    private SharedPreferences sp;
    /**
     * 轮播图导航点集合
     */
    private List<View> dotList = new ArrayList<>();

    public HomePager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        view = View.inflate(context, R.layout.pager_home, null);
        rl_carousel = (RelativeLayout) view.findViewById(R.id.rl_carousel);
        gv_home_options = (GridView) view.findViewById(R.id.gv_home_options);
        tv_focus_attention = (TextView) view.findViewById(R.id.tv_focus_attention);
        ll_home_news1 = (LinearLayout) view.findViewById(R.id.ll_home_news1);
        ll_home_news2 = (LinearLayout) view.findViewById(R.id.ll_home_news2);
        ll_home_news3 = (LinearLayout) view.findViewById(R.id.ll_home_news3);
//        iv_home_news_icon = (ImageView) view.findViewById(R.id.iv_home_news_icon);
//        tv_home_news_title = (TextView) view.findViewById(R.id.tv_home_news_title);
//        tv_home_news_content = (TextView) view.findViewById(R.id.tv_home_news_content);
        layout_roll_view = View.inflate(context, R.layout.layout_roll_view, null);
        dots_ll = (LinearLayout) layout_roll_view.findViewById(R.id.dots_ll);
        top_news_title = (TextView) layout_roll_view.findViewById(R.id.top_news_title);
        top_news_viewpager = (LinearLayout) layout_roll_view.findViewById(R.id.top_news_viewpager);
        rl_carousel.addView(layout_roll_view);
        return view;
    }

    @Override
    public void initData() {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        Drawable[] drawables = tv_focus_attention.getCompoundDrawables();
        drawables[0].setBounds(0, 0, 40, 40);
        tv_focus_attention.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        initDot();// 初始化轮播图底部小圆圈
        vp_carousel = new RollViewPager(context, dotList, CAROUSEL_IVS, new RollViewPager.OnViewClickListener() {
            @Override
            public void viewClick(int position) {
                showToastMsgShort(list_news_title.get(position));
                Intent intent = new Intent(context, NewsDetailActivity.class);
                context.startActivity(intent);
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
        gv_home_options.setAdapter(new MyOptionsAdapter());
        ll_home_news1.setOnClickListener(this);
        ll_home_news2.setOnClickListener(this);
        ll_home_news3.setOnClickListener(this);
    }

    private void initDot() {
        dotList.clear();
        dots_ll.removeAllViews();
        for (int i = 0; i < 3; i++) {
            View view = new View(context);
            if (i == 0) {
                view.setBackgroundResource(R.drawable.rb_shape_blue);
            } else {
                view.setBackgroundResource(R.drawable.rb_shape_gray);
            }
            // 指定点的大小
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    context.getResources().getDimensionPixelSize(R.dimen.dot_radius), context.getResources().getDimensionPixelSize(R.dimen.dot_radius));
            // 间距
            layoutParams.setMargins(10, 0, 10, 0);
            dots_ll.addView(view, layoutParams);

            dotList.add(view);
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
                            boolean isRegisteredUser = sp.getBoolean("isRegisteredUser", false);
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
                                    intent = new Intent(context, PrisonOpenActivity.class);
                                    context.startActivity(intent);
                                    break;
                                case 3:
                                    if (isRegisteredUser) {
                                        intent = new Intent(context, VisitingServiceActivity.class);
                                        context.startActivity(intent);
                                    } else {
                                        showToastMsgShort("注册后可用");
                                    }
                                    break;
                                case 5:
                                    if (isRegisteredUser) {
                                        intent = new Intent(context, PrisonWardenActivity.class);
                                        context.startActivity(intent);
                                    }else {
                                        showToastMsgShort("注册后使用");
                                    }
                                    break;
                                case 4:
                                    if(isRegisteredUser){
                                        intent = new Intent(context, FamilyServiceActivity.class);
                                        context.startActivity(intent);
                                    }else {
                                        showToastMsgShort("注册后使用");
                                    }
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
        Intent intent;
        switch (v.getId()){
            case R.id.ll_home_news1:
            case R.id.ll_home_news2:
            case R.id.ll_home_news3:
                intent = new Intent(context, NewsDetailActivity.class);
                context.startActivity(intent);
                break;
        }
        super.onClick(v);
    }
}
