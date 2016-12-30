package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.Opinion;
import com.gkzxhn.gkprison.api.ApiRequest;
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
 * created by hzn 2016/1/16
 * 意见反馈
 */
public class OpinionFeedbackActivity extends BaseActivity {

    private static final java.lang.String TAG = "OpinionFeedbackActivity";
    private EditText et_content;
    private String opinion_content;
    private TextView surplus_count;
    private Button bt_commit_opinions;
    private ProgressDialog commit_dialog;
    private String token;
    private boolean isFinish = false;
    private Handler handler = new Handler();
    private SweetAlertDialog pDialog;

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(this, R.layout.activity_opinion_feedback, null);
        et_content = (EditText) view.findViewById(R.id.et_content);
        surplus_count = (TextView) view.findViewById(R.id.surplus_count);
        bt_commit_opinions = (Button) view.findViewById(R.id.bt_commit_opinions);
        return view;
    }

    @Override
    protected void initData() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        token = sp.getString("token", "");
        setTitle("意见反馈");
        setBackVisibility(View.VISIBLE);
        bt_commit_opinions.setOnClickListener(this);
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 0 && s.length() <= 255) {
                    surplus_count.setText("" + (255 - s.length()));
                    surplus_count.setTextColor(getResources().getColor(R.color.tv_green));
                } else {
                    surplus_count.setText("" + (255 - s.length()));
                    surplus_count.setTextColor(getResources().getColor(R.color.tv_red));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_commit_opinions:
                opinion_content = et_content.getText().toString().trim();
                if(!TextUtils.isEmpty(opinion_content) && !(opinion_content.length() > 255)){
                    if(Utils.isNetworkAvailable(this)) {
                        sendOpinionsToServer(opinion_content);
                    }else {
                        showToastMsgShort("没有网络");
                    }
                }else {
                    if(opinion_content.length() > 255) {
                        showToastMsgShort("长度不合法");
                    }else {
                        showToastMsgShort("请输入意见反馈内容");
                    }
                }
                break;
            case R.id.rl_back:
                opinion_content = et_content.getText().toString().trim();
                if(!TextUtils.isEmpty(opinion_content)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(OpinionFeedbackActivity.this);
                    builder.setMessage("放弃反馈？");
                    builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            OpinionFeedbackActivity.this.finish();
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
                    OpinionFeedbackActivity.this.finish();
                }
                break;
        }
    }

    /**
     * 发送意见反馈内容至服务端
     */
    private void sendOpinionsToServer(String mopinion_content) {
        showDialog();
        Opinion opinion = new Opinion();
        Opinion.OpinionBean bean = opinion.new OpinionBean();
        bean.setContents(mopinion_content);
        opinion.setFeedback(bean);
        Gson gson = new Gson();
        String sendOpinion = gson.toJson(opinion);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiRequest feed = retrofit.create(ApiRequest.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset-utf-8"), sendOpinion);
        feed.sendOpinion(token, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "send opinion failed : " + e.getMessage());
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
                    public void onNext(Object o) {
                        Log.i(TAG, "send opinion success : " + o.toString());
                        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.gplus_color_1));
                        pDialog.setTitleText("提交成功，感谢您的反馈！")
                                .setConfirmText("确定")
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                OpinionFeedbackActivity.this.finish();
                            }
                        });
                    }
                });
    }

    /**
     * 进度对话框
     */
    private void showDialog(){
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("正在提交,请稍候...");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    public void onBackPressed() {
        opinion_content = et_content.getText().toString().trim();
        if(!TextUtils.isEmpty(opinion_content)){
            AlertDialog.Builder builder = new AlertDialog.Builder(OpinionFeedbackActivity.this);
            builder.setMessage("放弃反馈？");
            builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    OpinionFeedbackActivity.this.finish();
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
            super.onBackPressed();
        }
    }
}
