package com.gkzxhn.gkprison.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;

/**
 * activity基类
 */
public abstract class BaseActivity extends FragmentActivity implements View.OnClickListener{

    protected Context mContext;
    protected RelativeLayout rl_content;
    protected TextView tv_title;
    protected ImageView iv_back;
    protected ImageView iv_messge;
    protected TextView tv_messge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_base);
        mContext = this;
        rl_content = (RelativeLayout) findViewById(R.id.rl_content);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_messge = (ImageView)findViewById(R.id.iv_messge);
        tv_messge = (TextView)findViewById(R.id.tv_messge);
        View view = initView();
        rl_content.addView(view);
        initData();
        iv_back.setOnClickListener(this);
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

    /**
     * 弹出toast 显示时长short
     * @param pMsg
     */
    protected void showToastMsgShort(String pMsg) {
        Toast.makeText(mContext, pMsg, Toast.LENGTH_SHORT).show();
    }
    /**
     * 弹出toase 显示时长long
     * @param pMsg
     */
    protected void showToastMsgLong(String pMsg) {
        Toast.makeText(mContext, pMsg, Toast.LENGTH_LONG).show();
    }

    /**
     * 设置返回按钮是否可见
     * @param visibility
     */
    protected void setBackVisibility(int visibility){
        iv_back.setVisibility(visibility);
    }

    protected void setImageVisibility(int visibility){
        iv_messge.setVisibility(visibility);
    }

    protected void setTextVisibility(int visibility){
        tv_messge.setVisibility(visibility);
    }
    @Override
    public void onClick(View v) {
        //子类选择性重写
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
        }
    }
}
