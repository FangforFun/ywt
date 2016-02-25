package com.gkzxhn.gkprison.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.bean.UserInfo;
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

    private String url = Constants.URL_HEAD + "login";
    private Button bt_register;
    private ActionProcessButton btn_login;
    private EditText et_login_username;
    private EditText et_login_ic_card_num;
    private EditText et_identifying_code;
    private String username;
    private String ic_card_num;
    private String identifying_code;// 验证码
    private SharedPreferences sp;
    private Button bt_fast_login;
    private TextView tv_send_identifying_code;
    private boolean isRunning = false;// 倒计时任务正在执行
    private boolean isOK = false;// 服务器登录返回的是0才变成true
    private int countdown = 60;
    private Gson gson;
    private int successCode = 0;
    private UserInfo userInfo;
    RequestCallback<LoginInfo> callback;
    LoginInfo info;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:// 云信登录成功
                    successCode++;
                    if(successCode == 2) {
                        loginSuccessed();
                    }
                    break;
                case 2:// 发送登录信息至服务器成功
                    String result_login = (String) msg.obj;
                    gson = new Gson();
                    userInfo = gson.fromJson(result_login, UserInfo.class);
                    int code = userInfo.getCode();
                    if(code == 401){
                        showToastMsgShort("用户身份验证失败");
                        btn_login.setProgress(0);
                        btn_login.setText("登录失败");
                        btn_login.setEnabled(true);
                        btn_login.setClickable(true);
                        successCode = 0;
                        return;
                    }else if(code == 404 || code == 413){
                        showToastMsgShort("验证码错误");
                        btn_login.setProgress(0);
                        btn_login.setText("登录失败");
                        btn_login.setEnabled(true);
                        btn_login.setClickable(true);
                        successCode = 0;
                        return;
                    }else if(code == 200){
                        successCode++;
                        Log.i("登录成功啦", result_login);
//                        info = new LoginInfo(username, userInfo.getToken()); // config...
                        info = new LoginInfo(userInfo.getToken(), userInfo.getToken()); // config...
                        NIMClient.getService(AuthService.class).login(info)
                                .setCallback(callback);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("avatar", userInfo.getAvatar().split("\\|")[2]);
                        editor.commit();
                    }else {
                        btn_login.setProgress(0);
                        btn_login.setText("登录失败");
                        btn_login.setEnabled(true);
                        btn_login.setClickable(true);
                        successCode = 0;
                    }
//                    showToastMsgShort("返回码..." + code);
                    break;
                case 1:// 云信id登录失败
                    successCode = 0;
                    btn_login.setProgress(0);
                    btn_login.setText("登录失败");
                    btn_login.setEnabled(true);
                    btn_login.setClickable(true);
                    // 发送验证码设为可用
