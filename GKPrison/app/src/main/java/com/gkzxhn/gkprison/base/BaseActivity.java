package com.gkzxhn.gkprison.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;

/**
 * activity基类
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener{

//    protected Context mContext;
    protected RelativeLayout rl_content;// title以下的内容view
    protected TextView tv_title;
    protected ImageView iv_back;// 返回
    protected RelativeLayout rl_back;// 返回
    protected RelativeLayout rl_remittance;// 汇款
    protected RelativeLayout rl_home_menu;// 菜单
    protected ImageView iv_home_menu;// 菜单
    protected View ly_title_bar;
    protected TextView tv_remittance;// 汇款
//    protected TextView tv_user_type;
    protected Toolbar tool_bar;
    protected RelativeLayout rl_message;// 消息
    protected RelativeLayout rl_refresh;// 刷新
    protected ImageView iv_refresh;// 刷新
    protected Button bt_logout;// 注销
    protected View view_red_point;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_base);
        ly_title_bar = findViewById(R.id.ly_title_bar);
        rl_content = (RelativeLayout) findViewById(R.id.rl_content);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        rl_back = (RelativeLayout) findViewById(R.id.rl_back);
        rl_remittance = (RelativeLayout)findViewById(R.id.rl_remittance);
        tv_remittance = (TextView)findViewById(R.id.tv_remittance);
        iv_home_menu = (ImageView) findViewById(R.id.iv_home_menu);
        rl_home_menu = (RelativeLayout) findViewById(R.id.rl_home_menu);
//        tv_user_type = (TextView) findViewById(R.id.tv_user_type);
        rl_message = (RelativeLayout) findViewById(R.id.rl_message);
        rl_refresh = (RelativeLayout) findViewById(R.id.rl_refresh);
        iv_refresh = (ImageView) findViewById(R.id.iv_refresh);
        tool_bar = (Toolbar) findViewById(R.id.tool_bar);
        bt_logout = (Button) findViewById(R.id.bt_logout);
        view_red_point = findViewById(R.id.view_red_point);
        View view = initView();
        rl_content.addView(view);
        initData();
        rl_back.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
    protected void setBackVisibility(int visibility) {
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

    protected void setTextContent(String s){
        tv_remittance.setText(s);
    }

    /**
     * 设置刷新图标是否可见
     * @param visibility
     */
    protected void setRefreshVisibility(int visibility){
        rl_refresh.setVisibility(visibility);
    }

    /**
     * 设置消息图标是否可见
     * @param visibility
     */
    protected void setMessageVisibility(int visibility){
        rl_message.setVisibility(visibility);
    }

    /**
     * 设置汇款是否可见
     * @param visibility
     */
    protected void setRemittanceVisibility(int visibility){
        rl_remittance.setVisibility(visibility);
    }

    /**
     * 设置注销是否可见
     * @param visibility
     */
    protected void setLogoutVisibility(int visibility){
        bt_logout.setVisibility(visibility);
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
