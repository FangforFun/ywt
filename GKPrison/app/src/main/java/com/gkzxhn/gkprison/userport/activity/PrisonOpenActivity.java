package com.gkzxhn.gkprison.userport.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.News;
import com.gkzxhn.gkprison.userport.requests.ApiRequest;
import com.gkzxhn.gkprison.userport.view.RefreshLayout;
import com.gkzxhn.gkprison.userport.view.RollViewPager;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * 狱务公开页面
 */
public class PrisonOpenActivity extends BaseActivity {

    private static final java.lang.String TAG = "PrisonOpenActivity";
    @BindView(R.id.lv_prison_open) ListView lv_prison_open;
    private RollViewPager vp_carousel;
    private View layout_roll_view;
    private LinearLayout dots_ll;
    private TextView top_news_title;
    private LinearLayout top_news_viewpager;
    private final List<String> list_news_title = new ArrayList<>();
    private int jail_id;
    private List<News> allnews = new ArrayList<>();
    private List<News> newsList = new ArrayList<>();
    private ProgressDialog getNews_Dialog;
    private MyAdapter myAdapter;// 新闻列表适配器
    private RefreshLayout mRefreshLayout;
    private View footerLayout;
    private TextView textMore;
    private ProgressBar progressBar;
    private boolean isLoadingMore = false;

    /**
     * 相关ui操作
     */
    private void checkUI() {
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
        }
        if (isLoadingMore) {
            mRefreshLayout.setLoading(false);
            textMore.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            showToastMsgShort("加载完成");
            isLoadingMore = false;
        }
    }

    /**
     * 设置轮播图
     *
     * @param imgurl_list
     */
    private void setCarousel(List<String> imgurl_list) {
        if (newsList.size() > 3) {
            imgurl_list.add(Constants.RESOURSE_HEAD + newsList.get(0).getImage_url());
            imgurl_list.add(Constants.RESOURSE_HEAD + newsList.get(1).getImage_url());
            imgurl_list.add(Constants.RESOURSE_HEAD + newsList.get(2).getImage_url());
            imgurl_list.add(Constants.RESOURSE_HEAD + newsList.get(3).getImage_url());
            list_news_title.clear();
            list_news_title.add(newsList.get(0).getTitle());
            list_news_title.add(newsList.get(1).getTitle());
            list_news_title.add(newsList.get(2).getTitle());
            list_news_title.add(newsList.get(3).getTitle());
        } else if (newsList.size() == 3) {
            imgurl_list.add(Constants.RESOURSE_HEAD + newsList.get(0).getImage_url());
            imgurl_list.add(Constants.RESOURSE_HEAD + newsList.get(1).getImage_url());
            imgurl_list.add(Constants.RESOURSE_HEAD + newsList.get(2).getImage_url());
            list_news_title.clear();
            list_news_title.add(newsList.get(0).getTitle());
            list_news_title.add(newsList.get(1).getTitle());
            list_news_title.add(newsList.get(2).getTitle());
        } else if (newsList.size() == 2) {
            imgurl_list.add(Constants.RESOURSE_HEAD + newsList.get(0).getImage_url());
            imgurl_list.add(Constants.RESOURSE_HEAD + newsList.get(1).getImage_url());
            list_news_title.clear();
            list_news_title.add(newsList.get(0).getTitle());
            list_news_title.add(newsList.get(1).getTitle());
        } else if (newsList.size() == 1) {
            imgurl_list.add(Constants.RESOURSE_HEAD + newsList.get(0).getImage_url());
            list_news_title.clear();
            list_news_title.add(newsList.get(0).getTitle());
        }
        initDot();// 初始化轮播图底部小圆圈
        vp_carousel = new RollViewPager(getApplicationContext(), dotList, new RollViewPager.OnViewClickListener() {
            @Override
            public void viewClick(int position) {
                int i = newsList.get(position).getId();
                Intent intent = new Intent(PrisonOpenActivity.this, NewsDetailActivity.class);
                intent.putExtra("type", 1);// 0是轮播图   1是新闻
                intent.putExtra("id", i);
                startActivity(intent);
            }
        });
        vp_carousel.initImgUrl(imgurl_list);
        vp_carousel.initTitle(list_news_title, top_news_title);
        vp_carousel.startRoll();
        top_news_viewpager.removeAllViews();
        top_news_viewpager.addView(vp_carousel);
        if (getNews_Dialog.isShowing()) {
            getNews_Dialog.dismiss();
        }
    }

    /**
     * 轮播图导航点集合
     */
    private List<View> dotList = new ArrayList<>();

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_prison_open, null);
        ButterKnife.bind(this, view);
        lv_prison_open = (ListView) view.findViewById(R.id.lv_prison_open);
        mRefreshLayout = (RefreshLayout) view.findViewById(R.id.swipe_container);
//        rl_carousel = (RelativeLayout) view.findViewById(R.id.rl_carousel);
        layout_roll_view = View.inflate(getApplicationContext(), R.layout.layout_roll_view, null);
        dots_ll = (LinearLayout) layout_roll_view.findViewById(R.id.dots_ll);
        top_news_title = (TextView) layout_roll_view.findViewById(R.id.top_news_title);
        top_news_viewpager = (LinearLayout) layout_roll_view.findViewById(R.id.top_news_viewpager);
