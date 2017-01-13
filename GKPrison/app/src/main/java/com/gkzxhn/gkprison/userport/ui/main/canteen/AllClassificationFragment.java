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
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.base.BaseFragmentNew;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.event.ClickEven1;
import com.gkzxhn.gkprison.userport.event.ClickEvent;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.UIUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.gkzxhn.gkprison.userport.ui.main.MainUtils.analysisCommodityList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllClassificationFragment extends BaseFragmentNew implements AbsListView.OnScrollListener {

    public static final String TAG = AllClassificationFragment.class.getSimpleName();
    private SQLiteDatabase db = StringUtils.getSQLiteDB(getActivity());
    @BindView(R.id.lv_allclassification) ListView lv_allclassification;
    @BindView(R.id.iv_nothing) ImageView iv_nothing;//当商品列表没有数据时加载；
    private SalesAdapter adapter;
    private List<Commodity> commodities = new ArrayList<>();
    private int cart_id;
    private int qty = 0;
    private List<Integer> buycommidty = new ArrayList<>();//已购买的商品
    private List<Integer> buyqty = new ArrayList<>();//已购买商品数量
    private String token;
    private int jail_id;
    private int page;
    private View loadmore;
    private int visibleLastIndex = 0; //最后的可视索引；
    private int Items_id = 0;
    private int category_id;

    @Override
    protected void initUiAndListener(View view) {
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        loadmore = View.inflate(getActivity(), R.layout.bottom, null);
        lv_allclassification.addFooterView(loadmore);
        loadmore.setVisibility(View.GONE);
        lv_allclassification.setOnScrollListener(this);
    }

    @Override
    protected int setLayoutResId() {
        return R.layout.fragment_all_classification;
    }

    @Override
    protected void initData() {
        jail_id = (int) SPUtil.get(getActivity(), SPKeyConstants.JAIL_ID, 1);
        token = (String) SPUtil.get(getActivity(), SPKeyConstants.ACCESS_TOKEN, "");
        loadDataByClass();// 根据类别加载数据
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemLastIndex = adapter.getCount() - 1;
        int lastIndex = itemLastIndex + 1;
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == lastIndex) {
            loadmore.setVisibility(View.VISIBLE);
            loadMoreCommodities();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    private ProgressDialog getDataDialog;

    /**
     * 根据类别加载商品数据
     */
    private void loadDataByClass() {
        getDataDialog = UIUtils.showProgressDialog(getActivity(), "");
        Observable.create(new Observable.OnSubscribe<ResponseBody>() {
            @Override
            public void call(Subscriber<? super ResponseBody> subscriber) {
                Bundle bundle = getArguments();
                String times = bundle.getString("times");
                String sql = "select id from Cart where time = '" + times + "'";
                Cursor cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    cart_id = cursor.getInt(cursor.getColumnIndex("id"));
                }
                cursor.close();
                category_id = bundle.getInt("leibie", 1);
                Log.d(TAG, "goods class: " + category_id);
                getCommoditiesByCategoryId(subscriber, false);
            }
        }).map(new Func1<ResponseBody, Integer>() {
            @Override
            public Integer call(ResponseBody response) {
                String result = null;
                try {
                    result = response.string();
                } catch (IOException e) {
                    Log.e(TAG, "failed: " + e.getMessage());
                    return -1;
                }
                commodities = analysisCommodityList(result);
                if (commodities.size() == 0) {
                    return -1;// 若商品为0则显示无商品图标
                } else {
                    // 查询数据库中商品数量
                    String sql = "select distinct qty,Items_id from line_items where cart_id = " + cart_id;
                    Cursor cursor = db.rawQuery(sql, null);
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
                    int count = cursor.getCount();
                    cursor.close();
                    return count;
                }
            }
        })
                .unsubscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Integer>(){
                    @Override public void onError(Throwable e) {
                        showToastMsgShort(getString(R.string.goods_list_update_failed));
                        UIUtils.dismissProgressDialog(getDataDialog);
                        Log.i(TAG, e.getMessage());
                    }

                    @Override public void onNext(Integer code) {
                        iv_nothing.setVisibility(code == -1 ? View.VISIBLE : View.GONE);
                        if (code == -1) return;
                        adapter = new SalesAdapter();
                        lv_allclassification.setAdapter(adapter);
                        UIUtils.dismissProgressDialog(getDataDialog);
                    }
                });
    }

    /**
     * 根据类别id获取商品列表
     * @param subscriber
     * @param loadMore 是否是加载更多操作  默认加载page+1页  加载成功page值递增
     */
    private void getCommoditiesByCategoryId(final Subscriber<? super ResponseBody> subscriber
                                                        , final boolean loadMore) {
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
        apiRequest.getCommodities(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<ResponseBody>(){
            @Override public void onError(Throwable e) {
                Log.e(TAG, "get failed: " + e.getMessage());
                subscriber.onError(e);
            }

            @Override public void onNext(ResponseBody responseBody) {
                Log.i(TAG, "get success");
                subscriber.onNext(responseBody);
                if (loadMore)
                    page += 1;// 加载更多成功page+1  否则page不变
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) UIUtils.dismissProgressDialog(getDataDialog);
    }

    /**
     * 加载更多商品
     */
    private void loadMoreCommodities() {
        Observable.create(new Observable.OnSubscribe<ResponseBody>() {
            @Override public void call(Subscriber<? super ResponseBody> subscriber) {
                getCommoditiesByCategoryId(subscriber, true);
            }
        }).map(new Func1<ResponseBody, Integer>() {
            @Override public Integer call(ResponseBody response) {
                String result = null;
                try {
                    result = response.string();
                } catch (IOException e) {
                    Log.e(TAG, "load more failed: " + e.getMessage());
                    return -1;
                }
                List<Commodity> addCommodityList = analysisCommodityList(result);
                for (int i = 0; i < commodities.size(); i++) {
                    for (int j = 0; j < addCommodityList.size(); j++) {
                        if (commodities.get(i).getId() == addCommodityList.get(j).getId()) {
                            addCommodityList.remove(j);
                        }
                    }
                }
                if (addCommodityList.size() != 0) {
                    for (int i = 0; i < addCommodityList.size(); i++) {
                        adapter.addItem(addCommodityList.get(i));
                    }
                    String sql1 = "select distinct qty,Items_id from line_items where cart_id = " + cart_id;
                    Cursor cursor = db.rawQuery(sql1, null);
                    for (int i = 0; i < commodities.size(); i++) {
                        for (int j = 0; j < buyqty.size(); j++) {
                            if (commodities.get(i).getId() == buycommidty.get(j)) {
                                commodities.get(i).setQty(buyqty.get(j));
                            }
                        }
                    }
                    int count = cursor.getCount();
                    cursor.close();
                    return count;
                } else {
                    return -1;
                }
            }
        }).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Integer>(){
                    @Override public void onError(Throwable e) {
                        Log.i(TAG, "load more commodity failed: " + e.getMessage());
                        showToastMsgShort(getString(R.string.load_failed));
                    }

                    @Override public void onNext(Integer response) {
                        loadmore.setVisibility(View.GONE);
                        if (response == -1){
                            showToastMsgShort(getString(R.string.last_pager));
                        }else if(response == 0){
                            adapter.notifyDataSetChanged();
                        }
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
                        Log.d("fd", cart_id + "");
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
                        String sql = "delete from line_items where Items_id = " + Items_id + " and cart_id = " + cart_id;
                        db.execSQL(sql);
                        commodities.get(position).setQty(0);
                    } else if (i > 1) {
                        String sql = "update line_items set qty = " + j + " where Items_id = " + Items_id + "  and cart_id =" + cart_id;
                        db.execSQL(sql);
                        commodities.get(position).setQty(j);
                    }
                    String sql = "select qty from line_items where Items_id = " + Items_id + "  and cart_id = " + cart_id;
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
            viewHolder.tv_num.setText(commodities.get(position).getQty() + "");
            viewHolder.tv_title.setText(commodities.get(position).getTitle());
            viewHolder.tv_description.setText(commodities.get(position).getDescription());
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
        TextView tv_title;
        TextView tv_money;
        RelativeLayout rl_reduce;
        RelativeLayout rl_add;
        TextView tv_num;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(ClickEven1 even1) {
        int eventint = even1.getDelete();//接收点击事件传来的数据
        List<Integer> eventlist = even1.getList();//接收点击事件传来的数据
        if (eventint == 0) {
            int id = eventlist.get(0);
            int qty = eventlist.get(1);
            for (int i = 0; i < commodities.size(); i++) {
                if (commodities.get(i).getId() == id) {
                    commodities.get(i).setQty(qty);
                }
            }
            adapter.notifyDataSetChanged();
        } else if (eventint == 1) {
            for (int i = 0; i < commodities.size(); i++) {
                commodities.get(i).setQty(0);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
