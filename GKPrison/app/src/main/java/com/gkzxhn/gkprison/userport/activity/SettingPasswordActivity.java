package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.jungly.gridpasswordview.GridPasswordView;

/**
 * 设置密码
 */
public class SettingPasswordActivity extends BaseActivity {

    private TextView tv_please_input_pwd;
    private GridPasswordView gpv_pwd;
    private GridPasswordView gpv_confirm_pwd;
    private String pwd;
    private String confirm_pwd;
    private AlertDialog dialog;
    private SharedPreferences sp;
    private Handler handler = new Handler();
    private TextView tv_not_match_pwd;
    private GridPasswordView gpv_cancel_pwd;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_setting_password, null);
        tv_please_input_pwd = (TextView) view.findViewById(R.id.tv_please_input_pwd);
        gpv_pwd = (GridPasswordView) view.findViewById(R.id.gpv_pwd);
        gpv_confirm_pwd = (GridPasswordView) view.findViewById(R.id.gpv_confirm_pwd);
        tv_not_match_pwd = (TextView) view.findViewById(R.id.tv_not_match_pwd);
        gpv_cancel_pwd = (GridPasswordView) view.findViewById(R.id.gpv_cancel_pwd);
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        setTitle("设置密码");
        setBackVisibility(View.VISIBLE);
        String type = getIntent().getStringExtra("type");
        if(type.equals("close")){
            gpv_cancel_pwd.setVisibility(View.VISIBLE);
            gpv_pwd.setVisibility(View.GONE);
            gpv_confirm_pwd.setVisibility(View.GONE);
        }
        gpv_cancel_pwd.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {

            }

            @Override
            public void onInputFinish(String psw) {
                if(psw.equals(sp.getString("app_password", ""))){
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("app_password", "canceled");
                    editor.putBoolean("isLock", false);
                    editor.commit();
                    handler.postDelayed(show_dialog_task, 500);
                }else {
                    tv_not_match_pwd.setVisibility(View.VISIBLE);
                    tv_not_match_pwd.setText("密码错误，请重新输入");
                    gpv_cancel_pwd.clearPassword();
                }
            }
        });
        gpv_pwd.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {
                tv_not_match_pwd.setVisibility(View.GONE);
            }

            @Override
            public void onInputFinish(String psw) {
                pwd = psw;
                handler.postDelayed(delay_dismiss_pwd, 500);
            }
        });
        gpv_confirm_pwd.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {

            }

            @Override
            public void onInputFinish(String psw) {
                confirm_pwd = psw;
                if(pwd.equals(confirm_pwd)){
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("isLock", true);
                    editor.putString("app_password", pwd);
                    editor.commit();
                    handler.postDelayed(show_dialog_task, 500);
                }else {
                    handler.postDelayed(delay_dismiss_confirm_pwd, 1000);
                }
            }
        });
    }

    /**
     * 密码不匹配时延时隐藏确认输入密码
     */
    private Runnable delay_dismiss_confirm_pwd = new Runnable() {
        @Override
        public void run() {
            gpv_confirm_pwd.clearPassword();
            gpv_confirm_pwd.setVisibility(View.GONE);
            gpv_pwd.clearPassword();
            gpv_pwd.setVisibility(View.VISIBLE);
            tv_please_input_pwd.setText("输入密码");
            tv_not_match_pwd.setVisibility(View.VISIBLE);
        }
    };

    /**
     * 延时隐藏输入密码
     */
    private Runnable delay_dismiss_pwd = new Runnable() {
        @Override
        public void run() {
            gpv_pwd.setVisibility(View.GONE);
            gpv_confirm_pwd.setVisibility(View.VISIBLE);
            tv_please_input_pwd.setText("确认密码");
        }
    };

    /**
     * 提示对话框
     */
    private Runnable show_dialog_task = new Runnable() {
        @Override
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingPasswordActivity.this);
            builder.setMessage("设置成功");
            builder.setCancelable(false);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    SettingPasswordActivity.this.finish();
                }
            });
            dialog = builder.create();
            dialog.show();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(dialog != null && dialog.isShowing()){
                return false;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
