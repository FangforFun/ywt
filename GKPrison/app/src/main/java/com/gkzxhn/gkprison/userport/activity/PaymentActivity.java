package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.constant.WeixinConstants;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.utils.RSAUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.unionpay.UPPayAssistEx;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PaymentActivity extends BaseActivity {
    private ListView lv_pay_way;
    private Button bt_pay;
    private TextView tv_count_money;
    private String countmoney;
    private String[] pay_ways = {"银联支付", "支付宝支付","微信支付"};
    private int[] pay_way_icons = {R.drawable.pay_way_bank_card,R.drawable.pay_way_zhifubao,R.drawable.pay_way_weixin};
    private boolean[] ischeckeds = {true, false, false};
    private MyAdapter adapter;
    private String TradeNo;
    private String times;
    private int cart_id;
    private int jail_id;
    private String apply = "";
    private String saletype;
    private String bussinesstype;
    private SharedPreferences sp;
    private String token;
    private String payment_type = "";
    private String prepay_id = "";
    private  String mode = "01";
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String result = (String)msg.obj;
                    if (result.equals("error")){
                        showToastMsgShort("选择支付类型失败");
                    }else if (result.equals("success")){
                        Bundle bundle = msg.getData();
                        String type = bundle.getString("result");
                        int code = getResultcode(type);
                        Log.d("订单类型",payment_type);
                        if (code == 200){
                            if (payment_type.equals("alipay")){
                                Intent intent = new Intent(PaymentActivity.this,ZhifubaoPayActivity.class);
                                intent.putExtra("price",countmoney);
                                intent.putExtra("outorderno",TradeNo);
                                intent.putExtra("times",times);
                                intent.putExtra("cart_id",cart_id);
                                intent.putExtra("saletype",saletype);
                                intent.putExtra("bussiness",bussinesstype);
                                PaymentActivity.this.startActivity(intent);
                            }else if (payment_type.equals("weixin")){
                                Intent intent = new Intent(PaymentActivity.this,WeixinPayActivity.class);
                                String prepay_id = getPrepay_id(type);
                                intent.putExtra("prepay_id",prepay_id);
                                String app_id = getapp_id(type);
                                intent.putExtra("app_id",app_id);
                                String mch_id = getmch_id(type);
                                intent.putExtra("mch_id",mch_id);
                                String nonce_str = getnonce_str(type);
                                intent.putExtra("nonce_str", nonce_str);
                                String sign = getsign(type);
                                intent.putExtra("sign",sign);
                                intent.putExtra("price", countmoney);
                                intent.putExtra("times",times);
                                intent.putExtra("outorderno",TradeNo);
                                String t = gettimestamp(type);
                                intent.putExtra("timeStamp",t);
                                PaymentActivity.this.startActivity(intent);
                            }else if (payment_type.equals("unionpay")){
                                /**
                                Intent intent = new Intent(PaymentActivity.this,BankPayActivity.class);
                                intent.putExtra("price",countmoney);
                                PaymentActivity.this.startActivity(intent);
                                 **/

                                String tn = "";
                                UPPayAssistEx.startPay(PaymentActivity.this, null, null, tn, mode);
                            }
                        }

                    }
                    break;
            }
        }
    };

    private int getResultcode(String type) {
        int a = 0;
        try {
            JSONObject jsonObject = new JSONObject(type);
            a = jsonObject.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return a;
    }

    private String getPrepay_id(String type){
        String t = "";
        try {
            JSONObject jsonObject = new JSONObject(type);
            JSONObject jsonObject1 = jsonObject.getJSONObject("signed_params");
            t = jsonObject1.getString("prepayId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return t;
    }

    private  String getapp_id(String type){
        String t = "";
        try {
            JSONObject jsonObject = new JSONObject(type);
            JSONObject jsonObject1 = jsonObject.getJSONObject("signed_params");
            t = jsonObject1.getString("appId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return t;
    }

    private String getmch_id(String type){
        String t = "";
        try {
            JSONObject jsonObject = new JSONObject(type);
            JSONObject jsonObject1 = jsonObject.getJSONObject("signed_params");
            t = jsonObject1.getString("partnerId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return t;
    }

    private String getnonce_str(String type){
        String t = "";
        try {
            JSONObject jsonObject = new JSONObject(type);
            JSONObject jsonObject1 = jsonObject.getJSONObject("signed_params");
            t = jsonObject1.getString("nonceStr");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return t;
    }

    private String getsign(String type){
        String t = "";
        try {
            JSONObject jsonObject = new JSONObject(type);
            t = jsonObject.getString("sign");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return t;
    }
    private String gettimestamp(String type){
        String t = "";
        try {
            JSONObject jsonObject = new JSONObject(type);
            JSONObject jsonObject1 = jsonObject.getJSONObject("signed_params");
            t = jsonObject1.getString("timeStamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return t;
    }
    @Override
    protected View initView() {
        View view =View.inflate(this,R.layout.activity_payment,null);
        lv_pay_way = (ListView) view.findViewById(R.id.lv_pay_way);
        bt_pay = (Button) view.findViewById(R.id.bt_pay);
        tv_count_money = (TextView)view.findViewById(R.id.tv_count_money);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("支付");
        setBackVisibility(View.VISIBLE);
        TradeNo = getIntent().getStringExtra("TradeNo");
        Log.d("订单号",TradeNo);
        countmoney = getIntent().getStringExtra("totalmoney");
        times = getIntent().getStringExtra("times");
        cart_id = getIntent().getIntExtra("cart_id", 0);
        saletype = getIntent().getStringExtra("saletype");
        bussinesstype = getIntent().getStringExtra("bussiness");
        sp = getSharedPreferences("config", MODE_PRIVATE);
        token =sp.getString("token", "");
        jail_id = sp.getInt("jail_id",0);
        tv_count_money.setText(countmoney+"");
        adapter = new MyAdapter();
        lv_pay_way.setAdapter(adapter);
        lv_pay_way.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < ischeckeds.length; i++) {
                    if (i == position) {
                        ischeckeds[i] = true;
                    } else {
                        ischeckeds[i] = false;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        bt_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if (ischeckeds[0] == true){
                     payment_type = "unionpay";
                     send_payment_type(payment_type);

                 }else if (ischeckeds[1] == true){
                     payment_type = "alipay";
                     send_payment_type(payment_type);
                 }else if (ischeckeds[2] == true){
                     payment_type = "weixin";
                     send_payment_type(payment_type);
                 }
            }
        });
    }

    private void send_payment_type(String payment_type) {
        final String str = "{\"trade_no\":\""+TradeNo+"\",\"payment_type\":\"" + payment_type + "\"}";
        final String url = Constants.URL_HEAD +"prepay?jail_id="+jail_id+"&access_token=";
        new Thread(){
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                try {
                    String result = HttpRequestUtil.doHttpsPost(url + token,str);
                    Log.d("支付类型返回",result);
                    if (result.contains("StatusCode is")){
                        msg.obj = "error";
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }else {
                        msg.obj = "success";
                        Bundle bundle = new Bundle();
                        bundle.putString("result",result);
                        msg.setData(bundle);
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    msg.obj = "error";
                    msg.what = 1;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 3;
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
            ViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(PaymentActivity.this, R.layout.pay_way_item, null);
                holder = new ViewHolder();
                holder.iv_pay_way_icon = (ImageView) convertView.findViewById(R.id.iv_pay_way_icon);
                holder.tv_pay_way = (TextView) convertView.findViewById(R.id.tv_pay_way);
               holder.cb_pay_way = (CheckBox) convertView.findViewById(R.id.cb_pay_way);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.iv_pay_way_icon.setImageResource(pay_way_icons[position]);
            holder.tv_pay_way.setText(pay_ways[position]);

            holder.cb_pay_way.setChecked(ischeckeds[position]);
            return convertView;
        }
    }

    private static class ViewHolder{
        ImageView iv_pay_way_icon;
        TextView tv_pay_way;
        CheckBox cb_pay_way;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*************************************************
         * 步骤3：处理银联手机支付控件返回的支付结果
         ************************************************/
        if (data == null) {
            return;
        }

        String msg = "";
        /*
         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
         */
        String str = data.getExtras().getString("pay_result");
        if (str.equalsIgnoreCase("success")) {
            // 支付成功后，extra中如果存在result_data，取出校验
            // result_data结构见c）result_data参数说明
            if (data.hasExtra("result_data")) {
                String result = data.getExtras().getString("result_data");
                try {
                    JSONObject resultJson = new JSONObject(result);
                    String sign = resultJson.getString("sign");
                    String dataOrg = resultJson.getString("data");
                    // 验签证书同后台验签证书
                    // 此处的verify，商户需送去商户后台做验签
                    boolean ret = RSAUtil.verify(dataOrg, sign, mode);
                    if (ret) {
                        // 验证通过后，显示支付结果
                        msg = "支付成功！";
                    } else {
                        // 验证不通过后的处理
                        // 建议通过商户后台查询支付结果
                        msg = "支付失败！";
                    }
                } catch (JSONException e) {
                }
            } else {
                // 未收到签名信息
                // 建议通过商户后台查询支付结果
                msg = "支付成功！";
            }
        } else if (str.equalsIgnoreCase("fail")) {
            msg = "支付失败！";
        } else if (str.equalsIgnoreCase("cancel")) {
            msg = "用户取消了支付";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("支付结果通知");
        builder.setMessage(msg);
        builder.setInverseBackgroundForced(true);
        // builder.setCustomTitle();
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
