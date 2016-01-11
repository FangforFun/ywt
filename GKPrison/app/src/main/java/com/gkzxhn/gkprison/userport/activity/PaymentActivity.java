package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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
    private String[] pay_ways = {"银行卡支付", "支付宝支付", "微信支付"};
    private int[] pay_way_icons = {R.drawable.pay_way_bank_card,R.drawable.pay_way_zhifubao,R.drawable.pay_way_weixin};
    private boolean[] ischeckeds = {true, false, false};
    private MyAdapter adapter;
    private String TradeNo;
    private String apply = "";
    private String url = "http://10.93.1.10:3000/api/v1/orders";
    private String token = "cb21c49928249f05ae8e4075c6018ff0";
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
        setRemittanceVisibility(View.VISIBLE);
        setTextContent("完成");
        TradeNo = getIntent().getStringExtra("TradeNo");
        countmoney = getIntent().getStringExtra("totalmoney");
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

                     Intent intent = new Intent(PaymentActivity.this,BankPayActivity.class);
                     intent.putExtra("price",countmoney);
                     PaymentActivity.this.startActivity(intent);
                 }else if (ischeckeds[1] == true){

                     Intent intent = new Intent(PaymentActivity.this,ZhifubaoPayActivity.class);
                     intent.putExtra("price",countmoney);
                     intent.putExtra("outorderno",TradeNo);
                     PaymentActivity.this.startActivity(intent);
                 }else if (ischeckeds[2] == true){

                     Intent intent = new Intent(PaymentActivity.this,WeixinPayActivity.class);
                     intent.putExtra("price",countmoney);
                     PaymentActivity.this.startActivity(intent);
                 }
            }
        });
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



}
