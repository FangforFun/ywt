package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.bean.PayResult;
import com.gkzxhn.gkprison.utils.SignUtils;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.Utils;
import com.keda.sky.app.PcAppStackManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ZhifubaoPayActivity extends FragmentActivity {

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
    // 支付宝公钥
    public static final String RSA_PUBLIC = "";
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_CHECK_FLAG = 2;
    private int cart_id;
    private String times;
    private ListView lv_commidty;
    private PayAdapter adapter;
    private String orderkey = "";
    private String countmoney = "";
    private String getOutTradeNo = "";
    private TextView  totalmoney;
    private String saletype;
    private TextView tv_saletype;
    private String bussinesstype;
    private TextView tv_wuliu;
    private List<Commodity> commodities = new ArrayList<Commodity>();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);

                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();

                    String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        String type = "支付宝";
                        String sql = "update Cart set isfinish = 1,payment_type = '"+type+"' where time = '"+times+"'";
                        db.execSQL(sql);
                        Toast.makeText(ZhifubaoPayActivity.this, "支付成功",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ZhifubaoPayActivity.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        ZhifubaoPayActivity.this.startActivity(intent);
                        finish();




                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(ZhifubaoPayActivity.this, "支付结果确认中",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(ZhifubaoPayActivity.this, "支付失败",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                    break;
                }
                case SDK_CHECK_FLAG: {
                    Toast.makeText(ZhifubaoPayActivity.this, "检查结果为：" + msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PcAppStackManager.Instance().pushActivity(this);
        setContentView(R.layout.activity_zhifubao_pay);
        totalmoney = (TextView)findViewById(R.id.tv_count);
        countmoney = getIntent().getStringExtra("price");
        lv_commidty = (ListView)findViewById(R.id.lv_commidty);
        getOutTradeNo = getIntent().getStringExtra("outorderno");
        times = getIntent().getStringExtra("times");
        cart_id = getIntent().getIntExtra("cart_id", 0);
        saletype = getIntent().getStringExtra("saletype");
        bussinesstype = getIntent().getStringExtra("bussiness");
        tv_wuliu = (TextView)findViewById(R.id.tv_wuliu);
        tv_wuliu.setText(bussinesstype);
        tv_saletype = (TextView)findViewById(R.id.tv_sales_type);
        tv_saletype.setText(saletype);
        totalmoney.setText(countmoney);
        initDate();
        adapter = new PayAdapter();
        lv_commidty.setAdapter(adapter);
    }

    private void initDate() {
        String sql = "select distinct line_items.qty,Items.price,Items.title from line_items,Items,Cart where line_items.Items_id = Items.id and line_items.cart_id = "+cart_id;
        Cursor cursor = db.rawQuery(sql,null);
        while (cursor.moveToNext()){
            Commodity commodity = new Commodity();
            commodity.setQty(cursor.getInt(cursor.getColumnIndex("qty")));
            commodity.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            commodity.setPrice(cursor.getString(cursor.getColumnIndex("price")));
            commodities.add(commodity);
        }
        cursor.close();
    }


    /**
     * call alipay sdk pay. 调用SDK支付
     *
     */
    public void pay(View v) {
        if(Utils.isFastClick()){
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

        String orderInfo = getOrderInfo("测试的商品", "该测试商品的详细描述",countmoney );

        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        Log.d("MainActivity","jdksaj:"+sign);
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
                // 构造PayTask 对象
                PayTask alipay = new PayTask(ZhifubaoPayActivity.this);
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
    public void check(View v) {
        Runnable checkRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask payTask = new PayTask(ZhifubaoPayActivity.this);
                // 调用查询接口，获取查询结果
                boolean isExist = payTask.checkAccountIfExist();

                Letter msg = new Letter();
                msg.what = SDK_CHECK_FLAG;
                msg.obj = isExist;
                handler.sendMessage(msg);
            }
        };

        Thread checkThread = new Thread(checkRunnable);
        checkThread.start();

    }
 **/

    /**
     * get the sdk version. 获取SDK版本号
     *
     */
    public void getSDKVersion() {
        PayTask payTask = new PayTask(this);
        String version = payTask.getVersion();
        Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
    }


    /**
     * create the order info. 创建订单信息
     *
     */
    public String getOrderInfo(String subject, String body, String price) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo + "\"";


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
        Log.d("MainActivity","aaa:"+orderInfo);
        return orderInfo;

    }
    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     *
     */
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
    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content
     *            待签名订单信息
     */
    public String sign(String content) {
      //  Log.d("MainActivity","bbb:"+ SignUtils.sign("445", RSA_PRIVATE));
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     *
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }

    private class PayAdapter extends BaseAdapter{
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
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null){
                convertView = View.inflate(getApplicationContext(),R.layout.recoding_items,null);
                holder = new Holder();
                holder.title = (TextView)convertView.findViewById(R.id.tv_shopping_desciption);
                holder.price = (TextView)convertView.findViewById(R.id.tv_shopping_mongey);
                holder.qty = (TextView)convertView.findViewById(R.id.tv_shopping_qty);
                convertView.setTag(holder);
            }else {
                holder = (Holder)convertView.getTag();
            }
            holder.title.setText(commodities.get(position).getTitle());
            holder.price.setText(commodities.get(position).getPrice());
            holder.qty.setText(commodities.get(position).getQty()+"");
            return convertView;
        }

        private class Holder{
            TextView title;
            TextView price;
            TextView qty;
        }
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }

}
