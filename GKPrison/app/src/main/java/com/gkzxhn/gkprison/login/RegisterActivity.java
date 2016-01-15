package com.gkzxhn.gkprison.login;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.login.adapter.AutoTextAdapater;
import com.gkzxhn.gkprison.userport.bean.Register;
import com.gkzxhn.gkprison.userport.bean.Uuid_images_attributes;
import com.gkzxhn.gkprison.utils.Base64;
import com.gkzxhn.gkprison.utils.ImageTools;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.weiwangcn.betterspinner.library.BetterSpinner;


import junit.framework.Test;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by hzn 2015/12/15
 * 申请二维码页面
 */
public class RegisterActivity extends BaseActivity {

    private final String[] PRISONS = {"监狱1", "监狱2", "监狱3", "监狱4"};
//    private String url = "http://www.fushuile.com/api/v1/apply";
    private String url = Constants.URL_HEAD + "apply";
    private String url1 = Constants.URL_HEAD+"verify_code";
    private EditText et_name;// 姓名
    private EditText et_ic_card;// 身份证号
    private EditText et_phone_num;// 手机号
    private EditText et_relationship_with_prisoner;// 与服刑人员关系
    private EditText et_prisoner_number;// 服刑人员囚号
    private Gson gson;
    private AutoCompleteTextView actv_prison_choose;//监狱选择
    private List<String> suggest;// 自动提示的集合
    private Map<String, Integer> prison_map;
    private ArrayAdapter<String> aAdapter; // actv_prison_choose适配器
    private AutoTextAdapater autoTextAdapater;
    private String data; // 监狱选择访问服务器返回的字符串
    private EditText et_identifying_code;// 验证码
    private Button bt_send_identifying_code;// 发送验证码
    private Button bt_register;// 提交申请
    private TextView tv_read;// 我已阅读协议
    private CheckBox cb_agree_disagree;// 我已阅读复选框
    private ImageView iv_add_photo_01;
    private ImageView iv_add_photo_02;
    private TextView tv_software_protocol;// 蓝色软件协议
    private LinearLayout rl_register;// 注册进度布局
    private String name = "";
    private String apply = "";
    private int jail_id = 1;
    private int type_id = 3;
    private String ic_card = "";
    private String phone_num = "";
    private String relationship_with_prisoner = "";
    private String prisoner_number = "";// 囚号输入框内容
    private String prison_chooes = "";// 监狱选择输入框内容
    private String identifying_code = "";// 验证码输入框的内容
    private AlertDialog dialog;
    private AlertDialog agreement_dialog;
    private static final int TAKE_PHOTO = 0; //imageview1照相;
    private static final int CHOOSE_PHOTO = 1;//imageview1选图片;
    private static final int SCALE = 5;// 照片缩小比例
    private String uploadFile1 = "";
    private String uploadFile2 = "";
    private int imageclick = 0;
    private Bitmap newBitmap1;
    private Bitmap newBitmap2;
    private List<Uuid_images_attributes> uuid_images = new ArrayList<>();
    private int countdown = 60;
    private boolean isRunning = false;
    private SharedPreferences sp;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0: // 发送验证码请求成功
//                    showToastMsgShort("发送验证码成功，请注意查看短信");
                    String result = (String) msg.obj;
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        int error = jsonObject.getInt("error");
                        if(error == 400){
                            showToastMsgShort("验证码请求失败，请稍后再试");
                        }else if(error == 0){
                            showToastMsgShort("已发送");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 1: // 发送验证码请求失败
                    showToastMsgShort("验证码请求失败，请稍后再试");
                    break;
                case 2: // 发送验证码请求异常
                    showToastMsgShort("验证码请求异常，请稍后再试");
                    break;
                case 3:
                    String code = (String)msg.obj;
                    try {
                        JSONObject jsonObject = new JSONObject(code);
                        int error = jsonObject.getInt("error");
                        if (error == 0){
                            sendRegisterMessge();
                        }else if (error == 404){
                            showToastMsgShort("验证码错误");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;


                case 4: // 发送注册信息至服务器请求成功
                    String register_result = (String) msg.obj;
                    try {
                        JSONObject jsonObject = new JSONObject(register_result);
                        int error = jsonObject.getInt("error");
                        Log.i("呵呵呵呵", error + "");
                        if(error == 0){
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            builder.setCancelable(false);
                            View view = View.inflate(RegisterActivity.this, R.layout.register_commit_success_dialog, null);
                            Button bt_ok = (Button) view.findViewById(R.id.bt_ok);
                            bt_ok.setOnClickListener(RegisterActivity.this);
                            dialog = builder.create();
                            builder.setView(view);
                            builder.show();
                            // 把囚号保存在本地
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("prisoner_number", prisoner_number);
                            editor.commit();
                        }else if(error == 404){
                            showToastMsgShort("验证码错误");
                        } else if(error == 500){
                            showToastMsgShort("注册失败");
                        }else if(error == 501){
                            showToastMsgShort("已注册用户");
                        }else {
                            showToastMsgShort("注册失败");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToastMsgShort("异常");
                    }finally {
                        rl_register.setVisibility(View.GONE);
                        bt_register.setEnabled(true);
                    }
                    break;

                case 5:// 发送注册信息至服务器请求失败
                    showToastMsgShort("注册请求失败，请稍后再试");
                    rl_register.setVisibility(View.GONE);
                    bt_register.setEnabled(true);
                    break;

                case 6:// 发送注册信息至服务器请求异常
                    showToastMsgShort("注册请求异常，请稍后再试");
                    rl_register.setVisibility(View.GONE);
                    bt_register.setEnabled(true);
                    break;

            }

        }

    };

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_register, null);
        et_name = (EditText) view.findViewById(R.id.et_name);
        et_ic_card = (EditText) view.findViewById(R.id.et_ic_card);
        et_phone_num = (EditText) view.findViewById(R.id.et_phone_num);
        et_relationship_with_prisoner = (EditText) view.findViewById(R.id.et_relationship_with_prisoner);
        et_prisoner_number = (EditText) view.findViewById(R.id.et_prisoner_num);
        et_identifying_code = (EditText) view.findViewById(R.id.et_identifying_code);
        actv_prison_choose = (AutoCompleteTextView)view.findViewById(R.id.actv_prison_choose);
        bt_send_identifying_code = (Button) view.findViewById(R.id.bt_send_identifying_code);
        bt_register = (Button) view.findViewById(R.id.bt_register);
        iv_add_photo_01 = (ImageView) view.findViewById(R.id.iv_add_photo_01);
        iv_add_photo_02 = (ImageView) view.findViewById(R.id.iv_add_photo_02);
        tv_software_protocol = (TextView) view.findViewById(R.id.tv_software_protocol);
        tv_read = (TextView) view.findViewById(R.id.tv_read);
        cb_agree_disagree = (CheckBox) view.findViewById(R.id.cb_agree_disagree);
        iv_add_photo_01.setTag(1);
        iv_add_photo_02.setTag(2);
        rl_register = (LinearLayout) view.findViewById(R.id.rl_register);
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        setTitle("注册");
        setBackVisibility(View.VISIBLE);
        tv_software_protocol.setOnClickListener(this);
        tv_read.setOnClickListener(this);
        cb_agree_disagree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bt_register.setEnabled(true);
                    bt_register.setBackground(getResources().getDrawable(R.drawable.theme_bg_bt_selector));
                } else {
                    bt_register.setEnabled(false);
                    bt_register.setBackground(getResources().getDrawable(R.drawable.gray_bg_bt_selector));
                }
            }
        });
        bt_register.setOnClickListener(this);
        iv_add_photo_01.setOnClickListener(this);
        iv_add_photo_02.setOnClickListener(this);
        bt_send_identifying_code.setOnClickListener(this);
        prison_map = new HashMap<>();
        actv_prison_choose.setThreshold(1);
        actv_prison_choose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                new GetSuggestData().execute(newText);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     *
     */
    private class GetSuggestData extends AsyncTask<String,String,String> {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            autoTextAdapater = new AutoTextAdapater(suggest, RegisterActivity.this);
            actv_prison_choose.setAdapter(autoTextAdapater);
        }

        @Override
        protected String doInBackground(String... key) {
            String newText = key[0];
            newText = newText.trim();
            newText = newText.replace(" ", "+");
            try{
                HttpClient hClient = new DefaultHttpClient();
                HttpGet hGet = new HttpGet(Constants.URL_HEAD + "jails/" + newText);
                ResponseHandler<String> rHandler = new BasicResponseHandler();
                data = hClient.execute(hGet,rHandler);
                suggest = new ArrayList<>();
                prison_map.clear();
                JSONObject jsonObject = new JSONObject(data);
                JSONArray jArray = jsonObject.getJSONArray("jails");
                for(int i = 0; i < jArray.length(); i++){
                    JSONObject jsonObject1 = jArray.getJSONObject(i);
                    String suggestKey = jsonObject1.getString("title");
                    int id = jsonObject1.getInt("id");
                    suggest.add(suggestKey);
                    prison_map.put(suggestKey, id);
                }
            }catch(Exception e){
                Log.w("Error", e.getMessage());
            }
            return null;
        }
    }


    /**
     * 判断是否有网
     * @return
     */
    public boolean isNetworkAvailable(){
        Context context = getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 发送注册信息
     */

    private void sendRegisterMessge(){
        new Thread(){
            @Override
            public void run() {
                ByteArrayOutputStream bao1 = new ByteArrayOutputStream();
                newBitmap1.compress(Bitmap.CompressFormat.PNG, 100, bao1);
                byte[] ba1 = bao1.toByteArray();
                String tu1 = Base64.encode(ba1);
                ByteArrayOutputStream bao2 = new ByteArrayOutputStream();
                newBitmap2.compress(Bitmap.CompressFormat.PNG, 100, bao2);
                byte[] ba2 = bao2.toByteArray();
                String tu2 = Base64.encode(ba2);
                String[] tu = {tu1,tu2};
                for (int i = 0;i< tu.length;i++){
                    Uuid_images_attributes uuid_images_attributes = new Uuid_images_attributes();
                    uuid_images_attributes.setImage_data(tu[i]);
                    uuid_images.add(uuid_images_attributes);
                }
                Register register = new Register();
                register.setName(name);
                register.setUuid(ic_card);

                register.setRelationship(relationship_with_prisoner);
                register.setPrisoner_number(prisoner_number);
//                register.setPrison(prison_chooes);

                register.setJail_id(1);
//                Log.i("注册监狱id", prison_map.get(prison_chooes) + "");
                register.setType_id(3);
                register.setUuid_images_attributes(uuid_images);

                gson = new Gson();
                String str = gson.toJson(register);
                apply = "{\"apply\":"+str+"}";
                Log.d("注册信息",apply);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);
                Looper.prepare();
                Message msg = handler.obtainMessage();
                try {
                    StringEntity entity = new StringEntity(apply,HTTP.UTF_8);
                    entity.setContentType("application/json");
                    post.setEntity(entity);
                    HttpResponse response = httpClient.execute(post);
                    if (response.getStatusLine().getStatusCode()==200){
                        String result = EntityUtils.toString(response.getEntity(), "utf-8");
                        Log.d("注册请求成功",result);
                        msg.obj = result;
                        msg.what = 4;
                        handler.sendMessage(msg);
                    }else {
                        handler.sendEmptyMessage(5);
                        String result = EntityUtils.toString(response.getEntity(), "utf-8");
                        Log.d("注册请求失败", result);
                    }
                } catch (Exception e) {
                    handler.sendEmptyMessage(6);
                    Log.i("注册请求异常", e.getMessage());
                } finally {
                    Looper.loop();
                }
            }
        }.start();

    }
    /**
     * 发送手机号码和验证码
     */
    private void sendRegisterToServer() {
        rl_register.setVisibility(View.VISIBLE);
        bt_register.setEnabled(false);
        new Thread(){
            @Override
            public void run() {
                String phoneandcode = "{\"session\":{\"phone\":\""+phone_num+"\",\"code\":\""+identifying_code+"\"}}";
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url1);
                Looper.prepare();
                Message msg = handler.obtainMessage();
                try {
                    StringEntity entity = new StringEntity(phoneandcode,HTTP.UTF_8);
                    entity.setContentType("application/json");
                    post.setEntity(entity);
                    HttpResponse response = httpClient.execute(post);
                    if (response.getStatusLine().getStatusCode()==200){
                        String result = EntityUtils.toString(response.getEntity(), "utf-8");
                        Log.d("测试注册码",result);
                        msg.obj = result;
                        msg.what = 3;
                        handler.sendMessage(msg);
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

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_register:
                name = et_name.getText().toString().trim();
                ic_card = et_ic_card.getText().toString().trim().toLowerCase();
                phone_num = et_phone_num.getText().toString().trim();
                relationship_with_prisoner = et_relationship_with_prisoner.getText().toString().trim();
                prisoner_number = et_prisoner_number.getText().toString().trim();
                prison_chooes = actv_prison_choose.getText().toString().trim();
                identifying_code = et_identifying_code.getText().toString().trim();
                // 判断姓名是否都是汉字组成
                if(TextUtils.isEmpty(name)){
                    showToastMsgShort("姓名为空");
                    return;
                }else if(name.length() <= 1 || name.length() >= 30){
                    showToastMsgShort("姓名长度不合法");
                    return;
                }else {
                    Pattern p=Pattern.compile("[\u4e00-\u9fa5]");
                    Matcher m = p.matcher(name);
                    if(m.matches()) { // 不全是汉字
                        showToastMsgShort("姓名不合法");
                        return;
                    }
                }
                // 判断身份证号是否合法
                if(TextUtils.isEmpty(ic_card)){
                    showToastMsgShort("身份证号为空");
                    return;
                }else{
                    try {
                        if(!Utils.IDCardValidate(ic_card).equals("")){
                            showToastMsgShort("身份证号不合法");
                            return;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                // 判断手机号码是否合法
                if(TextUtils.isEmpty(phone_num)){
                    showToastMsgShort("手机号为空");
                    return;
                }else {
                    if(!Utils.isMobileNO(phone_num)){
                        showToastMsgShort("请输入正确的手机号码");
                        return;
                    }
                }
                // 判断输入的与服刑人员关系是否都是汉字
                if(TextUtils.isEmpty(relationship_with_prisoner)){
                    showToastMsgShort("与服刑人员关系为空");
                    return;
                }else {
                    Pattern p=Pattern.compile("[\u4e00-\u9fa5]");
                    Matcher m = p.matcher(name);
                    if(m.matches()) { // 不全是汉字
                        showToastMsgShort("请输入正确的与服刑人员的关系");
                        return;
                    }
                }
                //判断囚号
                if(TextUtils.isEmpty(prisoner_number)){
                    showToastMsgShort("服刑人员囚号为空");
                    return;
                }else {
                    // 囚号  数字组成的字符串
                    if(!Utils.isNumeric(prisoner_number)){
                        showToastMsgShort("请输入正确的囚号");
                        return;
                    }
                }
                //判断监狱选择
                if(TextUtils.isEmpty(prison_chooes)){
                    showToastMsgShort("");
                    return;
                }else {
                    // ToDo
                }
                // 判断验证码是否正确
                if(TextUtils.isEmpty(identifying_code)){
                    showToastMsgShort("验证码为空");
                    return;
                }
                // 判断是否上传身份证正反面照
                if(newBitmap1 == null || newBitmap2 == null){
                    showToastMsgShort("请上传身份证正反面照");
                    return;
                }
                if(isNetworkAvailable()) {
                    sendRegisterToServer(); // 发送注册信息至服务器
                }else {
                    showToastMsgShort("没有网络");
                }
                break;
            case R.id.bt_ok:
                dialog.dismiss();
                finish();
                break;
            case R.id.tv_software_protocol:
                AlertDialog.Builder agreement_builder = new AlertDialog.Builder(this);
                View agreement_view = View.inflate(this, R.layout.software_agreement_dialog, null);
                LinearLayout ll_explain_content = (LinearLayout) agreement_view.findViewById(R.id.ll_explain_content);
                agreement_dialog = agreement_builder.create();
                agreement_builder.setView(agreement_view);
                agreement_builder.show();
                agreement_dialog.setCancelable(true);
                ll_explain_content.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        long downTime = 0;
                        switch (event.getAction()){
                            case MotionEvent.ACTION_DOWN:
                                downTime = System.currentTimeMillis();
                                Log.i("按下了...", downTime + "");
                                break;
                            case MotionEvent.ACTION_UP:
                                long upTime = System.currentTimeMillis();
                                if(upTime - downTime < 500){
                                    agreement_dialog.dismiss();
                                }
                                Log.i("离开了...", upTime + "..." + (upTime - downTime));
                                break;
                        }
                        return false;
                    }
                });
                break;
            case R.id.iv_add_photo_01:
                showPhotoPicker(this);
                imageclick = 1;
                break;
            case R.id.iv_add_photo_02:
                showPhotoPicker(this);
                imageclick = 2;
                break;
            case R.id.tv_read:
                if(cb_agree_disagree.isChecked()){
                    cb_agree_disagree.setChecked(false);
                }else {
                    cb_agree_disagree.setChecked(true);
                }
                break;
            case R.id.bt_send_identifying_code:
                phone_num = et_phone_num.getText().toString().trim();
                // 判断手机号码是否合法
                if(TextUtils.isEmpty(phone_num)){
                    showToastMsgShort("手机号为空");
                    return;
                }else {
                    if(!Utils.isMobileNO(phone_num)){
                        showToastMsgShort("请输入正确的手机号码");
                        return;
                    }else {
                        if(isNetworkAvailable()) {
                            final String phone_str = "{" +
                                    "    \"apply\":{" +
                                    "        \"phone\":" + "\"" + phone_num + "\"" +
                                    "    }" +
                                    "}";
                            new Thread() {
                                @Override
                                public void run() {
                                    HttpClient httpClient = new DefaultHttpClient();
                                    HttpPost post = new HttpPost(Constants.URL_HEAD + Constants.REQUEST_SMS_URL);
                                    Looper.prepare();
                                    Message msg = handler.obtainMessage();
                                    try {
                                        Log.i("已发送", phone_str);
                                        StringEntity entity = new StringEntity(phone_str);
                                        entity.setContentType("application/json");
                                        entity.setContentEncoding("UTF-8");
                                        post.setEntity(entity);
                                        HttpResponse response = httpClient.execute(post);
                                        if (response.getStatusLine().getStatusCode() == 200) {
                                            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                                            Log.d("发送成功", result);
                                            msg.obj = result;
                                            msg.what = 0;
                                            handler.sendMessage(msg);
                                        } else {
                                            handler.sendEmptyMessage(1);
                                            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                                            Log.d("发送失败", result);
                                        }
                                    } catch (Exception e) {
                                        Log.i("发送验证码出异常啦：", e.getMessage());
                                        handler.sendEmptyMessage(2);
                                    } finally {
                                        Looper.loop();
                                    }
                                }
                            }.start();
                        }else {
                            showToastMsgShort("没有网络");
                            return;
                        }
                    }
                }
                bt_send_identifying_code.setEnabled(false);
                bt_send_identifying_code.setBackgroundColor(getResources().getColor(R.color.tv_gray));
                bt_send_identifying_code.setText(countdown + "秒后可重发");
                handler.postDelayed(identifying_Code_Task, 1000);
                break;
        }
    }

    @Override
    protected void onDestroy() {
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
            bt_send_identifying_code.setText(countdown + "秒后可重发");
            if(countdown == 0){
                bt_send_identifying_code.setEnabled(true);
                bt_send_identifying_code.setBackground(getResources().getDrawable(R.drawable.theme_bg_bt_selector));
                bt_send_identifying_code.setText("发送验证码");
                countdown = 60;
                isRunning = false;
            }else {
                handler.postDelayed(identifying_Code_Task, 1000);
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(dialog != null && dialog.isShowing()){
            return true;
        }else if(agreement_dialog != null && agreement_dialog.isShowing()){
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 显示相片操作(0 拍照 / 1 选择相片)
     *
     * @param context
     */
    private void showPhotoPicker(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("图片来源:");
        builder.setNegativeButton("取消", null);
        builder.setItems(new String[]{"拍照", "相册"},
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case TAKE_PHOTO:
                                Intent openCameraIntent = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                Uri imageUri = Uri.fromFile(new File(Environment
                                        .getExternalStorageDirectory(), "image.jpg"));
                                // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(openCameraIntent, TAKE_PHOTO);
                                break;

                            case CHOOSE_PHOTO:
                                Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                openAlbumIntent.setType("image/*");
                                startActivityForResult(openAlbumIntent, CHOOSE_PHOTO);
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO:
                    // 将处理过的图片显示在界面上，并保存到本地
                    if (imageclick == 1){
                        // 将保存在本地的图片取出并缩小后显示在界面上
                        Bitmap bitmap = BitmapFactory.decodeFile(Environment
                                .getExternalStorageDirectory() + "/image.jpg");
                        newBitmap1 = ImageTools.zoomBitmap(bitmap, bitmap.getWidth()
                                / SCALE, bitmap.getHeight() / SCALE);
                        // 由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
                        bitmap.recycle();
                        iv_add_photo_01.setImageBitmap(newBitmap1);
                        ImageTools.savePhotoToSDCard(newBitmap1, Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                .getAbsolutePath()
                                + "/Camera", String.valueOf(System.currentTimeMillis()));
                        uploadFile1 = Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                .getAbsolutePath()
                                + "/Camera/" + String.valueOf(System.currentTimeMillis()) + ".png";
                        Log.d("MainActivity",uploadFile1);
                    }else if (imageclick == 2){
                        // 将保存在本地的图片取出并缩小后显示在界面上
                        Bitmap bitmap = BitmapFactory.decodeFile(Environment
                                .getExternalStorageDirectory() + "/image.jpg");
                        newBitmap2 = ImageTools.zoomBitmap(bitmap, bitmap.getWidth()
                                / SCALE, bitmap.getHeight() / SCALE);
                        // 由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
                        bitmap.recycle();
                        iv_add_photo_02.setImageBitmap(newBitmap2);
                        ImageTools.savePhotoToSDCard(newBitmap2, Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                .getAbsolutePath()
                                + "/Camera", String.valueOf(System.currentTimeMillis()));
                        uploadFile2 = Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                .getAbsolutePath()
                                + "/Camera/" + String.valueOf(System.currentTimeMillis()) + ".png";
                            Log.d("MainActivity",uploadFile2);
                    }
                     break;
                case CHOOSE_PHOTO:
                    ContentResolver resolver = getContentResolver();
                    // 照片的原始资源地址
                    Uri originalUri = data.getData();
                    try {
                        // 使用ContentProvider通过URI获取原始图片
                        Bitmap photo = MediaStore.Images.Media.getBitmap(resolver,
                                originalUri);
                        if (photo != null) {

                            if (imageclick == 1) {
                                // 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
                                newBitmap1 = ImageTools.zoomBitmap(photo, photo.getWidth()
                                        / SCALE, photo.getHeight() / SCALE);
                                // 释放原始图片占用的内存，防止out of memory异常发生
                                photo.recycle();
                                iv_add_photo_01.setImageBitmap(newBitmap1);
                                uploadFile1 = Environment
                                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                        .getAbsolutePath()
                                        + "/Camera/" + "emptyphoto.png";
                            }else if (imageclick == 2){
                                newBitmap2 = ImageTools.zoomBitmap(photo, photo.getWidth()
                                        / SCALE, photo.getHeight() / SCALE);
                                // 释放原始图片占用的内存，防止out of memory异常发生
                                photo.recycle();
                                iv_add_photo_02.setImageBitmap(newBitmap2);
                                uploadFile2 = Environment
                                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                        .getAbsolutePath()
                                        + "/Camera/" + "emptyphoto.png";
                            }

                        }
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, "读取文件,出错啦", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
}
