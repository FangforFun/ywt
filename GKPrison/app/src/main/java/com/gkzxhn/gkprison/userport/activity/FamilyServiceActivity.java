package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
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
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.userport.bean.AA;
import com.gkzxhn.gkprison.userport.bean.Order;
import com.gkzxhn.gkprison.userport.bean.Prison;
import com.gkzxhn.gkprison.userport.bean.line_items_attributes;
import com.gkzxhn.gkprison.utils.ListViewParamsUtils;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * 家属服务
 */
public class FamilyServiceActivity extends BaseActivity {
    private ExpandableListView el_messge;
    private MyAdapter adapter;
    private String TradeNo;
    private String times = "";
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/databases/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
    private SharedPreferences sp;
    private String ip;
    private String money = "";
    private Gson gson;
    private String apply;
    private Prison prison = new Prison();
    private TextView prison_num;
    private TextView prison_gender;
    private TextView prison_crimes;
    private TextView prison_start_time;
    private TextView prison_end_time;
    private List<line_items_attributes> line_items_attributes = new ArrayList<>();
    private int jail_id;
    private String url1 = Constants.URL_HEAD + "services?access_token=";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String information = (String) msg.obj;
                    if (information.equals("error")) {
                        showToastMsgShort("同步数据有误");
                    } else if (information.equals("success")) {
                        Bundle bundle = msg.getData();
                        String prison_information = bundle.getString("result");
                        prison = analysisprison(prison_information);
                        prison_num.setText(prison.getPrisoner_number());
                        if (prison.getGender().equals("m")) {
                            prison_gender.setText("男");
                        } else {
                            prison_gender.setText("女");
                        }
                        prison_crimes.setText(prison.getCrimes());
                        prison_start_time.setText(prison.getPrison_term_started_at());
                        prison_end_time.setText(prison.getPrison_term_ended_at());
                    }
                    break;
                case 2:
                    String ording = (String) msg.obj;
                    if (ording.equals("error")) {
                        showToastMsgShort("操作失败，请稍后再试！");
                    } else if (ording.equals("success")) {
                        Bundle bundle = msg.getData();
                        String code = bundle.getString("result");
                        int pass_code = getResultcode(code);
                        if (pass_code == 200) {
                            TradeNo = getResultTradeno(code);
                            Intent intent = new Intent(FamilyServiceActivity.this, PaymentActivity.class);
                            intent.putExtra("totalmoney", money);
                            intent.putExtra("times", times);
                            intent.putExtra("TradeNo", TradeNo);
                            intent.putExtra("saletype", "汇款");
                            startActivity(intent);
                        }else {
                            // 其他情况就是等于500  超出每月800额度
                            // {"code":500,"msg":"Create order failed","errors":{"order":["超出每月800元限额"]}}
                            showToastMsgLong("抱歉，您本月汇款总额已超出800元限额！");
                        }
                    }
                    break;
            }
        }
    };

    private List<Integer> image_messge = new ArrayList<Integer>() {
        {
            add(R.drawable.sentence);
            add(R.drawable.consumption);
            add(R.drawable.buy);
        }
    };

    private List<String> text_messge = new ArrayList<String>() {
        {
            add("刑期变动");
            add("消费记录");
            add("购物签收");
        }
    };
    private List<String> sentence_time = new ArrayList(){
        {
            add("2016年5月30日");
            add("2016年5月20日");
            add("2016年5月10日");
        }
    };
    private List<String> sentence_cause = new ArrayList(){
        {
            add("制止狱内暴力");
            add("制止狱内暴力");
            add("制止狱内暴力");
        }
    };
    private List<String> sentence_time_add = new ArrayList(){
        {
            add("减刑三个月");
            add("减刑三个月");
            add("减刑三个月");
        }
    };
    private List<String> buyer_id = new ArrayList(){
        {
            add("1232423423423");
            add("1232423423423");
            add("1232423423423");
        }
    };
    private List<String> money1 = new ArrayList(){
        {
            add("120元");
            add("120元");
            add("120元");
        }
    };
    private List<String> commodity = new ArrayList(){
        {
            add("水杯");
            add("水杯");
            add("水杯");
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_family_service, null);
        el_messge = (ExpandableListView) view.findViewById(R.id.el_messge);
        el_messge.setGroupIndicator(null);
        prison_num = (TextView) view.findViewById(R.id.tv_prison_num);
        prison_gender = (TextView) view.findViewById(R.id.tv_mail_sex);
        prison_crimes = (TextView) view.findViewById(R.id.tv_crime_accent);
        prison_start_time = (TextView) view.findViewById(R.id.tv_sentence_time);
        prison_end_time = (TextView) view.findViewById(R.id.tv_sentence_time_end);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("家属服务");
        setBackVisibility(View.VISIBLE);
        setRemittanceVisibility(View.VISIBLE);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        jail_id = sp.getInt("jail_id", 0);
        ip = getLocalHostIp();
        adapter = new MyAdapter();
        el_messge.setAdapter(adapter);
        rl_remittance.setOnClickListener(this);
        getPrisonIformation();
    }

    private void getPrisonIformation() {
        if (Utils.isNetworkAvailable()) {
            new Thread() {
                String token = sp.getString("token", "");
                Message msg = handler.obtainMessage();

                @Override
                public void run() {
                    Looper.prepare();
                    try {
                        String result = HttpRequestUtil.doHttpsGet(url1 + token);
                        if (result.contains("StatusCode is ")) {
                            msg.obj = "error";
                            msg.what = 1;
                            handler.sendMessage(msg);
                        } else {
                            msg.obj = "success";
                            Bundle bundle = new Bundle();
                            bundle.putString("result", result);
                            msg.setData(bundle);
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        msg.obj = "error";
                        msg.what = 1;
                        handler.sendMessage(msg);
                        e.printStackTrace();
                    } finally {
                        Looper.loop();
                    }
                }
            }.start();
        } else {
            showToastMsgShort("没有网络");
        }
    }

    private Prison analysisprison(String t) {
        Prison prison = new Prison();
        try {
            JSONObject jsonObject = new JSONObject(t);
            JSONObject jsonObject1 = jsonObject.getJSONObject("prisoner");
            prison.setGender(jsonObject1.getString("gender"));
            prison.setCrimes(jsonObject1.getString("crimes"));
            prison.setPrison_term_ended_at(jsonObject1.getString("prison_term_ended_at"));
            prison.setPrison_term_started_at(jsonObject1.getString("prison_term_started_at"));
            prison.setPrisoner_number(jsonObject1.getString("prisoner_number"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return prison;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        //Intent intent;
        switch (v.getId()) {
            case R.id.rl_remittance:
                // intent = new Intent(this, RemittanceWaysActivity.class);
                //startActivity(intent);
                long time = System.currentTimeMillis();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(time);
                times = format.format(date);
                AlertDialog.Builder builder = new AlertDialog.Builder(FamilyServiceActivity.this);
                View view = FamilyServiceActivity.this.getLayoutInflater().inflate(R.layout.remittance_dialog, null);
                final EditText et_money = (EditText) view.findViewById(R.id.et_money);
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
                        if (Utils.isFastClick()) {
                            return;
                        }
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
                            String sql = "insert into Cart(time,out_trade_no,isfinish,total_money,remittance) values('" + times + "','" + TradeNo + "',0,'" + money + "',1)";
                            db.execSQL(sql);
                            int cart_id = 0;
                            String sql1 = "select id from Cart where time = '" + times + "'";
                            Cursor cursor = db.rawQuery(sql1, null);
                            while (cursor.moveToNext()) {
                                cart_id = cursor.getInt(cursor.getColumnIndex("id"));
                            }
                            String sql2 = "insert into line_items(Items_id,cart_id) values (9999," + cart_id + ")";
                            db.execSQL(sql2);
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
                break;
        }
    }

    private void sendOrderToServer() {
        int family_id = sp.getInt("family_id", 1);
        final Order order = new Order();
        order.setFamily_id(family_id);
        line_items_attributes lineitemsattributes = new line_items_attributes();
        lineitemsattributes.setItem_id(9999);
        lineitemsattributes.setQuantity(1);
        line_items_attributes.add(lineitemsattributes);
        order.setLine_items_attributes(line_items_attributes);
        order.setJail_id(jail_id);
        order.setCreated_at(times);
        Float f = Float.parseFloat(money);
        order.setAmount(f);
        gson = new Gson();
        apply = gson.toJson(order);
        final AA aa = new AA();
        aa.setOrder(order);
        final String str = gson.toJson(aa);

        new Thread() {
            @Override
            public void run() {
                String token = sp.getString("token", "");
                String url = Constants.URL_HEAD + "orders?jail_id=" + jail_id + "&access_token=";
                //       HttpClient httpClient = new DefaultHttpClient();
                //       HttpPost post = new HttpPost(url+token);
                String s = url + token;

                /**
                 StringEntity entity = new StringEntity(str);
                 entity.setContentType("application/json");
                 entity.setContentEncoding("UTF-8");
                 post.setEntity(entity);
                 HttpResponse response = httpClient.execute(post);
                 if (response.getStatusLine().getStatusCode() == 200){
                 String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                 }
                 }  catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
                 } catch (ClientProtocolException e) {
                 e.printStackTrace();
                 } catch (IOException e) {
                 e.printStackTrace();
                 }
                 **/
                Looper.prepare();
                Message msg = handler.obtainMessage();
                try {
                    String result = HttpRequestUtil.doHttpsPost(url + token, str);
                    Log.d("订单号", result);
                    if (result.contains("StatusCode is ")) {
                        msg.obj = "error";
                        msg.what = 2;
                        handler.sendMessage(msg);
                    } else {
                        msg.obj = "success";
                        Bundle bundle = new Bundle();
                        bundle.putString("result", result);
                        msg.setData(bundle);
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    msg.obj = "error";
                    msg.what = 2;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                } finally {
                    Looper.loop();
                }

            }
        }.start();
    }

    /**
     * 获取结果码
     * @param result
     * @return
     */
    private int getResultcode(String result) {
        int a = 0;
        try {
            JSONObject jsonObject = new JSONObject(result);
            a = jsonObject.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return a;
    }

    /**
     * 获取本地hostIP
     * @return
     */
    public String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip
                            .getHostAddress())) {
                        return ipaddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("feige", "获取本地ip地址失败");
            e.printStackTrace();
        }
        return ipaddress;
    }

    private class MyAdapter extends BaseExpandableListAdapter {

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
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.familyservice_item, null);
                viewHolder = new GroupViewHolder();
                viewHolder.image_click = (ImageView) convertView.findViewById(R.id.image_click);
                viewHolder.img_messge = (ImageView) convertView.findViewById(R.id.image_messge);
                viewHolder.tv_messge = (TextView) convertView.findViewById(R.id.tv_messge);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (GroupViewHolder) convertView.getTag();
            }
            if (isExpanded) {
                viewHolder.image_click.setImageResource(R.drawable.touchup);
            } else {
                viewHolder.image_click.setImageResource(R.drawable.touchdown);
            }
            viewHolder.img_messge.setImageResource(image_messge.get(groupPosition));
            viewHolder.tv_messge.setText(text_messge.get(groupPosition));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            if (groupPosition == 0) {
                convertView = View.inflate(getApplicationContext(), R.layout.sentence_change, null);
                ListView lv_sentence = (ListView)convertView.findViewById(R.id.lv_sentence_recod);
                SentenceAdapter adapter = new SentenceAdapter();
                 lv_sentence.setAdapter(adapter);
                ListViewParamsUtils.setListViewHeightBasedOnChildren(lv_sentence);
            } else if (groupPosition == 1) {
                convertView = View.inflate(getApplicationContext(), R.layout.consumption, null);
                  ListView lv_consumption = (ListView)convertView.findViewById(R.id.lv_consumption);
                ConsumptionAdapter adapter = new ConsumptionAdapter();
                   lv_consumption.setAdapter(adapter);
                 ListViewParamsUtils.setListViewHeightBasedOnChildren(lv_consumption);
            } else if (groupPosition == 2) {
                convertView = View.inflate(getApplicationContext(), R.layout.shoppingreceipt, null);
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

    private class GroupViewHolder {
        ImageView img_messge;
        TextView tv_messge;
        ImageView image_click;

    }


    private class SentenceAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 4;
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
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.sentence_change_item, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_sentence_time = (TextView) convertView.findViewById(R.id.tv_sentence_time);
                viewHolder.tv_sentence_case = (TextView) convertView.findViewById(R.id.tv_sentence_case);
                viewHolder.tv_sentence_add = (TextView) convertView.findViewById(R.id.tv_sentence_add);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                viewHolder.tv_sentence_time.setText("时间");
                viewHolder.tv_sentence_case.setText("原因");
                viewHolder.tv_sentence_add.setText("加/减刑");
            } else {
                viewHolder.tv_sentence_time.setText(sentence_time.get(position-1));
                viewHolder.tv_sentence_case.setText(sentence_cause.get(position-1));
                viewHolder.tv_sentence_add.setText(sentence_time_add.get(position-1));
            }
            return convertView;
        }

        private class ViewHolder {
            TextView tv_sentence_time;
            TextView tv_sentence_case;
            TextView tv_sentence_add;
        }
    }

    private class ConsumptionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 4;
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
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.sentence_change_item, null);
                viewHolder = new ViewHolder();
                viewHolder.buy_time = (TextView) convertView.findViewById(R.id.tv_sentence_time);
                viewHolder.buy_commodity = (TextView) convertView.findViewById(R.id.tv_sentence_case);
                viewHolder.buy_money = (TextView) convertView.findViewById(R.id.tv_sentence_add);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                viewHolder.buy_time.setText("购买时间");
                viewHolder.buy_commodity.setText("商品");
                viewHolder.buy_money.setText("金额");
            } else {
                viewHolder.buy_time.setText(sentence_time.get(position-1));
                viewHolder.buy_commodity.setText(commodity.get(position-1));
                viewHolder.buy_money.setText(money1.get(position-1));
            }
            return convertView;
        }

        private class ViewHolder {
            TextView buy_time;
            TextView buy_commodity;
            TextView buy_money;
        }
    }

    private class ReceiptAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 4;
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
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.shoppingreceipt_item, null);
                viewHolder = new ViewHolder();
                viewHolder.receipt = (ImageView) convertView.findViewById(R.id.image_receipt);
                viewHolder.qianshou = (TextView) convertView.findViewById(R.id.tv_qianshou);
                viewHolder.qianshou_time = (TextView) convertView.findViewById(R.id.tv_receipt_time);
                viewHolder.qianshou_id = (TextView) convertView.findViewById(R.id.tv_buy_id);
                viewHolder.qianshou_money = (TextView) convertView.findViewById(R.id.tv_money);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                viewHolder.qianshou.setText("确认签收");
                viewHolder.qianshou_time.setText("签收时间");
                viewHolder.qianshou_id.setText("购物ID");
                viewHolder.qianshou_money.setText("购物数值");
                viewHolder.receipt.setVisibility(View.GONE);
            } else {
                viewHolder.receipt.setVisibility(View.VISIBLE);
                viewHolder.qianshou_time.setText(sentence_time.get(position-1));
                viewHolder.qianshou_id.setText(buyer_id.get(position-1));
                viewHolder.qianshou_money.setText(money1.get(position-1));
            }
            return convertView;
        }

        private class ViewHolder {
            TextView qianshou_time;
            TextView qianshou_id;
            TextView qianshou_money;
            TextView qianshou;
            ImageView receipt;
        }
    }

    /**
     *
     * @param s
     * @return
     */
    private String getResultTradeno(String s) {
        String str = "";
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject jsonObject1 = jsonObject.getJSONObject("order");
            str = jsonObject1.getString("trade_no");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return str;
    }
}
