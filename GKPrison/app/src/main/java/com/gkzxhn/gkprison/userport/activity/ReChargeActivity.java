package com.gkzxhn.gkprison.userport.activity;

import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

public class ReChargeActivity extends BaseActivity {


    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_re_charge,null);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("充值");
        setBackVisibility(View.VISIBLE);
    }
}
