package com.gkzxhn.gkprison.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.zxing.CaptureActivity;

public class AgreementActivity extends BaseActivity {


    @Override
    protected View initView() {
        View view = View.inflate(mContext,R.layout.activity_agreement,null);
        return view;
    }

    @Override
    protected void initData() {
        setActionBarGone(View.GONE);

    }
    public void next(View view){
        Intent intent = new Intent(mContext, CaptureActivity.class);
        mContext.startActivity(intent);
        finish();
    }
}
