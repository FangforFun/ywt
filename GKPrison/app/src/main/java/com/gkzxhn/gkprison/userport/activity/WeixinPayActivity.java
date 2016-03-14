package com.gkzxhn.gkprison.userport.activity;

import android.view.View;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.tencent.mm.sdk.openapi.IWXAPI;

public class WeixinPayActivity extends BaseActivity {
    private TextView tv_prisonname;
    private TextView tv_ordernum;
    private TextView tv_money;
    private TextView tv_receviale;
    private IWXAPI api;

    @Override
    protected View initView() {
        View view = View.inflate(this,R.layout.activity_weixin_pay,null);
        tv_prisonname = (TextView)view.findViewById(R.id.tv_pay_prison);
        tv_ordernum = (TextView)view.findViewById(R.id.tv_transnum);
        tv_money = (TextView)view.findViewById(R.id.tv_pay_money);
        tv_receviale = (TextView)view.findViewById(R.id.tv_recevialbe);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("微信支付");
        setBackVisibility(View.VISIBLE);


    }
}
