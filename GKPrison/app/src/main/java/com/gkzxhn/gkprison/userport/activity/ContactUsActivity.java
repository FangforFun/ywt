package com.gkzxhn.gkprison.userport.activity;

import android.view.View;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

/**
 * created by huangzhengneng on 2016/1/28
 * 联系我们页面
 */
public class ContactUsActivity extends BaseActivity {

    private TextView tv_address;
    private TextView tv_zip_code;
    private TextView tv_tell;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_contact_us, null);
        tv_address = (TextView) view.findViewById(R.id.tv_address);
        tv_zip_code = (TextView) view.findViewById(R.id.tv_zip_code);
        tv_tell = (TextView) view.findViewById(R.id.tv_tell);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("联系我们");
        setBackVisibility(View.VISIBLE);
    }
}
