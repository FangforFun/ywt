package com.gkzxhn.gkprison.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

/**
 * activity基类
 */
public abstract class BaseActivity extends FragmentActivity {

    protected Context mContext;
    protected RelativeLayout rl_content;
    protected TextView tv_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_base);
        mContext = this;
        rl_content = (RelativeLayout) findViewById(R.id.rl_content);
        tv_title = (TextView) findViewById(R.id.tv_title);
        View view = initView();
        rl_content.addView(view);
        initData();
    }

    /**
     * view
     * @return
     */
    protected abstract View initView();

    /**
     * 填充数据
     */
    protected abstract void initData();

    /**
     * 设置标题
     * @param title
     */
    protected void setTitle(String title){
        tv_title.setText(title);
    }
}
