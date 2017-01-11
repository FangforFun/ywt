package com.gkzxhn.gkprison.userport.ui.main.canteen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.ApiRequest;
import com.gkzxhn.gkprison.api.okhttp.OkHttpUtils;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.base.BaseFragmentNew;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.AA;
import com.gkzxhn.gkprison.userport.bean.Order;
import com.gkzxhn.gkprison.userport.bean.Shoppinglist;
import com.gkzxhn.gkprison.userport.bean.line_items_attributes;
import com.gkzxhn.gkprison.userport.event.ClickEven1;
import com.gkzxhn.gkprison.userport.event.ClickEvent;
import com.gkzxhn.gkprison.userport.ui.main.MainUtils;
import com.gkzxhn.gkprison.userport.ui.main.canteen.adapter.AllChooseAdapter;
import com.gkzxhn.gkprison.userport.ui.main.pay.PaymentActivity;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.gkzxhn.gkprison.utils.UIUtils;
import com.google.gson.Gson;
import com.jauker.widget.BadgeView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * author:huangzhengneng
 * email:943852572@qq.com
 * date: 2016/8/2.
 * function:电子商务最底层fragment
 */
public class CanteenBaseFragment extends BaseFragmentNew implements AdapterView.OnItemClickListener{

    private static final String TAG = CanteenBaseFragment.class.getSimpleName();
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private SQLiteDatabase sqLiteDB = StringUtils.getSQLiteDB(getActivity());
    @BindView(R.id.rl_all_class) RelativeLayout rl_all_class;// 全部分类布局
    @BindView(R.id.rl_sales) RelativeLayout rl_sales;// 销量优先布局
    @BindView(R.id.bt_settlement) Button bt_settlement;// 结算按钮
    @BindView(R.id.tv_all_class) TextView tv_all_class;// 全部分类文本
    @BindView(R.id.tv_sales) TextView tv_sales;// 销量优先文本
    @BindView(R.id.sp_all_class) Spinner sp_all_class;// 全部分类spinner
    @BindView(R.id.sp_sales) Spinner sp_sales;// 销量优先spinner
    @BindView(R.id.tv_total_money) TextView tv_total_money;// 购物车总金额
    @BindView(R.id.fl_sales_choose) FrameLayout fl_sales_choose;// 销量选择
    @BindView(R.id.iv_buy_car_icon) View iv_buy_car_icon;
    @BindView(R.id.fl_buy_car) FrameLayout fl_buy_car;//购物车详情页面
    @BindView(R.id.lv_shopping_car) ListView lv_shopping_car;// 购物车物品清单
    @BindView(R.id.fl_choose) FrameLayout fl_choose;
    @BindView(R.id.lv_all_choose) ListView lv_all_choose;
    @BindView(R.id.lv_sales_choose) ListView lv_sales_choose;
    @BindView(R.id.rl_clear) RelativeLayout rl_clear;// 清空购物车

