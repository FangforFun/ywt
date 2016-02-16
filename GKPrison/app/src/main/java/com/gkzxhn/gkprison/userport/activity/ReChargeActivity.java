package com.gkzxhn.gkprison.userport.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

public class ReChargeActivity extends BaseActivity {
    private Button btn_recharge;
    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_re_charge,null);
        btn_recharge = (Button)view.findViewById(R.id.btn_recharge);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("充值");
        setBackVisibility(View.VISIBLE);
        btn_recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
