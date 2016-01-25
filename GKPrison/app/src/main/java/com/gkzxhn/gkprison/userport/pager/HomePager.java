package com.gkzxhn.gkprison.userport.pager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
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
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.activity.FamilyServiceActivity;
import com.gkzxhn.gkprison.userport.activity.LawsRegulationsActivity;
import com.gkzxhn.gkprison.userport.activity.NewsDetailActivity;
import com.gkzxhn.gkprison.userport.activity.PrisonIntroductionActivity;
import com.gkzxhn.gkprison.userport.activity.PrisonOpenActivity;
import com.gkzxhn.gkprison.userport.activity.PrisonWardenActivity;
import com.gkzxhn.gkprison.userport.activity.VisitingServiceActivity;
import com.gkzxhn.gkprison.userport.bean.News;
import com.gkzxhn.gkprison.userport.view.RollViewPager;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private LinearLayout ll_home_news1;
    private LinearLayout ll_home_news2;
    private LinearLayout ll_home_news3;
    private ImageView iv_home_news_icon1;
    private TextView tv_home_news_title1;
    private TextView tv_home_news_content1;
    private ImageView iv_home_news_icon2;
    private TextView tv_home_news_title2;
    private TextView tv_home_news_content2;
    private ImageView iv_home_news_icon3;
    private TextView tv_home_news_title3;
    private TextView tv_home_news_content3;
    private News focus_news_1;
    private News focus_news_2;
    private News focus_news_3;
    private final int[] CAROUSEL_IVS = {R.drawable.carousel_default, R.drawable.carousel_default, R.drawable.carousel_default};
    private final int[] OPTIONS_IVS_PRESS = {R.drawable.prison_introduction_press,
            R.drawable.laws_press, R.drawable.prison_open_press,
            R.drawable.visit_service_press, R.drawable.family_service_press,
            R.drawable.sms_press};
    private final int[] OPTIONS_IVS = {R.drawable.prison_introduction,
            R.drawable.laws, R.drawable.prison_open,
            R.drawable.visit_service,
            R.drawable.family_service, R.drawable.sms};
    private final String[] OPTIONS_TVS = {"监狱简介", "法律法规", "狱务公开", "工作动态", "家属服务", "投诉建议"};
    private final List<String> list_news_title = new ArrayList<>();
    private SharedPreferences sp;
    private String token;// 当前登录用户的token
    private boolean isRegisteredUser;
    private ProgressDialog dialog;
    private List<News> focus_news_list;
    private BitmapUtils bitmapUtils;
    private View view_01;
    private View view_02;
    private TextView tv_focus_attention;
    private boolean is_request_foucs_news_successed = true; // true表示请求成功  false表示请求失败  请求失败时显示上一次缓存的焦点新闻
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
        ll_home_news1 = (LinearLayout) view.findViewById(R.id.ll_home_news1);
        ll_home_news2 = (LinearLayout) view.findViewById(R.id.ll_home_news2);
        ll_home_news3 = (LinearLayout) view.findViewById(R.id.ll_home_news3);
        iv_home_news_icon1 = (ImageView) view.findViewById(R.id.iv_home_news_icon1);
        tv_home_news_title1 = (TextView) view.findViewById(R.id.tv_home_news_title1);
        tv_home_news_content1 = (TextView) view.findViewById(R.id.tv_home_news_content1);
        iv_home_news_icon2 = (ImageView) view.findViewById(R.id.iv_home_news_icon2);
        tv_home_news_title2 = (TextView) view.findViewById(R.id.tv_home_news_title2);
        tv_home_news_content2 = (TextView) view.findViewById(R.id.tv_home_news_content2);
        iv_home_news_icon3 = (ImageView) view.findViewById(R.id.iv_home_news_icon3);
        tv_home_news_title3 = (TextView) view.findViewById(R.id.tv_home_news_title3);
        tv_home_news_content3 = (TextView) view.findViewById(R.id.tv_home_news_content3);
        layout_roll_view = View.inflate(context, R.layout.layout_roll_view, null);
        dots_ll = (LinearLayout) layout_roll_view.findViewById(R.id.dots_ll);
        top_news_title = (TextView) layout_roll_view.findViewById(R.id.top_news_title);
        top_news_viewpager = (LinearLayout) layout_roll_view.findViewById(R.id.top_news_viewpager);
        rl_carousel.addView(layout_roll_view);
        view_01 = view.findViewById(R.id.view_01);
        view_02 = view.findViewById(R.id.view_02);
        tv_focus_attention = (TextView) view.findViewById(R.id.tv_focus_attention);
        return view;
    }

    @Override
    public void initData() {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        isRegisteredUser = sp.getBoolean("isRegisteredUser", false);
        Drawable[] drawables = tv_focus_attention.getCompoundDrawables();
        drawables[0].setBounds(0, 0, 40, 40);
        tv_focus_attention.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        initDot();// 初始化轮播图底部小圆圈
        vp_carousel = new RollViewPager(context, dotList, CAROUSEL_IVS, new RollViewPager.OnViewClickListener() {
            @Override
            public void viewClick(int position) {
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
        if(isRegisteredUser) {
            token = sp.getString("token", "");
        }
        getFocusNews();// 获取焦点新闻
    }

    /**
     * 加载数据进度对话框
     */
    private void showLoadingDialog() {
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("");
        dialog.show();
    }

    /**
     * 获取焦点新闻
     */
    private void getFocusNews() {
        showLoadingDialog();
        HttpUtils httpUtils = new HttpUtils();
        Log.i("获取焦点新闻url", Constants.URL_HEAD + Constants.NEWS_URL);
        httpUtils.send(HttpRequest.HttpMethod.GET, Constants.URL_HEAD + Constants.NEWS_URL, new RequestCallBack<Object>() {
            @Override
            public void onSuccess(ResponseInfo<Object> responseInfo) {
                parseFocusNews(responseInfo.result.toString());// 解析焦点新闻
                dialog.dismiss();
                fillNewsData();// 填充新闻数据
                is_request_foucs_news_successed = true;
                Log.i("获取焦点新闻成功", responseInfo.result.toString());
            }

            @Override
            public void onFailure(HttpException e, String s) {
                dialog.dismiss();
                Log.i("获取焦点新闻失败", e.getMessage() + "---" + s);
                setCacheNews();// 若请求失败  则显示缓存新闻
                is_request_foucs_news_successed = false;
            }
        });
    }

    /**
     * 设置缓存焦点新闻
     */
    private void setCacheNews() {
        String focus_news_1_title = sp.getString("focus_news_1_title", "");
        String focus_news_1_content = sp.getString("focus_news_1_content", "");
        String focus_news_1_img_url = sp.getString("focus_news_1_img_url", "");
        int focus_news_1_id = sp.getInt("focus_news_1_id", 0);
        if(!TextUtils.isEmpty(focus_news_1_title) && !TextUtils.isEmpty(focus_news_1_content) && !TextUtils.isEmpty(focus_news_1_img_url) && focus_news_1_id != 0){
            // 有第一条缓存新闻
            tv_home_news_title1.setText(focus_news_1_title);
            tv_home_news_content1.setText(focus_news_1_content);
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon1, Constants.RESOURSE_HEAD + focus_news_1_img_url);
        }

        String focus_news_2_title = sp.getString("focus_news_2_title", "");
        String focus_news_2_content = sp.getString("focus_news_2_content", "");
        String focus_news_2_img_url = sp.getString("focus_news_2_img_url", "");
        int focus_news_2_id = sp.getInt("focus_news_2_id", 0);
        if(!TextUtils.isEmpty(focus_news_2_title) && !TextUtils.isEmpty(focus_news_2_content) && !TextUtils.isEmpty(focus_news_2_img_url) && focus_news_2_id != 0){
            // 有第二条缓存新闻
            tv_home_news_title2.setText(focus_news_2_title);
            tv_home_news_content2.setText(focus_news_2_content);
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon2, Constants.RESOURSE_HEAD + focus_news_2_img_url);
        }

        String focus_news_3_title = sp.getString("focus_news_3_title", "");
        String focus_news_3_content = sp.getString("focus_news_3_content", "");
        String focus_news_3_img_url = sp.getString("focus_news_3_img_url", "");
        int focus_news_3_id = sp.getInt("focus_news_3_id", 0);
        if(!TextUtils.isEmpty(focus_news_3_title) && !TextUtils.isEmpty(focus_news_3_content) && !TextUtils.isEmpty(focus_news_3_img_url) && focus_news_3_id != 0){
            // 有第三条缓存新闻
            tv_home_news_title3.setText(focus_news_3_title);
            tv_home_news_content3.setText(focus_news_3_content);
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon3, Constants.RESOURSE_HEAD + focus_news_3_img_url);
        }
    }

    /**
     * 填充焦新闻数据
     */
    private void fillNewsData() {
        if(focus_news_list.size() == 0){
            ll_home_news1.setVisibility(View.GONE);
            ll_home_news2.setVisibility(View.GONE);
            ll_home_news3.setVisibility(View.GONE);
            view_01.setVisibility(View.GONE);
            view_02.setVisibility(View.GONE);
            tv_focus_attention.setVisibility(View.GONE);
        }else if(focus_news_list.size() == 1){
            ll_home_news2.setVisibility(View.GONE);
            ll_home_news3.setVisibility(View.GONE);
            view_01.setVisibility(View.VISIBLE);
            view_02.setVisibility(View.VISIBLE);
            tv_focus_attention.setVisibility(View.VISIBLE);

            focus_news_1 = focus_news_list.get(0);
            SharedPreferences.Editor editor = sp.edit(); // 缓存
            editor.putString("focus_news_1_title", focus_news_1.getTitle());
            editor.putString("focus_news_1_content", focus_news_1.getContents());
            editor.putString("focus_news_1_img_url", focus_news_1.getImage_url());
            editor.putInt("focus_news_1_id", focus_news_1.getId());
            editor.commit();
            tv_home_news_title1.setText(focus_news_1.getTitle());
            tv_home_news_content1.setText(focus_news_1.getContents());
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon1, Constants.RESOURSE_HEAD + focus_news_1.getImage_url());
        }else if(focus_news_list.size() == 2){
            ll_home_news3.setVisibility(View.GONE);
            view_01.setVisibility(View.VISIBLE);
            view_02.setVisibility(View.VISIBLE);
            tv_focus_attention.setVisibility(View.VISIBLE);

            focus_news_1 = focus_news_list.get(0);
            tv_home_news_title1.setText(focus_news_1.getTitle());
            tv_home_news_content1.setText(focus_news_1.getContents());
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon1, Constants.RESOURSE_HEAD + focus_news_1.getImage_url());

            focus_news_2 = focus_news_list.get(1);
            tv_home_news_title2.setText(focus_news_2.getTitle());
            tv_home_news_content2.setText(focus_news_2.getContents());
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon2, Constants.RESOURSE_HEAD + focus_news_2.getImage_url());

            SharedPreferences.Editor editor = sp.edit(); // 缓存
            editor.putString("focus_news_1_title", focus_news_1.getTitle());
            editor.putString("focus_news_1_content", focus_news_1.getContents());
            editor.putString("focus_news_1_img_url", focus_news_1.getImage_url());
            editor.putInt("focus_news_1_id", focus_news_1.getId());

            editor.putString("focus_news_2_title", focus_news_2.getTitle());
            editor.putString("focus_news_2_content", focus_news_2.getContents());
            editor.putString("focus_news_2_img_url", focus_news_2.getImage_url());
            editor.putInt("focus_news_2_id", focus_news_2.getId());
            editor.commit();
        }else if(focus_news_list.size() >= 3) {
            view_01.setVisibility(View.VISIBLE);
            view_02.setVisibility(View.VISIBLE);
            tv_focus_attention.setVisibility(View.VISIBLE);

            focus_news_1 = focus_news_list.get(0);
            tv_home_news_title1.setText(focus_news_1.getTitle());
            tv_home_news_content1.setText(focus_news_1.getContents());
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon1, Constants.RESOURSE_HEAD + focus_news_1.getImage_url());

            focus_news_2 = focus_news_list.get(1);
            tv_home_news_title2.setText(focus_news_2.getTitle());
            tv_home_news_content2.setText(focus_news_2.getContents());
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon2, Constants.RESOURSE_HEAD + focus_news_2.getImage_url());

            focus_news_3 = focus_news_list.get(2);
            tv_home_news_title3.setText(focus_news_3.getTitle());
            tv_home_news_content3.setText(focus_news_3.getContents());
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon3, Constants.RESOURSE_HEAD + focus_news_3.getImage_url());

            SharedPreferences.Editor editor = sp.edit(); // 缓存
            editor.putString("focus_news_1_title", focus_news_1.getTitle());
            editor.putString("focus_news_1_content", focus_news_1.getContents());
            editor.putString("focus_news_1_img_url", focus_news_1.getImage_url());
            editor.putInt("focus_news_1_id", focus_news_1.getId());

            editor.putString("focus_news_2_title", focus_news_2.getTitle());
            editor.putString("focus_news_2_content", focus_news_2.getContents());
            editor.putString("focus_news_2_img_url", focus_news_2.getImage_url());
            editor.putInt("focus_news_2_id", focus_news_2.getId());

            editor.putString("focus_news_3_title", focus_news_3.getTitle());
            editor.putString("focus_news_3_content", focus_news_3.getContents());
            editor.putString("focus_news_3_img_url", focus_news_3.getImage_url());
            editor.putInt("focus_news_3_id", focus_news_3.getId());
            editor.commit();
        }
    }

    /**
     * 解析焦点新闻
     */
    private void parseFocusNews(String focus_news) {
        focus_news_list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(focus_news);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                News news = new News();
                news.setContents(jsonObject.getString("contents"));
                news.setCreated_at(jsonObject.getString("created_at"));
                news.setId(jsonObject.getInt("id"));
                news.setImage_content_type(jsonObject.getString("image_content_type"));
                news.setImage_file_name(jsonObject.getString("image_file_name"));
                news.setImage_file_size(jsonObject.getInt("image_file_size"));
                news.setImage_updated_at(jsonObject.getString("image_updated_at"));
                news.setImage_url(jsonObject.getString("image_url"));
                news.setIsFocus(jsonObject.getBoolean("isFocus"));
                news.setJail_id(jsonObject.getInt("jail_id"));
                news.setTitle(jsonObject.getString("title"));
                news.setType_id(TextUtils.isEmpty(jsonObject.getInt("type_id") + "") ? 1 : 1);
                news.setUpdated_at(jsonObject.getString("updated_at"));
                if(news.getIsFocus()) {
                    focus_news_list.add(news);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToastMsgShort("新闻解析异常");
        }
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
                                    intent = new Intent(context, VisitingServiceActivity.class);
                                    context.startActivity(intent);
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
                intent = new Intent(context, NewsDetailActivity.class);
                if(is_request_foucs_news_successed) {
                    intent.putExtra("id", focus_news_1.getId());
                }else {
                    int focus_news_1_id = sp.getInt("focus_news_1_id", 0);
                    intent.putExtra("id", focus_news_1_id);
                }
                context.startActivity(intent);
                break;
            case R.id.ll_home_news2:
                intent = new Intent(context, NewsDetailActivity.class);
                if(is_request_foucs_news_successed) {
                    intent.putExtra("id", focus_news_1.getId());
                }else {
                    int focus_news_2_id = sp.getInt("focus_news_2_id", 0);
                    intent.putExtra("id", focus_news_2_id);
                }
                context.startActivity(intent);
                break;
            case R.id.ll_home_news3:
                intent = new Intent(context, NewsDetailActivity.class);
                if(is_request_foucs_news_successed) {
                    intent.putExtra("id", focus_news_1.getId());
                }else {
                    int focus_news_3_id = sp.getInt("focus_news_3_id", 0);
                    intent.putExtra("id", focus_news_3_id);
                }
                context.startActivity(intent);
                break;
        }
        super.onClick(v);
    }
}
