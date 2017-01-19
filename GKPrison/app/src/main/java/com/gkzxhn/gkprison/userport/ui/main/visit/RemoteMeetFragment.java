package com.gkzxhn.gkprison.userport.ui.main.visit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.ApiRequest;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.base.BaseFragmentNew;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.ui.ReChargeActivity;
import com.gkzxhn.gkprison.userport.bean.Balance;
import com.gkzxhn.gkprison.userport.ui.main.MainUtils;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.gkzxhn.gkprison.utils.Utils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * author:huangzhengneng
 * email:943852572@qq.com
 * date: 2016/8/2.
 * function:会见页面fragment
 */

public class RemoteMeetFragment extends BaseFragmentNew implements AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener{

    private static final String[] REQUEST_TIME = Utils.afterNDay(30).toArray(new String[Utils.afterNDay(30).size()]);// 时间选择
    private static final String TAG = "RemoteMeetFragment";

    @BindView(R.id.rl_meeting) RelativeLayout rl_meeting;// 会见
    @BindView(R.id.rl_visit) RelativeLayout rl_visit;// 探监
    @BindView(R.id.tv_meeting_request_name) TextView tv_meeting_request_name;// 会见申请姓名
    @BindView(R.id.tv_meeting_request_id_num) TextView tv_meeting_request_id_num;// 会见申请身份证
    @BindView(R.id.tv_meeting_request_relationship) TextView tv_meeting_request_relationship;// 会见申请人与服刑人员关系
    @BindView(R.id.tv_meeting_request_phone) TextView tv_meeting_request_phone;// 会见申请电话号码
    @BindView(R.id.bs_meeting_request_time) Spinner sp_meeting_request_time;// 会见申请时间
    @BindView(R.id.tv_meeting_last_time) TextView tv_meeting_last_time;// 上次会见时间
    @BindView(R.id.bt_commit_request) Button bt_commit_request;// 提交会见申请按钮
    @BindView(R.id.bs_visit_request_time) Spinner sp_visit_request_time;// 探监申请时间
    @BindView(R.id.tv_visit_request_name) TextView tv_visit_request_name;// 探监申请姓名
    @BindView(R.id.tv_visit_request_relationship) TextView tv_visit_request_relationship;// 探监申请又服刑人员关系
    @BindView(R.id.tv_visit_request_id_num) TextView tv_visit_request_id_num;// 探监申请身份证
    @BindView(R.id.tv_visit_request_phone) TextView tv_visit_request_phone;// 探监申请手机号
    @BindView(R.id.bt_commit_request_visit) Button bt_commit_request_visit;// 探监申请按钮
    @BindView(R.id.rg_top_guide) RadioGroup rg_top_guide;// 顶部页面切换
    @BindView(R.id.rb_top_guide_meeting) RadioButton rb_top_guide_meeting;
    @BindView(R.id.rb_top_guide_visit) RadioButton rb_top_guide_visit;
    @BindView(R.id.tv_remotely_visit_num) TextView tv_remotly_num;
    @BindView(R.id.bt_remotely) TextView bt_recharge;

    private boolean isCommonUser;// 普通用户/监狱用户
    private int family_id = 0;
    private int vedionum;

    private String meeting_request_time = ""; // 会见申请时间
    private String visit_request_time = "";
    private ProgressDialog dialog;
    private String id_num;// 身份证号
    private String name;// 姓名
    private String username;// 用户名
    private String relationship;// 与囚犯关系
    private String last_meeting_time;// 上次会见时间

