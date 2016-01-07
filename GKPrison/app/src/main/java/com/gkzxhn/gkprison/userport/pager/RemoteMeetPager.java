package com.gkzxhn.gkprison.userport.pager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BasePager;
import com.gkzxhn.gkprison.constant.Constants;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * Created by hzn on 2015/12/3.
 */
public class RemoteMeetPager extends BasePager {

    private TextView tv_meeting_request_name;
    private TextView tv_meeting_request_id_num;
    private TextView tv_meeting_request_relationship;
    private TextView tv_meeting_request_phone;
    private BetterSpinner bs_meeting_request_time;
    private Button bt_commit_request;
    private SharedPreferences sp;
    private static final String MEETING_REQUEST_URL = Constants.URL_HEAD + "/api/v1/apply?access_token=";
    private static final String[] REQUEST_TIME = new String[] {
            "2016-01-05", "2016-01-06", "2016-01-07", "2016-01-08", "2016-01-09"
    };
    private ArrayAdapter<String> adapter;

    public RemoteMeetPager(Context context) {
        super(context);
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0: // 发送会见申请成功
                    showToastMsgLong("提交成功，提交结果会以短信方式发送至您的手机，请注意查收");
                    break;
                case 1: // 发送会见申请失败
                    showToastMsgLong("提交失败，请稍后再试");
                    break;
                case 2:// 发送会见申请异常
                    showToastMsgLong("提交异常，请稍后再试");
                    break;
            }
        }
    };
    private Message msg = handler.obtainMessage();

    @Override
    public View initView() {
        view = View.inflate(context, R.layout.pager_remote_meeting, null);
        tv_meeting_request_name = (TextView) view.findViewById(R.id.tv_meeting_request_name);
        tv_meeting_request_id_num = (TextView) view.findViewById(R.id.tv_meeting_request_id_num);
        tv_meeting_request_relationship = (TextView) view.findViewById(R.id.tv_meeting_request_relationship);
        tv_meeting_request_phone = (TextView) view.findViewById(R.id.tv_meeting_request_phone);
        bs_meeting_request_time = (BetterSpinner) view.findViewById(R.id.bs_meeting_request_time);
        bt_commit_request = (Button) view.findViewById(R.id.bt_commit_request);
        return view;
    }

    @Override
    public void initData() {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        if(sp.getBoolean("isCommonUser", false)){
            tv_meeting_request_name.setText(sp.getString("name", ""));
            tv_meeting_request_id_num.setText(sp.getString("password", ""));
            tv_meeting_request_phone.setText(sp.getString("username", ""));
            tv_meeting_request_relationship.setText(sp.getString("relationship", ""));
        }
        adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, REQUEST_TIME);
        bs_meeting_request_time.setAdapter(adapter);
        bt_commit_request.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_commit_request:
                if(sp.getBoolean("isCommonUser", false)) {
                    if(!TextUtils.isEmpty(bs_meeting_request_time.getText().toString())) {
                        sendRequestToServer();
                    }else {
                        showToastMsgShort("请选择申请会见时间");
                        return;
                    }
                }else {
                    showToastMsgShort("请先登录");
                }
                break;
        }
    }

    /**
     * 发送申请至服务器
     */
    private void sendRequestToServer() {
        new Thread(){
            @Override
            public void run() {
                String prisoner_number = sp.getString("prisoner_number", "4000002");
                String body = "{\"apply\":{\"phone\":\"" + sp.getString("username", "") + "\",\"uuid\":\"" + sp.getString("password", "") + "\",\"app_date\":\"" + bs_meeting_request_time.getText().toString() + "\",\"name\":\"" + tv_meeting_request_name.getText().toString() + "\",\"relationship\":\"" + tv_meeting_request_relationship.getText().toString() + "\",\"jail_id\":\"1\",\"prisoner_number\":\"" + prisoner_number + "\",\"type_id\":\"1\"}}";
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost post = new HttpPost(MEETING_REQUEST_URL + sp.getString("token", ""));
                Looper.prepare();
                try {
                    StringEntity entity = new StringEntity(body, HTTP.UTF_8);
                    entity.setContentType("application/json");
                    post.setEntity(entity);
                    Log.d("开始发送", body + "---" + MEETING_REQUEST_URL + sp.getString("token", ""));
                    HttpResponse httpResponse = httpClient.execute(post);
                    if (httpResponse.getStatusLine().getStatusCode() == 200){
                        String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                        msg.obj = result;
                        msg.what = 0;
                        handler.sendMessage(msg);
                        Log.d("发送成功", result);
                    }else {
                        String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                        msg.obj = result;
                        msg.what = 1;
                        handler.sendMessage(msg);
                        Log.d("发送失败", result);
                    }
                } catch (Exception e){
                    Log.d("发送异常", e.getMessage());
                    handler.sendEmptyMessage(2);
                } finally {
                    Looper.loop();
                }
            }
        }.start();
    }
}
