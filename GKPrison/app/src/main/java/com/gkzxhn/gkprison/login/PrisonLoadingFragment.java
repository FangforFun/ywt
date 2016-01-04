package com.gkzxhn.gkprison.login;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.DemoCache;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.gkzxhn.gkprison.utils.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

/**
 * A simple {@link Fragment} subclass.
 * 监狱用户登录界面
 */
public class PrisonLoadingFragment extends BaseFragment {

    private ActionProcessButton btn_login;
    private EditText et_username;
    private EditText et_password;
    private String username;
    private String password;
    private SharedPreferences sp;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:// 云信登录成功
                    btn_login.setProgress(0);
                    btn_login.setText("登录成功");
                    btn_login.setEnabled(true);
                    btn_login.setClickable(true);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.putBoolean("isCommonUser", false);
                    editor.commit();
                    DemoCache.setAccount(username);// 设置云信id缓存
                    Intent intent = new Intent(context, DateMeetingListActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    break;
                case 1:// 云信id登录失败
                    btn_login.setProgress(0);
                    btn_login.setText("登录失败");
                    btn_login.setEnabled(true);
                    btn_login.setClickable(true);
                    break;
            }
        }
    };

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_prison_loading, null);
        btn_login = (ActionProcessButton) view.findViewById(R.id.btn_login);
        et_username = (EditText) view.findViewById(R.id.et_username);
        et_password = (EditText) view.findViewById(R.id.et_password);
        return view;
    }

    @Override
    protected void initData() {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.isNetworkAvailable()){
                    showToastMsgShort("网络不可用，请检查网络设置");
                    return;
                }
                username = et_username.getText().toString().trim();
                password = et_password.getText().toString().trim();
                if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
                    Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    btn_login.setEnabled(false);
                    btn_login.setClickable(false);
                    btn_login.setMode(ActionProcessButton.Mode.ENDLESS);
                    btn_login.setProgress(1);
                    btn_login.setText("正在登录...");
                    LoginInfo info = new LoginInfo(username, tokenFromPassword(password)); // config...
                    final RequestCallback<LoginInfo> callback =
                            new RequestCallback<LoginInfo>() {
                                @Override
                                public void onSuccess(LoginInfo loginInfo) {
                                    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
                                    handler.sendEmptyMessage(0);
                                    Looper.loop();
                                }

                                @Override
                                public void onFailed(int i) {
                                    switch (i){
                                        case 302:
                                            Toast.makeText(context, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 503:
                                            Toast.makeText(context, "服务器繁忙", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 415:
                                            Toast.makeText(context, "网络出错，请检查网络", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 408:
                                            Toast.makeText(context, "请求超时，请稍后再试", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 403:
                                            Toast.makeText(context, "非法操作或没有权限", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 200:
                                            Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 422:
                                            Toast.makeText(context, "您的账号已被禁用", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 500:
                                            Toast.makeText(context, "服务器错误", Toast.LENGTH_SHORT).show();
                                            break;
                                        default:
                                            Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                    handler.sendEmptyMessage(1);
                                    Looper.loop();
                                }

                                @Override
                                public void onException(Throwable throwable) {
                                    Toast.makeText(context, "登录失败2", Toast.LENGTH_SHORT).show();
                                    handler.sendEmptyMessage(1);
                                    Looper.loop();
                                }
                                // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用
                            };
                    NIMClient.getService(AuthService.class).login(info)
                            .setCallback(callback);
                }
            }
        });
    }

    private String tokenFromPassword(String password) {
        String appKey = readAppKey(context);
        boolean isDemo = "45c6af3c98409b18a84451215d0bdd6e".equals(appKey)
                || "fe416640c8e8a72734219e1847ad2547".equals(appKey);

        return isDemo ? MD5Utils.ecoder(password) : password;
    }

    private static String readAppKey(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