//                    removeCodeTask();
                    break;
                case 3:// 发送登录信息至服务器失败
                case 4:// 发送登录信息至服务器异常
                    btn_login.setProgress(0);
                    btn_login.setText("登录失败");
                    btn_login.setEnabled(true);
                    btn_login.setClickable(true);
                    if(msg.what == 4){
                        showToastMsgShort("登录异常,请稍后再试");
                    }else if(msg.what == 3){
                       showToastMsgShort("登录失败，请稍后再试");
                    }
                    break;
                case 7:// 验证码已发送
                    String result = (String) msg.obj;
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        int _code = jsonObject.getInt("code");
                        if(_code == 400){
                            showToastMsgShort("验证码请求失败，请稍后再试");
                        }else if(_code == 200){
                            showToastMsgShort("已发送");
                        }
                        Log.i("登录验证码", _code + "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 8:// 验证码发送失败
                    showToastMsgShort("验证码请求失败，请稍后再试");
                    removeCodeTask();
                    break;
                case 9:// 验证码发送异常
                    showToastMsgShort("验证码请求异常，请稍后再试");
                    removeCodeTask();
                    break;
            }
        }
    };

    /**
     * 登录成功
     */
    private void loginSuccessed() {
        btn_login.setProgress(0);
        btn_login.setText("登录成功");
        btn_login.setEnabled(true);
        btn_login.setClickable(true);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", username);
        editor.putString("password", ic_card_num);
        editor.putBoolean("isCommonUser", true);
        if (userInfo != null) {
            editor.putString("name", userInfo.getUser().getName());
            editor.putString("relationship", userInfo.getUser().getRelationship());
            editor.putString("token", userInfo.getToken());
            editor.putInt("family_id", userInfo.getUser().getId());
            editor.putString("jail", userInfo.getJail());
        }
        editor.putBoolean("isRegisteredUser", true);
        editor.commit();
        DemoCache.setAccount(userInfo.getToken());
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_person_loading, null);
        bt_register = (Button) view.findViewById(R.id.bt_register);
        btn_login = (ActionProcessButton) view.findViewById(R.id.btn_login);
        et_login_username = (EditText) view.findViewById(R.id.et_login_username);
        et_login_ic_card_num = (EditText) view.findViewById(R.id.et_login_ic_card_num);
        bt_fast_login = (Button) view.findViewById(R.id.bt_fast_login);
        tv_send_identifying_code = (TextView) view.findViewById(R.id.tv_send_identifying_code);
        et_identifying_code = (EditText) view.findViewById(R.id.et_identifying_code);
        return view;
    }

    @Override
    protected void initData() {
        HttpRequestUtil.initHttpClient(null);
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
                identifying_code = et_identifying_code.getText().toString().trim();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(ic_card_num) || TextUtils.isEmpty(identifying_code)) {
                    Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (Utils.isNetworkAvailable()) {
                        btn_login.setEnabled(false);
                        btn_login.setClickable(false);
                        btn_login.setMode(ActionProcessButton.Mode.ENDLESS);
                        btn_login.setProgress(1);
                        btn_login.setText("正在登录...");
                        callback = new RequestCallback<LoginInfo>() {
                            @Override
                            public void onSuccess(LoginInfo loginInfo) {
                                handler.sendEmptyMessage(0);
                                Looper.loop();
                            }

                            @Override
                            public void onFailed(int i) {
                                switch (i) {
                                    case 302:
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
                                Log.i("云信id登录异常", throwable.getMessage());
                                handler.sendEmptyMessage(1);
                                Looper.loop();
                            }
                        };
                        new Thread() {
                            @Override
                            public void run() {
                                String str = "{\"session\":{ \"phone\":\"" + username + "\", \"uuid\":\"" + ic_card_num + "\", \"code\":\"" + identifying_code + "\"}}";
//                                HttpClient httpClient = new DefaultHttpClient();
//                                HttpPost post = new HttpPost(url);
//                                Looper.prepare();
                                Message msg = handler.obtainMessage();
                                try {
//                                    StringEntity entity = new StringEntity(str, HTTP.UTF_8);
//                                    entity.setContentType("application/json");
//                                    post.setEntity(entity);
//                                    HttpResponse httpResponse = httpClient.execute(post);
//                                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
//                                        String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
//                                        Log.d("登录信息发送成功", result);
//                                        msg.obj = result;
//                                        msg.what = 2;
//                                        handler.sendMessage(msg);
//                                    } else {
//                                        handler.sendEmptyMessage(3);
//                                        String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
//                                        Log.d("登录信息发送失败", result);
//                                    }
                                    String result = HttpRequestUtil.doHttpsPost(url, str);
                                    if (result.contains("StatusCode is ")) {
                                        handler.sendEmptyMessage(3);
                                        Log.d("登录信息发送失败", result);
                                    } else {
                                        Log.d("登录信息发送成功", result);
                                        msg.obj = result;
                                        msg.what = 2;
                                        handler.sendMessage(msg);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    handler.sendEmptyMessage(4);
                                    Log.d("登录信息发送异常", e.getMessage());
                                }
//                                finally {
//                                    Looper.loop();
//                                }
                            }
                        }.start();
                    } else {
                        showToastMsgLong("没有网络");
                    }
                }
            }
        });
        bt_fast_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("isRegisteredUser", false);
                editor.commit();
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
                if (TextUtils.isEmpty(username)) {
                    showToastMsgShort("手机号为空");
                    return;
                } else {
                    if (!Utils.isMobileNO(username)) {
                        showToastMsgShort("请输入正确的用户名");
                        return;
                    } else {
                        if (Utils.isNetworkAvailable()) {
                            final String phone_str = "{" +
                                    "    \"apply\":{" +
                                    "        \"phone\":" + "\"" + username + "\"" +
                                    "    }" +
                                    "}";
                            new Thread() {
                                @Override
                                public void run() {
//                                    HttpClient httpClient = new DefaultHttpClient();
//                                    HttpPost post = new HttpPost(Constants.URL_HEAD + Constants.REQUEST_SMS_URL);
//                                    Looper.prepare();
                                    Message msg = handler.obtainMessage();
                                    try {
//                                        Log.i("已发送", phone_str);
//                                        StringEntity entity = new StringEntity(phone_str);
//                                        entity.setContentType("application/json");
//                                        entity.setContentEncoding("UTF-8");
//                                        post.setEntity(entity);
//                                        HttpResponse response = httpClient.execute(post);
//                                        if (response.getStatusLine().getStatusCode() == 200) {
//                                            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
//                                            Log.d("发送成功", result);
//                                            msg.obj = result;
//                                            msg.what = 7;
//                                            handler.sendMessage(msg);
//                                        } else {
//                                            handler.sendEmptyMessage(8);
//                                            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
//                                            Log.d("发送失败", result);
//                                        }
                                        String result = HttpRequestUtil.doHttpsPost(Constants.URL_HEAD + Constants.REQUEST_SMS_URL, phone_str);
                                        if (result.contains("StatusCode is ")) {
                                            handler.sendEmptyMessage(8);
                                            Log.d("发送失败", result);
                                        } else {
                                            Log.d("发送成功", result);
                                            msg.obj = result;
                                            msg.what = 7;
                                            handler.sendMessage(msg);
                                        }
                                    } catch (Exception e) {
                                        handler.sendEmptyMessage(9);
                                        Log.i("发送验证码异常", e.getMessage());
                                    }
//                                    finally {
//                                        Looper.loop();
//                                    }
                                }
                            }.start();
                        } else {
                            showToastMsgLong("没有网络");
                            return;
                        }
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
            removeCodeTask();
//            handler = null;
        }
    }

    /**
     * 验证码发送倒计时任务
     */
    private Runnable identifying_Code_Task = new Runnable() {
        @Override
        public void run() {
            isRunning = true;
            countdown--;
            tv_send_identifying_code.setText(countdown + "秒后可重发");
            if(countdown == 0){
                removeCodeTask();
            }else {
                handler.postDelayed(identifying_Code_Task, 1000);
            }
        }
    };

    /**
     * 移除倒计时任务
     */
    private void removeCodeTask() {
        handler.removeCallbacks(identifying_Code_Task);
        tv_send_identifying_code.setEnabled(true);
        tv_send_identifying_code.setBackgroundColor(getResources().getColor(R.color.white));
        tv_send_identifying_code.setTextColor(getResources().getColor(R.color.theme));
        tv_send_identifying_code.setText("发送验证码");
        countdown = 60;
        isRunning = false;
    }
}