    /**
     * 解析实地探监申请结果
     * @param result
     */
    private void checkVisitRequestResult(String result) {
        dialog.dismiss();
        bt_commit_request_visit.setEnabled(true);
        int code = MainUtils.getJsonCode(result);
        AlertDialog.Builder visit_builder = new AlertDialog.Builder(getActivity());
        View visit_success_dialog_view = View.inflate(getActivity(), R.layout.msg_ok_cancel_dialog, null);
        visit_builder.setCancelable(false);
        View view_01_ = visit_success_dialog_view.findViewById(R.id.view_01);
        view_01_.setVisibility(View.GONE);
        TextView tv_msg_dialog_ = (TextView) visit_success_dialog_view.findViewById(R.id.tv_msg_dialog);
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
        if(code == 200) {
            tv_msg_dialog_.setText(R.string.apply_meeting_success);
            visit_success_dialog.show();
            String committed_time = (String) SPUtil.get(getActivity(), SPKeyConstants.COMMITTED_TIME, "");
            SPUtil.put(getActivity(), SPKeyConstants.COMMITTED_TIME, committed_time + visit_request_time + "/");
        }else {
            String reason = MainUtils.getApplyFailedResult(result);
            String text = getString(R.string.commit_failed_reason) + reason;
            tv_msg_dialog_.setText(text);
            visit_success_dialog.show();
        }
    }

