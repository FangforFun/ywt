package com.gkzxhn.gkprison.userport.activity;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.keda.sky.app.PcAppStackManager;

/**
 * 申请结果
 */
public class ApplyResultActivity extends BaseActivity {

    private TextView tv_request_state;//申请状态
    private TextView tv_request_name;// 申请人
    private TextView tv_request_time;// 申请时间
    private LinearLayout ll_request_not_pass_reason;// 探监申请未通过原因
    private LinearLayout ll_request_pass_notice;// 探监申请通过备注
    private LinearLayout ll_meeting_request_pass_notice;// 会见申请通过备注
    private LinearLayout ll_meeting_request_not_pass_reason;// 会见未通过原因
//    private TextView tv_visit_time;
    private TextView tv_meeting_time;
    private TextView tv_meeting_not_pass_reason;
    private TextView tv_time;// 会见/探监时间文本

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(getApplicationContext(), R.layout.activity_apply_result, null);
        tv_request_name = (TextView) view.findViewById(R.id.tv_request_name);
        tv_request_time = (TextView) view.findViewById(R.id.tv_request_time);
        tv_request_state = (TextView) view.findViewById(R.id.tv_request_state);
        ll_request_not_pass_reason = (LinearLayout) view.findViewById(R.id.ll_request_not_pass_reason);
        ll_request_pass_notice = (LinearLayout) view.findViewById(R.id.ll_request_pass_notice);
        ll_meeting_request_pass_notice = (LinearLayout) view.findViewById(R.id.ll_meeting_request_pass_notice);
        ll_meeting_request_not_pass_reason = (LinearLayout) view.findViewById(R.id.ll_meeting_request_not_pass_reason);
        tv_meeting_time = (TextView) view.findViewById(R.id.tv_meeting_time);
        tv_meeting_not_pass_reason = (TextView) view.findViewById(R.id.tv_meeting_not_pass_reason);
        tv_time = (TextView) view.findViewById(R.id.tv_time);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("申请结果页");
        setBackVisibility(View.VISIBLE);
        String result = getIntent().getStringExtra("result");
        String apply_date = getIntent().getStringExtra("apply_date");
        String name = getIntent().getStringExtra("name");
        String reason = getIntent().getStringExtra("reason");
        int type_id = getIntent().getIntExtra("type_id", 0);
        tv_request_name.setText(name);
        tv_request_time.setText(apply_date.contains(" ") ? apply_date.substring(0, apply_date.lastIndexOf(" ")) : apply_date);
        if(type_id == 2){
            if(result.contains("已通过")) {
                String meeting_date = getIntent().getStringExtra("meeting_date");
                Log.i("meeting_date", meeting_date);
//                tv_visit_time.setText(meeting_date.contains("UTC") ? meeting_date.replace("UTC", "") : meeting_date);
                ll_request_pass_notice.setVisibility(View.VISIBLE);
                tv_request_state.setText(result);
                tv_time.setText("探监时间：");
                tv_request_time.setText(apply_date);
                tv_request_state.setTextColor(getResources().getColor(R.color.tv_green));
                ll_request_not_pass_reason.setVisibility(View.GONE);
                ll_meeting_request_pass_notice.setVisibility(View.GONE);
                ll_meeting_request_not_pass_reason.setVisibility(View.GONE);
            }else {
                ll_request_pass_notice.setVisibility(View.GONE);
                tv_request_state.setText(result);
                tv_request_state.setTextColor(getResources().getColor(R.color.tv_red));
                tv_time.setText("申请时间：");
                tv_request_time.setText(apply_date);
                ll_request_not_pass_reason.setVisibility(View.VISIBLE);
                ll_meeting_request_pass_notice.setVisibility(View.GONE);
                ll_meeting_request_not_pass_reason.setVisibility(View.GONE);
            }
        }else if(type_id == 1){
            if(result.contains("已通过")){
                String meeting_date = getIntent().getStringExtra("meeting_date");
                ll_request_pass_notice.setVisibility(View.GONE);
                tv_request_state.setText(result);
                tv_request_state.setTextColor(getResources().getColor(R.color.tv_green));
                ll_request_not_pass_reason.setVisibility(View.GONE);
                ll_meeting_request_not_pass_reason.setVisibility(View.GONE);
                ll_meeting_request_pass_notice.setVisibility(View.VISIBLE);
                tv_meeting_time.setText(meeting_date.contains("UTC") ? meeting_date.replace("UTC", "") : meeting_date);
            }else {
                ll_request_pass_notice.setVisibility(View.GONE);
                tv_request_state.setText("未通过");
                tv_request_state.setTextColor(getResources().getColor(R.color.tv_red));
                ll_request_not_pass_reason.setVisibility(View.GONE);
                ll_meeting_request_not_pass_reason.setVisibility(View.VISIBLE);
                ll_meeting_request_pass_notice.setVisibility(View.GONE);
                tv_meeting_not_pass_reason.setText(reason);
            }
        }
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }

}
