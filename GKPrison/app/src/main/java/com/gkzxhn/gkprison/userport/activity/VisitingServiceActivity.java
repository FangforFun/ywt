package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.DemoCache;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.utils.Utils;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * 探监服务--探监申请
 */
public class VisitingServiceActivity extends BaseActivity {

    private BetterSpinner bs_visit_request_time;
    private TextView tv_visit_request_name;
    private TextView tv_visit_request_relationship;
    private TextView tv_visit_request_id_num;
    private TextView tv_visit_request_phone;
    private Button bt_commit_request;
    private AlertDialog dialog;
    private static final String MEETING_REQUEST_URL = Constants.URL_HEAD + "apply?access_token=";
    private static final String[] REQUEST_TIME = new String[] {
            "2016-01-13", "2016-01-14", "2016-01-15", "2016-01-16", "2016-01-17"
    };
    private ArrayAdapter<String> adapter;
    private SharedPreferences sp;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0: // 发送探监申请成功
//                    showToastMsgLong("提交成功，提交结果会以短信方式发送至您的手机，请注意查收");
                    AlertDialog.Builder builder = new AlertDialog.Builder(VisitingServiceActivity.this);
                    builder.setMessage("        提交成功，提交结果会以短信方式发送至您的手机，请注意查收。");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setCancelable(false);
                    dialog = builder.create();
                    builder.show();
                    break;
                case 1: // 发送探监申请失败
                    showToastMsgLong("提交失败，请稍后再试");
                    break;
                case 2:// 发送探监申请异常
                    showToastMsgLong("提交异常，请稍后再试");
                    break;
            }
        }
    };
    private Message msg = handler.obtainMessage();

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_visiting_service,null);
        bs_visit_request_time = (BetterSpinner) view.findViewById(R.id.bs_visit_request_time);
        tv_visit_request_name = (TextView) view.findViewById(R.id.tv_visit_request_name);
        tv_visit_request_relationship = (TextView) view.findViewById(R.id.tv_visit_request_relationship);
        tv_visit_request_id_num = (TextView) view.findViewById(R.id.tv_visit_request_id_num);
        tv_visit_request_phone = (TextView) view.findViewById(R.id.tv_visit_request_phone);
        bt_commit_request = (Button) view.findViewById(R.id.bt_commit_request);
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        if(sp.getBoolean("isCommonUser", false)){
            tv_visit_request_name.setText(sp.getString("name", ""));
            tv_visit_request_id_num.setText(sp.getString("password", ""));
            tv_visit_request_phone.setText(sp.getString("username", ""));
            tv_visit_request_relationship.setText(sp.getString("relationship", ""));
        }
        setTitle("申请探监");
        setBackVisibility(View.VISIBLE);
        adapter = new ArrayAdapter<>(DemoCache.getContext(),
                android.R.layout.simple_dropdown_item_1line, REQUEST_TIME);
        bs_visit_request_time.setAdapter(adapter);
        bt_commit_request.setOnClickListener(this);
    }
}