    /**
     * 远程会见请求结果
     * @param result
     */
    private void checkRequestMeetResult(String result) {
        dialog.dismiss();
        bt_commit_request.setEnabled(true);
        int code = MainUtils.getJsonCode(result);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        View commit_success_dialog_view = View.inflate(getActivity(), R.layout.msg_ok_cancel_dialog, null);
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
            tv_msg_dialog.setText(R.string.apply_meeting_success);
            commit_success_dialog.show();
            String committed_meeting_time = (String) SPUtil.get(getActivity(),SPKeyConstants.COMMITTED_MEETING_TIME, "");
            SPUtil.put(getActivity(), SPKeyConstants.COMMITTED_MEETING_TIME, committed_meeting_time + meeting_request_time + "/");
        }else {
            String reason = MainUtils.getApplyFailedResult(result);
            String text = getString(R.string.commit_failed_reason) + reason;
            tv_msg_dialog.setText(text);
            commit_success_dialog.show();
        }
    }

    @Override
    protected void initUiAndListener(View view) {
        ButterKnife.bind(this, view);
        Drawable[] drawables = rb_top_guide_meeting.getCompoundDrawables();
        drawables[0].setBounds(0, 0, getActivity().getResources().getDimensionPixelSize(R.dimen.home_tab_width), getActivity().getResources().getDimensionPixelSize(R.dimen.visit_tab_height));
        rb_top_guide_meeting.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        Drawable[] drawables2 = rb_top_guide_visit.getCompoundDrawables();
        drawables2[0].setBounds(0, 0, getActivity().getResources().getDimensionPixelSize(R.dimen.home_tab_width), getActivity().getResources().getDimensionPixelSize(R.dimen.visit_tab_height));
        rb_top_guide_visit.setCompoundDrawables(drawables2[0], drawables2[1], drawables2[2], drawables2[3]);
    }

    @Override
    protected int setLayoutResId() {
        return R.layout.fragment_remote_meeting;
    }

    @Override
    protected void initData() {
        id_num = (String) SPUtil.get(getActivity(), SPKeyConstants.PASSWORD, "");
        family_id = (int) SPUtil.get(getActivity(), SPKeyConstants.FAMILY_ID, -1);
        isCommonUser = (boolean) SPUtil.get(getActivity(), SPKeyConstants.IS_COMMON_USER, false);
        getBalance(); // 获取余额
        name = (String) SPUtil.get(getActivity(), SPKeyConstants.NAME, "");
        username = (String) SPUtil.get(getActivity(), SPKeyConstants.USERNAME, "");
        relationship = (String) SPUtil.get(getActivity(), SPKeyConstants.RELATION_SHIP, "");
        last_meeting_time = getString(R.string.last_meeting_time) + SPUtil.get(getActivity(),
                SPKeyConstants.LAST_MEETING_TIME, getString(R.string.no_meeting));
        if(isCommonUser){
            tv_meeting_request_name.setText(name);
            String start_ = id_num.substring(0, 5);
            String end_ = id_num.substring(id_num.length() - 4, id_num.length());
            tv_meeting_request_id_num.setText(start_ + "******" + end_);// 显示身份证前4位和后4位
            tv_meeting_request_phone.setText(username);
            tv_meeting_request_relationship.setText(relationship);
            tv_meeting_last_time.setText(last_meeting_time);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, REQUEST_TIME);
        ArrayAdapter<String> visit_adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, REQUEST_TIME);
        sp_meeting_request_time.setAdapter(adapter);
        sp_visit_request_time.setAdapter(visit_adapter);
        sp_meeting_request_time.setOnItemSelectedListener(this);
        sp_visit_request_time.setOnItemSelectedListener(this);
        rg_top_guide.setOnCheckedChangeListener(this);
    }

    /**
     * 获取申请会见可用余额
     */
    private void getBalance() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiRequest api = retrofit.create(ApiRequest.class);
        api.getBalance(family_id)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Balance>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        Log.i(TAG, "get balance failed : " + e.getMessage());
                        vedionum = 0;
                        tv_remotly_num.setText(vedionum + "");
                    }

                    @Override public void onNext(Balance result) {
                        Log.i(TAG, "get balance success : " + result.toString());
                        int code = result.getCode();
                        if(code == 200) {
                            Balance.FamilyBean bean = result.getFamily();
                            String num = bean.getBalance();
                            float a = Float.parseFloat(num);
                            int n = (int) a;
                            vedionum = n / 5;
                        }else {
                            vedionum = 0;
                        }
                        tv_remotly_num.setText(String.valueOf(vedionum));
                    }
                });
    }

    /**
     * 设置上次会见时间
     */
    public void setLastMeetingTime(){
        String text = getString(R.string.last_meeting_time) + SPUtil.get(getActivity(),
                SPKeyConstants.LAST_MEETING_TIME, getString(R.string.no_meeting));
        tv_meeting_last_time.setText(text);
    }

    @OnClick({R.id.bt_commit_request, R.id.bt_commit_request_visit, R.id.bt_remotely})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_commit_request:
                if(isCommonUser) {
                    if(!TextUtils.isEmpty(meeting_request_time)) {
                        String committed_meeting_time = (String) SPUtil.get(getActivity(), SPKeyConstants.COMMITTED_MEETING_TIME, "");
                        if(committed_meeting_time.contains(meeting_request_time)){
                            showToastMsgLong(getString(R.string.requested_choose_other));
                            return;
                        }else if (vedionum == 0){
                            showToastMsgShort(getString(R.string.balance_insufficient));
                            return;
                        }else {
                            sendMeetingRequestToServer();
                        }
                    }else {
                        showToastMsgShort(getString(R.string.choose_meeting_time));
                        return;
                    }
                }else {
                    showToastMsgShort(getString(R.string.please_pre_login));
                }
                break;
            case R.id.bt_commit_request_visit:
                if(isCommonUser) {
                    if(!TextUtils.isEmpty(visit_request_time)) {
                        String committed_time = (String) SPUtil.get(getActivity(), SPKeyConstants.COMMITTED_TIME, "");
                        if(committed_time != null && committed_time.contains(visit_request_time)){
                            showToastMsgLong("您已申请过当日实地探监，请选择其他日期。");
                            return;
                        }else {
                            sendVisitRequestToServer();
                        }
                    }else {
                        showToastMsgShort(getString(R.string.choose_visit_time));
                        return;
                    }
                }else {
                    showToastMsgShort(getString(R.string.please_pre_login));
                }
                break;
            case R.id.bt_remotely:
                Intent intent = new Intent(getActivity(), ReChargeActivity.class);
                getActivity().startActivity(intent);
                break;
        }
    }

    /**
     * 发送探监申请至服务器
     */
    private void sendVisitRequestToServer() {
        if(!SystemUtil.isNetWorkUnAvailable()) {
            bt_commit_request_visit.setEnabled(false);
            showProgressDialog();
            String prisoner_number = (String) SPUtil.get(getActivity(), "prisoner_number", "4000002");
            String param = "{\"apply\":{\"phone\":\"" + SPUtil.get(getActivity(), "username", "")
                    + "\",\"uuid\":\"" + SPUtil.get(getActivity(), "password", "") + "\",\"app_date\":\""
                    + visit_request_time + "\",\"name\":\"" + tv_visit_request_name.getText().toString()
                    + "\",\"relationship\":\"" + tv_visit_request_relationship.getText().toString()
                    + "\",\"jail_id\":" + SPUtil.get(getActivity(), "jail_id", 1)
                    + ",\"prisoner_number\":\"" + prisoner_number + "\",\"type_id\":2}}";
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.URL_HEAD)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiRequest api = retrofit.create(ApiRequest.class);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), param);
            api.sendMeetingRequest(SPUtil.get(getActivity(), "token", "") + "", body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<ResponseBody>() {
                        @Override public void onError(Throwable e) {
                            Log.e(TAG, "send visit request failed : " + e.getMessage());
                            dialog.dismiss();
                            showToastMsgLong(getString(R.string.commit_execption_retry));
                            bt_commit_request_visit.setEnabled(true);
                        }

                        @Override public void onNext(ResponseBody responseBody) {
                            try {
                                String result = responseBody.string();
                                Log.i(TAG, "send visit request success : " + result);
                                checkVisitRequestResult(result);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }else {
            showToastMsgShort(getString(R.string.network_unavailable));
        }
    }

    /**
     * 发送会见申请至服务器
     */
    private void sendMeetingRequestToServer() {
        if(!SystemUtil.isNetWorkUnAvailable()) {
            bt_commit_request.setEnabled(false);
            showProgressDialog();
            String prisoner_number = (String) SPUtil.get(getActivity(), "prisoner_number", "");
            String param = "{\"apply\":{\"phone\":\"" + SPUtil.get(getActivity(), "username", "") + "\",\"uuid\":\""
                    + SPUtil.get(getActivity(), "password", "") + "\",\"app_date\":\"" + meeting_request_time
                    + "\",\"name\":\"" + tv_meeting_request_name.getText().toString() + "\",\"relationship\":\""
                    + tv_meeting_request_relationship.getText().toString() + "\",\"jail_id\":"
                    + SPUtil.get(getActivity(), "jail_id", 1) + ",\"prisoner_number\":\""
                    + prisoner_number + "\",\"type_id\":1}}";
            Log.i(TAG, param);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.URL_HEAD)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), param);
            ApiRequest api = retrofit.create(ApiRequest.class);
            api.sendMeetingRequest(SPUtil.get(getActivity(), "token", "") + "", body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "send meet request failed : " + e.getMessage());
                            showToastMsgLong(getString(R.string.commit_execption_retry));
                            bt_commit_request.setEnabled(true);
                            dialog.dismiss();
                        }

                        @Override
                        public void onNext(ResponseBody response) {
                            try {
                                String result = response.string();
                                Log.i(TAG, "send meet request success : " + result);
                                checkRequestMeetResult(result);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "send meet request exception : " + e.getMessage());
                            }
                        }
                    });
        }else {
            showToastMsgShort(getString(R.string.network_unavailable));
        }
    }

    /**
     * 初始化并显示加载进度条对话框
     */
    private void showProgressDialog() {
        dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("正在提交，请稍后");
        dialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.bs_meeting_request_time){
            meeting_request_time = REQUEST_TIME[position];
        }else if(parent.getId() == R.id.bs_visit_request_time){
            visit_request_time = REQUEST_TIME[position];
        }
    }
    @Override public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_top_guide_meeting) {
            rl_meeting.setVisibility(View.VISIBLE);
            rl_visit.setVisibility(View.GONE);
            if (isCommonUser) {
                tv_meeting_request_name.setText(name);
                String start_ = id_num.substring(0, 5);
                String end_ = id_num.substring(id_num.length() - 4, id_num.length());
                tv_meeting_request_id_num.setText(start_ + "******" + end_);// 显示身份证前4位和后4位
                tv_meeting_request_phone.setText(username);
                tv_meeting_request_relationship.setText(relationship);
                tv_meeting_last_time.setText(last_meeting_time);
            }
        }else if(checkedId == R.id.rb_top_guide_visit){
            rl_meeting.setVisibility(View.GONE);
            rl_visit.setVisibility(View.VISIBLE);
            if (isCommonUser) {
                tv_visit_request_name.setText(name);
                String start_ = id_num.substring(0, 5);
                String end_ = id_num.substring(id_num.length() - 4, id_num.length());
                tv_meeting_request_id_num.setText(start_ + "******" + end_);// 显示身份证前4位和后4位
                tv_visit_request_phone.setText(username);
                tv_visit_request_relationship.setText(relationship);
            }
        }
    }
}
