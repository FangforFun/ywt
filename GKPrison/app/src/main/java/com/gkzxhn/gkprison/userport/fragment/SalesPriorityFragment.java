package com.gkzxhn.gkprison.userport.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.bean.Shoppinglist;
import com.gkzxhn.gkprison.userport.event.ClickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class SalesPriorityFragment extends BaseFragment {
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
    private List<Commodity> commodities = new ArrayList<Commodity>();
    private ListView lv_sale;
    private SalesAdapter adapter;
    private int cart_id = 0;
    private int qty = 0;
    private int Items_id;
    private List<Shoppinglist> shoppinglists = new ArrayList<Shoppinglist>();
    private List<Integer> image = new ArrayList<Integer>(){
        {
            add(R.drawable.beizi1);
            add(R.drawable.beizi2);
            add(R.drawable.beizi3);
            add(R.drawable.beizi4);
        }
    };
    private List<String> description = new ArrayList<String>(){
        {
            add("天禧玻璃杯带过滤便携男女士茶杯创意可爱运动情侣透明水杯杯子");
            add("潮牌易拉罐学生保温不锈钢吸管水杯大肚杯创意随行随手情侣杯子");
            add("天禧玻璃杯带过滤便携男女士茶杯创意可爱运动情侣透明水杯杯子");
            add("天禧玻璃杯带过滤便携男女士茶杯创意可爱运动情侣透明水杯杯子");
        }
    };

    private List<String> money = new ArrayList<String>(){
        {
            add("29.8");
            add("41.3");
            add("38.9");
            add("24.1");
        }
    };

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_sales_priority,null);
        lv_sale = (ListView)view.findViewById(R.id.lv_sales);
        return view;
    }

    @Override
    protected void initData() {
        Bundle bundle = getArguments();
        String times = bundle.getString("times");
        String sql1 = "select id from Cart where time = '"+times+"'";
        Cursor cursor1 = db.rawQuery(sql1, null);
        while (cursor1.moveToNext()){
            cart_id = cursor1.getInt(cursor1.getColumnIndex("id"));
        }
        Cursor cursor = db.query("Items",null,null,null,null,null,null);
        while (cursor.moveToNext()) {
            if (commodities.size() < cursor.getCount()) {
                Commodity commodity = new Commodity();
                commodity.setRanking(cursor.getInt(cursor.getColumnIndex("sales_heat")));
                commodity.setId(cursor.getInt(cursor.getColumnIndex("id")));
                commodity.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                commodity.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                commodity.setCategory_id(cursor.getInt(cursor.getColumnIndex("category_id")));
                commodity.setAvatar_url(cursor.getString(cursor.getColumnIndex("avatar_url")));
                String sql = "select line_items.qty from line_items where line_items.Items_id = "+commodity.getId()+" and line_items.cart_id = "+cart_id;
                Cursor cursor2 = db.rawQuery(sql,null);
                if (cursor2.getCount() != 0){
                    while (cursor2.moveToNext()) {
                        commodity.setQty(cursor2.getInt(cursor2.getColumnIndex("qty")));
                    }
                }else {
                    commodity.setQty(0);
                }
                commodities.add(commodity);
            }
        }
        Collections.sort(commodities, new Comparator<Commodity>() {
            @Override
            public int compare(Commodity lhs, Commodity rhs) {
                int heat1 = lhs.getRanking();
                int heat2 = rhs.getRanking();
                if (heat1 > heat2){
                    return 1;
                }
                return -1;
            }
        });
        adapter = new SalesAdapter();
        lv_sale.setAdapter(adapter);
    }

    private class SalesAdapter extends BaseAdapter {

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
            final ViewHolder viewHolder ;
            if (convertView == null){
                convertView = View.inflate(getActivity(),R.layout.sales_item,null);
                viewHolder = new ViewHolder();
                viewHolder.rl_reduce = (RelativeLayout)convertView.findViewById(R.id.rl_reduce);
                viewHolder.rl_add = (RelativeLayout)convertView.findViewById(R.id.rl_add);
                viewHolder.tv_num = (TextView)convertView.findViewById(R.id.tv_num);
                viewHolder.imageView_shopping = (ImageView) convertView.findViewById(R.id.image_shopping);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_commodity);
                viewHolder.tv_description = (TextView) convertView.findViewById(R.id.tv_description);
                viewHolder.tv_money = (TextView) convertView.findViewById(R.id.tv_money);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            final Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what){
                        case 1:
                            int i = (Integer)msg.obj;
                            viewHolder.tv_num.setText(i + "");
                            break;
                        case 2:
                            int j = (Integer)msg.obj;
                            viewHolder.tv_num.setText(j+"");
                    }
                }
            };

            viewHolder.rl_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String t = viewHolder.tv_num.getText().toString();
                    Items_id = commodities.get(position).getId();
                    int i = Integer.parseInt(t);
                    int j = i + 1;
                    if ( i == 0){
                        String sql = "insert into line_items(Items_id,cart_id,qty) values ("+ Items_id  +"," + cart_id +",1)";
                        db.execSQL(sql);
                    }else {
                        String sql = "update line_items set qty = "+ j +" where Items_id = " +Items_id +" and cart_id ="+cart_id;
                        db.execSQL(sql);
                    }
                    String sql = "select qty from line_items where Items_id = "+Items_id+" and cart_id ="+cart_id;
                    Cursor cursor = db.rawQuery(sql,null);
                    if (cursor.getCount() == 0){
                        qty = 0;
                    }else {
                        while (cursor.moveToNext()){
                            qty = cursor.getInt(cursor.getColumnIndex("qty"));
                        }
                    }

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
                        String sql = "delete from line_items where Items_id = " + Items_id + " and cart_id ="+cart_id;
                        db.execSQL(sql);
                    } else if (i > 1) {
                        String sql = "update line_items set qty = " + j + " where Items_id = " + Items_id + " and cart_id="+cart_id;
                        db.execSQL(sql);
                    }
                    String sql = "select qty from line_items where Items_id = " + Items_id+" and cart_id ="+cart_id;
                    Cursor cursor = db.rawQuery(sql, null);
                    if (cursor.getCount() == 0) {
                        qty = 0;
                    } else {
                        while (cursor.moveToNext()) {
                            qty = cursor.getInt(cursor.getColumnIndex("qty"));
                        }
                    }
                    Message msg = handler.obtainMessage();
                    msg.obj = qty;
                    msg.what = 2;
                    handler.sendMessage(msg);

                    EventBus.getDefault().post(new ClickEvent());
                }
            });
            viewHolder.imageView.setImageResource(image.get(position));
            viewHolder.tv_num.setText(commodities.get(position).getQty()+"");
            viewHolder.tv_description.setText(commodities.get(position).getDescription());
            viewHolder.tv_money.setText(commodities.get(position).getPrice());

            return convertView;
        }
    }
    private class ViewHolder{
        ImageView imageView;
        TextView  tv_description;
        TextView  tv_money;
        ImageView imageView_shopping;
        RelativeLayout rl_reduce;
        RelativeLayout rl_add;
        TextView tv_num;
    }


}
