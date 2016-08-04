package com.gkzxhn.gkprison.userport.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
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
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.activity.FamilyServiceActivity;
import com.gkzxhn.gkprison.userport.activity.LawsRegulationsActivity;
import com.gkzxhn.gkprison.userport.activity.NewsDetailActivity;
import com.gkzxhn.gkprison.userport.activity.PrisonIntroductionActivity;
import com.gkzxhn.gkprison.userport.activity.PrisonOpenActivity;
import com.gkzxhn.gkprison.userport.activity.PrisonWardenActivity;
import com.gkzxhn.gkprison.userport.activity.WorkDynamicActivity;
import com.gkzxhn.gkprison.userport.bean.News;
import com.gkzxhn.gkprison.userport.requests.ApiRequest;
import com.gkzxhn.gkprison.userport.view.RollViewPager;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.lidroid.xutils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * author:huangzhengneng
 * email:943852572@qq.com
 * date: 2016/8/2.
 * function:
 */

public class HomeFragment extends BaseFragment implements View.OnClickListener{

    private static final java.lang.String TAG = "HomeFragment";
    private final int[] OPTIONS_IVS_PRESS = {
            R.drawable.prison_introduction_press,
            R.drawable.laws_press, R.drawable.prison_open_press,
            R.drawable.visit_service_press, R.drawable.family_service_press,
            R.drawable.sms_press};
    private final int[] OPTIONS_IVS = {
            R.drawable.prison_introduction,
            R.drawable.laws, R.drawable.prison_open,
            R.drawable.visit_service,
            R.drawable.family_service, R.drawable.sms};
    private final String[] OPTIONS_TVS = {"监狱简介", "法律法规", "狱务公开", "工作动态", "家属服务", "投诉建议"};

    @BindView(R.id.rl_carousel) RelativeLayout rl_carousel;
    @BindView(R.id.dots_ll) LinearLayout dots_ll;
    @BindView(R.id.ll_title_dot) LinearLayout ll_title_dot;
    @BindView(R.id.top_news_title) TextView top_news_title;
    @BindView(R.id.top_news_viewpager) LinearLayout top_news_viewpager;
    @BindView(R.id.gv_home_options) GridView gv_home_options;
    @BindView(R.id.ll_home_news1) LinearLayout ll_home_news1;
    @BindView(R.id.ll_home_news2) LinearLayout ll_home_news2;
    @BindView(R.id.ll_home_news3) LinearLayout ll_home_news3;
    @BindView(R.id.iv_home_news_icon1) ImageView iv_home_news_icon1;
    @BindView(R.id.tv_home_news_title1) TextView tv_home_news_title1;
    @BindView(R.id.tv_home_news_content1) TextView tv_home_news_content1;
    @BindView(R.id.iv_home_news_icon2) ImageView iv_home_news_icon2;
    @BindView(R.id.tv_home_news_title2) TextView tv_home_news_title2;
    @BindView(R.id.tv_home_news_content2) TextView tv_home_news_content2;
    @BindView(R.id.iv_home_news_icon3) ImageView iv_home_news_icon3;
    @BindView(R.id.tv_home_news_title3) TextView tv_home_news_title3;
    @BindView(R.id.tv_home_news_content3) TextView tv_home_news_content3;
    @BindView(R.id.view_01) View view_01;
    @BindView(R.id.view_02) View view_02;
    @BindView(R.id.tv_focus_attention) TextView tv_focus_attention;
    @BindView(R.id.srl_refresh) SwipeRefreshLayout srl_refresh;

    private RollViewPager vp_carousel;
    private News focus_news_1;
    private News focus_news_2;
    private News focus_news_3;

    private final List<String> list_news_title = new ArrayList<>();
    private int jail_id;
    private boolean isRegisteredUser;
    private boolean is_request_foucs_news_successed = true; // true表示请求成功  false表示请求失败  请求失败时显示上一次缓存的焦点新闻
    private String token;// 当前登录用户的token
    private ProgressDialog dialog;
    private List<News> focus_news_list;
    private List<News> allnews;
    private BitmapUtils bitmapUtils;
    /**
     * 轮播图导航点集合
     */
    private List<View> dotList = new ArrayList<>();

    @Override
    protected View initView() {
        View view = View.inflate(context, R.layout.fragment_home, null);
        ButterKnife.bind(this, view);
        srl_refresh.setColorSchemeResources(R.color.theme);
        return view;
    }

