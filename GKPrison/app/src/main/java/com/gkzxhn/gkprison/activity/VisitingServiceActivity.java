package com.gkzxhn.gkprison.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gkzxhn.gkprison.R;

public class VisitingServiceActivity extends BaseActivity {


    @Override
    protected View initView() {
        View view = View.inflate(mContext,R.layout.activity_visiting_service,null);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("申请探监");
        setBackVisibility(View.VISIBLE);
    }
}
