package com.gkzxhn.gkprison.userport.pager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BasePager;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hzn on 2015/12/3.
 * 探监pager
 */
public class RemoteMeetPager extends BasePager {

    private RelativeLayout rl_meeting;// 会见
    private RelativeLayout rl_visit;// 探监
    private TextView tv_meeting_request_name;// 会见申请姓名
    private TextView tv_meeting_request_id_num;// 会见申请身份证
    private TextView tv_meeting_request_relationship;// 会见申请人与服刑人员关系
    private TextView tv_meeting_request_phone;// 会见申请电话号码
    private BetterSpinner bs_meeting_request_time;// 会见申请时间
    private TextView tv_meeting_last_time;// 上次会见时间
    private Button bt_commit_request;// 提交会见申请按钮
    private SharedPreferences sp;
    private BetterSpinner bs_visit_request_time;// 探监申请时间
    private TextView tv_visit_request_name;// 探监申请姓名
    private TextView tv_visit_request_relationship;// 探监申请又服刑人员关系
    private TextView tv_visit_request_id_num;// 探监申请身份证
    private TextView tv_visit_request_phone;// 探监申请手机号
    private Button bt_commit_request_visit;// 探监申请按钮
    private RadioGroup rg_top_guide;// 顶部页面切换
    private RadioButton rb_top_guide_meeting;
    private RadioButton rb_top_guide_visit;
    private boolean isCommonUser;// 普通用户/监狱用户
    private static final String MEETING_REQUEST_URL = Constants.URL_HEAD + "apply?access_token=";
    private static final String[] REQUEST_TIME = Utils.afterNDay(30).toArray(new String[Utils.afterNDay(30).size()]);// 时间选择
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> visit_adapter;
    private ProgressDialog dialog;