    private float total;// 总金额
    private String totalMoneyStr;// 总金额字符串
    private int cart_id = 0;// 购物车id
    private List<Shoppinglist> commodities = new ArrayList<>();// 已选商品集合
    private AllClassificationFragment allClassFragment;
    private SalesPriorityFragment salesFragment;
    private Bundle data;// 需要传到商品展示fragment中的bundle
    private BadgeView badgeView;
    private List<Integer> lcount = new ArrayList<Integer>();
    private int allcount;
    private String TradeNo;// 订单号
    private List<line_items_attributes> line_items_attributes = new ArrayList<>();
    private String times;
    private BuyCarAdapter adapter;
    private ProgressDialog getOrderInfoDialog;// 获取订单的对话框
    private List<Integer> eventlist = new ArrayList<Integer>();//用于点击事件传值
    private int click = 1;
    private int clicksalse = 1;
    private int jail_id;// 监狱id
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    int i = (Integer) msg.obj;
                    badgeView.setText(i + "");
                    break;
            }
        }
    };
    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String s = (String) msg.obj;
                    tv_total_money.setText(s);
                    break;
            }
        }
    };

    @Override
    protected void initUiAndListener(View view) {
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        initBadgeView();
        initUi();
    }

    /**
     * 初始化相关ui状态
     */
    private void initUi() {
        sp_all_class.setEnabled(true);
        sp_all_class.setFocusable(true);
        sp_sales.setEnabled(false);
        sp_sales.setFocusable(false);
        tv_all_class.setTextColor(getResources().getColor(R.color.theme));
        sp_all_class.setBackgroundResource(R.drawable.spinner_down);
        rl_all_class.requestFocus();
    }

    /**
     * 初始化购物车右上角的badge view
     */
    private void initBadgeView() {
        badgeView = new BadgeView(getActivity());
        badgeView.setTargetView(iv_buy_car_icon);
        badgeView.setTextSize(6);
        badgeView.setShadowLayer(3, 0, 0, Color.parseColor("#f10000"));
        badgeView.setBadgeGravity(Gravity.TOP | Gravity.RIGHT);
    }

    @Override
    protected int setLayoutResId() {
        return R.layout.fragment_canteen;
    }

    @OnClick({R.id.rl_clear, R.id.iv_buy_car_icon, R.id.fl_buy_car,
            R.id.fl_choose, R.id.fl_sales_choose, R.id.rl_all_class,
            R.id.rl_sales, R.id.bt_settlement})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.rl_clear:
                clearShoppingCar();
                break;
            case R.id.iv_buy_car_icon:
                int i = fl_buy_car.getVisibility();
                if (commodities.size() == 0) {
                    showToastMsgShort(getString(R.string.not_select_goods));
                } else {
                    fl_buy_car.setVisibility(i == View.VISIBLE ? View.GONE : View.VISIBLE);
                }
                break;
            case R.id.fl_buy_car:
                fl_buy_car.setVisibility(View.GONE);
                break;
            case R.id.fl_choose:
                fl_choose.setVisibility(View.GONE);
                break;
            case R.id.fl_sales_choose:
                fl_sales_choose.setVisibility(View.GONE);
                break;
            case R.id.rl_all_class:
                tv_all_class.setTextColor(getResources().getColor(R.color.theme));
                tv_sales.setTextColor(getResources().getColor(R.color.tv_bg));
                sp_all_class.setBackgroundResource(R.drawable.spinner_down);
                sp_sales.setBackgroundResource(R.drawable.spinner);
                if (click == 1) {
                    fl_choose.setVisibility(View.VISIBLE);
                    click = 2;
                } else if (click == 2) {
                    fl_choose.setVisibility(View.GONE);
                    allClassFragment = new AllClassificationFragment();
                    allClassFragment.setArguments(data);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_commodity, allClassFragment).commit();
                    click = 1;
                }
                break;
            case R.id.rl_sales:
                tv_sales.setTextColor(getResources().getColor(R.color.theme));
                tv_all_class.setTextColor(getResources().getColor(R.color.tv_bg));
                sp_sales.setBackgroundResource(R.drawable.spinner_down);
                sp_all_class.setBackgroundResource(R.drawable.spinner);
                if (clicksalse == 1) {
                    fl_sales_choose.setVisibility(View.VISIBLE);
                    salesFragment = new SalesPriorityFragment();
                    salesFragment.setArguments(data);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_commodity, salesFragment).commit();
                    clicksalse = 2;
                } else if (clicksalse == 2) {
                    fl_sales_choose.setVisibility(View.GONE);
                    clicksalse = 1;
                }
                break;
            case R.id.bt_settlement:
                if (allcount != 0) {
                    getOrderInfo();
                } else {
                    ToastUtil.showShortToast(getString(R.string.please_select_goods));
                }
                break;
        }
    }

    @Override
    protected void initData() {
        TradeNo = MainUtils.getOutTradeNo();
        jail_id = (int) SPUtil.get(getActivity(), SPKeyConstants.JAIL_ID, 0);
        times = StringUtils.formatTime(System.currentTimeMillis(), TIME_PATTERN);
        insertAndQueryFromDB();
        initDefaultPager();
        // 设置类别选择适配器
        AllChooseAdapter chooseAdapter = new AllChooseAdapter(getActivity());
        lv_sales_choose.setAdapter(chooseAdapter);
        lv_all_choose.setAdapter(chooseAdapter);
        lv_sales_choose.setOnItemClickListener(this);
        lv_all_choose.setOnItemClickListener(this);
    }

    /**
     * 初始化默认显示的商品展示页面
     */
    private void initDefaultPager() {
        data = new Bundle();
        data.putString("times", times);
        data.putInt("leibie", 0); // 将时间及类别发送至商品展示fragment
        allClassFragment = new AllClassificationFragment();
        allClassFragment.setArguments(data);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_commodity, allClassFragment).commit();
    }

    /**
     * 通过全部分类等类别却换商品展示页面
     * @param code
     */
    private void switchAllClassPager(int code) {
        allClassFragment = new AllClassificationFragment();
        data.putInt("leibie", code);
        allClassFragment.setArguments(data);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_commodity, allClassFragment).commit();
        fl_choose.setVisibility(View.GONE);
        click = 1;
    }

    /**
     * 通过销量优先等类别切换商品展示页面
     * @param code
     */
    private void switchSalesPager(int code) {
        salesFragment = new SalesPriorityFragment();
        data.putInt("leibie", code);
        salesFragment.setArguments(data);
        getActivity().getSupportFragmentManager().
                beginTransaction().replace(R.id.fl_commodity, salesFragment).commit();
        fl_sales_choose.setVisibility(View.GONE);
        clicksalse = 1;
    }

    /**
     * 插入时间记录  根据时间查询购物车id并赋值给变量
     */
    private void insertAndQueryFromDB() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                String insert_sql = "insert into Cart (time, isfinish, remittance) values ('" + times + "', 0, 0)";
                sqLiteDB.execSQL(insert_sql);
                Log.d(TAG, "times: " + times);
                String query_sql = "select id from Cart where time = '" + times + "'";
                Cursor cursor = sqLiteDB.rawQuery(query_sql, null);
                while (cursor.moveToNext()) {
                    cart_id = cursor.getInt(cursor.getColumnIndex("id"));
                    subscriber.onNext(cart_id);
                }
                cursor.close();
            }
        }).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.i(TAG, "query cart id : " + integer);
                    }
                });
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (getOrderInfoDialog != null && getOrderInfoDialog.isShowing()){
            getOrderInfoDialog.dismiss();
        }
        super.onDestroy();
    }

    public void onEvent(ClickEvent event) {
        // 从事件中获得参数值
        commodities.clear();
        lcount.clear();
        line_items_attributes.clear();
        allcount = 0;
        String sql = "select distinct line_items.Items_id,line_items.qty,line_items.id,line_items.price,line_items.title from line_items,Cart where line_items.cart_id = " + cart_id;
        Cursor cursor = sqLiteDB.rawQuery(sql, null);
        Log.d("ff", cursor.getCount() + "");
        total = 0;
        if (cursor.getCount() == 0) {
            tv_total_money.setText("0.0");
            badgeView.setVisibility(View.GONE);
        } else {
            badgeView.setVisibility(View.VISIBLE);
            while (cursor.moveToNext()) {
                Shoppinglist shoppinglist = new Shoppinglist();
                shoppinglist.setId(cursor.getInt(cursor.getColumnIndex("Items_id")));
                shoppinglist.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                shoppinglist.setQty(cursor.getInt(cursor.getColumnIndex("qty")));
                shoppinglist.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                commodities.add(shoppinglist);
            }
            adapter = new BuyCarAdapter();
            lv_shopping_car.setAdapter(adapter);
        }
        cursor.close();
        for (int i = 0; i < commodities.size(); i++) {
            String t = commodities.get(i).getPrice();
            float p = Float.parseFloat(t);
            int n = commodities.get(i).getQty();
            line_items_attributes lineitemsattributes = new line_items_attributes();
            lineitemsattributes.setItem_id(commodities.get(i).getId());
            lineitemsattributes.setQuantity(n);
            line_items_attributes.add(lineitemsattributes);
            total += p * n;
            lcount.add(n);
        }
        //  total += 2;
        for (int i = 0; i < lcount.size(); i++) {
            allcount += lcount.get(i);
        }
        Message msg1 = handler.obtainMessage();
        msg1.obj = allcount;
        msg1.what = 1;
        handler.sendMessage(msg1);
        if (allcount != 0) {
            DecimalFormat fnum = new DecimalFormat("####0.00");
            totalMoneyStr = fnum.format(total);
            Message msg = handler1.obtainMessage();
            msg.obj = totalMoneyStr;
            msg.what = 1;
            handler1.sendMessage(msg);
        } else if (allcount == 0) {
            //    total -= 2;
            DecimalFormat fnum = new DecimalFormat("####0.00");
            totalMoneyStr = fnum.format(total);
            Message msg = handler1.obtainMessage();
            msg.obj = totalMoneyStr;
            msg.what = 1;
            handler1.sendMessage(msg);
        }
    }

    /**
     * 获取订单信息
     */
    private void getOrderInfo() {
        getOrderInfoDialog = UIUtils.showProgressDialog(getActivity(), "");
        String orderBody = getOrderBody();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiRequest apiRequest = retrofit.create(ApiRequest.class);
        String token = (String) SPUtil.get(getActivity(), SPKeyConstants.ACCESS_TOKEN, "");
        apiRequest.getOrderInfo(jail_id, token, OkHttpUtils.getRequestBody(orderBody))
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<ResponseBody, Boolean>() {
                    @Override
                    public Boolean call(ResponseBody responseBody) {
                        try {
                            String result = responseBody.string();
                            Log.i(TAG, "get order info success : " + result);
                            TradeNo = MainUtils.getResultTradeNo(result);
                            String sql = "update Cart set total_money = '" + totalMoneyStr + "',count = "
                                    + allcount + ",out_trade_no ='" + TradeNo + "'   where time = '" + times + "'";
                            sqLiteDB.execSQL(sql);
                            return MainUtils.getResultCode(result) == 200;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            UIUtils.dismissProgressDialog(getOrderInfoDialog);
                        }
                        showToastMsgShort(getString(R.string.get_order_failed));
                        return false;
                    }
                })
                .subscribe(new SimpleObserver<Boolean>(){
                    @Override public void onError(Throwable e) {
                        UIUtils.dismissProgressDialog(getOrderInfoDialog);
                        showToastMsgShort(getString(R.string.get_order_failed));
                        Log.i(TAG, "get order info failed : " + e.getMessage());
                    }

                    @Override public void onNext(Boolean success) {
                        if (success) {
                            Intent intent = new Intent(getActivity(), PaymentActivity.class);
                            intent.putExtra("totalmoney", totalMoneyStr);
                            intent.putExtra("TradeNo", TradeNo);
                            intent.putExtra("times", times);
                            intent.putExtra("cart_id", cart_id);
                            intent.putExtra("bussiness", getString(R.string._2));
                            startActivity(intent);
                        }
                    }
                });
    }

    /**
     * 获取订单请求体
     * @return
     */
    private String getOrderBody() {
        int family_id = (int) SPUtil.get(getActivity(), SPKeyConstants.FAMILY_ID, 1);
        Order order = new Order();
        order.setFamily_id(family_id);
        order.setLine_items_attributes(line_items_attributes);
        order.setJail_id(jail_id);
        order.setCreated_at(times);
        Float f = Float.parseFloat(totalMoneyStr);
        order.setAmount(f);
        Gson gson = new Gson();
        AA aa = new AA();
        aa.setOrder(order);
        return gson.toJson(aa);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(parent.getId()){
            case R.id.lv_all_choose:
                switchAllClassPager(position);
                break;
            case R.id.lv_sales_choose:
                switchSalesPager(position);
                break;
        }
    }


    private class BuyCarAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return commodities.size();
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
            final BuyCarAdapter.ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.buycar_items, null);
                viewHolder = new BuyCarAdapter.ViewHolder();
                viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.price = (TextView) convertView.findViewById(R.id.tv_price);
                viewHolder.add = (RelativeLayout) convertView.findViewById(R.id.rl_buycar_add);
                viewHolder.reduce = (RelativeLayout) convertView.findViewById(R.id.rl_buycar_reduce);
                viewHolder.num = (TextView) convertView.findViewById(R.id.tv_buycar_num);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (BuyCarAdapter.ViewHolder) convertView.getTag();
            }
            viewHolder.title.setText(commodities.get(position).getTitle());
            viewHolder.num.setText(String.valueOf(commodities.get(position).getQty()));
            viewHolder.price.setText(commodities.get(position).getPrice());
            viewHolder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 购物车中条目商品的添加
                    Observable.create(new Observable.OnSubscribe<Integer>() {
                        @Override
                        public void call(Subscriber<? super Integer> subscriber) {
                            String GoodsCount = viewHolder.num.getText().toString().trim();
                            int singleGoodsCount = Integer.parseInt(GoodsCount);
                            int id = commodities.get(position).getId();
                            // 更新数据库
                            String updateCountSql = "update line_items set qty =" + (singleGoodsCount + 1) + "  where Items_id =" + id + "  and cart_id =" + cart_id;
                            sqLiteDB.execSQL(updateCountSql);
                            String price = commodities.get(position).getPrice();
                            float p = Float.parseFloat(price);
                            total += p;
                            DecimalFormat fnum = new DecimalFormat("####0.00");
                            totalMoneyStr = fnum.format(total);
                            allcount += 1;
                            // 查询数量
                            String sql1 = "select qty from line_items where Items_id = " + id +
                                    " and cart_id =" + cart_id;
                            Cursor cursor = sqLiteDB.rawQuery(sql1, null);
                            int qty = 0;
                            while (cursor.moveToNext()) {
                                qty = cursor.getInt(cursor.getColumnIndex("qty"));
                            }
                            cursor.close();
                            commodities.get(position).setQty(singleGoodsCount + 1);
                            int d = commodities.get(position).getId();
                            eventlist.add(d);
                            eventlist.add(qty);
                            subscriber.onNext(qty);
                        }
                    }).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Integer>() {
                                @Override
                                public void call(Integer code) {
                                    tv_total_money.setText(totalMoneyStr);
                                    badgeView.setText(String.valueOf(allcount));
                                    viewHolder.num.setText(String.valueOf(code));
                                    EventBus.getDefault().post(new ClickEven1(0, eventlist)); // 通知商品展示fragment修改数量
                                    eventlist.clear();
                                }
                            });
                }
            });
            viewHolder.reduce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 购物车中条目商品的减法
                    Observable.create(new Observable.OnSubscribe<Integer>() {
                        @Override
                        public void call(Subscriber<? super Integer> subscriber) {
                            String t = viewHolder.num.getText().toString();
                            int i = Integer.parseInt(t);
                            int id = commodities.get(position).getId();
                            if (i > 1){
                                // 如果条目的数量大于1个  就更新
                                String sql = "update line_items set qty=" + (i + 1) + " where Items_id =" + id + "  and cart_id=" + cart_id;
                                sqLiteDB.execSQL(sql);
                            }
                            String price = commodities.get(position).getPrice();
                            float p = Float.parseFloat(price);
                            total -= p;
                            DecimalFormat fnum = new DecimalFormat("####0.00");
                            totalMoneyStr = fnum.format(total);
                            allcount -= 1;
                            int qty = 0;
                            if (i == 1){
                                // 如果条目数量等于1再减就删除此条目了
                                String sql = "delete from line_items where Items_id =" + id + "  and cart_id =" + cart_id;
                                sqLiteDB.execSQL(sql);
                            }else {
                                // 大于1的查询数量
                                String sql1 = "select qty from line_items where Items_id = " + id + "  and cart_id = " + cart_id;
                                Cursor cursor = sqLiteDB.rawQuery(sql1, null);
                                while (cursor.moveToNext()) {
                                    qty = cursor.getInt(cursor.getColumnIndex("qty"));
                                }
                                cursor.close();
                                commodities.get(position).setQty(i + 1);
                            }
                            int d = commodities.get(position).getId();
                            eventlist.add(d);
                            eventlist.add(qty);
                            if (i == 1){
                                // 等于1的从已选商品集合中删除此条目
                                commodities.remove(position);
                            }
                            subscriber.onNext(qty);
                        }
                    }).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Integer>() {
                                @Override
                                public void call(Integer code) {
                                    tv_total_money.setText(totalMoneyStr);
                                    badgeView.setText(String.valueOf(allcount));
                                    viewHolder.num.setText(String.valueOf(code));
                                    if (commodities.size() == 0) {
                                        fl_buy_car.setVisibility(View.GONE);// 已选商品为空就隐藏购物车
                                    }
                                    EventBus.getDefault().post(new ClickEven1(0, eventlist));// 通知商品展示页面更新数量
                                    eventlist.clear();
                                    adapter.notifyDataSetChanged();
                                }
                            });
                }
            });
            return convertView;
        }

        private class ViewHolder {
            TextView title;
            TextView price;
            RelativeLayout add;
            RelativeLayout reduce;
            TextView num;
        }
    }

    /**
     * 清空购物车相关操作
     */
    private void clearShoppingCar() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                String sql = "delete from line_items where cart_id = " + cart_id;
                sqLiteDB.execSQL(sql);
                allcount = 0; // 购物车商品数量清0
                totalMoneyStr = "0.00";// 购物车商品金额清0
                // 通知商品列表fragment清空
                for (int i = 0; i < commodities.size(); i++) {
                    eventlist.add(commodities.get(i).getPosition());
                }
                commodities.clear();
                EventBus.getDefault().post(new ClickEven1(1, eventlist));
                eventlist.clear();
                subscriber.onNext(0);
            }
        }).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer code) {
                        badgeView.setText(String.valueOf(allcount));
                        tv_total_money.setText(totalMoneyStr);
                        fl_buy_car.setVisibility(View.GONE);
                    }
                });
    }
}
