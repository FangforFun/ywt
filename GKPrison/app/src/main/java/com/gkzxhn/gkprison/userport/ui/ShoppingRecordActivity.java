package com.gkzxhn.gkprison.userport.ui;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.rx.RxUtils;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.base.BaseActivityNew;
import com.gkzxhn.gkprison.userport.adapter.ShoppingAdapter;
import com.gkzxhn.gkprison.userport.bean.Cart;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.gkzxhn.gkprison.utils.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 购物记录
 */
public class ShoppingRecordActivity extends BaseActivityNew {

    private static final java.lang.String TAG = "ShoppingRecordActivity";
    @BindView(R.id.iv_nothing) ImageView iv_nothing;
    @BindView(R.id.lv_shopping_recode) ListView lv_shoppingrecoder;
    @BindView(R.id.tv_title) TextView tv_title;
    @BindView(R.id.rl_back) RelativeLayout rl_back;
    private ShoppingAdapter adapter;
    private List<Cart> carts = new ArrayList<>();
    private SQLiteDatabase db = StringUtils.getSQLiteDB(this);
    private ProgressDialog loading_dialog;
    private Subscription querySubscription;

    @Override
    public int setLayoutResId() {
        return R.layout.activity_shopping_recoder;
    }

    @Override
    protected void initUiAndListener() {
        ButterKnife.bind(this);
        tv_title.setText(getString(R.string.shopping_record));
        rl_back.setVisibility(View.VISIBLE);
        getShoppingRecord();
    }

    @Override
    protected boolean isApplyStatusBarColor() {
        return true;
    }

    @Override
    protected boolean isApplyTranslucentStatus() {
        return true;
    }

    @OnClick(R.id.rl_back)
    public void onClick(){
        finish();
    }

    @Override
    protected void onDestroy() {
        UIUtils.dismissProgressDialog(loading_dialog);
        RxUtils.unSubscribe(querySubscription);
        super.onDestroy();
    }

    /**
     * 获取购物记录
     */
    private void getShoppingRecord() {
        loading_dialog = UIUtils.showProgressDialog(this);
        querySubscription = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                carts.clear();
                String sql = "select * from Cart where isfinish = 1 and remittance = 0";
                Cursor cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    Cart cart = new Cart();
                    cart.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    cart.setCount(cursor.getInt(cursor.getColumnIndex("count")));
                    cart.setTime(cursor.getString(cursor.getColumnIndex("time")));
                    cart.setOut_trade_no(cursor.getString(cursor.getColumnIndex("out_trade_no")));
                    cart.setFinish(cursor.getInt(cursor.getColumnIndex("isfinish")));
                    cart.setTotal_money(cursor.getString(cursor.getColumnIndex("total_money")));
                    carts.add(cart);
                }
                cursor.close();
                sort();// 排序
                for (int i = 0; i < carts.size(); i++) {
                    List<Commodity> commodities = new ArrayList<>();
                    int cart_id = carts.get(i).getId();
                    String sql1 = "select distinct line_items.qty,line_items.price,line_items.title from line_items,Cart where Cart.isfinish = 1 and line_items.cart_id = " + cart_id;
                    Cursor cursor1 = db.rawQuery(sql1, null);
                    while (cursor1.moveToNext()) {
                        Commodity commodity = new Commodity();
                        commodity.setTitle(cursor1.getString(cursor1.getColumnIndex("title")));
                        commodity.setPrice(cursor1.getString(cursor1.getColumnIndex("price")));
                        commodity.setQty(cursor1.getInt(cursor1.getColumnIndex("qty")));
                        commodities.add(commodity);
                    }
                    carts.get(i).setCommodityList(commodities);
                    cursor1.close();
                }
            }
        }).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Object>(){
                    @Override public void onError(Throwable e) {
                        UIUtils.dismissProgressDialog(loading_dialog);
                        Log.e(TAG, "query record failed: " + e.getMessage());
                        ToastUtil.showShortToast(getString(R.string.query_failed));
                    }

                    @Override public void onNext(Object o) {
                        UIUtils.dismissProgressDialog(loading_dialog);
                        showUI();
                    }
                });
    }

    /**
     * 显示ui
     */
    private void showUI() {
        if(carts.size() == 0){
            iv_nothing.setVisibility(View.VISIBLE);
        }else {
            iv_nothing.setVisibility(View.GONE);
            if(adapter == null){
                adapter = new ShoppingAdapter(ShoppingRecordActivity.this, carts);
                lv_shoppingrecoder.setAdapter(adapter);
            }else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 排序
     */
    private void sort() {
        Collections.sort(carts, new Comparator<Cart>() {
            @Override
            public int compare(Cart lhs, Cart rhs) {
                int heat1 = lhs.getId();
                int heat2 = rhs.getId();
                if (heat1 < heat2) {
                    return 1;
                }
                return -1;
            }
        });
    }
}
