package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.userport.bean.Letter;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class WriteMessageActivity extends BaseActivity {

    private EditText et_theme;
    private EditText et_content;
    private Button bt_commit_write_message;
    private String theme;
    private String contents;
    private Gson gson;
    private SharedPreferences sp;
    private int jail_id;
    private String token;
    private int family_id = 0;
    private SweetAlertDialog pDialog;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:// 发送成功(200)
                    pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.gplus_color_1));
                    pDialog.setTitleText("提交成功，感谢您的反馈！")
                            .setConfirmText("确定")
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            WriteMessageActivity.this.finish();
                        }
                    });
                    break;
                case 1:// 发送失败(不是200)
                    pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
                    pDialog.setTitleText("提交失败，请稍后再试！")
                            .setConfirmText("确定")
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    break;
                case 2://不支持的编码异常
                    pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
                    pDialog.setTitleText("提交异常，请稍后再试！")
                            .setConfirmText("确定")
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    break;
                case 3://客户端协议异常
                    pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
                    pDialog.setTitleText("提交异常，请稍后再试！")
                            .setConfirmText("确定")
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    break;
                case 4:// io异常  服务器未开启
                    pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
                    pDialog.setTitleText("提交异常，请稍后再试！")
                            .setConfirmText("确定")
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    break;
            }
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_write_message, null);
        et_theme = (EditText) view.findViewById(R.id.et_theme);
        et_content = (EditText) view.findViewById(R.id.et_content);
        bt_commit_write_message = (Button) view.findViewById(R.id.bt_commit_write_message);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("写信");
        setBackVisibility(View.VISIBLE);
        bt_commit_write_message.setOnClickListener(this);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        jail_id = sp.getInt("jail_id",1);
        family_id = sp.getInt("family_id", 1);
        token = sp.getString("token", "");
        rl_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_commit_write_message:
                theme = et_theme.getText().toString();
                contents = et_content.getText().toString();
                if (theme.equals("主题:") || TextUtils.isEmpty(theme)){
                    showToastMsgShort("请输入主题");
                    return;
                }else if (TextUtils.isEmpty(contents)){
                    showToastMsgShort("请输入内容");
                    return;
                }else {
                    if(Utils.isNetworkAvailable()) {
                        sendMessage();
                    }else {
                        showToastMsgShort("没有网络,请检查网络设置");
                    }
                }
                break;
            case R.id.rl_back:
                contents = et_content.getText().toString().trim();
                theme = et_theme.getText().toString().trim();
                if(!TextUtils.isEmpty(contents) || !TextUtils.isEmpty(theme)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(WriteMessageActivity.this);
                    builder.setMessage("放弃写信？");
                    builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            WriteMessageActivity.this.finish();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    WriteMessageActivity.this.finish();
                }
                break;
        }
    }

    /**
     * 提交
     */
    private void sendMessage(){
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("正在提交,请稍后...");
        pDialog.setCancelable(false);
        pDialog.show();
        Letter letter = new Letter();
        letter.setTheme(theme);
        letter.setContents(contents);
        letter.setJail_id(1);
        letter.setFamily_id(family_id);
        gson = new Gson();
        String message = gson.toJson(letter);
        final String sendmessage = "{\"message\":"+message+"}";
        new Thread(){
            @Override
            public void run() {
//                HttpClient httpClient = new DefaultHttpClient();
//                HttpPost post = new HttpPost(url + token);
                try {
                    String url = Constants.URL_HEAD + "mail_boxes?jail_id="+jail_id+"&access_token=";
//                    StringEntity entity = new StringEntity(sendmessage,HTTP.UTF_8);
//                    entity.setContentType("application/json");
//                    post.setEntity(entity);
//                    HttpResponse response = httpClient.execute(post);
//                    if (response.getStatusLine().getStatusCode()==200){
//                        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
//                        Log.d("写信成功", result);
//                        Message msg = handler.obtainMessage();
//                        msg.what = 0;
//                        msg.obj = result;
//                        handler.sendMessage(msg);
//                        SystemClock.sleep(500);// 模拟网络差的情景
//                    }else {
//                        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
//                        Log.d("写信失败", result);
//                        Message msg = handler.obtainMessage();
//                        msg.what = 1;
//                        msg.obj = result;
//                        handler.sendMessage(msg);
//                        SystemClock.sleep(500);// 模拟网络差的情景
//                    }
                    String result = HttpRequestUtil.doHttpsPost(url + token, sendmessage);
                    if(result.contains("StatusCode is ")){
                        Log.d("写信失败", result);
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = result;
                        handler.sendMessage(msg);
                        SystemClock.sleep(500);// 模拟网络差的情景
                    }else {
                        Log.d("写信成功", result);
                        Message msg = handler.obtainMessage();
                        msg.what = 0;
                        msg.obj = result;
                        handler.sendMessage(msg);
                        SystemClock.sleep(500);// 模拟网络差的情景
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    SystemClock.sleep(500);// 模拟网络差的情景
                    handler.sendEmptyMessage(2);
                    Log.i("写信异常","异常1");
                }
//                catch (ClientProtocolException e) {
//                    e.printStackTrace();
//                    SystemClock.sleep(500);// 模拟网络差的情景
//                    handler.sendEmptyMessage(3);
//                    Log.i("写信异常", "异常2");
//                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                    SystemClock.sleep(500);// 模拟网络差的情景
//                    handler.sendEmptyMessage(4);
//                    Log.i("写信异常", "异常3");
//                }
            }
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            contents = et_content.getText().toString().trim();
            theme = et_theme.getText().toString().trim();
            if(!TextUtils.isEmpty(contents) || !TextUtils.isEmpty(theme)){
                AlertDialog.Builder builder = new AlertDialog.Builder(WriteMessageActivity.this);
                builder.setMessage("放弃写信？");
                builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        WriteMessageActivity.this.finish();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else {
                WriteMessageActivity.this.finish();
            }
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
