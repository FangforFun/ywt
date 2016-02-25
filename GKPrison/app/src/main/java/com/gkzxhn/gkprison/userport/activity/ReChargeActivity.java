package com.gkzxhn.gkprison.userport.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

public class ReChargeActivity extends BaseActivity {
    private Button btn_recharge;
    private RadioButton five;
    private RadioButton twenty;
    private RadioButton fifty;
    private RadioButton hundred;
    private int rechargemoney = 0;
    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_re_charge,null);
        btn_recharge = (Button)view.findViewById(R.id.btn_recharge);
        five = (RadioButton)view.findViewById(R.id.rb_five);
        twenty = (RadioButton)view.findViewById(R.id.rb_twenty);
        fifty = (RadioButton)view.findViewById(R.id.rb_fifty);
        hundred = (RadioButton)view.findViewById(R.id.rb_hundred);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("充值");
        setBackVisibility(View.VISIBLE);
        if (five.isChecked()){
            rechargemoney = 5;
        }else if (twenty.isChecked()){
            rechargemoney = 20;
        }else if (fifty.isChecked()){
            rechargemoney = 50;
        }else if (hundred.isChecked()){
            rechargemoney = 100;
        }
        btn_recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastMsgShort(rechargemoney+"");
            }
        });
    }
}
