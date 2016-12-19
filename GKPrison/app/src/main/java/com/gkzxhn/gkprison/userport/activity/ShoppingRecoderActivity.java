package com.gkzxhn.gkprison.userport.activity;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.userport.adapter.ShoppingAdapter;
import com.gkzxhn.gkprison.userport.bean.Cart;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.keda.sky.app.PcAppStackManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 购物记录
 */
public class ShoppingRecoderActivity extends BaseActivity {

    private ImageView iv_nothing;
    private ListView lv_shoppingrecoder;
    private ShoppingAdapter adapter;
    private List<Cart> carts = new ArrayList<>();
    private SQLiteDatabase db = StringUtils.getSQLiteDB(this);
    private ProgressDialog loading_dialog;

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(getApplicationContext(), R.layout.activity_shopping_recoder, null);
        lv_shoppingrecoder = (ListView) view.findViewById(R.id.lv_shopping_recode);
        iv_nothing = (ImageView) view.findViewById(R.id.iv_nothing);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("购物记录");
        setBackVisibility(View.VISIBLE);
        getShoppingRecoder();
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }


    /**
     * 获取购物记录
     */
    private void getShoppingRecoder() {
        initAndShowDialog();
        getData();
    }

    private void getData() {
        new Thread(){
            @Override
            public void run() {
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
                showUI();
            }
        }.start();
    }

    private void showUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(loading_dialog.isShowing())
                    loading_dialog.dismiss();
                if(carts.size() == 0){
                    iv_nothing.setVisibility(View.VISIBLE);
                }else {
                    iv_nothing.setVisibility(View.GONE);
                    if(adapter == null){
                        adapter = new ShoppingAdapter(ShoppingRecoderActivity.this, carts);
                        lv_shoppingrecoder.setAdapter(adapter);
                    }else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
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

    /**
     * 初始化并且显示加载对话框
     */
    private void initAndShowDialog() {
        loading_dialog = new ProgressDialog(this);
        loading_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading_dialog.setMessage("加载中...");
        loading_dialog.show();
    }
}
