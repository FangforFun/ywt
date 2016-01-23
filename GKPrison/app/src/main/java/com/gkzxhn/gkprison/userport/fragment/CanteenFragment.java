package com.gkzxhn.gkprison.userport.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.activity.PaymentActivity;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.userport.bean.AA;
import com.gkzxhn.gkprison.userport.bean.line_items_attributes;
import com.gkzxhn.gkprison.userport.bean.Order;
import com.gkzxhn.gkprison.userport.bean.Shoppinglist;
import com.gkzxhn.gkprison.userport.event.ClickEven1;
import com.gkzxhn.gkprison.userport.event.ClickEvent;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jauker.widget.BadgeView;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.greenrobot.event.EventBus;

/**
 * Created by zhengneng on 2015/12/21.
 */
public class CanteenFragment extends BaseFragment {
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
    private RelativeLayout rl_allclass;
    private RelativeLayout rl_sales;
    private RelativeLayout rl_zhineng;
    private float total ;
    private String send;
    private Button settlement;
    private TextView tv_allclass;
    private TextView tv_sales;
    private TextView tv_zhineng;
    private Spinner sp_allclass;
    private Gson gson;
    private String url = Constants.URL_HEAD + "orders?jail_id=1&access_token=";
    private Spinner sp_sales;
    private Spinner sp_zhineng;
    private TextView tv_total_money;
    private int cart_id = 0;
    private String result;
    private List<Shoppinglist> commodities = new ArrayList<Shoppinglist>();
    AllClassificationFragment allclass;
    SalesPriorityFragment sales;
    IntellingentSortingFragment zhineng;
    private Bundle data;
    private View image_buycar;
    private int count = 0;
    private String ip;
    private BadgeView badgeView;
    private List<Integer> lcount = new ArrayList<Integer>();
    private int allcount;
    private String TradeNo;
    private SharedPreferences sp;
    private String apply = "";
    private  List<line_items_attributes> line_items_attributes = new ArrayList<line_items_attributes>();
    private String times;
    private ProgressDialog dialog;
    private FrameLayout fl;//购物车详情页面
    private ListView lv_buycar;
    private BuyCarAdapter adapter;
    private RelativeLayout clear;
    private List<Integer> eventlist = new ArrayList<Integer>();//用于点击事件传值
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    int i = (Integer)msg.obj;
                    badgeView.setText(i+"");
                    break;
            }
        }
    };
    private Handler handler1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String s = (String)msg.obj;
                    tv_total_money.setText(s);
                    break;
            }
        }
    };


    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_canteen, null);
        settlement = (Button)view.findViewById(R.id.bt_shopping_cart_commit);
        rl_allclass = (RelativeLayout)view.findViewById(R.id.rl_allclass);
        rl_sales = (RelativeLayout)view.findViewById(R.id.rl_sales);
        rl_zhineng = (RelativeLayout)view.findViewById(R.id.rl_zhineng);
        tv_allclass = (TextView)view.findViewById(R.id.tv_allclass);
        tv_sales = (TextView)view.findViewById(R.id.tv_sales);
        tv_zhineng = (TextView)view.findViewById(R.id.tv_zhineng);
        sp_allclass = (Spinner)view.findViewById(R.id.sp_allclass);
        sp_sales = (Spinner)view.findViewById(R.id.sp_sales);
        sp_zhineng = (Spinner)view.findViewById(R.id.sp_zhineng);
        tv_total_money = (TextView)view.findViewById(R.id.tv_total_money);
        image_buycar = view.findViewById(R.id.image_buycar);
        badgeView = new BadgeView(context);
        badgeView.setTargetView(image_buycar);
        fl = (FrameLayout)view.findViewById(R.id.fl_buycar);
        lv_buycar = (ListView)view.findViewById(R.id.lv_buycar);
        clear = (RelativeLayout)view.findViewById(R.id.rl_clear);
        badgeView.setTextSize(6);
        badgeView.setShadowLayer(3, 0, 0, Color.parseColor("#f10000"));
        badgeView.setBadgeGravity(Gravity.TOP | Gravity.RIGHT);
        return view;
    }

    @Override
    protected void initData() {
        ip = getLocalHostIp();
        TradeNo = getOutTradeNo();
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        times = format.format(date);
        String sql = "insert into Cart (time,out_trade_no,isfinish,remittance) values ('"+times+"','"+TradeNo+"',0,0)";
        db.execSQL(sql);
        String sql1 = "select id from Cart where time = '"+times+"'";
        Cursor cursor = db.rawQuery(sql1, null);
        while (cursor.moveToNext()){
            cart_id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        data = new Bundle();
        data.putString("times",times);
        View image_buycar = view.findViewById(R.id.image_buycar);
        EventBus.getDefault().register(this);
        sp_allclass.setEnabled(true);
        sp_allclass.setFocusable(true);
        sp_sales.setEnabled(false);
        sp_sales.setFocusable(false);
        tv_allclass.setTextColor(Color.parseColor("#6495ed"));
        sp_allclass.setBackgroundResource(R.drawable.spinner_down);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sql = "delete from line_items where cart_id ="+cart_id;
                db.execSQL(sql);
                allcount = 0;
                Message msg = handler.obtainMessage();
                msg.obj = allcount;
                msg.what = 1;
                handler.sendMessage(msg);
                send = "0.00";
                Message msg1 = handler1.obtainMessage();
                msg1.obj = send;
                msg1.what = 1;
                handler1.sendMessage(msg1);
                for (int i = 0;i < commodities.size();i++){
                    eventlist.add(commodities.get(i).getPosition());
                }
                commodities.clear();
                EventBus.getDefault().post(new ClickEven1(1,eventlist));
                eventlist.clear();
                fl.setVisibility(View.GONE);
            }
        });
        image_buycar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = fl.getVisibility();
                if (commodities.size() == 0){
                    showToastMsgShort("没有选择商品");
                }else {
                    if (i == 0) {
                        fl.setVisibility(View.GONE);
                    } else if (i == 8) {
                        fl.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fl.setVisibility(View.GONE);
            }
        });

        sp_allclass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 switch (position) {
                    case 0:
                        allclass = new AllClassificationFragment();
                        data.putInt("leibie", 0);
                        allclass.setArguments(data);
                        ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, allclass).commit();
                        break;
                    case 1:
                        allclass = new AllClassificationFragment();
                        data.putInt("leibie", 1);
                        allclass.setArguments(data);
                        ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, allclass).commit();
                        break;
                    case 2:
                        allclass = new AllClassificationFragment();
                        data.putInt("leibie", 2);
                        allclass.setArguments(data);
                        ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, allclass).commit();
                        break;
                    case 3:
                        allclass = new AllClassificationFragment();
                        data.putInt("leibie", 3
                        );
                        allclass.setArguments(data);
                        ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, allclass).commit();
                        break;
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(context, "unselected", Toast.LENGTH_SHORT).show();
            }
        });
        rl_allclass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_allclass.setTextColor(Color.parseColor("#6495ed"));
                tv_sales.setTextColor(context.getResources().getColor(R.color.tv_bg));
                tv_zhineng.setTextColor(context.getResources().getColor(R.color.tv_bg));
                sp_allclass.setBackgroundResource(R.drawable.spinner_down);
                sp_sales.setBackgroundResource(R.drawable.spinner);
                sp_zhineng.setBackgroundResource(R.drawable.spinner);
                allclass = new AllClassificationFragment();
                allclass.setArguments(data);
                ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, allclass).commit();
                sp_sales.setEnabled(false);
                sp_sales.setFocusable(false);
                sp_allclass.setEnabled(true);
                sp_allclass.setFocusable(true);
            }
        });
        rl_sales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_sales.setTextColor(Color.parseColor("#6495ed"));
                tv_allclass.setTextColor(Color.parseColor("#333333"));
                tv_zhineng.setTextColor(Color.parseColor("#333333"));
                sp_sales.setBackgroundResource(R.drawable.spinner_down);
                sp_allclass.setBackgroundResource(R.drawable.spinner);
                sp_zhineng.setBackgroundResource(R.drawable.spinner);
                sales = new SalesPriorityFragment();
                sales.setArguments(data);
                ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, sales).commit();
                sp_allclass.setEnabled(false);
                sp_allclass.setFocusable(false);
                sp_sales.setEnabled(true);
                sp_sales.setFocusable(true);
                sp_sales.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(context, " position=" + position + " id=" + id, Toast.LENGTH_SHORT).show();
                        switch (position) {
                            case 0:
                                sales = new SalesPriorityFragment();
                                data.putInt("leibie", 0);
                                sales.setArguments(data);
                                ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, sales).commit();
                                break;
                            case 1:
                                sales = new SalesPriorityFragment();
                                data.putInt("leibie", 1);
                                sales.setArguments(data);
                                ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, sales).commit();
                                break;
                            case 2:
                                sales = new SalesPriorityFragment();
                                data.putInt("leibie", 2);
                                sales.setArguments(data);
                                ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, sales).commit();
                                break;
                            case 3:
                                sales = new SalesPriorityFragment();
                                data.putInt("leibie", 3);
                                sales.setArguments(data);
                                ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, sales).commit();
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
        rl_zhineng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_zhineng.setTextColor(Color.parseColor("#6495ed"));
                tv_sales.setTextColor(Color.parseColor("#333333"));
                tv_allclass.setTextColor(Color.parseColor("#333333"));
                sp_zhineng.setBackgroundResource(R.drawable.spinner_down);
                sp_sales.setBackgroundResource(R.drawable.spinner);
                sp_allclass.setBackgroundResource(R.drawable.spinner);
                zhineng = new IntellingentSortingFragment();
                zhineng.setArguments(data);
                ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, zhineng).commit();
            }
        });
        try {
            settlement.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (allcount != 0) {
                        sendOrderToServer();
                        String sql = "update Cart set total_money = '"+send+"',count = "+allcount+"  where time = '"+times+"'";
                        db.execSQL(sql);

                            Intent intent = new Intent(context, PaymentActivity.class);
                            intent.putExtra("totalmoney", send);
                            intent.putExtra("TradeNo", TradeNo);
                            intent.putExtra("times", times);
                            context.startActivity(intent);

                    } else {
                        Toast.makeText(context, "请选择商品", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    public void onEvent(ClickEvent event) {
        // 从事件中获得参数值
//        Toast.makeText(context, "点我，点我", Toast.LENGTH_SHORT).show();
        commodities.clear();
        lcount.clear();
        line_items_attributes.clear();
        allcount = 0;
        String sql = "select distinct line_items.Items_id,line_items.qty,line_items.position,Items.price,Items.title from line_items,Items,Cart where line_items.Items_id = Items.id and line_items.cart_id = "+cart_id;
        Cursor cursor = db.rawQuery(sql, null);
        total = 0;
        if (cursor.getCount() == 0){
            tv_total_money.setText("0.0");
            badgeView.setVisibility(View.GONE);
        }else {
            badgeView.setVisibility(View.VISIBLE);
            while (cursor.moveToNext()){
                Shoppinglist shoppinglist = new Shoppinglist();
                shoppinglist.setId(cursor.getInt(cursor.getColumnIndex("Items_id")));
                shoppinglist.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                shoppinglist.setQty(cursor.getInt(cursor.getColumnIndex("qty")));
                shoppinglist.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                shoppinglist.setPosition(cursor.getInt(cursor.getColumnIndex("position")));
                commodities.add(shoppinglist);
            }
            adapter = new BuyCarAdapter();
            lv_buycar.setAdapter(adapter);
        }
        for (int i = 0;i < commodities.size();i++){
            String t = commodities.get(i).getPrice();
            float p = Float.parseFloat(t);
            int n = commodities.get(i).getQty();
            line_items_attributes lineitemsattributes = new line_items_attributes();
            lineitemsattributes.setItem_id(commodities.get(i).getId());
            lineitemsattributes.setQuantity(n);
            line_items_attributes.add(lineitemsattributes);
            total += p * n ;
            count = n;
            lcount.add(count);
        }
        for (int i = 0;i < lcount.size();i++){
            allcount += lcount.get(i);
        }

        Message msg1 = handler.obtainMessage();
        msg1.obj = allcount;
        msg1.what = 1;
        handler.sendMessage(msg1);
        DecimalFormat fnum = new DecimalFormat("####0.00");
        send = fnum.format(total);
        Message msg = handler1.obtainMessage();
        msg.obj = send;
        msg.what = 1;
        handler1.sendMessage(msg);
    }

    private void sendOrderToServer() {
        int family_id = sp.getInt("family_id",1);
        final Order order = new Order();
        order.setFamily_id(family_id);
        order.setIp(ip);
        order.setLine_items_attributes(line_items_attributes);
        order.setJail_id(1);
        order.setCreated_at(times);
        Float f = Float.parseFloat(send);
        order.setAmount(f);
        gson = new Gson();
        order.setTrade_no(TradeNo);
        apply = gson.toJson(order);
        Log.d("结算发送",apply);
        final AA aa = new AA();
        aa.setOrder(order);
        final String str = gson.toJson(aa);
       new Thread(){
            @Override
          public void run() {
                String token = sp.getString("token", "");
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url+token);
                String s = url+token;
                try {
                    StringEntity entity = new StringEntity(str);
                    entity.setContentType("application/json");
                    entity.setContentEncoding("UTF-8");
                    post.setEntity(entity);
                    HttpResponse response = httpClient.execute(post);
                    if (response.getStatusLine().getStatusCode() == 200){
                         result = EntityUtils.toString(response.getEntity(), "UTF-8");
                         Log.d("成功",result);

                    }
                }  catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }.start();


    }


    public String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);
        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    public String getLocalHostIp() {
        String ipaddress = "";
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements())
            {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements())
                {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip
                            .getHostAddress()))
                    {
                        return ipaddress = ip.getHostAddress();
                    }
                }

            }
        }
        catch (SocketException e)
        {
            Log.e("feige", "获取本地ip地址失败");
            e.printStackTrace();
        }
        return ipaddress;

    }

    private class BuyCarAdapter extends BaseAdapter{

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
            if (convertView == null){
                convertView = View.inflate(context,R.layout.buycar_items,null);
                viewHolder = new ViewHolder();
                viewHolder.title = (TextView)convertView.findViewById(R.id.tv_title);
                viewHolder.price = (TextView)convertView.findViewById(R.id.tv_price);
                viewHolder.add = (RelativeLayout)convertView.findViewById(R.id.rl_buycar_add);
                viewHolder.reduce = (RelativeLayout)convertView.findViewById(R.id.rl_buycar_reduce);
                viewHolder.num = (TextView)convertView.findViewById(R.id.tv_buycar_num);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.title.setText(commodities.get(position).getTitle());
            viewHolder.num.setText(commodities.get(position).getQty()+"");
            viewHolder.price.setText(commodities.get(position).getPrice());
            final Handler handler2 = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what){
                        case 1:
                            int i = (Integer)msg.obj;
                            viewHolder.num.setText(i+"");
                            break;
                    }
                }
            };
            viewHolder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Utils.isFastClick()){
                        return;
                    }
                    String t = viewHolder.num.getText().toString();
                    int i = Integer.parseInt(t);
                    int j = i + 1;
                    int id = commodities.get(position).getId();
                    String sql = "update line_items set qty ="+j+"  where Items_id ="+id+"  and cart_id ="+cart_id;
                    db.execSQL(sql);
                    String price = commodities.get(position).getPrice();
                    float p = Float.parseFloat(price);
                    total += p;
                    DecimalFormat fnum = new DecimalFormat("####0.00");
                    send = fnum.format(total);
                    Message msg = handler1.obtainMessage();
                    msg.obj = send;
                    msg.what = 1;
                    handler1.sendMessage(msg);
                    allcount += 1;
                    Message msg1 = handler.obtainMessage();
                    msg1.obj = allcount;
                    msg1.what = 1;
                    handler.sendMessage(msg1);
                    String sql1 = "select qty from line_items where Items_id = "+id+" and cart_id ="+cart_id;
                    Cursor cursor = db.rawQuery(sql1,null);
                    int qty = 0;
                    if (cursor.getCount() == 0){
                        qty = 0;
                    }else {
                        while (cursor.moveToNext()){
                            qty = cursor.getInt(cursor.getColumnIndex("qty"));
                        }
                    }
                    Message msg2 = handler2.obtainMessage();
                    msg2.obj = qty;
                    msg2.what = 1;
                    handler2.sendMessage(msg2);
                    commodities.get(position).setQty(j);
                    int d = commodities.get(position).getPosition();
                    eventlist.add(d);
                    eventlist.add(qty);
                    EventBus.getDefault().post(new ClickEven1(0,eventlist));
                    eventlist.clear();
                }
            });
            viewHolder.reduce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Utils.isFastClick()){
                        return;
                    }
                    String t = viewHolder.num.getText().toString();
                    int i = Integer.parseInt(t);
                    int id = commodities.get(position).getId();
                    if (i == 1){
                        String price = commodities.get(position).getPrice();
                        float  p = Float.parseFloat(price);
                        total -= p;
                        DecimalFormat fnum = new DecimalFormat("####0.00");
                        send = fnum.format(total);
                        Message msg = handler1.obtainMessage();
                        msg.obj = send;
                        msg.what = 1;
                        handler1.sendMessage(msg);
                        allcount -= 1;
                        Message msg1 = handler.obtainMessage();
                        msg1.obj = allcount;
                        msg1.what = 1;
                        handler.sendMessage(msg1);
                        String sql = "delete from line_items where Items_id ="+id+"  and cart_id ="+cart_id;
                        db.execSQL(sql);
                        adapter.notifyDataSetChanged();
                        int d = commodities.get(position).getPosition();
                        eventlist.add(d);
                        eventlist.add(0);
                        EventBus.getDefault().post(new ClickEven1(0,eventlist));
                        eventlist.clear();
                        commodities.remove(position);
                    }else {
                        int j = i-1;
                        String sql = "update line_items set qty="+j+" where Items_id ="+id+"  and cart_id="+cart_id;
                        db.execSQL(sql);
                        String price = commodities.get(position).getPrice();
                        float p = Float.parseFloat(price);
                        total -= p;
                        DecimalFormat fnum = new DecimalFormat("####0.00");
                        send = fnum.format(total);
                        Message msg = handler1.obtainMessage();
                        msg.obj = send;
                        msg.what = 1;
                        handler1.sendMessage(msg);
                        allcount -= 1;
                        Message msg1 = handler.obtainMessage();
                        msg1.obj = allcount;
                        msg1.what = 1;
                        handler.sendMessage(msg1);
                        String sql1 = "select qty from line_items where Items_id = " + id + "  and cart_id = " + cart_id;
                        Cursor cursor = db.rawQuery(sql1, null);
                        int qty = 0;
                        while (cursor.moveToNext()) {
                            qty = cursor.getInt(cursor.getColumnIndex("qty"));
                        }
                        Message msg2 = handler2.obtainMessage();
                        msg2.obj = qty;
                        msg2.what = 1;
                        handler2.sendMessage(msg2);
                        commodities.get(position).setQty(j);
                        int d = commodities.get(position).getPosition();
                        eventlist.add(d);
                        eventlist.add(qty);
                        EventBus.getDefault().post(new ClickEven1(0,eventlist));
                        eventlist.clear();
                    }

                    if (commodities.size()==0){
                        fl.setVisibility(View.GONE);
                    }
                }
            });
            return convertView;
        }

        private class ViewHolder{
            TextView title;
            TextView price;
            RelativeLayout add;
            RelativeLayout reduce;
            TextView num;
        }
    }
}
