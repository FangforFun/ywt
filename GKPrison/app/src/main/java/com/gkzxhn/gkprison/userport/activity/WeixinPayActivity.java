package com.gkzxhn.gkprison.userport.activity;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.constant.WeixinConstants;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.gkzxhn.gkprison.utils.Utils;
import com.netease.nim.uikit.common.util.string.MD5;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONObject;

public class WeixinPayActivity extends BaseActivity {
    private TextView tv_prisonname;
    private TextView tv_ordernum;
    private TextView tv_money;
    private TextView tv_receviale;
    private IWXAPI api;
    private Button btn_pay;
    private SharedPreferences sp;
    private String prisonname;
    private String prepay_id;
    private String countmoney;
    private String app_id;
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String timeStamp;
    private String packge;
    @Override
    protected View initView() {
        View view = View.inflate(this,R.layout.activity_weixin_pay,null);
        tv_prisonname = (TextView)view.findViewById(R.id.tv_pay_prison);
        tv_ordernum = (TextView)view.findViewById(R.id.tv_transnum);
        tv_money = (TextView)view.findViewById(R.id.tv_pay_money);
        tv_receviale = (TextView)view.findViewById(R.id.tv_recevialbe);
        btn_pay = (Button)view.findViewById(R.id.btn_pay);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("微信支付");
        setBackVisibility(View.VISIBLE);
        sp = getSharedPreferences("config", MODE_PRIVATE);
         long t = System.currentTimeMillis();
        long m = t/1000;
        Log.d("timeshow",m+"");
        timeStamp = getIntent().getStringExtra("timeStamp");
        packge = "Sign=WXPay";
        prisonname = sp.getString("prisonname", "");
        tv_prisonname.setText(prisonname);
        tv_receviale.setText(prisonname);
        prepay_id = getIntent().getStringExtra("prepay_id");
        tv_ordernum.setText(prepay_id);
        countmoney = getIntent().getStringExtra("price");
        tv_money.setText(countmoney);
        app_id = getIntent().getStringExtra("app_id");
        mch_id = getIntent().getStringExtra("mch_id");
        nonce_str = getIntent().getStringExtra("nonce_str");
        sign = getIntent().getStringExtra("sign");
        api = WXAPIFactory.createWXAPI(this, WeixinConstants.APP_ID, false);
        api.registerApp(WeixinConstants.APP_ID);
        /**
        String str = "appid="+app_id+"&noncestr="+nonce_str+"&package="+packge+"&partnerid="+mch_id+"&prepayid="+prepay_id+"&timestamp="+timeStamp+"";
        Log.d("sign",str);
        String key = "d75699d893882dea526ea05e9c7a4090";
        String pass = str+"&key="+key;
        String bb = MD5.getStringMD5(pass);
        sign = bb.toUpperCase();
         **/

        Log.d("sign",sign);
        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                PayReq req = new PayReq();
                //req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
                req.appId			= app_id;
                req.partnerId		= mch_id;
                req.prepayId		= prepay_id;
                req.nonceStr		= nonce_str;
                req.timeStamp		= timeStamp;
                req.packageValue	= packge;
                req.sign			= sign;
                req.extData			= "app data"; // optiona
                // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                api.sendReq(req);
                Log.i("zhifu", "-----=");
            }
        });
    }
}
