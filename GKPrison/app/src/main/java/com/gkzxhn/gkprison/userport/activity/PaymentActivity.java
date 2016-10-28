package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.constant.WeixinConstants;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.userport.bean.PayResult;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.gkzxhn.gkprison.utils.RSAUtil;
import com.gkzxhn.gkprison.utils.SignUtils;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.Utils;
import com.keda.sky.app.PcAppStackManager;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.unionpay.UPPayAssistEx;


import org.apache.http.NameValuePair;

import org.apache.http.message.BasicNameValuePair;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.LinkedList;
import java.util.List;

/**
 * 支付方式选择
 */
public class PaymentActivity extends BaseActivity {
    private ListView lv_pay_way;
    private Button bt_pay;
    private TextView tv_count_money;
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
    StringBuffer sb;
    PayReq req = new PayReq();
    private SQLiteDatabase db = StringUtils.getSQLiteDB(this);
    public static final String PARTNER = "2088121417397335";
    // 商户收款账号
    public static final String SELLER = "130146668@qq.com";
    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAL62E/1KH+LunsXU" +
            "9shOfFXhg6S7qd9e8b1TieCtVuZpK/maw69rv8oxoqTCXD/oUuuwszY7JIXVX8o0" +
            "vcnK30rxTM10i5YIk16VTvqvf5D3VKxYfDJlldkiaxlO79L5A8lg224jjAbjWkqm" +
            "kvIhaoAxXq1ythdBY1KujN9OJnjVAgMBAAECgYEAnihF35KvavVVOt9oYamlN1u0" +
            "XtM7B4GnnMlA2NEn9iFWVMPicQI8paQQK+77rgwvaEK7/MeDfHH95KVkl4rlLcMG" +
            "FoUUAgvrRFdS2Xv6RSaci3fvkai7MeHKQQ8j/+1dABjJcQF/OfMPHpCPrK4kxQ5Q" +
            "sCF132mjUpiwtpzV+ikCQQD1GO6Wx/fSV2+Ihaa9coPR57kI6xpDr48r9utUHVIw" +
            "w0sTblsGWwgs+to7SG10m6kOqb+vsCTayoFY1cQEA9vzAkEAxzHRrn1uOXzF+V/w" +
            "FcsQvZrC9oK4ed0Lanc36WiJf8a31X8w7N0PxXzQVHbatm3GrrII2q/0ASntxmfW" +
            "RxbyFwJBAOzHrk9KVhcF00Ev5Pqmc8TIORDtl80GAKm3fHchcHKdaJ0YAqXsMcTK" +
            "fyPAf8WkT7lTslR3NdOMyVLaCOjcFZMCQHRUsgJXmoHUTsJetxXjK/mvYmEY4qe4" +
            "4ivhSDP2KycGZOI4j9glGkrZo8lQSFb2MWxg6S7eR4BOfmC6z7dgvS0CQQDLrn0S" +
            "+lotPkFBMBCh38KVEEI9Pb71SkRL05kHR4+sesm4a4bh72mrMKbcCXYxJRPXwt7k" +
            "yQd7PvRoFL2mwp8f";
    private static final int SDK_PAY_FLAG = 2;
    private static final int SDK_CHECK_FLAG = 3;
    private String apply = "";
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
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String result = (String) msg.obj;
                    if (result.equals("error")) {
                        showToastMsgShort("选择支付类型失败");
                    } else if (result.equals("success")) {
                        Bundle bundle = msg.getData();
                        String type = bundle.getString("result");
                        int code = getResultCode(type);
                        Log.d("订单类型", payment_type);
                        if (code == 200) {
                            if (payment_type.equals("alipay")) {
                                /**
                                 Intent intent = new Intent(PaymentActivity.this,ZhifubaoPayActivity.class);
                                 intent.putExtra("price",countmoney);
                                 intent.putExtra("outorderno",TradeNo);
                                 intent.putExtra("times",times);
                                 intent.putExtra("cart_id",cart_id);
                                 intent.putExtra("saletype",saletype);
                                 intent.putExtra("bussiness", bussinesstype);
                                 // PaymentActivity.this.startActivity(intent);
                                 **/

                                alipay();
                            } else if (payment_type.equals("weixin")) {
                                // Intent intent = new Intent(PaymentActivity.this,WeixinPayActivity.class);
                                prepay_id = getPrepay_id(type);
                                // intent.putExtra("prepay_id",prepay_id);
                                app_id = getapp_id(type);
                                //   intent.putExtra("app_id",app_id);
                                mch_id = getmch_id(type);
                                //   intent.putExtra("mch_id",mch_id);
                                nonce_str = getnonce_str(type);
                                //   intent.putExtra("nonce_str", nonce_str);
                                String sign = getsign(type);
                                // intent.putExtra("sign",sign);
                                //  intent.putExtra("price", countmoney);
                                //   intent.putExtra("times",times);
                                //intent.putExtra("outorderno",TradeNo);
                                timeStamp = gettimestamp(type);
                                //PaymentActivity.this.startActivity(intent);
                                weixinPay();
                            } else if (payment_type.equals("unionpay")) {
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
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);
                    bt_pay.setEnabled(true);
                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();

                    String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        String type = "支付宝";
                        String sql = "update Cart set isfinish = 1,payment_type = '" + type + "' where time = '" + times + "'";
                        db.execSQL(sql);
                        Toast.makeText(PaymentActivity.this, "支付成功",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        PaymentActivity.this.startActivity(intent);
                        finish();
                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(PaymentActivity.this, "支付结果确认中",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(PaymentActivity.this, "支付失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
                case SDK_CHECK_FLAG: {
                    Toast.makeText(PaymentActivity.this, "检查结果为：" + msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

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

    private String getPrepay_id(String type) {
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

    private String getapp_id(String type) {
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

    private String getmch_id(String type) {
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

    private String getnonce_str(String type) {
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

    private String getsign(String type) {
        String t = "";
        try {
            JSONObject jsonObject = new JSONObject(type);
            t = jsonObject.getString("sign");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return t;
    }

    private String gettimestamp(String type) {
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
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(this, R.layout.activity_payment, null);
        lv_pay_way = (ListView) view.findViewById(R.id.lv_pay_way);
        bt_pay = (Button) view.findViewById(R.id.bt_pay);
        tv_count_money = (TextView) view.findViewById(R.id.tv_count_money);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("支付");
        setBackVisibility(View.VISIBLE);
        TradeNo = getIntent().getStringExtra("TradeNo");
        Log.d("订单号", TradeNo);
        countmoney = getIntent().getStringExtra("totalmoney");
        times = getIntent().getStringExtra("times");
        cart_id = getIntent().getIntExtra("cart_id", 0);
        saletype = getIntent().getStringExtra("saletype");
        bussinesstype = getIntent().getStringExtra("bussiness");
        sp = getSharedPreferences("config", MODE_PRIVATE);
        token = sp.getString("token", "");
        jail_id = sp.getInt("jail_id", 0);
        tv_count_money.setText(countmoney + "");
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
                bt_pay.setEnabled(false);
                dialog = new ProgressDialog(PaymentActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("");
                dialog.show();
                //if (ischeckeds[0] == true){
                //  payment_type = "unionpay";
                //  send_payment_type(payment_type);
                // }else
                if (ischeckeds[0] == true) {
                    payment_type = "alipay";
                    send_payment_type(payment_type);
                } else if (ischeckeds[1] == true) {
                    payment_type = "weixin";
                    send_payment_type(payment_type);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }


    private void send_payment_type(String payment_type) {
        final String str = "{\"trade_no\":\"" + TradeNo + "\",\"payment_type\":\"" + payment_type + "\"}";
        final String url = Constants.URL_HEAD + "prepay?jail_id=" + jail_id + "&access_token=";
        new Thread() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                try {
                    String result = HttpRequestUtil.doHttpsPost(url + token, str);
                    Log.d("支付类型返回", result);
                    if (result.contains("StatusCode is")) {
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
                }
            }
        }.start();
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


    /**
     * call alipay sdk pay. 调用SDK支付
     */
    public void alipay() {
        if (Utils.isFastClick()) {
            return;
        }
        if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE)
                || TextUtils.isEmpty(SELLER)) {
            new AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    //
                                    finish();
                                }
                            }).show();
            return;
        }
        // 订单

        String orderInfo = getOrderInfo("测试的商品", "该测试商品的详细描述", countmoney);

        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        Log.d("MainActivity", "jdksaj:" + sign);
        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + getSignType();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                // 构造PayTask 对象
                PayTask alipay = new PayTask(PaymentActivity.this);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * create the order info. 创建订单信息
     */
    public String getOrderInfo(String subject, String body, String price) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + TradeNo + "\"";


        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";


        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + "https://www.fushuile.com/api/v1/payment"
                + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";
        Log.d("MainActivity", "aaa:" + orderInfo);
        return orderInfo;
    }

    public String sign(String content) {
        //  Log.d("MainActivity","bbb:"+ SignUtils.sign("445", RSA_PRIVATE));
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }

    private void weixinPay() {
        String packge = "Sign=WXPay";
        api = WXAPIFactory.createWXAPI(this, WeixinConstants.APP_ID, true);
        req.appId = app_id;
        req.nonceStr = nonce_str;
        req.packageValue = packge;
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
        req.sign = genAppSign(signParams);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        api.registerApp(WeixinConstants.APP_ID);
        api.sendReq(req);
        bt_pay.setEnabled(true);
    }

    private String genAppSign(List<NameValuePair> params) {
        sb = new StringBuffer();
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        Log.d("sa", sb.toString());
        sb.append("key=");
        sb.append("d75699d893882dea526ea05e9c7a4090");
        Log.d("dd", sb.toString());
        //  sb.append("sign str\n" + sb.toString() + "\n\n");
        String appSign = MD5Utils.ecoder(sb.toString()).toUpperCase();
        Log.d("orion1", appSign);
        return appSign;
    }
}
