package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.AA;
import com.gkzxhn.gkprison.userport.bean.Items;
import com.gkzxhn.gkprison.userport.bean.Order;
import com.gkzxhn.gkprison.utils.ListViewParamsUtils;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FamilyServiceActivity extends BaseActivity {
    private ExpandableListView el_messge;
    private MyAdapter adapter;
    private String TradeNo;
    private String times ="";
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
    private SharedPreferences sp;
    private String ip;
    private String money = "";
    private Gson gson;
    private String apply;
    private List<Items> itemses = new ArrayList<Items>();
    private String url = Constants.URL_HEAD + "orders?jail_id=1&access_token=";
    private List<String> sentence_time = new ArrayList<String>(){
        {
            add("2014年2月11日");
            add("2014年4月11日");
            add("2014年6月11日");
            add("2014年8月11日");
        }
    };
    private List<String> sentence_cause = new ArrayList<String>(){
        {
            add("制止狱内暴力");
            add("做事认真勤快");
            add("表现良好");
            add("后台很硬");
        }
    };
    private List<String> sentence_time_add = new ArrayList<String>(){
        {
            add("减刑3个月");
            add("减刑3个月");
            add("减刑3个月");
            add("加刑3个月");
        }
    };
    private List<String> commodity = new ArrayList<String>(){
        {
            add("杯子");
            add("零食");
            add("热水瓶");
            add("笔记本");
        }
    };
    private List<String> money1 = new ArrayList<String>(){
        {
            add("20元");
            add("10元");
            add("40元");
            add("5元");
        }
    };
    private List<String> buyer_id = new ArrayList<String>(){
        {
            add("18609018373");
            add("13909018386");
            add("18209648389");
            add("13909018373");
        }
    };
    private List<Integer> image_messge = new ArrayList<Integer>(){
        {
            add(R.drawable.sentence);
            add(R.drawable.consumption);
            add(R.drawable.buy);
        }
    };

    private List<String> text_messge = new ArrayList<String>(){
        {
            add("刑期变动");
            add("消费记录");
            add("购物签收");
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_family_service,null);
        el_messge = (ExpandableListView)view.findViewById(R.id.el_messge);
        el_messge.setGroupIndicator(null);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("家属服务");
        setBackVisibility(View.VISIBLE);
        setRemittanceVisibility(View.VISIBLE);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        ip = getLocalHostIp();
        adapter = new MyAdapter();
        el_messge.setAdapter(adapter);
        rl_remittance.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        //Intent intent;
        switch (v.getId()){
            case R.id.rl_remittance:
               // intent = new Intent(this, RemittanceWaysActivity.class);
                //startActivity(intent);
                long time = System.currentTimeMillis();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(time);
                times = format.format(date);
                TradeNo = getOutTradeNo();
                AlertDialog.Builder builder = new AlertDialog.Builder(FamilyServiceActivity.this);
                View view = FamilyServiceActivity.this.getLayoutInflater().inflate(R.layout.remittance_dialog,null);
                final EditText et_money = (EditText)view.findViewById(R.id.et_money);
                Editable ea = et_money.getText();
                et_money.setSelection(ea.length());
                TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
                TextView tv_ok = (TextView) view.findViewById(R.id.tv_ok);
                builder.setView(view);
                final AlertDialog dialog = builder.create();
                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                tv_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        money = et_money.getText().toString();
                        if (TextUtils.isEmpty(money)) {
                            Toast.makeText(getApplicationContext(), "请输入汇款金额", Toast.LENGTH_SHORT).show();
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                                dialog.dismiss();
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            return;
                        } else {
                            sendOrderToServer();
                            String sql = "insert into Cart(time,out_trade_no,isfinish,total_money,remittance) values('"+times+"','"+TradeNo+"',0,'"+money+"',1)";
                            db.execSQL(sql);
                            int cart_id = 0;
                            String sql1 = "select id from Cart where time = '"+times+"'";
                            Cursor cursor = db.rawQuery(sql1,null);
                            while (cursor.moveToNext()){
                                cart_id = cursor.getInt(cursor.getColumnIndex("id"));
                            }
                            String sql2 = "insert into line_items(Items_id,cart_id) values (9999,"+cart_id+")";
                            db.execSQL(sql2);
                            Intent intent = new Intent(FamilyServiceActivity.this, RemittanceWaysActivity.class);
                            intent.putExtra("money", money);
                            intent.putExtra("times",times);
                            intent.putExtra("TradeNo",TradeNo);
                            startActivity(intent);
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
                break;
        }
    }

    private void sendOrderToServer() {
        int family_id = sp.getInt("family_id",1);
        final Order order = new Order();
        order.setFamily_id(family_id);
        order.setIp(ip);
        Items  items = new Items();
        items.setItem_id(9999);
        items.setQuantity(1);
        itemses.add(items);
        order.setItems(itemses);
        order.setJail_id(1);
        order.setCreated_at(times);
        Float f = Float.parseFloat(money);
        order.setAmount(f);
        gson = new Gson();
        order.setOut_trade_no(TradeNo);
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
                Log.d("订单号成功", s);
                try {
                    StringEntity entity = new StringEntity(str);
                    entity.setContentType("application/json");
                    entity.setContentEncoding("UTF-8");
                    post.setEntity(entity);
                    HttpResponse response = httpClient.execute(post);
                    if (response.getStatusLine().getStatusCode() == 200){
                        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                        Log.d("订单号成功", result);
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

    private class MyAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            return image_messge.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupPosition;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupViewHolder viewHolder;
            if (convertView == null){
                convertView = View.inflate(getApplicationContext(),R.layout.familyservice_item,null);
                viewHolder = new GroupViewHolder();
                viewHolder.image_click = (ImageView)convertView.findViewById(R.id.image_click);
                viewHolder.img_messge = (ImageView)convertView.findViewById(R.id.image_messge);
                viewHolder.tv_messge = (TextView)convertView.findViewById(R.id.tv_messge);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (GroupViewHolder)convertView.getTag();
            }
            if (isExpanded){
                viewHolder.image_click.setImageResource(R.drawable.clickup);
            }else {
                viewHolder.image_click.setImageResource(R.drawable.clickdown);
            }
            viewHolder.img_messge.setImageResource(image_messge.get(groupPosition));
            viewHolder.tv_messge.setText(text_messge.get(groupPosition));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

                if (groupPosition == 0){
                    convertView = View.inflate(getApplicationContext(),R.layout.sentence_change,null);
                    ListView lv_sentence = (ListView)convertView.findViewById(R.id.lv_sentence_recod);
                    SentenceAdapter adapter = new SentenceAdapter();
                    lv_sentence.setAdapter(adapter);
                    ListViewParamsUtils.setListViewHeightBasedOnChildren(lv_sentence);
                }else if (groupPosition == 1){
                    convertView = View.inflate(getApplicationContext(),R.layout.consumption,null);
                    ListView lv_consumption = (ListView)convertView.findViewById(R.id.lv_consumption);
                    ConsumptionAdapter adapter = new ConsumptionAdapter();
                    lv_consumption.setAdapter(adapter);
                    ListViewParamsUtils.setListViewHeightBasedOnChildren(lv_consumption);
                }else if (groupPosition == 2){
                    convertView = View.inflate(getApplicationContext(),R.layout.shoppingreceipt,null);
                    ListView lv_shopping = (ListView)convertView.findViewById(R.id.lv_shopping);
                    ReceiptAdapter adapter = new ReceiptAdapter();
                    lv_shopping.setAdapter(adapter);
                    ListViewParamsUtils.setListViewHeightBasedOnChildren(lv_shopping);
                }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
    private class GroupViewHolder{
        ImageView img_messge;
        TextView tv_messge;
        ImageView image_click;

    }


    private class SentenceAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return sentence_time.size()+1;
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
            ViewHolder viewHolder;
            if (convertView == null){
                convertView = View.inflate(getApplicationContext(),R.layout.sentence_change_item,null);
                viewHolder = new ViewHolder();
                viewHolder.tv_sentence_time = (TextView)convertView.findViewById(R.id.tv_sentence_time);
                viewHolder.tv_sentence_case = (TextView)convertView.findViewById(R.id.tv_sentence_case);
                viewHolder.tv_sentence_add = (TextView)convertView.findViewById(R.id.tv_sentence_add);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            if (position == 0){
                viewHolder.tv_sentence_time.setText("时间");
                viewHolder.tv_sentence_case.setText("原因");
                viewHolder.tv_sentence_add.setText("加/减刑");
            }else {
                viewHolder.tv_sentence_time.setText(sentence_time.get(position-1));
                viewHolder.tv_sentence_case.setText(sentence_cause.get(position-1));
                viewHolder.tv_sentence_add.setText(sentence_time_add.get(position-1));
            }
            return convertView;
        }
        private class ViewHolder{
            TextView tv_sentence_time;
            TextView tv_sentence_case;
            TextView tv_sentence_add;
        }
    }

    private class ConsumptionAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return sentence_time.size()+1;
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
            ViewHolder viewHolder;
            if (convertView == null){
                convertView = View.inflate(getApplicationContext(),R.layout.sentence_change_item,null);
                viewHolder = new ViewHolder();
                viewHolder.buy_time = (TextView)convertView.findViewById(R.id.tv_sentence_time);
                viewHolder.buy_commodity = (TextView)convertView.findViewById(R.id.tv_sentence_case);
                viewHolder.buy_money = (TextView)convertView.findViewById(R.id.tv_sentence_add);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            if (position == 0){
                viewHolder.buy_time.setText("购买时间");
                viewHolder.buy_commodity.setText("商品");
                viewHolder.buy_money.setText("金额");
            }else {
                viewHolder.buy_time.setText(sentence_time.get(position-1));
                viewHolder.buy_commodity.setText(commodity.get(position-1));
                viewHolder.buy_money.setText(money1.get(position-1));
            }
            return convertView;
        }
        private class ViewHolder{
            TextView buy_time;
            TextView buy_commodity;
            TextView buy_money;
        }
    }

    private class ReceiptAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return money1.size()+1;
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
            ViewHolder viewHolder;
            if (convertView == null){
                convertView = View.inflate(getApplicationContext(),R.layout.shoppingreceipt_item,null);
                viewHolder = new ViewHolder();
                viewHolder.receipt = (ImageView)convertView.findViewById(R.id.image_receipt);
                viewHolder.qianshou = (TextView)convertView.findViewById(R.id.tv_qianshou);
                viewHolder.qianshou_time = (TextView)convertView.findViewById(R.id.tv_receipt_time);
                viewHolder.qianshou_id = (TextView)convertView.findViewById(R.id.tv_buy_id);
                viewHolder.qianshou_money = (TextView)convertView.findViewById(R.id.tv_money);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            if (position == 0){
                viewHolder.qianshou.setText("确认签收");
                viewHolder.qianshou_time.setText("签收时间");
                viewHolder.qianshou_id.setText("购物ID");
                viewHolder.qianshou_money.setText("购物数值");
                viewHolder.receipt.setVisibility(View.GONE);
            }else {
                viewHolder.receipt.setVisibility(View.VISIBLE);
                viewHolder.qianshou_time.setText(sentence_time.get(position-1));
                viewHolder.qianshou_id.setText(buyer_id.get(position-1));
                viewHolder.qianshou_money.setText(money1.get(position-1));
            }
            return convertView;
        }
        private class ViewHolder{
            TextView qianshou_time;
            TextView qianshou_id;
            TextView qianshou_money;
            TextView qianshou;
            ImageView receipt;
        }
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
}
