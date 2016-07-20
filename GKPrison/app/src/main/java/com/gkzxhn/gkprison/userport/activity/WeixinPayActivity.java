package com.gkzxhn.gkprison.userport.activity;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.WeixinConstants;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private String nonce_str;
    private String sign;
    private String timeStamp;
    private String packge;
    StringBuffer sb;
    private String tradeno;
    private String times;
    private String mch_id;
    PayReq req = new PayReq();
    private Map<String,Object> jsonmap = new HashMap<String,Object>();
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
        sb = new StringBuffer();
        setTitle("微信支付");
        setBackVisibility(View.VISIBLE);
        tradeno = getIntent().getStringExtra("outorderno");
        sp = getSharedPreferences("config", MODE_PRIVATE);
        timeStamp = getIntent().getStringExtra("timeStamp");
        packge = "Sign=WXPay";
        prisonname = sp.getString("prisonname", "德山监狱");
        times = getIntent().getStringExtra("times");
        tv_prisonname.setText(prisonname);
        tv_receviale.setText(prisonname);
        prepay_id = getIntent().getStringExtra("prepay_id");
        tv_ordernum.setText(tradeno);
        countmoney = getIntent().getStringExtra("price");
        tv_money.setText(countmoney);
        app_id = getIntent().getStringExtra("app_id");
        mch_id = getIntent().getStringExtra("mch_id");
        nonce_str = getIntent().getStringExtra("nonce_str");
        sign = getIntent().getStringExtra("sign");
        api = WXAPIFactory.createWXAPI(this, WeixinConstants.APP_ID,true);
        req.appId			= app_id;
        req.nonceStr		= nonce_str;
        req.packageValue	= packge;
        req.partnerId		= mch_id;
        req.prepayId		= prepay_id;
        req.timeStamp		= timeStamp;


        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        //signParams.add(new BasicNameValuePair("sign", sign));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
        Log.d("jb",sign);
        req.sign = genAppSign(signParams);

/**
        String str = "appid="+app_id+"&noncestr="+nonce_str+"&package="+packge+"&partnerid="+mch_id+"&prepayid="+prepay_id+"&sign="+sign+"&timestamp="+timeStamp;
        Log.d("sign",str);
        String bb = MD5Utils.ecoder(str);
        req.sign = bb;

**/

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //req.appId = "wxf8b4f85f3a794e77";  // 测试用appId


                api.registerApp(WeixinConstants.APP_ID);
               // req.extData			= "app data"; // optiona
                // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                api.sendReq(req);
                Log.i("zhifu", "-----=");
            }
        });
    }
    private String genAppSign(List<NameValuePair> params) {


        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        Log.d("sa", sb.toString());
        sb.append("key=");
        sb.append("d75699d893882dea526ea05e9c7a4090");
        Log.d("dd",sb.toString());
        //  sb.append("sign str\n" + sb.toString() + "\n\n");
        String appSign = MD5Utils.ecoder(sb.toString()).toUpperCase();
        Log.d("orion1",appSign);
        return appSign;
    }


}
