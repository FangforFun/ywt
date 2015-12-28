package com.gkzxhn.gkprison.login;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.scan.CaptureActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.activity.RegisterActivity;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * 个人用户登录界面
 */
public class PersonLoadingFragment extends BaseFragment {
    private String url = "http://10.93.1.10:3000/api/v1/login";
    private Button bt_register;
    private Button btn_login;
    private EditText et_login_username;
    private EditText et_login_ic_card_num;
    private String username;
    private String ic_card_num;
    private SharedPreferences sp;
    private String token = "cb21c49928249f05ae8e4075c6018ff0";
    private Button bt_scan_login;

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_person_loading, null);
        bt_register = (Button) view.findViewById(R.id.bt_register);
        btn_login = (Button) view.findViewById(R.id.btn_login);
        et_login_username = (EditText) view.findViewById(R.id.et_login_username);
        et_login_ic_card_num = (EditText) view.findViewById(R.id.et_login_ic_card_num);
        bt_scan_login = (Button) view.findViewById(R.id.bt_scan_login);
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
        bt_scan_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CaptureActivity.class);
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
                    final RequestCallback<LoginInfo> callback =
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
                new Thread(){
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost post = new HttpPost(url);
                        List<NameValuePair> values = new ArrayList<NameValuePair>();
                        BasicNameValuePair value1 = new BasicNameValuePair("name",username);
                        BasicNameValuePair value2 = new BasicNameValuePair("uuid",ic_card_num);
                        BasicNameValuePair value3 = new BasicNameValuePair("token",token);
                        values.add(value1);
                        values.add(value2);
                        values.add(value3);
                        try {
                            HttpEntity entity = new UrlEncodedFormEntity(values,"utf-8");
                            post.setEntity(entity);
                            HttpResponse httpResponse = httpClient.execute(post);
                            if (httpResponse.getStatusLine().getStatusCode() == 200){
//                                Intent intent = new Intent(context, MainActivity.class);
//                                startActivity(intent);
//                                getActivity().finish();
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
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
