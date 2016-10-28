package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.login.LoadingActivity;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.welcome.WelcomeActivity;
import com.jungly.gridpasswordview.GridPasswordView;
import com.keda.sky.app.PcAppStackManager;

/**
 * 输入app解锁密码
 */
public class InputPasswordActivity extends BaseActivity {

    private TextView tv_pwd_error;
    private GridPasswordView gpv_input_pwd;
    private SharedPreferences sp;
    private Handler handler = new Handler();

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(this, R.layout.activity_input_password, null);
        tv_pwd_error = (TextView) view.findViewById(R.id.tv_pwd_error);
        gpv_input_pwd = (GridPasswordView) view.findViewById(R.id.gpv_input_pwd);
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        setTitle("输入密码");
        setBackVisibility(View.VISIBLE);
        gpv_input_pwd.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {
                tv_pwd_error.setVisibility(View.GONE);
            }

            @Override
            public void onInputFinish(String psw) {
                if(sp.getString("app_password", "").equals(psw)){
//                    handler.postDelayed(go_to_next_task, 1000);
                    handler.post(go_to_next_task);
                }else {
                    tv_pwd_error.setVisibility(View.VISIBLE);
                    gpv_input_pwd.clearPassword();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }


    /**
     * 密码输入正确，下一步
     */
    private Runnable go_to_next_task = new Runnable() {
        @Override
        public void run() {
            Intent intent;
            if (sp.getBoolean("is_first", true)) {
                intent = new Intent(InputPasswordActivity.this, WelcomeActivity.class);
                startActivity(intent);
            } else {
                if (TextUtils.isEmpty(sp.getString("username", "")) || TextUtils.isEmpty(sp.getString("password", ""))) {
                    intent = new Intent(InputPasswordActivity.this, LoadingActivity.class);
                    startActivity(intent);
                } else {
                    if (sp.getBoolean("isCommonUser", true)) {
                        intent = new Intent(InputPasswordActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        intent = new Intent(InputPasswordActivity.this, DateMeetingListActivity.class);
                        startActivity(intent);
                    }
                }
            }
            InputPasswordActivity.this.finish();
        }
    };
}
