package com.gkzxhn.gkprison.userport.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.userport.bean.Cart;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.utils.ListViewParamsUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物记录
 */
public class ShoppingRecoderActivity extends BaseActivity {
    private ListView lv_shoppingrecoder;
    private ShoppingAdapter adapter;
    private List<Cart> carts = new ArrayList<Cart>();

    private List<List<Commodity>> inner = new ArrayList<>();
    private List<String> money_count = new ArrayList<String>(){
        {
            add("¥ 59.80");
            add("¥ 59.80");
        }
    };
    private List<String> recodertime = new ArrayList<String>(){
        {
            add("2015年5月4日");
            add("2015年6月3日");
        }
    };
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_shopping_recoder,null);
        lv_shoppingrecoder = (ListView)view.findViewById(R.id.lv_shopping_recode);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("购物记录");
        setBackVisibility(View.VISIBLE);
        String sql = "select * from Cart where finish = 1";
        Cursor cursor = db.rawQuery(sql,null);
        while (cursor.moveToNext()){
            Cart cart = new Cart();
            cart.setTime(cursor.getString(cursor.getColumnIndex("time")));
            cart.setOut_trade_no(cursor.getString(cursor.getColumnIndex("out_trade_no")));
            cart.setFinish(cursor.getInt(cursor.getColumnIndex("finish")));
            cart.setTotal_money(cursor.getString(cursor.getColumnIndex("total_money")));
            carts.add(cart);
        }

       for (int i = 0; i < carts.size();i++){
           List<Commodity> commodities = new ArrayList<Commodity>();
           int cart_id = carts.get(i).getId();
           String sql1 = "select line_items.id,line_items.qty,Items.price,Cart.time,Items.description from line_items,Items,Cart where line_items.Items_id = Items.id and  Cart.finish = 1 and line_items.cart_id = "+cart_id+"  ";
           Cursor cursor1 = db.rawQuery(sql1, null);
           while (cursor1.moveToNext()){
               Commodity commodity = new Commodity();
               commodity.setDescription(cursor1.getString(cursor1.getColumnIndex("description")));
               commodity.setPrice(cursor1.getString(cursor1.getColumnIndex("price")));
               commodity.setQty(cursor1.getInt(cursor1.getColumnIndex("qty")));
               commodities.add(commodity);
           }
           inner.add(commodities);
       }
        adapter = new ShoppingAdapter();
        lv_shoppingrecoder.setAdapter(adapter);
    }
    private class ShoppingAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return carts.size();
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
          ViewHoler viewHoler;
            if (convertView == null){
                convertView = View.inflate(getApplicationContext(),R.layout.shoppingrecode_item,null);
                viewHoler = new ViewHoler();
                viewHoler.tv_paytime = (TextView)convertView.findViewById(R.id.tv_paytime);
                viewHoler.tvshopping_money = (TextView)convertView.findViewById(R.id.tvshopping_money);
                viewHoler.lv_recode = (ListView)convertView.findViewById(R.id.lv_recode);
                viewHoler.tv_alipay_trading_num = (TextView) convertView.findViewById(R.id.tv_alipay_trading_num);
                viewHoler.tv_transact_state = (TextView) convertView.findViewById(R.id.tv_transact_state);
                convertView.setTag(viewHoler);
            }else {
                viewHoler = (ViewHoler)convertView.getTag();
            }
            viewHoler.tv_paytime.setText(carts.get(position).getTime());
            viewHoler.tv_alipay_trading_num.setText(carts.get(position).getOut_trade_no());
            viewHoler.tvshopping_money.setText(carts.get(position).getTotal_money());
            setListViewHeightBasedOnChildren(viewHoler.lv_recode);
            CommidtyAapter adpter = new CommidtyAapter(getApplicationContext(),inner.get(position));
           viewHoler.lv_recode.setAdapter(adpter);
            return convertView;
        }
        private class ViewHoler{
            TextView tv_alipay_trading_num;
            TextView tvshopping_money;
            TextView tv_paytime;
            TextView tv_transact_state;
            ListView lv_recode;
        }

        private class CommidtyAapter extends BaseAdapter{
            private Context context;
            private List<Commodity> list = new ArrayList<Commodity>();

            public CommidtyAapter(Context context, List<Commodity> list) {
                this.context = context;
                this.list = list;
            }

            @Override
            public int getCount() {
                return list.size();
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
                ViewHolder1 holder1;
                if (convertView == null){
                    convertView = View.inflate(getApplicationContext(),R.layout.recoding_items,null);
                    holder1 = new ViewHolder1();
                    holder1.description = (TextView)convertView.findViewById(R.id.tv_shopping_desciption);
                    holder1.price = (TextView)convertView.findViewById(R.id.tv_shopping_mongey);
                    holder1.qty = (TextView)convertView.findViewById(R.id.tv_shopping_qty);
                    convertView.setTag(holder1);
                }else {
                    holder1 = (ViewHolder1)convertView.getTag();
                }
                holder1.description.setText(list.get(position).getDescription());
                holder1.price.setText(list.get(position).getPrice());
                holder1.qty.setText(list.get(position).getQty());
                return convertView;
            }
        }
        private class ViewHolder1{
            TextView description;
            TextView price;
            TextView qty;
        }
    }
    private float calculation(String price,int qty){
        Float f = Float.parseFloat(price);
        float count = f * qty;
        return count;
    }


    public void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {        return;    }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
