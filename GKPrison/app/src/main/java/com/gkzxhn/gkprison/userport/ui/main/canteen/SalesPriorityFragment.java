package com.gkzxhn.gkprison.userport.ui.main.canteen;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.ApiRequest;
import com.gkzxhn.gkprison.api.rx.RxUtils;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.base.BaseFragmentNew;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.event.ClickEven1;
import com.gkzxhn.gkprison.userport.event.ClickEvent;
import com.gkzxhn.gkprison.userport.ui.main.MainUtils;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.UIUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class SalesPriorityFragment extends BaseFragmentNew implements AbsListView.OnScrollListener {

    private static final String TAG = SalesPriorityFragment.class.getSimpleName();
    private SQLiteDatabase db = StringUtils.getSQLiteDB(getActivity());
    private List<Commodity> commodities = new ArrayList<Commodity>();
    @BindView(R.id.lv_sales) ListView lv_sale;
    @BindView(R.id.iv_nothing) ImageView iv_nothing;//当没有商品时显示；
    private SalesAdapter adapter;
    private int cart_id = 0;
    private int qty = 0;
    private int Items_id;
    private String token;
    private int jail_id;
    private int page;
    private List<Commodity> addcommdity = new ArrayList<>();
    private List<Integer> buycommidty = new ArrayList<>();//已购买的商品
    private List<Integer> buyqty = new ArrayList<>();//已购买商品数量
    private View loadMoreView;
    private int visibleLastIndex = 0; //最后的可视索引；
    private int category_id;
    private int eventint = 0;//接收点击事件传来的数据
    private List<Integer> eventlist = new ArrayList<Integer>();//接收点击事件传来的数据

    /**
     * 添加更多商品
     * @param commodities
     */
    private void addMoreCommodities(List<Commodity> commodities) {
        for (int i = 0; i < commodities.size(); i++) {
            adapter.addItem(commodities.get(i));
        }
    }

    @Override
    protected void initUiAndListener(View view) {
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        loadMoreView = View.inflate(getActivity(), R.layout.bottom, null);
        lv_sale.addFooterView(loadMoreView);
        loadMoreView.setVisibility(View.GONE);
    }

    @Override
    protected int setLayoutResId() {
        return R.layout.fragment_sales_priority;
    }

    @Override
    protected void initData() {
        jail_id = (int) SPUtil.get(getActivity(), SPKeyConstants.JAIL_ID, 1);
        token = (String) SPUtil.get(getActivity(), SPKeyConstants.ACCESS_TOKEN, "");
        lv_sale.setOnScrollListener(this);
        getData();
    }

    private ProgressDialog getDataDialog;

    /**
     * 获取数据
     */
    private void getData() {
        getDataDialog = UIUtils.showProgressDialog(getActivity(), "");
        Bundle bundle = getArguments();
        String times = bundle.getString("times");
        category_id = bundle.getInt("leibie", 0);
        String sql = "select id from Cart where time = '" + times + "'";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            cart_id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        getCommoditiesByCategoryId(false, new SimpleObserver<ResponseBody>(){
            @Override public void onError(Throwable e) {
                Log.e(TAG, "get commodities failed: " + e.getMessage());
                UIUtils.dismissProgressDialog(getDataDialog);
                showToastMsgShort(getString(R.string.load_failed));
                iv_nothing.setVisibility(View.VISIBLE);
            }

            @Override public void onNext(ResponseBody responseBody) {
                String result = "";
                try {
                    result = responseBody.string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                parseCommoditiesResult(result);
            }
        });
    }

    /**
     * 解析商品结果订阅
     */
    private Subscription parseResultSubscription;

    /**
     * 解析结果
     * @param result
     */
    private void parseCommoditiesResult(final String result) {
        parseResultSubscription = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override public void call(Subscriber<? super Integer> subscriber) {
                commodities = MainUtils.analysisCommodityList(result);
                if (commodities.size() == 0) subscriber.onNext(-1);
                Collections.sort(commodities, new Comparator<Commodity>() {
                    @Override public int compare(Commodity lhs, Commodity rhs) {
                        int heat1 = lhs.getRanking();
                        int heat2 = rhs.getRanking();
                        if (heat1 < heat2) return 1;
                        return -1;
                    }
                });
                String sql = "select distinct qty,Items_id from line_items where cart_id = " + cart_id;
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor.getCount() > 0){
                    while (cursor.moveToNext()) {
                        Commodity commodity = new Commodity();
                        commodity.setId(cursor.getInt(cursor.getColumnIndex("Items_id")));
                        commodity.setQty(cursor.getInt(cursor.getColumnIndex("qty")));
                        buycommidty.add(commodity.getId());
                        buyqty.add(commodity.getQty());
                    }
                    for (int i = 0; i < commodities.size(); i++) {
                        for (int j = 0; j < buyqty.size(); j++) {
                            if (commodities.get(i).getId() == buycommidty.get(j)) {
                                commodities.get(i).setQty(buyqty.get(j));
                            }
                        }
                    }
                }
                int count = cursor.getCount();
                cursor.close();
                subscriber.onNext(count);
            }
        }).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Integer>() {
                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "parse failed: " + e.getMessage());
                        UIUtils.dismissProgressDialog(getDataDialog);
                        showToastMsgShort(getString(R.string.load_failed));
                        iv_nothing.setVisibility(View.VISIBLE);
                    }

                    @Override public void onNext(Integer integer) {
                        UIUtils.dismissProgressDialog(getDataDialog);
                        Log.i(TAG, "parse success : " + integer);
                        iv_nothing.setVisibility(integer == -1 ? View.VISIBLE : View.GONE);
                        adapter = new SalesAdapter();
                        lv_sale.setAdapter(adapter);
                    }
                });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            UIUtils.dismissProgressDialog(getDataDialog);
        }
    }

    /**
     * 获取商品订阅
     */
    private Subscription getCommoditiesSubscription;

    /**
     * 根据类别id获取商品列表
     * @param loadMore
     * @param subscriber
     */
    private void getCommoditiesByCategoryId(boolean loadMore, SimpleObserver<ResponseBody> subscriber) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiRequest apiRequest = retrofit.create(ApiRequest.class);
        Map<String, String> map = new HashMap<>();
        map.put("page", String.valueOf(loadMore ? page + 1 : page));// 加载下一页  第一次进来加载当前页
        if (category_id != 0) {
            map.put("category_id", String.valueOf(category_id));
        }
        map.put("access_token", token);
        map.put("jail_id", String.valueOf(jail_id));
        getCommoditiesSubscription = apiRequest.getCommodities(map).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemLastIndex = adapter.getCount() - 1;
        int lastIndex = itemLastIndex + 1;
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == lastIndex) {
            loadMoreView.setVisibility(View.VISIBLE);
            loadMore();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }


    /**
     * 加载更多商品
     */
    private void loadMore() {
        getCommoditiesByCategoryId(true, new SimpleObserver<ResponseBody>(){
            @Override public void onError(Throwable e) {
                Log.e(TAG, "load more failed: " + e.getMessage());
                loadMoreView.setVisibility(View.GONE);
                showToastMsgShort(getString(R.string.load_failed));
            }

            @Override public void onNext(ResponseBody responseBody) {
                String result = "";
                try {
                     result = responseBody.string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                page += 1;// 加载更多成功page+1  否则page不变
                parseLoadMoreResult(result);
            }
        });
    }

    private Subscription parseMoreSubscription;

    /**
     * 解析加载更多的结果
     * @param result
     */
    private void parseLoadMoreResult(final String result) {
        parseMoreSubscription = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override public void call(Subscriber<? super Integer> subscriber) {
                addcommdity = MainUtils.analysisCommodityList(result);
                for (int i = 0; i < commodities.size(); i++) {
                    for (int j = 0; j < addcommdity.size(); j++) {
                        if (commodities.get(i).getId() == addcommdity.get(j).getId()) {
                            addcommdity.remove(j);
                        }
                    }
                }
                Collections.sort(addcommdity, new Comparator<Commodity>() {
                    @Override public int compare(Commodity lhs, Commodity rhs) {
                        int heat1 = lhs.getRanking();
                        int heat2 = rhs.getRanking();
                        if (heat1 < heat2) {
                            return 1;
                        }
                        return -1;
                    }
                });
                if (addcommdity.size() > 0) {
                    String sql1 = "select distinct qty,Items_id from line_items where cart_id = " + cart_id;
                    Cursor cursor1 = db.rawQuery(sql1, null);
                    for (int i = 0; i < commodities.size(); i++) {
                        for (int j = 0; j < buyqty.size(); j++) {
                            if (commodities.get(i).getId() == buycommidty.get(j)) {
                                commodities.get(i).setQty(buyqty.get(j));
                            }
                        }
                    }
                    int count = cursor1.getCount();
                    cursor1.close();
                    subscriber.onNext(count);
                }
            }
        }).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Integer>(){
                    @Override public void onError(Throwable e) {
                        loadMoreView.setVisibility(View.GONE);
                        Log.e(TAG, "parse more result failed: " + e.getMessage());
                        showToastMsgShort(getString(R.string.load_failed));
                    }

                    @Override public void onNext(Integer integer) {
                        if (addcommdity.size() == 0){
                            showToastMsgShort(getString(R.string.last_pager));
                        }else if(addcommdity.size() > 0){
                            addMoreCommodities(addcommdity);
                        }
                        loadMoreView.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private class SalesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return commodities.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.sales_item, null);
                viewHolder = new ViewHolder();
                viewHolder.rl_reduce = (RelativeLayout) convertView.findViewById(R.id.rl_reduce);
                viewHolder.rl_add = (RelativeLayout) convertView.findViewById(R.id.rl_add);
                viewHolder.tv_num = (TextView) convertView.findViewById(R.id.tv_num);
                viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_commodity);
                viewHolder.tv_description = (TextView) convertView.findViewById(R.id.tv_description);
                viewHolder.tv_money = (TextView) convertView.findViewById(R.id.tv_money);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            int i = (Integer) msg.obj;
                            viewHolder.tv_num.setText(i + "");
                            break;
                        case 2:
                            int j = (Integer) msg.obj;
                            viewHolder.tv_num.setText(j + "");
                    }
                }
            };

            viewHolder.rl_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String t = viewHolder.tv_num.getText().toString();
                    Items_id = commodities.get(position).getId();
                    String price = commodities.get(position).getPrice();
                    String title = commodities.get(position).getTitle();
                    int i = Integer.parseInt(t);
                    int j = i + 1;
                    if (i == 0) {
                        String sql = "insert into line_items(Items_id,cart_id,qty,position,price,title) values (" + Items_id + "," + cart_id + ",1," + position + ",'" + price + "','" + title + "')";
                        db.execSQL(sql);
                        commodities.get(position).setQty(1);
                    } else {
                        String sql = "update line_items set qty = " + j + " where Items_id = " + Items_id + " and cart_id =" + cart_id;
                        db.execSQL(sql);
                        commodities.get(position).setQty(j);
                    }
                    String sql = "select qty from line_items where Items_id = " + Items_id + " and cart_id =" + cart_id;
                    Cursor cursor = db.rawQuery(sql, null);
                    if (cursor.getCount() == 0) {
                        qty = 0;
                    } else {
                        while (cursor.moveToNext()) {
                            qty = cursor.getInt(cursor.getColumnIndex("qty"));
                        }
                    }
                    cursor.close();
                    Message msg = handler.obtainMessage();
                    msg.obj = qty;
                    msg.what = 1;
                    handler.sendMessage(msg);
                    EventBus.getDefault().post(new ClickEvent());
                }
            });
            viewHolder.rl_reduce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String t = viewHolder.tv_num.getText().toString();
                    Items_id = commodities.get(position).getId();
                    int i = Integer.parseInt(t);
                    int j = i - 1;
                    if (i == 1) {
                        String sql = "delete from line_items where Items_id = " + Items_id + " and cart_id =" + cart_id;
                        db.execSQL(sql);
                        commodities.get(position).setQty(0);
                    } else if (i > 1) {
                        String sql = "update line_items set qty = " + j + " where Items_id = " + Items_id + " and cart_id=" + cart_id;
                        db.execSQL(sql);
                        commodities.get(position).setQty(j);
                    }
                    String sql = "select qty from line_items where Items_id = " + Items_id + " and cart_id =" + cart_id;
                    Cursor cursor = db.rawQuery(sql, null);
                    if (cursor.getCount() == 0) {
                        qty = 0;
                    } else {
                        while (cursor.moveToNext()) {
                            qty = cursor.getInt(cursor.getColumnIndex("qty"));
                        }
                    }
                    cursor.close();
                    Message msg = handler.obtainMessage();
                    msg.obj = qty;
                    msg.what = 2;
                    handler.sendMessage(msg);
                    String price = commodities.get(position).getPrice();
                    EventBus.getDefault().post(new ClickEvent());
                }
            });
            String t = Constants.RESOURSE_HEAD + commodities.get(position).getAvatar_url();
            Picasso.with(viewHolder.imageView.getContext()).load(t).placeholder(R.drawable.default_img).error(R.drawable.default_img).into(viewHolder.imageView);
            viewHolder.tv_num.setText(String.valueOf(commodities.get(position).getQty()));
            viewHolder.tv_title.setText(commodities.get(position).getTitle());
            // viewHolder.tv_description.setText(commodities.get(position).getDescription());
            viewHolder.tv_money.setText(commodities.get(position).getPrice());
            return convertView;
        }

        public void addItem(Commodity commodity) {
            commodities.add(commodity);
        }
    }

    private class ViewHolder {
        ImageView imageView;
        TextView tv_description;
        TextView tv_money;
        ImageView imageView_shopping;
        RelativeLayout rl_reduce;
        RelativeLayout rl_add;
        TextView tv_num;
        TextView tv_title;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        RxUtils.unSubscribe(parseMoreSubscription, getCommoditiesSubscription, parseResultSubscription);
    }

    public void onEvent(ClickEven1 even1) {
        eventint = even1.getDelete();
        eventlist = even1.getList();
        if (eventint == 0) {
            int id = eventlist.get(0);
            int qty = eventlist.get(1);
            commodities.get(id).setQty(qty);
            adapter.notifyDataSetChanged();
        } else if (eventint == 1) {
            for (int i = 0; i < eventlist.size(); i++) {
                commodities.get(eventlist.get(i)).setQty(0);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
