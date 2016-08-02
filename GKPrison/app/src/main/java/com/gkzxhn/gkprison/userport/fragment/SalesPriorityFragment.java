package com.gkzxhn.gkprison.userport.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.event.ClickEven1;
import com.gkzxhn.gkprison.userport.event.ClickEvent;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SalesPriorityFragment extends BaseFragment implements AbsListView.OnScrollListener {
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/databases/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
    private List<Commodity> commodities = new ArrayList<Commodity>();
    private ListView lv_sale;
    private SalesAdapter adapter;
    private int cart_id = 0;
    private int qty = 0;
    private String url;
    private int Items_id;
    private String token;
    private int jail_id;
    private int page;
    private List<Commodity> addcommdity = new ArrayList<>();
    private List<Integer> buycommidty = new ArrayList<>();//已购买的商品
    private List<Integer> buyqty = new ArrayList<>();//已购买商品数量
    private View loadmore;
    private ImageView iv_nothing;//当没有商品时显示；
    private int visibleLastIndex = 0; //最后的可视索引；
    private int visibleItemCount;//当前窗口可见项总数；
    private int category_id;
    private SharedPreferences sp;
    private int eventint = 0;//接收点击事件传来的数据
    private List<Integer> eventlist = new ArrayList<Integer>();//接收点击事件传来的数据
    OkHttpClient client = new OkHttpClient();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String result = (String) msg.obj;
                    commodities = analysiscommodity(result);
                    if (commodities.size() == 0) {
                        iv_nothing.setVisibility(View.VISIBLE);
                    } else {
                        iv_nothing.setVisibility(View.GONE);
                        Log.d("dd", commodities.size() + "");
                        Collections.sort(commodities, new Comparator<Commodity>() {
                            @Override
                            public int compare(Commodity lhs, Commodity rhs) {
                                int heat1 = lhs.getRanking();
                                int heat2 = rhs.getRanking();
                                if (heat1 < heat2) {
                                    return 1;
                                }
                                return -1;
                            }
                        });
                        String sql = "select distinct qty,Items_id from line_items where cart_id = " + cart_id;
                        Cursor cursor = db.rawQuery(sql, null);
                        if (cursor.getCount() == 0) {
                            adapter = new SalesAdapter(context, commodities);
                            lv_sale.setAdapter(adapter);
                        } else {
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
                            adapter = new SalesAdapter(context, commodities);
                            lv_sale.setAdapter(adapter);
                        }
                    }
                    break;
                case 2:
                    String add = (String) msg.obj;
                    addcommdity = analysiscommodity(add);
                    for (int i = 0; i < commodities.size(); i++) {
                        for (int j = 0; j < addcommdity.size(); j++) {
                            if (commodities.get(i).getId() == addcommdity.get(j).getId()) {
                                addcommdity.remove(j);
                            }
                        }
                    }
                    Collections.sort(addcommdity, new Comparator<Commodity>() {
                        @Override
                        public int compare(Commodity lhs, Commodity rhs) {
                            int heat1 = lhs.getRanking();
                            int heat2 = rhs.getRanking();
                            if (heat1 < heat2) {
                                return 1;
                            }
                            return -1;
                        }
                    });
                    if (addcommdity.size() != 0) {
                        loadDate(addcommdity);
                        String sql1 = "select distinct qty,Items_id from line_items where cart_id = " + cart_id;
                        Cursor cursor1 = db.rawQuery(sql1, null);
                        if (cursor1.getCount() == 0) {
                            loadmore.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        } else {
                            for (int i = 0; i < commodities.size(); i++) {
                                for (int j = 0; j < buyqty.size(); j++) {
                                    if (commodities.get(i).getId() == buycommidty.get(j)) {
                                        commodities.get(i).setQty(buyqty.get(j));
                                    }
                                }
                            }
                            loadmore.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        showToastMsgShort("已到最后一页");
                        loadmore.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    private void loadDate(List<Commodity> adddate) {
        for (int i = 0; i < adddate.size(); i++) {
            adapter.addItem(adddate.get(i));
        }
    }

    @Override
    protected View initView() {
        View view = View.inflate(context, R.layout.fragment_sales_priority, null);
        lv_sale = (ListView) view.findViewById(R.id.lv_sales);
        loadmore = View.inflate(context, R.layout.bottom, null);
        iv_nothing = (ImageView) view.findViewById(R.id.iv_nothing);
        return view;
    }

    @Override
    protected void initData() {
        lv_sale.addFooterView(loadmore);
        loadmore.setVisibility(View.GONE);
        sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        jail_id = sp.getInt("jail_id", 1);
        token = sp.getString("token", "");
        EventBus.getDefault().register(this);
        getDate();
        lv_sale.setOnScrollListener(this);
    }


    /**
     * 解析商品列表
     *
     * @param s
     * @return
     */
    private List<Commodity> analysiscommodity(String s) {
        List<Commodity> commodities = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(s);
            for (int i = 0; i < jsonArray.length(); i++) {
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

    private void getDate() {
        Bundle bundle = getArguments();
        String times = bundle.getString("times");
        category_id = bundle.getInt("leibie", 0);
        String sql = "select id from Cart where time = '" + times + "'";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            cart_id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        if (category_id == 0) {
            url = Constants.URL_HEAD + "items?page=" + page + "&access_token=" + token + "&jail_id=" + jail_id;
            Log.d("ff", url);
            new Thread() {
                @Override
                public void run() {
                    Request request = new Request.Builder().url(url).build();
                    try {
                        Message msg = handler.obtainMessage();
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String result = response.body().string();
                            Log.d("dd", result);
                            msg.obj = result;
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else if (category_id == 1) {
            url = Constants.URL_HEAD + "items?page=" + page + "&category_id=" + category_id + "&access_token=" + token + "&jail_id=" + jail_id;
            new Thread() {
                @Override
                public void run() {
                    Request request = new Request.Builder().url(url).build();
                    try {
                        Message msg = handler.obtainMessage();
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String result = response.body().string();
                            Log.d("dd", result);
                            msg.obj = result;
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else if (category_id == 2) {
            url = Constants.URL_HEAD + "items?page=" + page + "&category_id=" + category_id + "&access_token=" + token + "&jail_id=" + jail_id;
            new Thread() {
                @Override
                public void run() {
                    Request request = new Request.Builder().url(url).build();
                    try {
                        Message msg = handler.obtainMessage();
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String result = response.body().string();
                            Log.d("dd", result);
                            msg.obj = result;
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else if (category_id == 3) {
            url = Constants.URL_HEAD + "items?page=" + page + "&category_id=" + category_id + "&access_token=" + token + "&jail_id=" + jail_id;
            new Thread() {
                @Override
                public void run() {
                    Request request = new Request.Builder().url(url).build();
                    try {
                        Message msg = handler.obtainMessage();
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String result = response.body().string();
                            Log.d("dd", result);
                            msg.obj = result;
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemLastIndex = adapter.getCount() - 1;
        int lastIndex = itemLastIndex + 1;
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == lastIndex) {
            loadmore.setVisibility(View.VISIBLE);
            loadmore();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }


    /**
     * 加载更多商品
     */
    private void loadmore() {
        page += 1;
        if (category_id == 0) {
            final String addurl = Constants.URL_HEAD + "items?page=" + page + "&access_token=" + token + "&jail_id=" + jail_id;
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Request request = new Request.Builder().url(addurl).build();
                    try {
                        Message msg = handler.obtainMessage();
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String result = response.body().string();
                            msg.obj = result;
                            msg.what = 2;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Looper.loop();
                    }
                }
            }.start();
        } else if (category_id == 1) {
            final String addurl = Constants.URL_HEAD + "items?page=" + page + "&category_id=" + category_id + "&access_token=" + token + "&jail_id=" + jail_id;
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Request request = new Request.Builder().url(addurl).build();
                    try {
                        Message msg = handler.obtainMessage();
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String result = response.body().string();
                            msg.obj = result;
                            msg.what = 2;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Looper.loop();
                    }
                }
            }.start();
        } else if (category_id == 2) {
            final String addurl = Constants.URL_HEAD + "items?page=" + page + "&category_id=" + category_id + "&access_token=" + token + "&jail_id=" + jail_id;
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Request request = new Request.Builder().url(addurl).build();
                    try {
                        Message msg = handler.obtainMessage();
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String result = response.body().string();
                            msg.obj = result;
                            msg.what = 2;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Looper.loop();
                    }
                }
            }.start();
        } else if (category_id == 3) {
            final String addurl = Constants.URL_HEAD + "items?page=" + page + "&category_id=" + category_id + "&access_token=" + token + "&jail_id=" + jail_id;
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Request request = new Request.Builder().url(addurl).build();
                    try {
                        Message msg = handler.obtainMessage();
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String result = response.body().string();
                            msg.obj = result;
                            msg.what = 2;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Looper.loop();
                    }
                }
            }.start();
        }
    }


    private class SalesAdapter extends BaseAdapter {
        private List<Commodity> commodityList;
        private LayoutInflater inflater;


        public SalesAdapter(Context context, List<Commodity> commodityList) {
            this.commodityList = commodityList;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

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
                convertView = View.inflate(context, R.layout.sales_item, null);
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
