package com.gkzxhn.gkprison.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.zxing.CaptureActivity;

public class AgreementActivity extends BaseActivity {

    private Button btn_next;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_agreement,null);
        btn_next = (Button) view.findViewById(R.id.btn_next);
        return view;
    }

    @Override
    protected void initData() {
        setActionBarGone(View.GONE);
        btn_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.btn_next:
                Intent intent = new Intent(this, CaptureActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
