package com.gkzxhn.gkprison.activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * activity基类
 */
public abstract class BaseActivity extends SlidingFragmentActivity implements View.OnClickListener{

//    protected Context mContext;
    protected RelativeLayout rl_content;
    protected TextView tv_title;
    protected ImageView iv_back;
    protected RelativeLayout rl_back;
    protected TextView tv_messge;
    protected RelativeLayout rl_home_menu;
    protected ImageView iv_home_menu;
    protected View ly_title_bar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }
        setContentView(R.layout.activity_base);
        setBehindContentView(R.layout.menu_frame);
        getSlidingMenu().setSlidingEnabled(false);
//        mContext = getApplicationContext();
        ly_title_bar = findViewById(R.id.ly_title_bar);
        rl_content = (RelativeLayout) findViewById(R.id.rl_content);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        rl_back = (RelativeLayout) findViewById(R.id.rl_back);
        tv_messge = (TextView)findViewById(R.id.tv_messge);
        iv_home_menu = (ImageView) findViewById(R.id.iv_home_menu);
        rl_home_menu = (RelativeLayout) findViewById(R.id.rl_home_menu);
        View view = initView();
        rl_content.addView(view);
        initData();
        rl_back.setOnClickListener(this);
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
        Toast.makeText(this, pMsg, Toast.LENGTH_SHORT).show();
    }
    /**
     * 弹出toase 显示时长long
     * @param pMsg
     */
    protected void showToastMsgLong(String pMsg) {
        Toast.makeText(this, pMsg, Toast.LENGTH_LONG).show();
    }

    /**
     * 设置返回按钮是否可见
     * @param visibility
     */
    protected void setBackVisibility(int visibility){
        rl_back.setVisibility(visibility);
    }

    /**
     * 设置侧拉菜单按钮是否可见
     * @param visibility
     */
    protected void setMenuVisibility(int visibility){
        rl_home_menu.setVisibility(visibility);
    }

    protected void setActionBarGone(int visibility){
        ly_title_bar.setVisibility(visibility);
    }


    protected void setTextVisibility(int visibility){
        tv_messge.setVisibility(visibility);
    }
    @Override
    public void onClick(View v) {
        //子类选择性重写
        switch (v.getId()){
            case R.id.rl_back:
                finish();
                break;
        }
    }
}
