package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.AliPayConst;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.constant.WeixinConstants;
import com.gkzxhn.gkprison.userport.bean.PayResult;
import com.gkzxhn.gkprison.userport.requests.ApiRequest;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.RSAUtil;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.unionpay.UPPayAssistEx;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 选择支付方式
 */
public class PaymentActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "PaymentActivity";
    @BindView(R.id.lv_pay_way) ListView lv_pay_way;
    @BindView(R.id.bt_pay) Button bt_pay;
    @BindView(R.id.tv_count_money) TextView tv_count_money;
    private String countmoney;
    private String[] pay_ways = {"支付宝支付", "微信支付"};
    private int[] pay_way_icons = {R.drawable.pay_way_zhifubao, R.drawable.pay_way_weixin};
    private boolean[] ischeckeds = {true, false, false};
    private MyAdapter adapter;
    public static String TradeNo;
    public static String times;
    private IWXAPI api;
    private int cart_id;
    private int jail_id;
    private PayReq req = new PayReq();
    private SQLiteDatabase db = StringUtils.getSQLiteDB(this);
    private String saletype;
    private String bussinesstype;
    private SharedPreferences sp;
    private String token;
    private String payment_type = "";
    private String prepay_id = "";
    private String mode = "01";
    private String app_id = "";
    public static String mch_id = "";
    private ProgressDialog dialog;
    private String nonce_str = "";
    private String timeStamp = "";

    /**
     * 获取结果码
     * @param type
     * @return
     */
    private int getResultCode(String type) {
        int a = 0;
        try {
            JSONObject jsonObject = new JSONObject(type);
            a = jsonObject.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return a;
    }

    /**
     * 获取签名参数
     * @param result
     * @param key
     * @return
     */
    private String getSignedParams(String result, String key) {
        String t = "";
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject jsonObject1 = jsonObject.getJSONObject("signed_params");
            t = jsonObject1.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * 获取签名
     * @param type
     * @return
     */
    private String getSign(String type) {
        String t = "";
        try {
            JSONObject jsonObject = new JSONObject(type);
            t = jsonObject.getString("sign");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_payment, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("支付");
        setBackVisibility(View.VISIBLE);
        getData();// 获取intent以及sp数据
        tv_count_money.setText(countmoney);
        adapter = new MyAdapter();
        lv_pay_way.setAdapter(adapter);
        lv_pay_way.setOnItemClickListener(this);
        bt_pay.setOnClickListener(this);
    }

    /**
     * init and show
     */
    private void showProgressDialog() {
        dialog = new ProgressDialog(PaymentActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("");
        dialog.show();
    }

    /**
     * 获取intent以及sp中数据并赋值
     */
    private void getData() {
        TradeNo = getIntent().getStringExtra("TradeNo");
        countmoney = getIntent().getStringExtra("totalmoney");
        times = getIntent().getStringExtra("times");
        cart_id = getIntent().getIntExtra("cart_id", 0);
        saletype = getIntent().getStringExtra("saletype");
        bussinesstype = getIntent().getStringExtra("bussiness");
        sp = getSharedPreferences("config", MODE_PRIVATE);
        token = sp.getString("token", "");
        jail_id = sp.getInt("jail_id", 0);
    }

    /**
     * 发送支付类型
     */
    private void send_payment_type() {
        String str = "{\"trade_no\":\"" + TradeNo + "\",\"payment_type\":\"" + payment_type + "\"}";
        Log.i(TAG, str);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiRequest request = retrofit.create(ApiRequest.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), str);
        request.sendPaymentType(jail_id + "", token, body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "get payment type failed : " + e.getMessage());
                        showToastMsgShort(getString(R.string.select_payment_failed));
                        bt_pay.setEnabled(true);
                        dismissDialog();
                    }

                    @Override public void onNext(ResponseBody res) {
                        try {
                            String result = res.string();
                            Log.i(TAG,  "get payment type success : " + result);
                            int code = getResultCode(result);
                            if (code == 200) {
                                if (payment_type.equals("alipay")) {
                                    alipay();// 支付宝支付
                                } else if (payment_type.equals("weixin")) {
                                    setResultToField(result);// 赋值
                                    WXPay();// 微信支付
                                } else if (payment_type.equals("unionpay")) {
                                    String tn = "";
                                    UPPayAssistEx.startPay(PaymentActivity.this, null, null, tn, mode);
                                }
                            }else {
                                showToastMsgShort(getString(R.string.select_payment_failed));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i(TAG,  "get payment type exception : " + e.getMessage());
                            showToastMsgShort(getString(R.string.select_payment_failed));
                        }
                        bt_pay.setEnabled(true);
                        dismissDialog();
                    }
                });
    }

    /**
     * 微信支付给一些字段赋值
     * @param result
     */
    private void setResultToField(String result) {
        prepay_id = getSignedParams(result, "prepayId");
        app_id = getSignedParams(result, "appId");
        mch_id = getSignedParams(result, "partnerId");
        nonce_str = getSignedParams(result, "nonceStr");
        String sign = getSign(result);
        timeStamp = getSignedParams(result, "timeStamp");
    }

    /**
     * dismiss dialog
     */
    private void dismissDialog() {
        if (dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        for (int i = 0; i < ischeckeds.length; i++) {
            ischeckeds[i] = (i == position);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_pay:
                bt_pay.setEnabled(false);
                showProgressDialog();
                if (ischeckeds[0]) {
                    payment_type = "alipay";
                    send_payment_type();
                } else if (ischeckeds[1]) {
                    payment_type = "weixin";
                    send_payment_type();
                }
                break;
        }
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return pay_ways.length;
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
            if (convertView == null) {
                convertView = View.inflate(PaymentActivity.this, R.layout.pay_way_item, null);
                holder = new ViewHolder();
                holder.iv_pay_way_icon = (ImageView) convertView.findViewById(R.id.iv_pay_way_icon);
                holder.tv_pay_way = (TextView) convertView.findViewById(R.id.tv_pay_way);
                holder.cb_pay_way = (CheckBox) convertView.findViewById(R.id.cb_pay_way);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.iv_pay_way_icon.setImageResource(pay_way_icons[position]);
            holder.tv_pay_way.setText(pay_ways[position]);

            holder.cb_pay_way.setChecked(ischeckeds[position]);
            return convertView;
        }
    }

    private static class ViewHolder {
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
                        msg = "支付成功！"; // 验证通过后，显示支付结果
                    } else {
                        // 验证不通过后的处理
                        // 建议通过商户后台查询支付结果
                        msg = "支付失败！";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    /**
     * 支付宝支付
     */
    public void alipay() {
        if (TextUtils.isEmpty(AliPayConst.PARTNER) || TextUtils.isEmpty(AliPayConst.RSA_PRIVATE)
                || TextUtils.isEmpty(AliPayConst.SELLER)) {
            new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    PaymentActivity.this.finish();}}).show();
            return;
        }
        String orderInfo = AliPayConst.getOrderInfo("测试的商品", "该测试商品的详细描述", countmoney, TradeNo);
        String sign = AliPayConst.sign(orderInfo);// 对订单做RSA 签名
        Log.d(TAG, "jdksaj: " + sign);
        try {
            sign = URLEncoder.encode(sign, "UTF-8"); // 仅需对sign 做URL编码
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            showToastMsgShort("签名编码异常");
            return;
        }
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + AliPayConst.getSignType();// 完整的符合支付宝参数规范的订单信息
        dismissDialog();

        // 必须异步调用支付接口
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                PayTask alipay = new PayTask(PaymentActivity.this);// 构造PayTask 对象
                subscriber.onNext(alipay.pay(payInfo));// 调用支付接口，获取支付结果
            }
        }) .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<String>() {
                @Override
                public void call(String res) {
                    PayResult payResult = new PayResult(res);
                    bt_pay.setEnabled(true);
                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        String type = "支付宝";
                        String sql = "update Cart set isfinish = 1,payment_type = '" + type + "' where time = '" + times + "'";
                        db.execSQL(sql);
                        showToastMsgShort(getString(R.string.pay_success));
                        Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        PaymentActivity.this.startActivity(intent);
                        PaymentActivity.this.finish();
                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            showToastMsgShort(getString(R.string.pay_result_confirming));
                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            showToastMsgShort(getString(R.string.pay_failed));
                        }
                    }
                }
            });
    }

    /**
     * 微信支付
     */
    private void WXPay() {
        String mPackageValue = "Sign=WXPay";
        api = WXAPIFactory.createWXAPI(this, WeixinConstants.APP_ID, true);
        req.appId = app_id;
        req.nonceStr = nonce_str;
        req.packageValue = mPackageValue;
        req.partnerId = mch_id;
        req.prepayId = prepay_id;
        req.timeStamp = timeStamp;
        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        //signParams.add(new BasicNameValuePair("sign", sign));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
        req.sign = WeixinConstants.genAppSign(signParams);
        api.registerApp(WeixinConstants.APP_ID);
        api.sendReq(req);

        dismissDialog();
        bt_pay.setEnabled(true);
    }
}
