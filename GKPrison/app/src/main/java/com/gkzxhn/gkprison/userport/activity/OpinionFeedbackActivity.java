package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * created by hzn 2016/1/16
 * 意见反馈
 */
public class OpinionFeedbackActivity extends BaseActivity {

    private EditText et_content;
    private String opinion_content;
    private TextView surplus_count;
    private Button bt_commit_opinions;
    private ProgressDialog commit_dialog;
    private SharedPreferences sp;
    private String token;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    commit_dialog.setMessage("提交成功，谢谢您的反馈，我们会做的更好。");
                    handler.postDelayed(dismiss_dialog_delay_task, 2000);
                    break;
                case 1:
                    commit_dialog.setMessage("糟糕，提交失败了，歇会儿再试吧！");
                    handler.postDelayed(dismiss_dialog_delay_task, 2000);
                    break;
                case 2:
                    commit_dialog.setMessage("糟糕，提交异常了，歇会儿再试吧！");
                    handler.postDelayed(dismiss_dialog_delay_task, 2000);
                    break;
            }
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_opinion_feedback, null);
        et_content = (EditText) view.findViewById(R.id.et_content);
        surplus_count = (TextView) view.findViewById(R.id.surplus_count);
        bt_commit_opinions = (Button) view.findViewById(R.id.bt_commit_opinions);
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
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
    public void onClick(View v) {
//        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_commit_opinions:
                opinion_content = et_content.getText().toString().trim();
                if(!TextUtils.isEmpty(opinion_content) && !(opinion_content.length() > 255)){
                    if(Utils.isNetworkAvailable()) {
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
    private void sendOpinionsToServer(final String mopinion_content) {
        showDialog();
        new Thread(){
            @Override
            public void run() {
                String opinion = "{\"feedback\":{\"contents\":\"" + mopinion_content + "\"}}";
                Log.i("反馈内容", opinion);
                HttpClient hc = new DefaultHttpClient();
                HttpPost post = new HttpPost(Constants.URL_HEAD + Constants.FEEDBACK + token);
                StringEntity entity = null;
                try {
                    entity = new StringEntity(opinion, HTTP.UTF_8);
                    entity.setContentType("application/json");
                    post.setEntity(entity);
                    HttpResponse response = hc.execute(post);
                    if(response.getStatusLine().getStatusCode() == 200){
                        String result = EntityUtils.toString(response.getEntity(), "utf-8");
                        Log.i("意见反馈返回", result);
                        handler.sendEmptyMessage(0);
                    }else {
                        String result = EntityUtils.toString(response.getEntity(), "utf-8");
                        Log.i("意见反馈返回", result);
                        handler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(2);
                }
            }
        }.start();
    }

    /**
     * 进度对话框
     */
    private void showDialog(){
        commit_dialog = new ProgressDialog(this);
        commit_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        commit_dialog.setCancelable(false);
        commit_dialog.setCanceledOnTouchOutside(false);
        commit_dialog.setMessage("正在提交...");
        commit_dialog.show();
    }

    /**
     * 消去对话框任务
     */
    private Runnable dismiss_dialog_delay_task = new Runnable() {
        @Override
        public void run() {
            commit_dialog.dismiss();
            OpinionFeedbackActivity.this.finish();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
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
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
