package com.gkzxhn.gkprison.userport.activity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.userport.adapter.CommidtyAdapter;
import com.gkzxhn.gkprison.userport.adapter.ShoppingAdapter;
import com.gkzxhn.gkprison.userport.bean.Cart;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.utils.ListViewParamsUtils;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物记录
 */
public class ShoppingRecoderActivity extends BaseActivity {
    private TextView nothing;
    private ListView lv_shoppingrecoder;
    private ShoppingAdapter adapter;
    private List<Cart> carts = new ArrayList<Cart>();
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);




    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_shopping_recoder,null);
        lv_shoppingrecoder = (ListView)view.findViewById(R.id.lv_shopping_recode);
        nothing = (TextView)view.findViewById(R.id.tv_nothing);
        return view;
    }

   @Override
    protected void initData() {
        setTitle("购物记录");
        setBackVisibility(View.VISIBLE);
        carts.clear();
        String sql = "select * from Cart where isfinish = 1 and remittance = 0";
        Cursor cursor = db.rawQuery(sql,null);
        while (cursor.moveToNext()){
            Cart cart = new Cart();
            cart.setId(cursor.getInt(cursor.getColumnIndex("id")));
            cart.setCount(cursor.getInt(cursor.getColumnIndex("count")));
            cart.setTime(cursor.getString(cursor.getColumnIndex("time")));
            cart.setOut_trade_no(cursor.getString(cursor.getColumnIndex("out_trade_no")));
            cart.setFinish(cursor.getInt(cursor.getColumnIndex("isfinish")));
            cart.setTotal_money(cursor.getString(cursor.getColumnIndex("total_money")));
            carts.add(cart);
        }

       if (cursor.getCount() == 0){
            nothing.setVisibility(View.VISIBLE);
       }else {
           nothing.setVisibility(View.GONE);
       }

       for (int i = 0; i < carts.size();i++){
           List<Commodity> commodities = new ArrayList<Commodity>();
           int cart_id = carts.get(i).getId();
           String sql1 = "select distinct line_items.qty,Items.price,Items.title from line_items,Items,Cart where line_items.Items_id = Items.id and  Cart.isfinish = 1 and line_items.cart_id = "+cart_id;
           Cursor cursor1 = db.rawQuery(sql1, null);
           Log.d("消费记录",cursor1.getCount()+"");
           while (cursor1.moveToNext()){
               Commodity commodity = new Commodity();
               commodity.setTitle(cursor1.getString(cursor1.getColumnIndex("title")));
               commodity.setPrice(cursor1.getString(cursor1.getColumnIndex("price")));
               commodity.setQty(cursor1.getInt(cursor1.getColumnIndex("qty")));
               commodities.add(commodity);

           }
           carts.get(i).setCommodityList(commodities);
       }

       adapter = new ShoppingAdapter(this,carts);
       lv_shoppingrecoder.setAdapter(adapter);

    }

}
