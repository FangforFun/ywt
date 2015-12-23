package com.gkzxhn.gkprison.login;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.activity.RegisterActivity;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

/**
 * A simple {@link Fragment} subclass.
 * 个人用户登录界面
 */
public class PersonLoadingFragment extends BaseFragment {

    private Button bt_register;
    private Button btn_login;
    private EditText et_login_username;
    private EditText et_login_ic_card_num;
    private String username;
    private String ic_card_num;
    private SharedPreferences sp;

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_person_loading, null);
        bt_register = (Button) view.findViewById(R.id.bt_register);
        btn_login = (Button) view.findViewById(R.id.btn_login);
        et_login_username = (EditText) view.findViewById(R.id.et_login_username);
        et_login_ic_card_num = (EditText) view.findViewById(R.id.et_login_ic_card_num);
        return view;
    }

    @Override
    protected void initData() {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = et_login_username.getText().toString().trim();
                ic_card_num = et_login_ic_card_num.getText().toString().trim();
                if(TextUtils.isEmpty(username) || TextUtils.isEmpty(ic_card_num)){
                    Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    LoginInfo info = new LoginInfo(username, tokenFromPassword(ic_card_num)); // config...
                    RequestCallback<LoginInfo> callback =
                            new RequestCallback<LoginInfo>() {
                                @Override
                                public void onSuccess(LoginInfo loginInfo) {
                                    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("username", username);
                                    editor.putString("password", ic_card_num);
                                    editor.commit();
                                    Intent intent = new Intent(context, MainActivity.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                }

                                @Override
                                public void onFailed(int i) {
                                    Toast.makeText(context, "登录失败" + i + username + ic_card_num, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onException(Throwable throwable) {
                                    Toast.makeText(context, "登录失败2", Toast.LENGTH_SHORT).show();
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