    @Override
    protected void initData() {
        isRegisteredUser = (Boolean) SPUtil.get(getActivity(), "isRegisteredUser", false);
        jail_id = (int) SPUtil.get(getActivity(), "jail_id", 0);
        showLoadingDialog(); // 初次进来加载对话框
        getFocusNews();// 获取焦点新闻
        Drawable[] drawables = tv_focus_attention.getCompoundDrawables();
        drawables[0].setBounds(0, 0, 40, 40);
        tv_focus_attention.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        gv_home_options.setAdapter(new MyOptionsAdapter());
        ll_home_news1.setOnClickListener(this);
        ll_home_news2.setOnClickListener(this);
        ll_home_news3.setOnClickListener(this);
        if(isRegisteredUser) {
            token = (String) SPUtil.get(getActivity(), "token", "");
        }
        srl_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFocusNews();// 获取焦点新闻
            }
        });
    }

    /**
     * 加载数据进度对话框
     */
    private void showLoadingDialog() {
        if(dialog == null) {
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("");
            dialog.show();
        }else {
            dialog.show();
        }
    }

    /**
     * 获取焦点新闻
     */
    private void getFocusNews() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiRequest api = retrofit.create(ApiRequest.class);
        api.getNews(jail_id)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<News>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        if(dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        setCacheNews();// 若请求失败  则显示缓存新闻
                        is_request_foucs_news_successed = false;
                        if(srl_refresh.isRefreshing()){
                            srl_refresh.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onNext(List<News> newses) {
                        focus_news_list = new ArrayList<>();
                        allnews = new ArrayList<>();
                        for (News news : newses) {
//                            Log.i(TAG, "news : " + news.toString());
                            if(news.getIsFocus()){
                                focus_news_list.add(news);
                            }
                            allnews.add(news);
                        }
                        Log.i(TAG, "焦点新闻数目:" + focus_news_list.size() + "，总新闻数：" + allnews.size());
                        setRoll();// 设置轮播
                        fillNewsData();// 填充新闻数据
                    }
                });
    }

    /**
     * 设置轮播
     */
    private void setRoll() {
        list_news_title.clear();
        List<String> imgurl_list = new ArrayList<>();
        if (allnews.size() > 2) {
            list_news_title.add("");
            list_news_title.add("");
            list_news_title.add("");
//            imgurl_list.add(Constants.RESOURSE_HEAD + allnews.get(0).getImage_url());
//            imgurl_list.add(Constants.RESOURSE_HEAD + allnews.get(1).getImage_url());
//            imgurl_list.add(Constants.RESOURSE_HEAD + allnews.get(2).getImage_url());
            imgurl_list.add("");
            imgurl_list.add("");
            imgurl_list.add("");
        } else if(allnews.size() == 1){
            list_news_title.add("");
//            imgurl_list.add(Constants.RESOURSE_HEAD + allnews.get(0).getImage_url());
            imgurl_list.add("");
        } else if(allnews.size() == 2){
            list_news_title.add("");
            list_news_title.add("");
//            imgurl_list.add(Constants.RESOURSE_HEAD + allnews.get(0).getImage_url());
//            imgurl_list.add(Constants.RESOURSE_HEAD + allnews.get(1).getImage_url());
            imgurl_list.add("");
            imgurl_list.add("");
        }else { // <=0

        }
        initDot();// 初始化轮播图底部小圆圈
        vp_carousel = new RollViewPager(context, dotList, new RollViewPager.OnViewClickListener() {
            @Override
            public void viewClick(int position) {
                if(allnews.size() > 0) {
                    int i = allnews.get(position).getId();
                    Intent intent = new Intent(context, NewsDetailActivity.class);
                    intent.putExtra("id", i);
                    intent.putExtra("type", 0);// 0是轮播图   1是新闻
                    intent.putExtra("index", position + 1);
                    context.startActivity(intent);
                }else {
                    showToastMsgShort("抱歉，没有数据...");
                }
            }
        });
//        if(allnews.size() > 0 && list_news_title.size() > 0) {
        vp_carousel.initTitle(list_news_title, top_news_title);
        vp_carousel.initImgUrl(imgurl_list);
//        }else {
//            showToastMsgShort("抱歉,没有数据...");
//        }
        vp_carousel.startRoll();
        top_news_viewpager.removeAllViews();
        top_news_viewpager.addView(vp_carousel);
    }

    /**
     * 设置缓存焦点新闻
     */
    private void setCacheNews() {
        String focus_news_1_title = (String) SPUtil.get(getActivity(), "focus_news_1_title", "");
        String focus_news_1_content = (String) SPUtil.get(getActivity(), "focus_news_1_content", "");
        String focus_news_1_img_url = (String) SPUtil.get(getActivity(), "focus_news_1_img_url", "");
        int focus_news_1_id = (int) SPUtil.get(getActivity(), "focus_news_1_id", 0);
        if(!TextUtils.isEmpty(focus_news_1_title) && !TextUtils.isEmpty(focus_news_1_content)
                && !TextUtils.isEmpty(focus_news_1_img_url) && focus_news_1_id != 0){
            // 有第一条缓存新闻
            tv_home_news_title1.setText(Html.fromHtml(focus_news_1_title));
            tv_home_news_content1.setText(Html.fromHtml(focus_news_1_content));
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon1, Constants.RESOURSE_HEAD + focus_news_1_img_url);
        }

        String focus_news_2_title = (String) SPUtil.get(getActivity(), "focus_news_2_title", "");
        String focus_news_2_content = (String) SPUtil.get(getActivity(), "focus_news_2_content", "");
        String focus_news_2_img_url = (String) SPUtil.get(getActivity(), "focus_news_2_img_url", "");
        int focus_news_2_id = (int) SPUtil.get(getActivity(), "focus_news_2_id", 0);
        if(!TextUtils.isEmpty(focus_news_2_title) && !TextUtils.isEmpty(focus_news_2_content)
                && !TextUtils.isEmpty(focus_news_2_img_url) && focus_news_2_id != 0){
            // 有第二条缓存新闻
            tv_home_news_title2.setText(Html.fromHtml(focus_news_2_title));
            tv_home_news_content2.setText(Html.fromHtml(focus_news_2_content));
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon2, Constants.RESOURSE_HEAD + focus_news_2_img_url);
        }

        String focus_news_3_title = (String) SPUtil.get(getActivity(), "focus_news_3_title", "");
        String focus_news_3_content = (String) SPUtil.get(getActivity(), "focus_news_3_content", "");
        String focus_news_3_img_url = (String) SPUtil.get(getActivity(), "focus_news_3_img_url", "");
        int focus_news_3_id = (int) SPUtil.get(getActivity(), "focus_news_3_id", 0);
        if(!TextUtils.isEmpty(focus_news_3_title) && !TextUtils.isEmpty(focus_news_3_content) && !TextUtils.isEmpty(focus_news_3_img_url) && focus_news_3_id != 0){
            // 有第三条缓存新闻
            tv_home_news_title3.setText(Html.fromHtml(focus_news_3_title));
            tv_home_news_content3.setText(Html.fromHtml(focus_news_3_content));
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
            SPUtil.put(getActivity(), "focus_news_1_title", focus_news_1.getTitle());
            SPUtil.put(getActivity(), "focus_news_1_content", focus_news_1.getContents());
            SPUtil.put(getActivity(), "focus_news_1_img_url", focus_news_1.getImage_url());
            SPUtil.put(getActivity(), "focus_news_1_id", focus_news_1.getId());
            tv_home_news_title1.setText(Html.fromHtml(focus_news_1.getTitle()));
            tv_home_news_content1.setText(Html.fromHtml(focus_news_1.getContents()));
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon1, Constants.RESOURSE_HEAD + focus_news_1.getImage_url());
        }else if(focus_news_list.size() == 2){
            ll_home_news3.setVisibility(View.GONE);
            view_01.setVisibility(View.VISIBLE);
            view_02.setVisibility(View.VISIBLE);
            tv_focus_attention.setVisibility(View.VISIBLE);

            focus_news_1 = focus_news_list.get(0);
            tv_home_news_title1.setText(Html.fromHtml(focus_news_1.getTitle()));
            tv_home_news_content1.setText(Html.fromHtml(focus_news_1.getContents()));
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon1, Constants.RESOURSE_HEAD + focus_news_1.getImage_url());

            focus_news_2 = focus_news_list.get(1);
            tv_home_news_title2.setText(Html.fromHtml(focus_news_2.getTitle()));
            tv_home_news_content2.setText(Html.fromHtml(focus_news_2.getContents()));
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon2, Constants.RESOURSE_HEAD + focus_news_2.getImage_url());

            SPUtil.put(getActivity(), "focus_news_1_title", focus_news_1.getTitle());
            SPUtil.put(getActivity(), "focus_news_1_content", focus_news_1.getContents());
            SPUtil.put(getActivity(), "focus_news_1_img_url", focus_news_1.getImage_url());
            SPUtil.put(getActivity(), "focus_news_1_id", focus_news_1.getId());

            SPUtil.put(getActivity(), "focus_news_2_title", focus_news_2.getTitle());
            SPUtil.put(getActivity(), "focus_news_2_content", focus_news_2.getContents());
            SPUtil.put(getActivity(), "focus_news_2_img_url", focus_news_2.getImage_url());
            SPUtil.put(getActivity(), "focus_news_2_id", focus_news_2.getId());
        }else if(focus_news_list.size() >= 3) {
            view_01.setVisibility(View.VISIBLE);
            view_02.setVisibility(View.VISIBLE);
            tv_focus_attention.setVisibility(View.VISIBLE);

            focus_news_1 = focus_news_list.get(0);
            tv_home_news_title1.setText(Html.fromHtml(focus_news_1.getTitle()));
            tv_home_news_content1.setText(Html.fromHtml(focus_news_1.getContents()));
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon1, Constants.RESOURSE_HEAD + focus_news_1.getImage_url());

            focus_news_2 = focus_news_list.get(1);
            tv_home_news_title2.setText(Html.fromHtml(focus_news_2.getTitle()));
            tv_home_news_content2.setText(Html.fromHtml(focus_news_2.getContents()));
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon2, Constants.RESOURSE_HEAD + focus_news_2.getImage_url());

            focus_news_3 = focus_news_list.get(2);
            tv_home_news_title3.setText(Html.fromHtml(focus_news_3.getTitle()));
            tv_home_news_content3.setText(Html.fromHtml(focus_news_3.getContents()));
            bitmapUtils = new BitmapUtils(context);
            bitmapUtils.display(iv_home_news_icon3, Constants.RESOURSE_HEAD + focus_news_3.getImage_url());

            SPUtil.put(getActivity(), "focus_news_1_title", focus_news_1.getTitle());
            SPUtil.put(getActivity(), "focus_news_1_content", focus_news_1.getContents());
            SPUtil.put(getActivity(), "focus_news_1_img_url", focus_news_1.getImage_url());
            SPUtil.put(getActivity(), "focus_news_1_id", focus_news_1.getId());

            SPUtil.put(getActivity(), "focus_news_2_title", focus_news_2.getTitle());
            SPUtil.put(getActivity(), "focus_news_2_content", focus_news_2.getContents());
            SPUtil.put(getActivity(), "focus_news_2_img_url", focus_news_2.getImage_url());
            SPUtil.put(getActivity(), "focus_news_2_id", focus_news_2.getId());

            SPUtil.put(getActivity(), "focus_news_3_title", focus_news_3.getTitle());
            SPUtil.put(getActivity(), "focus_news_3_content", focus_news_3.getContents());
            SPUtil.put(getActivity(), "focus_news_3_img_url", focus_news_3.getImage_url());
            SPUtil.put(getActivity(), "focus_news_3_id", focus_news_3.getId());
        }
        if(dialog.isShowing()) {
            dialog.dismiss();// 消掉加载对话框进度条
        }
        if(srl_refresh.isRefreshing()){
            srl_refresh.setRefreshing(false);
        }
    }

    private void initDot() {
        dotList.clear();
        dots_ll.removeAllViews();
//        if(list_news_title.size() > 0){
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
//        }
        ll_title_dot.setGravity(Gravity.CENTER);
        ll_title_dot.setBackgroundColor(Color.TRANSPARENT);
    }

    private class MyOptionsAdapter extends BaseAdapter {

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
                            boolean isRegisteredUser = (Boolean) SPUtil.get(getActivity(), "isRegisteredUser", false);
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
                                    intent = new Intent(context, WorkDynamicActivity.class);
                                    context.startActivity(intent);
                                    break;
                                case 5:
                                    if (isRegisteredUser) {
                                        intent = new Intent(context, PrisonWardenActivity.class);
                                        context.startActivity(intent);
                                    }else {
                                        showToastMsgShort(context.getString(R.string.enable_logined));
                                    }
                                    break;
                                case 4:
                                    if(isRegisteredUser){
                                        intent = new Intent(context, FamilyServiceActivity.class);
                                        context.startActivity(intent);
                                    }else {
                                        showToastMsgShort(context.getString(R.string.enable_logined));
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
                intent.putExtra("type", 1);// 0是轮播图   1是新闻
                if(is_request_foucs_news_successed) {
                    intent.putExtra("id", focus_news_1.getId());
                }else {
                    int focus_news_1_id = (int) SPUtil.get(getActivity(), "focus_news_1_id", 0);
                    intent.putExtra("id", focus_news_1_id);
                }
                context.startActivity(intent);
                break;
            case R.id.ll_home_news2:
                intent = new Intent(context, NewsDetailActivity.class);
                intent.putExtra("type", 1);// 0是轮播图   1是新闻
                if(is_request_foucs_news_successed) {
                    intent.putExtra("id", focus_news_2.getId());
                }else {
                    int focus_news_2_id = (int) SPUtil.get(getActivity(), "focus_news_2_id", 0);
                    intent.putExtra("id", focus_news_2_id);
                }
                context.startActivity(intent);
                break;
            case R.id.ll_home_news3:
                intent = new Intent(context, NewsDetailActivity.class);
                intent.putExtra("type", 1);// 0是轮播图   1是新闻
                if(is_request_foucs_news_successed) {
                    intent.putExtra("id", focus_news_3.getId());
                }else {
                    int focus_news_3_id = (int) SPUtil.get(getActivity(), "focus_news_3_id", 0);
                    intent.putExtra("id", focus_news_3_id);
                }
                context.startActivity(intent);
                break;
        }
    }
}
