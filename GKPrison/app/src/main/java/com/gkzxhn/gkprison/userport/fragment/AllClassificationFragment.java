package com.gkzxhn.gkprison.userport.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.event.ClickEven1;
import com.gkzxhn.gkprison.userport.event.ClickEvent;

import com.gkzxhn.gkprison.userport.view.PullToRefreshListView;
import com.gkzxhn.gkprison.userport.view.PullToRefreshListView.OnRefreshListener;
import com.gkzxhn.gkprison.utils.Utils;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    private int qty = 0;
    private RelativeLayout xiala;
    private int Items_id = 0;
    private int category_id;
    private SharedPreferences sp;
    private String url = Constants.URL_HEAD + "items?jail_id=1&access_token=";
    private int eventint = 0;//接收点击事件传来的数据
    private List<Integer> eventlist = new ArrayList<Integer>();//接收点击事件传来的数据
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String m = (String)msg.obj;
                    if (m.equals("success")){
                        Bundle bundle = msg.getData();
                        String commodity = bundle.getString("result");
                        commodities = analysiscommodity(commodity);
                        if (commodities.size() != 0){
                            String sql = "delete from Items where 1=1";
                            db.execSQL(sql);
                            for (int i = 0;i < commodities.size();i++){
                                String sql1 = "insert into Items (id,title,description,price,avatar_url,category_id,ranking) values ("+commodities.get(i).getId()+",'"+commodities.get(i).getTitle()+"','"+commodities.get(i).getDescription()+"','"+ commodities.get(i).getPrice()+"','"+ commodities.get(i).getAvatar_url()+"',"+commodities.get(i).getCategory_id() + ","+commodities.get(i).getRanking() + ")";
                                db.execSQL(sql1);
                            }
                        }
                    }else if (m.equals("error")){
                        Toast.makeText(context, "同步数据失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    @Override
    protected View initView() {

        view = View.inflate(context,R.layout.fragment_all_classification,null);
        lv_allclass = (PullToRefreshListView)view.findViewById(R.id.lv_allclassification);
        return view;
    }

    @Override
    protected void initData() {
        ((PullToRefreshListView)lv_allclass).setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetDateTask().execute();
            }
        });
        EventBus.getDefault().register(this);
        getDate();
        adapter = new SalesAdapter();
        lv_allclass.setAdapter(adapter);

    }
    private class GetDateTask extends AsyncTask<Void,Void,List<Commodity>>{

        @Override
        protected List<Commodity> doInBackground(Void... params) {
            sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
            Message msg = handler.obtainMessage();
            HttpClient httpClient = new DefaultHttpClient();
            String token = sp.getString("token", "");
            HttpGet httpGet = new HttpGet(url + token);
            try {
                HttpResponse response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String result = EntityUtils.toString(response.getEntity(), "utf-8");
                    msg.obj = "success";
                    Bundle bundle = new Bundle();
                    bundle.putString("result", result);
                    msg.setData(bundle);
                    msg.what = 1;
                    handler.sendMessage(msg);
                } else {
                    msg.obj = "error";
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
                msg.obj = "error";
                msg.what = 1;
                handler.sendMessage(msg);
            }
            return commodities;
        }

        @Override
        protected void onPostExecute(List<Commodity> commodities) {
            ((PullToRefreshListView)lv_allclass).onRefreshComplete();
            super.onPostExecute(commodities);

        }
    }




    private void getDate(){
        Bundle bundle = getArguments();
        String times = bundle.getString("times");
        category_id = bundle.getInt("leibie", 0);
        String sql1 = "select id from Cart where time = '"+times+"'";
        Cursor cursor = null;
        Cursor cursor1 = db.rawQuery(sql1, null);
        while (cursor1.moveToNext()){
            cart_id = cursor1.getInt(cursor1.getColumnIndex("id"));
        }
       if (category_id == 1){
            String sql = "select * from Items where category_id = 1";
            cursor = db.rawQuery(sql,null);
        }else if (category_id == 2){
            String sql = "select * from Items where category_id = 2";
            cursor = db.rawQuery(sql,null);
        }else if (category_id == 3){
            String sql = "select * from Items where category_id = 3";
            cursor = db.rawQuery(sql,null);
        }
        while (cursor.moveToNext()) {
            if (commodities.size() < cursor.getCount()) {
                Commodity commodity = new Commodity();
                commodity.setId(cursor.getInt(cursor.getColumnIndex("id")));
                commodity.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                commodity.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                commodity.setCategory_id(cursor.getInt(cursor.getColumnIndex("category_id")));
                commodity.setAvatar_url(cursor.getString(cursor.getColumnIndex("avatar_url")));
                commodity.setTitle(cursor.getString(cursor.getColumnIndex("title")));
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
    }

    /**
     *  解析商品列表
     * @param s
     * @return
     */
    private List<Commodity> analysiscommodity(String s){
        List<Commodity> commodities = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(s);
            for (int i = 0;i < jsonArray.length();i++){
                Commodity commodity = new Commodity();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                commodity.setId(jsonObject.getInt("id"));
                commodity.setTitle(jsonObject.getString("title"));
                commodity.setDescription(jsonObject.getString("description"));
                commodity.setAvatar_url(jsonObject.getString("avatar_url"));
                commodity.setPrice(jsonObject.getString("price"));
                commodity.setCategory_id(jsonObject.getInt("category_id"));
                commodities.add(commodity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return commodities;
    }



private class SalesAdapter extends BaseAdapter{

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
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.sales_item, null);
                viewHolder = new ViewHolder();
                viewHolder.rl_reduce = (RelativeLayout)convertView.findViewById(R.id.rl_reduce);
                viewHolder.rl_add = (RelativeLayout)convertView.findViewById(R.id.rl_add);
                viewHolder.tv_num = (TextView)convertView.findViewById(R.id.tv_num);
                viewHolder.tv_title = (TextView)convertView.findViewById(R.id.tv_title);
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
                    Items_id = commodities.get(position).getId();
                    int i = Integer.parseInt(t);
                    int j = i + 1;
                    if ( i == 0){
                        String sql = "insert into line_items(Items_id,cart_id,qty,position) values ("+ Items_id +"," + cart_id +",1,"+position+")";
                        db.execSQL(sql);
                        commodities.get(position).setQty(1);
                    }else {
                        String sql = "update line_items set qty = "+ j +" where Items_id = " +Items_id+" and cart_id ="+cart_id;
                        db.execSQL(sql);
                        commodities.get(position).setQty(j);
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
                    Message msg = handler.obtainMessage();
                    msg.obj = qty;
                    msg.what = 2;
                    handler.sendMessage(msg);
                    EventBus.getDefault().post(new ClickEvent());

                }
            });
            String t = Constants.RESOURSE_HEAD+commodities.get(position).getAvatar_url();
            Picasso.with(viewHolder.imageView.getContext()).load(t).placeholder(R.drawable.default_img).error(R.drawable.default_img).into(viewHolder.imageView);

            viewHolder.tv_num.setText(commodities.get(position).getQty() + "");
            viewHolder.tv_title.setText(commodities.get(position).getTitle());
            viewHolder.tv_description.setText(commodities.get(position).getDescription());
            viewHolder.tv_money.setText(commodities.get(position).getPrice());
            DecimalFormat fnum = new DecimalFormat("####0.0");
            tv_count = fnum.format(count);
            return convertView;
        }
    }
    private class ViewHolder{
        ImageView imageView;
        TextView  tv_description;
        TextView tv_title;
        TextView  tv_money;
        RelativeLayout rl_reduce;
        RelativeLayout rl_add;
        TextView tv_num;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(ClickEven1 even1){

        eventint = even1.getDelete();
        eventlist = even1.getList();
        if (eventint == 0) {
            int id = eventlist.get(0);
            int qty = eventlist.get(1);
            commodities.get(id).setQty(qty);
            adapter.notifyDataSetChanged();
        }else if (eventint == 1){
            for (int i = 0;i < eventlist.size();i++){
                commodities.get(eventlist.get(i)).setQty(0);
            }
        }
        adapter.notifyDataSetChanged();

        /**
        commodities.clear();
        Cursor cursor =null;
        if (category_id == 0){
            cursor = db.query("line_items_attributes",null,null,null,null,null,null);
        }else if (category_id == 1){
            String sql = "select * from line_items_attributes where category_id = 1";
            cursor = db.rawQuery(sql,null);
        }else if (category_id == 2){
            String sql = "select * from line_items_attributes where category_id = 2";
            cursor = db.rawQuery(sql,null);
        }else if (category_id == 3){
            String sql = "select * from line_items_attributes where category_id = 3";
            cursor = db.rawQuery(sql,null);
        }
        while (cursor.moveToNext()) {
            if (commodities.size() < cursor.getCount()) {
                Commodity commodity = new Commodity();
                commodity.setId(cursor.getInt(cursor.getColumnIndex("id")));
                commodity.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                commodity.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                commodity.setCategory_id(cursor.getInt(cursor.getColumnIndex("category_id")));
                commodity.setAvatar_url(cursor.getString(cursor.getColumnIndex("avatar_url")));
                commodity.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                String sql = "select qty from line_items where line_items.Items_id = "+commodity.getId()+" and line_items.cart_id = "+cart_id;
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
        adapter.notifyDataSetChanged();
         **/
    }

}
