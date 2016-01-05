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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.DemoCache;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.scan.CaptureActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.bean.UserInfo;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * 个人用户登录界面
 */
public class PersonLoadingFragment extends BaseFragment {
    private String url = "http://10.93.1.10:3000/api/v1/login";
    private Button bt_register;
    private ActionProcessButton btn_login;
    private EditText et_login_username;
    private EditText et_login_ic_card_num;
    private EditText et_identifying_code;
    private String username;
    private String ic_card_num;
    private String identifying_code;// 验证码
    private SharedPreferences sp;
    private String token = "cb21c49928249f05ae8e4075c6018ff0";
    private Button bt_scan_login;
    private Button bt_fast_login;
    private TextView tv_send_identifying_code;
    private boolean isRunning = false;// 倒计时任务正在执行
    private boolean isOK = false;// 服务器登录返回的是0才变成true
    private int countdown = 60;
    private int login_code = 0;// 云信id登录成功加1   短信验证码成功加1  当code==2时判断登录成功
    private Gson gson;
    private UserInfo userInfo;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:// 云信登录成功
                case 2:// 发送登录信息至服务器成功
                    login_code++;
                    if(msg.what == 2){
                        String result = (String) msg.obj;
                        gson = new Gson();
                        userInfo = gson.fromJson(result, UserInfo.class);
                        int error = userInfo.getError();
                        if(error == 201){
                            showToastMsgShort("用户身份验证失败");
                            login_code = 0;
                            btn_login.setProgress(0);
                            btn_login.setText("登录失败");
                            btn_login.setEnabled(true);
                            btn_login.setClickable(true);
                            return;
                        }else if(error == 404){
                            showToastMsgShort("验证码错误");
                            login_code = 0;
                            btn_login.setProgress(0);
                            btn_login.setText("登录失败");
                            btn_login.setEnabled(true);
                            btn_login.setClickable(true);
                            return;
                        }else if(error == 0){
                            isOK = true;
                        }
                    }
                    if(login_code == 2 && isOK) {
                        btn_login.setProgress(0);
                        btn_login.setText("登录成功");
                        btn_login.setEnabled(true);
                        btn_login.setClickable(true);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("username", username);
                        editor.putString("password", ic_card_num);
                        editor.putBoolean("isCommonUser", true);
                        if(userInfo != null){
                            editor.putString("name", userInfo.getUser().getName());
                            editor.putString("relationship", userInfo.getUser().getRelationship());
                            editor.putString("token", userInfo.getToken());
                        }
                        editor.commit();
                        DemoCache.setAccount(username);
                        Intent intent = new Intent(context, MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    break;
                case 1:// 云信id登录失败
                case 3:// 发送登录信息至服务器失败
                case 4:// 发送登录信息至服务器异常
                    btn_login.setProgress(0);
                    btn_login.setText("登录失败");
                    btn_login.setEnabled(true);
                    btn_login.setClickable(true);
                    if(msg.what == 4){
                        showToastMsgShort("登录异常,请稍后再试");
                    }else if(msg.what == 3){
                       showToastMsgShort("登录失败，请检查网络");
                    }
                    break;
                case 7:// 验证码已发送
                    String result = (String) msg.obj;
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        int error = jsonObject.getInt("error");
                        if(error == 400){
                            showToastMsgShort("验证码请求失败，请稍后再试");
                            tv_send_identifying_code.setEnabled(true);
                            tv_send_identifying_code.setBackgroundColor(getResources().getColor(R.color.white));
                            tv_send_identifying_code.setTextColor(getResources().getColor(R.color.theme));
                            tv_send_identifying_code.setText("发送验证码");
                            handler.removeCallbacks(identifying_Code_Task);
                        }else if(error == 0){
                            showToastMsgShort("已发送");
                        }
                        Log.i("登录验证码", error + "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 8:// 验证码发送失败
                    showToastMsgShort("验证码请求失败，请稍后再试");
                    tv_send_identifying_code.setEnabled(true);
                    tv_send_identifying_code.setBackgroundColor(getResources().getColor(R.color.white));
                    tv_send_identifying_code.setTextColor(getResources().getColor(R.color.theme));
                    tv_send_identifying_code.setText("发送验证码");
                    handler.removeCallbacks(identifying_Code_Task);
                    break;
                case 9:// 验证码发送异常
                    showToastMsgShort("验证码请求异常，请稍后再试");
                    tv_send_identifying_code.setEnabled(true);
                    tv_send_identifying_code.setBackgroundColor(getResources().getColor(R.color.white));
                    tv_send_identifying_code.setTextColor(getResources().getColor(R.color.theme));
                    tv_send_identifying_code.setText("发送验证码");
                    handler.removeCallbacks(identifying_Code_Task);
                    break;
            }
        }
    };
    private Message msg = handler.obtainMessage();

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_person_loading, null);
        bt_register = (Button) view.findViewById(R.id.bt_register);
        btn_login = (ActionProcessButton) view.findViewById(R.id.btn_login);
        et_login_username = (EditText) view.findViewById(R.id.et_login_username);
        et_login_ic_card_num = (EditText) view.findViewById(R.id.et_login_ic_card_num);
        bt_scan_login = (Button) view.findViewById(R.id.bt_scan_login);
        bt_fast_login = (Button) view.findViewById(R.id.bt_fast_login);
        tv_send_identifying_code = (TextView) view.findViewById(R.id.tv_send_identifying_code);
        et_identifying_code = (EditText) view.findViewById(R.id.et_identifying_code);
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
                identifying_code = et_identifying_code.getText().toString().trim();
                if(TextUtils.isEmpty(username) || TextUtils.isEmpty(ic_card_num) || TextUtils.isEmpty(identifying_code)){
                    Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    btn_login.setEnabled(false);
                    btn_login.setClickable(false);
                    btn_login.setMode(ActionProcessButton.Mode.ENDLESS);
                    btn_login.setProgress(1);
                    btn_login.setText("正在登录...");
                    LoginInfo info = new LoginInfo(username, tokenFromPassword(ic_card_num)); // config...
                    final RequestCallback<LoginInfo> callback =
                            new RequestCallback<LoginInfo>() {
                                @Override
                                public void onSuccess(LoginInfo loginInfo) {
                                    handler.sendEmptyMessage(0);
                                    Looper.loop();
//                                    btn_login.setProgress(0);
//                                    btn_login.setText("登录成功");
//                                    btn_login.setEnabled(true);
//                                    btn_login.setClickable(true);
//                                    SharedPreferences.Editor editor = sp.edit();
//                                    editor.putString("username", username);
//                                    editor.putString("password", ic_card_num);
//                                    editor.putBoolean("isCommonUser", true);
//                                    editor.commit();
//                                    DemoCache.setAccount(username);
//                                    Intent intent = new Intent(context, MainActivity.class);
//                                    startActivity(intent);
//                                    getActivity().finish();
                                }

                                @Override
                                public void onFailed(int i) {
                                    switch (i){
                                        case 302:
//                                            Toast.makeText(context, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(context, "手机号或者身份证号错误", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(context, "登录异常", Toast.LENGTH_SHORT).show();
                                    handler.sendEmptyMessage(1);
                                    Looper.loop();
                                }
                                // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用
                            };
                    NIMClient.getService(AuthService.class).login(info)
                            .setCallback(callback);
//                    /*
                    new Thread(){
                        @Override
                        public void run() {
                            String str = "{\"session\":{ \"phone\":\"" + username + "\", \"uuid\":\"" + ic_card_num + "\", \"code\":\"" + identifying_code + "\"}}";
                            HttpClient httpClient = new DefaultHttpClient();
                            HttpPost post = new HttpPost(url);
                            Looper.prepare();
                            try {
                                StringEntity entity = new StringEntity(str, HTTP.UTF_8);
                                entity.setContentType("application/json");
                                post.setEntity(entity);
                                HttpResponse httpResponse = httpClient.execute(post);
                                if (httpResponse.getStatusLine().getStatusCode() == 200){
                                    String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                                    Log.d("MainActivity",result);
                                    msg.obj = result;
                                    msg.what = 2;
                                    handler.sendMessage(msg);
                                }else {
                                    handler.sendEmptyMessage(3);
                                    String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                                    Log.d("MainActivity", result);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                handler.sendEmptyMessage(4);
                                Log.d("MainActivity", e.getMessage());
                            }  finally {
                                Looper.loop();
                            }
                        }
                    }.start();
//                    */
                }
            }
        });
        bt_fast_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        tv_send_identifying_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = et_login_username.getText().toString().trim();
                // 判断手机号码是否合法
                if(TextUtils.isEmpty(username)){
                    showToastMsgShort("手机号为空");
                    return;
                }else {
                    if(!Utils.isMobileNO(username)){
                        showToastMsgShort("请输入正确的用户名");
                        return;
                    }else {
                        final String phone_str = "{" +
                                "    \"apply\":{" +
                                "        \"phone\":" + "\"" + username + "\"" +
                                "    }" +
                                "}";
                        new Thread(){
                            @Override
                            public void run() {
                                HttpClient httpClient = new DefaultHttpClient();
                                HttpPost post = new HttpPost("http://10.93.1.10:3000/api/v1/request_sms");
                                Looper.prepare();
                                try {
                                    Log.i("已发送", phone_str);
                                    StringEntity entity = new StringEntity(phone_str);
                                    entity.setContentType("application/json");
                                    entity.setContentEncoding("UTF-8");
                                    post.setEntity(entity);
                                    HttpResponse response = httpClient.execute(post);
                                    if (response.getStatusLine().getStatusCode() == 200){
                                        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                                        Log.d("发送成功", result);
                                        msg.obj = result;
                                        msg.what = 7;
                                        handler.sendMessage(msg);
                                    }else {
                                        handler.sendEmptyMessage(8);
                                        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                                        Log.d("发送失败", result);
                                    }
                                } catch (Exception e){
                                    handler.sendEmptyMessage(9);
                                } finally {
                                    Looper.loop();
                                }
                            }
                        }.start();
                        tv_send_identifying_code.setEnabled(false);
                        tv_send_identifying_code.setBackgroundColor(getResources().getColor(R.color.tv_gray));
                        tv_send_identifying_code.setTextColor(getResources().getColor(R.color.white));
                        tv_send_identifying_code.setText(countdown + "秒后可重发");
                        handler.postDelayed(identifying_Code_Task, 1000);
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(handler != null && isRunning){
            handler.removeCallbacks(identifying_Code_Task);
            handler = null;
        }
    }

    private Runnable identifying_Code_Task = new Runnable() {
        @Override
        public void run() {
            isRunning = true;
            countdown--;
            tv_send_identifying_code.setText(countdown + "秒后可重发");
            if(countdown == 0){
                tv_send_identifying_code.setEnabled(true);
                tv_send_identifying_code.setBackgroundColor(getResources().getColor(R.color.white));
                tv_send_identifying_code.setTextColor(getResources().getColor(R.color.theme));
                tv_send_identifying_code.setText("发送验证码");
                countdown = 60;
                isRunning = false;
            }else {
                handler.postDelayed(identifying_Code_Task, 1000);
            }
        }
    };

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
