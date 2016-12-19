package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.Letter;
import com.gkzxhn.gkprison.userport.requests.ApiRequest;
import com.gkzxhn.gkprison.userport.view.sweet_alert_dialog.SweetAlertDialog;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;
import com.keda.sky.app.PcAppStackManager;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * created by huangzhengneng on 2016/2/1
 * 写信页面
 */
public class WriteMessageActivity extends BaseActivity {

    private static final java.lang.String TAG = "WriteMessageActivity";
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

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
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
        jail_id = sp.getInt("jail_id", 1);
        family_id = sp.getInt("family_id", 1);
        token = sp.getString("token", "");
        rl_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_commit_write_message:
                theme = et_theme.getText().toString();
                contents = et_content.getText().toString();
                if (theme.equals("主题:") || TextUtils.isEmpty(theme)) {
                    showToastMsgShort("请输入主题");
                    return;
                } else if (TextUtils.isEmpty(contents)) {
                    showToastMsgShort("请输入内容");
                    return;
                } else {
                    if (Utils.isNetworkAvailable(this)) {
                        sendMessage();
                    } else {
                        showToastMsgShort("没有网络,请检查网络设置");
                    }
                }
                break;
            case R.id.rl_back:
                contents = et_content.getText().toString().trim();
                theme = et_theme.getText().toString().trim();
                if (!TextUtils.isEmpty(contents) || !TextUtils.isEmpty(theme)) {
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
                } else {
                    WriteMessageActivity.this.finish();
                }
                break;
        }
    }

    /**
     * 提交
     */
    private void sendMessage() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("正在提交,请稍候...");
        pDialog.setCancelable(false);
        pDialog.show();

        Letter letter = new Letter();
        Letter.MessageBean bean = letter.new MessageBean();
        bean.setTheme(theme);
        bean.setContents(contents);
        bean.setJail_id(jail_id);
        bean.setFamily_id(family_id);
        letter.setMessage(bean);
        gson = new Gson();
        String message = gson.toJson(letter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiRequest writeMessage = retrofit.create(ApiRequest.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), message);
        Log.i(TAG, message);
        writeMessage.sendMessage(jail_id, token, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Object>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "send failed : " + e.getMessage());
                    pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
                    pDialog.setTitleText("提交失败，请稍后再试！")
                            .setConfirmText("确定")
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                        }
                    });
                }

                @Override
                public void onNext(Object result) {
                    Log.i(TAG, "send success : " + result.toString());
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
                }
            });

//        String url = Constants.URL_HEAD + "mail_boxes?jail_id=" + jail_id + "&access_token=";
//        OkHttpClient client = new OkHttpClient();
//        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), message);
//        Log.i(TAG, sendMessage);
//        Request request = new Request.Builder()
//                .url(url + token)
//                .post(body)
//                .build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
//                        pDialog.setTitleText("提交失败，请稍后再试！")
//                                .setConfirmText("确定")
//                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
//                        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                sweetAlertDialog.dismiss();
//                            }
//                        });
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.gplus_color_1));
//                        pDialog.setTitleText("提交成功，感谢您的反馈！")
//                                .setConfirmText("确定")
//                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
//                        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                sweetAlertDialog.dismiss();
//                                WriteMessageActivity.this.finish();
//                            }
//                        });
//                    }
//                });
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        if(pDialog != null && pDialog.isShowing()){
            // 正在show时屏蔽返回键
        }else {
            contents = et_content.getText().toString().trim();
            theme = et_theme.getText().toString().trim();
            if (!TextUtils.isEmpty(contents) || !TextUtils.isEmpty(theme)) {
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
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }

}
