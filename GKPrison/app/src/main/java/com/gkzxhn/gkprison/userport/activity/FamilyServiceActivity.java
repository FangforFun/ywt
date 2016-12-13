package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.AA;
import com.gkzxhn.gkprison.userport.bean.Order;
import com.gkzxhn.gkprison.userport.bean.Prison;
import com.gkzxhn.gkprison.userport.bean.PrisonerInfo;
import com.gkzxhn.gkprison.userport.bean.line_items_attributes;
import com.gkzxhn.gkprison.userport.requests.ApiRequest;
import com.gkzxhn.gkprison.utils.ListViewParamsUtils;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 家属服务
 */
public class FamilyServiceActivity extends BaseActivity {
    private static final String TAG = "FamilyServiceActivity";
    @BindView(R.id.el_messge) ExpandableListView el_messge;
    @BindView(R.id.tv_prison_num) TextView prison_num;
    @BindView(R.id.tv_mail_sex) TextView prison_gender;
    @BindView(R.id.tv_crime_accent) TextView prison_crimes;
    @BindView(R.id.tv_sentence_time) TextView prison_start_time;
    @BindView(R.id.tv_sentence_time_end) TextView prison_end_time;
    private MyAdapter adapter;
    private String TradeNo;
    private String times = "";
    private SQLiteDatabase db = StringUtils.getSQLiteDB(this);
    private SharedPreferences sp;
    private String ip;
    private String money = "";
    private Gson gson;
    private List<line_items_attributes> line_items_attributes = new ArrayList<>();
    private int jail_id;

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
    private List<String> sentence_time = new ArrayList<String>(){
        {
            add("2016年5月30日");
            add("2016年5月20日");
            add("2016年5月10日");
        }
    };
    private List<String> sentence_cause = new ArrayList<String>(){
        {
            add("制止狱内暴力");
            add("制止狱内暴力");
            add("制止狱内暴力");
        }
    };
    private List<String> sentence_time_add = new ArrayList<String>(){
        {
            add("减刑三个月");
            add("减刑三个月");
            add("减刑三个月");
        }
    };
    private List<String> buyer_id = new ArrayList<String>(){
        {
            add("1232423423423");
            add("1232423423423");
            add("1232423423423");
        }
    };
    private List<String> money1 = new ArrayList<String>(){
        {
            add("120元");
            add("120元");
            add("120元");
        }
    };
    private List<String> commodity = new ArrayList<String>(){
        {
            add("水杯");
            add("水杯");
            add("水杯");
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_family_service, null);
        ButterKnife.bind(this, view);
        el_messge.setGroupIndicator(null);
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
        getPrisonInformation();
    }

    /**
     * 获取囚犯信息
     */
    private void getPrisonInformation() {
        if (Utils.isNetworkAvailable()) {
            String token = sp.getString("token", "");
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.URL_HEAD)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            ApiRequest request = retrofit.create(ApiRequest.class);
            request.getPrisonerInfo(token)
                    .map(new Func1<PrisonerInfo, Prison>() {
                        @Override
                        public Prison call(PrisonerInfo prisonerInfo) {
                            Log.i(TAG, prisonerInfo.toString());
                            return getPerson(prisonerInfo);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Prison>() {
                        @Override public void onCompleted() {}
                        @Override public void onError(Throwable e) {
                            showToastMsgShort("同步数据有误");
                        }

                        @Override public void onNext(Prison prison) {
                            if (prison != null) {
                                Log.i(TAG, prison.toString());
                                prison_num.setText(prison.getPrisoner_number());
                                if (prison.getGender().equals("m")) {
                                    prison_gender.setText("男");
                                } else {
                                    prison_gender.setText("女");
                                }
                                prison_crimes.setText(prison.getCrimes());
                                prison_start_time.setText(prison.getPrison_term_started_at());
                                prison_end_time.setText(prison.getPrison_term_ended_at());
                            }else {
                                showToastMsgShort("同步数据有误");
                            }
                        }
                    });
        } else {
            showToastMsgShort("没有网络");
        }
    }

    /**
     * 转换数据
     * @param prisonerInfo
     * @return
     */
    private Prison getPerson(PrisonerInfo prisonerInfo) {
        Prison prison = null;
        if (prisonerInfo.getCode() == 200) {
            prison = new Prison();
            prison.setGender(prisonerInfo.getPrisoner().getGender());
            prison.setCrimes(prisonerInfo.getPrisoner().getCrimes());
            prison.setPrison_term_ended_at(prisonerInfo.getPrisoner().getPrison_term_ended_at());
            prison.setPrison_term_started_at(prisonerInfo.getPrisoner().getPrison_term_started_at());
            prison.setPrisoner_number(prisonerInfo.getPrisoner().getPrisoner_number());
        }
        return prison;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rl_remittance:
                times = StringUtils.formatTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
                AlertDialog.Builder builder = new AlertDialog.Builder(FamilyServiceActivity.this);
                View view = View.inflate(FamilyServiceActivity.this, R.layout.remittance_dialog, null);
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
                            showToastMsgShort(getString(R.string.input_money));
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                                dialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            sendOrderToServer();// 发送至服务器
                            saveOrderRecord();// 保存汇款订单记录
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
                break;
        }
    }

    /**
     * 保存汇款订单记录至数据库
     */
    private void saveOrderRecord() {
        String sql = "insert into Cart(time,out_trade_no,isfinish,total_money,remittance) values('"
                + times + "','" + TradeNo + "',0,'" + money + "',1)";
        db.execSQL(sql);
        int cart_id = 0;
        String sql1 = "select id from Cart where time = '" + times + "'";
        Cursor cursor = db.rawQuery(sql1, null);
        while (cursor.moveToNext()) {
            cart_id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        String sql2 = "insert into line_items(Items_id,cart_id) values (9999," + cart_id + ")";
        db.execSQL(sql2);
        cursor.close();
    }

    /**
     * 发送汇款订单至服务器
     */
    private void sendOrderToServer() {
        String str = getOrderJsonStr();
        String token = sp.getString("token", "");
        Log.i(TAG, str + "-------" + token);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiRequest request = retrofit.create(ApiRequest.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), str);
        request.sendOrder(jail_id, token, body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        showToastMsgShort("操作失败，请稍后再试！");
                    }

                    @Override public void onNext(ResponseBody responseBody) {
                        try {
                            String result = responseBody.string();
                            Log.i(TAG, "send order result : " + result);
                            int pass_code = getResultcode(result);
                            if (pass_code == 200) {
                                TradeNo = getResultTradno(result);
                                selectPayment();// 选择支付方式
                            }else {
                                // 其他情况就是等于500  超出每月800额度
                                // {"code":500,"msg":"Create order failed","errors":{"order":["超出每月800元限额"]}}
                                showToastMsgLong(getString(R.string.out_￥_800));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            showToastMsgShort("操作失败，请稍后再试！");
                        }
                    }
                });
    }

    /**
     * 选择支付方式
     */
    private void selectPayment() {
        Intent intent = new Intent(FamilyServiceActivity.this, PaymentActivity.class);
        intent.putExtra("totalmoney", money);
        intent.putExtra("times", times);
        intent.putExtra("TradeNo", TradeNo);
        intent.putExtra("saletype", "汇款");
        startActivity(intent);
    }

    /**
     * 获取订单json字符串
     * @return
     */
    private String getOrderJsonStr() {
        int family_id = sp.getInt("family_id", 1);
        Order order = new Order();
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
        AA aa = new AA();
        aa.setOrder(order);
        return gson.toJson(aa);
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
    private String getResultTradno(String s) {
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
