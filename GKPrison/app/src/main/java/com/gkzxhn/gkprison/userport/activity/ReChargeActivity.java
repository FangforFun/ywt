package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.AA;
import com.gkzxhn.gkprison.userport.bean.Order;
import com.gkzxhn.gkprison.userport.bean.line_items_attributes;
import com.gkzxhn.gkprison.userport.requests.ApiRequest;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
import rx.schedulers.Schedulers;

public class ReChargeActivity extends BaseActivity {

    private static final String TAG = "ReChargeActivity";
    @BindView(R.id.btn_recharge) Button btn_recharge;
    @BindView(R.id.rb_five) RadioButton five;
    @BindView(R.id.rb_twenty) RadioButton twenty;
    @BindView(R.id.rb_fifty) RadioButton fifty;
    @BindView(R.id.rb_hundred) RadioButton hundred;
    private String ip;
    private String money;
    private String TradeNo = "";
    private SharedPreferences sp;
    private List<line_items_attributes> line_items_attributes = new ArrayList<line_items_attributes>();
    private String times = "";
    private Gson gson;
    private String apply;
    private int jail_id;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_re_charge, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.recharge));
        setBackVisibility(View.VISIBLE);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        jail_id = sp.getInt("jail_id", 0);
        ip = getLocalHostIp();
        btn_recharge.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.btn_recharge:
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
                times = StringUtils.formatTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
                sendOrderToServer();
                break;
        }
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
                                TradeNo = getResultTradeno(result);
                                selectPayment();// 选择支付方式
                            }else {
                                // 其他情况就是等于500  超出每月800额度
                                // {"code":500,"msg":"Create order failed","errors":{"order":["超出每月800元限额"]}}
                                showToastMsgShort("上传数据失败");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            showToastMsgShort("上传数据失败");
                        }
                    }
                });
    }


    /**
     * 选择支付方式
     */
    private void selectPayment() {
        Intent intent = new Intent(ReChargeActivity.this, PaymentActivity.class);
        intent.putExtra("totalmoney", money);
        intent.putExtra("times", times);
        intent.putExtra("TradeNo", TradeNo);
        intent.putExtra("saletype", "视频充值");
        startActivity(intent);
    }

    /**
     * 获取汇款订单json字符串
     * @return
     */
    private String getOrderJsonStr() {
        int family_id = sp.getInt("family_id", 1);
        Order order = new Order();
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
        Log.d(TAG, apply);
        AA aa = new AA();
        aa.setOrder(order);
        return gson.toJson(aa);
    }

    /**
     * 获取本地ip
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