//        rl_carousel.addView(layout_roll_view);
        lv_prison_open.addHeaderView(layout_roll_view);

        footerLayout = getLayoutInflater().inflate(R.layout.listview_footer, null);
        textMore = (TextView) footerLayout.findViewById(R.id.text_more);
        progressBar = (ProgressBar) footerLayout.findViewById(R.id.load_progress_bar);

        lv_prison_open.addFooterView(footerLayout);
        mRefreshLayout.setChildView(lv_prison_open);

        mRefreshLayout.setColorSchemeResources(R.color.theme);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("狱务公开");
        setBackVisibility(View.VISIBLE);
        jail_id = (int) SPUtil.get(this, "jail_id", 0);
        getNews(0);// 获取新闻
        lv_prison_open.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    int i = newsList.get(position - 1).getId();
                    Intent intent = new Intent(PrisonOpenActivity.this, NewsDetailActivity.class);
                    intent.putExtra("type", 1);// 0是轮播图   1是新闻
                    intent.putExtra("id", i);
                    startActivity(intent);
                }
            }
        });
        mRefreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNews(1);
            }
        });
        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                getNews(2);
            }
        });
        textMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNews(2);
            }
        });
    }

    /**
     * 获取新闻
     */
    private void getNews(final int getType) {
        if (Utils.isNetworkAvailable()) {
            if (getType == 0) {// 进入页面
                showProgressDialog();
            }else if (getType == 2) {// 上拉加载
                textMore.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                isLoadingMore = true;
            }
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.URL_HEAD)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiRequest api = retrofit.create(ApiRequest.class);
            api.getNews(jail_id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<News>>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "get news failed : " + e.getMessage());
                            Toast.makeText(getApplicationContext(), "加载数据失败", Toast.LENGTH_SHORT).show();
                            if (getNews_Dialog.isShowing()) {
                                getNews_Dialog.dismiss();
                            }
                            checkUI();// 相关ui操作
                        }

                        @Override
                        public void onNext(List<News> newses) {
                            newsList.clear();
                            for (News news : newses){
                                Log.i(TAG, news.toString());
                                allnews.add(news);
                                if(news.getType_id() == 1){
                                    newsList.add(news);
                                }
                            }
                            sortNews();// 将新闻按新闻id排序
                            List<String> imgurl_list = new ArrayList<>();
                            setCarousel(imgurl_list); // 设置轮播
                            if (myAdapter == null) {
                                myAdapter = new MyAdapter();
                                lv_prison_open.setAdapter(myAdapter);
                            } else {
                                myAdapter.notifyDataSetChanged();
                            }
                            checkUI();// 相关ui操作
                            if(getType == 1){
                                showToastMsgShort("刷新成功");
                            }
                        }
                    });
        } else {
            showToastMsgShort("没有网络");
        }
    }

    /**
     * 将新闻按id排序
     */
    private void sortNews() {
        Collections.sort(newsList, new Comparator<News>() {
            @Override
            public int compare(News lhs, News rhs) {
                int heat1 = lhs.getId();
                int heat2 = rhs.getId();
                if (heat1 < heat2) {
                    return 1;
                }
                return -1;
            }
        });
    }

    /**
     * 初始化并显示进度条对话框
     */
    private void showProgressDialog() {
        getNews_Dialog = new ProgressDialog(this);
        getNews_Dialog.setMessage("正在加载...");
        getNews_Dialog.setCanceledOnTouchOutside(false);
        getNews_Dialog.setCancelable(false);
        getNews_Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        getNews_Dialog.show();
    }

    /**
     * 初始化轮播小圆点
     */
    private void initDot() {
        dotList.clear();
        dots_ll.removeAllViews();
        for (int i = 0; i < list_news_title.size(); i++) {
            View view = new View(getApplicationContext());
            if (i == 0) {
                view.setBackgroundResource(R.drawable.rb_shape_blue);
            } else {
                view.setBackgroundResource(R.drawable.rb_shape_gray);
            }
            // 指定点的大小
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.dot_radius), getResources().getDimensionPixelSize(R.dimen.dot_radius));
            // 间距
            layoutParams.setMargins(10, 0, 10, 0);
            dots_ll.addView(view, layoutParams);
            dotList.add(view);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return newsList.size();
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
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.prison_open_item, null);
                holder = new ViewHolder();
                holder.iv_home_news_icon = (ImageView) convertView.findViewById(R.id.iv_home_news_icon);
                holder.tv_home_news_title = (TextView) convertView.findViewById(R.id.tv_home_news_title);
                holder.tv_home_news_content = (TextView) convertView.findViewById(R.id.tv_home_news_content);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String t = Constants.RESOURSE_HEAD + newsList.get(position).getImage_url();
            Picasso.with(holder.iv_home_news_icon.getContext()).load(t).into(holder.iv_home_news_icon);
            holder.tv_home_news_title.setText(Html.fromHtml(newsList.get(position).getTitle()));
            holder.tv_home_news_content.setText(Html.fromHtml(newsList.get(position).getContents()));
            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView iv_home_news_icon;
        TextView tv_home_news_title;
        TextView tv_home_news_content;
        ImageView iv_home_news_go;
    }
}
