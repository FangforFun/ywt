package com.gkzxhn.gkprison.userport.fragment;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.gkzxhn.gkprison.userport.event.ClickEvent;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllClassificationFragment extends BaseFragment {
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
    private ListView lv_allclass;
    private SalesAdapter adapter;
    private List<Commodity> commodities = new ArrayList<Commodity>();
    private float count = 0;
    private int cart_id = 0;
    private String tv_count = "0.0";
    private List<Integer> image = new ArrayList<Integer>(){
        {
            add(R.drawable.beizi1);
            add(R.drawable.beizi2);
            add(R.drawable.beizi3);
            add(R.drawable.beizi4);
        }
    };
    @Override
    protected View initView() {
        view = View.inflate(context,R.layout.fragment_all_classification,null);
        lv_allclass = (ListView)view.findViewById(R.id.lv_allclassification);
        return view;
    }



    @Override
    protected void initData() {
        String sql2 = "delete from cart where 1=1";
        db.execSQL(sql2);
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String times = format.format(date);
        String sql = "insert into Cart (time) values ('"+times+"')";
        db.execSQL(sql);
        String sql1 = "select id from Cart where time = '"+times+"'";
        Cursor cursor1 = db.rawQuery(sql1, null);
        while (cursor1.moveToNext()){
            cart_id = cursor1.getInt(cursor1.getColumnIndex("id"));
        }
        Cursor cursor = db.query("Items",null,null,null,null,null,null);
        while (cursor.moveToNext()){
            Commodity commodity = new Commodity();
            commodity.setId(cursor.getInt(cursor.getColumnIndex("id")));
            commodity.setPrice(cursor.getString(cursor.getColumnIndex("price")));
            commodity.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            commodity.setCategory_id(cursor.getInt(cursor.getColumnIndex("category_id")));
            commodity.setAvatar_url(cursor.getString(cursor.getColumnIndex("avatar_url")));
            commodities.add(commodity);
        }
        adapter = new SalesAdapter();
        lv_allclass.setAdapter(adapter);
    }

    private class SalesAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return image.size();
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
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.sales_item, null);
                viewHolder = new ViewHolder();
                viewHolder.rl_reduce = (RelativeLayout)convertView.findViewById(R.id.rl_reduce);
                viewHolder.rl_add = (RelativeLayout)convertView.findViewById(R.id.rl_add);
                viewHolder.tv_num = (TextView)convertView.findViewById(R.id.tv_num);
                viewHolder.imageView_shopping = (ImageView) convertView.findViewById(R.id.image_shopping);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_commodity);
                viewHolder.tv_description = (TextView) convertView.findViewById(R.id.tv_description);
                viewHolder.tv_money = (TextView) convertView.findViewById(R.id.tv_money);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
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
                    String p = commodities.get(position).getPrice();
                    int n = commodities.get(position).getId();
                    int i = Integer.parseInt(t);
                    int j = i + 1;
                    if ( i == 0){
                        String sql = "insert into line_items(num,cart_id,qty) values ("+ n +"," + cart_id +",1)";
                        db.execSQL(sql);
                    }else {
                        String sql = "update line_items set qty = "+ j +" where num = " +n+"";
                        db.execSQL(sql);
                    }

                Message msg = handler.obtainMessage();
                    msg.obj = j;
                    msg.what = 1;
                    handler.sendMessage(msg);
                    EventBus.getDefault().post(new ClickEvent());
                }
            });
            viewHolder.rl_reduce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String t = viewHolder.tv_num.getText().toString();
                    int n = commodities.get(position).getId();
                    int i = Integer.parseInt(t);
                    int j = i - 1;
                    if (i == 1) {
                        String sql = "delete from line_items where num = " + n + "";
                        db.execSQL(sql);
                    } else if (i > 1) {
                        String sql = "update line_items set qty = " + j + " where num = " + n + "";
                        db.execSQL(sql);
                    }
                    if (i > 0) {
                        i -= 1;
                        Message msg = handler.obtainMessage();
                        msg.obj = i;
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                    EventBus.getDefault().post(new ClickEvent());
                }
            });
            viewHolder.imageView.setImageResource(image.get(position));

            viewHolder.tv_description.setText(commodities.get(position).getDescription());
            viewHolder.tv_money.setText(commodities.get(position).getPrice());
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                String t = viewHolder.tv_money.getText().toString();

                @Override
                public void onClick(View v) {
                    if (viewHolder.imageView_shopping.getDrawable() == null) {
                        viewHolder.imageView_shopping.setImageResource(R.drawable.shopping_check);
                        float f = Float.parseFloat(t);
                        count += f;


                    } else {
                        viewHolder.imageView_shopping.setImageResource(0);
                        float f = Float.parseFloat(t);
                        count -= f;

                    }
                }
            });
            DecimalFormat fnum = new DecimalFormat("####0.0");
            tv_count = fnum.format(count);


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
