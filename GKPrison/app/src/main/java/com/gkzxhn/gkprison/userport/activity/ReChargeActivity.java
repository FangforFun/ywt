package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.userport.bean.AA;
import com.gkzxhn.gkprison.userport.bean.Order;
import com.gkzxhn.gkprison.userport.bean.line_items_attributes;
import com.gkzxhn.gkprison.userport.ui.main.pay.PaymentActivity;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;
import com.keda.sky.app.PcAppStackManager;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class ReChargeActivity extends BaseActivity {
    private Button btn_recharge;
    private RadioButton five;
    private RadioButton twenty;
    private RadioButton fifty;
    private RadioButton hundred;
    private String ip;
    private String money;
    private String TradeNo = "";
    private SharedPreferences sp;
    private List<line_items_attributes> line_items_attributes = new ArrayList<line_items_attributes>();
    private String times = "";
    private Gson gson;
    private String apply;
    private int jail_id;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String recharge = (String) msg.obj;
                    if (recharge.equals("error")) {
                        showToastMsgShort("上传数据失败");
                    } else if (recharge.equals("success")) {
                        Bundle bundle = msg.getData();
                        String code = bundle.getString("result");
                        TradeNo = getResultTradeno(code);
                        int a = getResultcode(code);
                        if (a == 200) {
                            Intent intent = new Intent(ReChargeActivity.this, PaymentActivity.class);
                            intent.putExtra("totalmoney", money);
                            intent.putExtra("times", times);
                            intent.putExtra("TradeNo", TradeNo);
                            intent.putExtra("saletype", "视频充值");
                            startActivity(intent);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(getApplicationContext(), R.layout.activity_re_charge, null);
        btn_recharge = (Button) view.findViewById(R.id.btn_recharge);
        five = (RadioButton) view.findViewById(R.id.rb_five);
        twenty = (RadioButton) view.findViewById(R.id.rb_twenty);
        fifty = (RadioButton) view.findViewById(R.id.rb_fifty);
        hundred = (RadioButton) view.findViewById(R.id.rb_hundred);
        return view;
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }


    @Override
    protected void initData() {
        setTitle("充值");
        setBackVisibility(View.VISIBLE);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        jail_id = sp.getInt("jail_id", 0);
        ip = getLocalHostIp();
        btn_recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isFastClick()) {
                    return;
                }
                if (five.isChecked()) {
                    money = "5";
                } else if (twenty.isChecked()) {
                    money = "20";
                } else if (fifty.isChecked()) {
                    money = "50";
                } else if (hundred.isChecked()) {
                    money = "100";
                } else {
                    showToastMsgShort("请选择充值金额");
                    return;
                }
                long time = System.currentTimeMillis();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = new Date(time);
                times = format.format(date);
                sendOrderToServer();
            }
        });
    }

    private void sendOrderToServer() {
        int family_id = sp.getInt("family_id", 1);
        final Order order = new Order();
        order.setFamily_id(family_id);
        line_items_attributes lineitemsattributes = new line_items_attributes();
        lineitemsattributes.setItem_id(9988);
        lineitemsattributes.setQuantity(1);
        line_items_attributes.add(lineitemsattributes);
        order.setLine_items_attributes(line_items_attributes);
        order.setJail_id(jail_id);
        order.setCreated_at(times);
        Float f = Float.parseFloat(money);
        order.setAmount(f);
        gson = new Gson();
        apply = gson.toJson(order);
        Log.d("成功", apply);
        final AA aa = new AA();
        aa.setOrder(order);
        final String str = gson.toJson(aa);

        new Thread() {
            @Override
            public void run() {
                String token = sp.getString("token", "");
                //       HttpClient httpClient = new DefaultHttpClient();
                //       HttpPost post = new HttpPost(url+token);
                String url = Constants.URL_HEAD + "orders?jail_id=" + jail_id + "&access_token=";
                String s = url + token;
                Looper.prepare();
                Message msg = handler.obtainMessage();
                try {
                    String result = HttpRequestUtil.doHttpsPost(url + token, str);
                    Log.d("订单号", result);
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
    }

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

}