    public RemoteMeetPager(Context context) {
        super(context);
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0: // 发送会见申请成功
                    String result = (String) msg.obj;
                    int code = 0;
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        code = jsonObject.getInt("code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(false);
                    View commit_success_dialog_view = View.inflate(context, R.layout.msg_ok_cancel_dialog, null);
                    View view_01 = commit_success_dialog_view.findViewById(R.id.view_01);
                    view_01.setVisibility(View.GONE);
                    TextView tv_msg_dialog = (TextView) commit_success_dialog_view.findViewById(R.id.tv_msg_dialog);
                    TextView tv_cancel = (TextView) commit_success_dialog_view.findViewById(R.id.tv_cancel);
                    tv_cancel.setVisibility(View.GONE);
                    TextView tv_ok = (TextView) commit_success_dialog_view.findViewById(R.id.tv_ok);
                    builder.setView(commit_success_dialog_view);
                    final AlertDialog commit_success_dialog = builder.create();
                    tv_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            commit_success_dialog.dismiss();
                        }
                    });
                    if(code == 200) {
                        tv_msg_dialog.setText("提交成功，提交结果将会以短信方式发送至您的手机，请注意查收。");
                        commit_success_dialog.show();
                        bt_commit_request.setEnabled(true);
                        String committed_meeting_time = sp.getString("committed_meeting_time", "");
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("committed_meeting_time", committed_meeting_time + bs_meeting_request_time.getText().toString() + "/");
                        editor.commit();
                    }else {
                        try {
                            String message = jsonObject.getString("msg");
                            JSONObject jsonObject1 = jsonObject.getJSONObject("errors");
                            JSONArray jsonArray = jsonObject1.getJSONArray("apply_create");
//                            String reason_01 = "";
//                            String reason_02 = "";
//                            for (int i = 0; i < jsonArray.length(); i++){
//
//                            }
                            String reason = "";
                            if(jsonArray.length() == 1) {
                                reason = jsonArray.getString(0);
                            }else {
                                reason = jsonArray.getString(0) + "," + jsonArray.getString(1);
                            }
                            tv_msg_dialog.setText("提交失败，原因：" + reason);
                            commit_success_dialog.show();
                            bt_commit_request.setEnabled(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1: // 发送会见申请失败
                    showToastMsgLong("提交失败，请稍后再试");
                    bt_commit_request.setEnabled(true);
                    dialog.dismiss();
                    break;
                case 2:// 发送会见申请异常
                    showToastMsgLong("提交异常，请稍后再试");
                    bt_commit_request.setEnabled(true);
                    dialog.dismiss();
                    break;
                case 3: // 发送探监申请成功
                    dialog.dismiss();
//                    showToastMsgLong("提交成功，提交结果会以短信方式发送至您的手机，请注意查收");
                    AlertDialog.Builder visit_builder = new AlertDialog.Builder(context);
                    View visit_success_dialog_view = View.inflate(context, R.layout.msg_ok_cancel_dialog, null);
                    visit_builder.setCancelable(false);
                    View view_01_ = visit_success_dialog_view.findViewById(R.id.view_01);
                    view_01_.setVisibility(View.GONE);
                    TextView tv_msg_dialog_ = (TextView) visit_success_dialog_view.findViewById(R.id.tv_msg_dialog);
                    tv_msg_dialog_.setText("提交成功，提交结果会以短信方式发送至您的手机，请注意查收。");
                    TextView tv_cancel_ = (TextView) visit_success_dialog_view.findViewById(R.id.tv_cancel);
                    tv_cancel_.setVisibility(View.GONE);
                    TextView tv_ok_ = (TextView) visit_success_dialog_view.findViewById(R.id.tv_ok);
                    visit_builder.setView(visit_success_dialog_view);
                    final AlertDialog visit_success_dialog = visit_builder.create();
                    tv_ok_.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            visit_success_dialog.dismiss();
                        }
                    });
                    visit_success_dialog.show();
                    bt_commit_request_visit.setEnabled(true);
                    String committed_time = sp.getString("committed_time", "");
                    SharedPreferences.Editor editor_ = sp.edit();
                    editor_.putString("committed_time", committed_time + bs_visit_request_time.getText().toString() + "/");
                    editor_.commit();
                    break;
                case 4: // 发送探监申请失败
                    dialog.dismiss();
                    showToastMsgLong("提交失败，请稍后再试");
                    bt_commit_request_visit.setEnabled(true);
                    break;
                case 5:// 发送探监申请异常
                    dialog.dismiss();
                    showToastMsgLong("提交异常，请稍后再试");
                    bt_commit_request_visit.setEnabled(true);
                    break;
            }
        }
    };

    @Override
    public View initView() {
        view = View.inflate(context, R.layout.pager_remote_meeting, null);
        tv_meeting_request_name = (TextView) view.findViewById(R.id.tv_meeting_request_name);
        tv_meeting_request_id_num = (TextView) view.findViewById(R.id.tv_meeting_request_id_num);
        tv_meeting_request_relationship = (TextView) view.findViewById(R.id.tv_meeting_request_relationship);
        tv_meeting_request_phone = (TextView) view.findViewById(R.id.tv_meeting_request_phone);
        bs_meeting_request_time = (BetterSpinner) view.findViewById(R.id.bs_meeting_request_time);
        bt_commit_request = (Button) view.findViewById(R.id.bt_commit_request);
        tv_meeting_last_time = (TextView) view.findViewById(R.id.tv_meeting_last_time);
        rl_meeting = (RelativeLayout) view.findViewById(R.id.rl_meeting);
        rl_visit = (RelativeLayout) view.findViewById(R.id.rl_visit);
        bs_visit_request_time = (BetterSpinner) view.findViewById(R.id.bs_visit_request_time);
        tv_visit_request_name = (TextView) view.findViewById(R.id.tv_visit_request_name);
        tv_visit_request_relationship = (TextView) view.findViewById(R.id.tv_visit_request_relationship);
        tv_visit_request_id_num = (TextView) view.findViewById(R.id.tv_visit_request_id_num);
        tv_visit_request_phone = (TextView) view.findViewById(R.id.tv_visit_request_phone);
        bt_commit_request_visit = (Button) view.findViewById(R.id.bt_commit_request_visit);
        rg_top_guide = (RadioGroup) view.findViewById(R.id.rg_top_guide);
        rb_top_guide_meeting = (RadioButton) view.findViewById(R.id.rb_top_guide_meeting);
        rb_top_guide_visit = (RadioButton) view.findViewById(R.id.rb_top_guide_visit);
        Drawable[] drawables = rb_top_guide_meeting.getCompoundDrawables();
        drawables[0].setBounds(0, 0, context.getResources().getDimensionPixelSize(R.dimen.home_tab_width), context.getResources().getDimensionPixelSize(R.dimen.visit_tab_height));
        rb_top_guide_meeting.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        Drawable[] drawables2 = rb_top_guide_visit.getCompoundDrawables();
        drawables2[0].setBounds(0, 0, context.getResources().getDimensionPixelSize(R.dimen.home_tab_width), context.getResources().getDimensionPixelSize(R.dimen.visit_tab_height));
        rb_top_guide_visit.setCompoundDrawables(drawables2[0], drawables2[1], drawables2[2], drawables2[3]);
        return view;
    }

    @Override
    public void initData() {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        isCommonUser = sp.getBoolean("isCommonUser", false);
        if(isCommonUser){
            tv_meeting_request_name.setText(sp.getString("name", ""));
            tv_meeting_request_id_num.setText(sp.getString("password", ""));
            tv_meeting_request_phone.setText(sp.getString("username", ""));
            tv_meeting_request_relationship.setText(sp.getString("relationship", ""));
            tv_meeting_last_time.setText(sp.getString("last_meeting_time", "上次会见时间：暂无会见"));
        }
        adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, REQUEST_TIME);
        visit_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, REQUEST_TIME);
        bs_meeting_request_time.setAdapter(adapter);
        bs_visit_request_time.setAdapter(visit_adapter);
        bt_commit_request.setOnClickListener(this);
        bt_commit_request_visit.setOnClickListener(this);
        rg_top_guide.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_top_guide_meeting:
                        rl_meeting.setVisibility(View.VISIBLE);
                        rl_visit.setVisibility(View.GONE);
                        if (isCommonUser) {
                            tv_meeting_request_name.setText(sp.getString("name", ""));
                            tv_meeting_request_id_num.setText(sp.getString("password", ""));
                            tv_meeting_request_phone.setText(sp.getString("username", ""));
                            tv_meeting_request_relationship.setText(sp.getString("relationship", ""));
                            tv_meeting_last_time.setText(sp.getString("last_meeting_time", "暂无会见"));
                        }
                        break;
                    case R.id.rb_top_guide_visit:
                        rl_meeting.setVisibility(View.GONE);
                        rl_visit.setVisibility(View.VISIBLE);
                        if (isCommonUser) {
                            tv_visit_request_name.setText(sp.getString("name", ""));
                            tv_visit_request_id_num.setText(sp.getString("password", ""));
                            tv_visit_request_phone.setText(sp.getString("username", ""));
                            tv_visit_request_relationship.setText(sp.getString("relationship", ""));
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_commit_request:
                if(Utils.isFastClick()){
                    return;
                }
                if(isCommonUser) {
                    if(!TextUtils.isEmpty(bs_meeting_request_time.getText().toString())) {
                        String committed_meeting_time = sp.getString("committed_meeting_time", "");
                        if(committed_meeting_time.contains(bs_meeting_request_time.getText().toString())){
                            showToastMsgLong("您已经申请过" + bs_meeting_request_time.getText().toString() + "的会见，请勿重复申请!");
                            return;
                        }else {
                            sendMeetingRequestToServer();
                        }
                    }else {
                        showToastMsgShort("请选择申请会见时间");
                        return;
                    }
                }else {
                    showToastMsgShort("请先登录");
                }
                break;
            case R.id.bt_commit_request_visit:
                if(Utils.isFastClick()){
                    return;
                }
                if(isCommonUser) {
                    if(!TextUtils.isEmpty(bs_visit_request_time.getText().toString())) {
                        String committed_time = sp.getString("committed_time", "");
                        if(committed_time.contains(bs_visit_request_time.getText().toString())){
                            showToastMsgLong("您已经申请过" + bs_visit_request_time.getText().toString() + "的实地探监，请勿重复申请!");
                            return;
                        }else {
                            sendVisitRequestToServer();
                        }
                    }else {
                        showToastMsgShort("请选择申请探监时间");
                        return;
                    }
                }else {
                    showToastMsgShort("请先登录");
                }
                break;
        }
    }

    /**
     * 发送探监申请至服务器
     */
    private void sendVisitRequestToServer() {
        if(Utils.isNetworkAvailable()) {
            bt_commit_request_visit.setEnabled(false);
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage("正在提交，请稍后");
            dialog.show();
            new Thread() {
                @Override
                public void run() {
                    String prisoner_number = sp.getString("prisoner_number", "4000002");
                    String body = "{\"apply\":{\"phone\":\"" + sp.getString("username", "") + "\",\"uuid\":\"" + sp.getString("password", "") + "\",\"app_date\":\"" + bs_visit_request_time.getText().toString() + "\",\"name\":\"" + tv_visit_request_name.getText().toString() + "\",\"relationship\":\"" + tv_visit_request_relationship.getText().toString() + "\",\"jail_id\":1,\"prisoner_number\":\"" + prisoner_number + "\",\"type_id\":2}}";
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost post = new HttpPost(MEETING_REQUEST_URL + sp.getString("token", ""));
                    try {
                        StringEntity entity = new StringEntity(body, HTTP.UTF_8);
                        entity.setContentType("application/json");
                        post.setEntity(entity);
                        Log.d("开始发送", body + "---" + MEETING_REQUEST_URL + sp.getString("token", ""));
                        HttpResponse httpResponse = httpClient.execute(post);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                            Message msg = handler.obtainMessage();
                            msg.obj = result;
                            msg.what = 3;
                            handler.sendMessage(msg);
                            Log.d("发送成功", result);
                        } else {
                            String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                            Message msg = handler.obtainMessage();
                            msg.obj = result;
                            msg.what = 4;
                            handler.sendMessage(msg);
                            Log.d("发送失败", result);
                        }
                    } catch (Exception e) {
                        Log.d("发送异常", e.getMessage());
                        handler.sendEmptyMessage(5);
                    }
                }
            }.start();
        }else {
            showToastMsgShort("没有网络");
        }
    }

    /**
     * 发送会见申请至服务器
     */
    private void sendMeetingRequestToServer() {
        if(Utils.isNetworkAvailable()) {
            bt_commit_request.setEnabled(false);
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage("正在提交，请稍后");
            dialog.show();
            new Thread() {
                @Override
                public void run() {
                    String prisoner_number = sp.getString("prisoner_number", "4000002");
                    String body = "{\"apply\":{\"phone\":\"" + sp.getString("username", "") + "\",\"uuid\":\"" +
                            sp.getString("password", "") + "\",\"app_date\":\"" + bs_meeting_request_time.getText().toString()
                            + "\",\"name\":\"" + tv_meeting_request_name.getText().toString() + "\",\"relationship\":\""
                            + tv_meeting_request_relationship.getText().toString() + "\",\"jail_id\":1,\"prisoner_number\":\""
                            + prisoner_number + "\",\"type_id\":1}}";
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost post = new HttpPost(MEETING_REQUEST_URL + sp.getString("token", ""));
                    try {
                        StringEntity entity = new StringEntity(body, HTTP.UTF_8);
                        entity.setContentType("application/json");
                        post.setEntity(entity);
                        Log.d("开始发送", body + "---" + MEETING_REQUEST_URL + sp.getString("token", ""));
                        HttpResponse httpResponse = httpClient.execute(post);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                            Message msg = handler.obtainMessage();
                            msg.obj = result;
                            msg.what = 0;
                            handler.sendMessage(msg);
                            Log.d("发送成功", result);
                        } else {
                            String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                            Message msg = handler.obtainMessage();
                            msg.obj = result;
                            msg.what = 1;
                            handler.sendMessage(msg);
                            Log.d("发送失败", result);
                        }
                    } catch (Exception e) {
                        Log.d("发送异常", e.getMessage());
                        handler.sendEmptyMessage(2);
                    }
                }
            }.start();
        }else {
            showToastMsgShort("没有网络");
        }
    }
}
