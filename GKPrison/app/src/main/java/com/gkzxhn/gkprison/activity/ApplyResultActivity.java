package com.gkzxhn.gkprison.activity;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

/**
 * 申请结果
 */
public class ApplyResultActivity extends BaseActivity {

    private TextView tv_request_state;//申请状态
    private LinearLayout ll_request_not_pass_reason;// 探监申请未通过原因
    private LinearLayout ll_request_pass_notice;// 探监申请通过备注
    private LinearLayout ll_meeting_request_pass_notice;// 会见申请通过备注
    private LinearLayout ll_meeting_request_not_pass_reason;// 会见未通过原因
    private String type;

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.activity_apply_result, null);
        tv_request_state = (TextView) view.findViewById(R.id.tv_request_state);
        ll_request_not_pass_reason = (LinearLayout) view.findViewById(R.id.ll_request_not_pass_reason);
        ll_request_pass_notice = (LinearLayout) view.findViewById(R.id.ll_request_pass_notice);
        ll_meeting_request_pass_notice = (LinearLayout) view.findViewById(R.id.ll_meeting_request_pass_notice);
        ll_meeting_request_not_pass_reason = (LinearLayout) view.findViewById(R.id.ll_meeting_request_not_pass_reason);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("申请结果页");
        setBackVisibility(View.VISIBLE);
        type = getIntent().getStringExtra("type");
        if(type.equals("探监已通过")){
            ll_request_pass_notice.setVisibility(View.VISIBLE);
            tv_request_state.setText("已通过");
            tv_request_state.setTextColor(getResources().getColor(R.color.tv_green));
            ll_request_not_pass_reason.setVisibility(View.GONE);
            ll_meeting_request_pass_notice.setVisibility(View.GONE);
            ll_meeting_request_not_pass_reason.setVisibility(View.GONE);
        }else if(type.equals("探监未通过")){
            ll_request_pass_notice.setVisibility(View.GONE);
            tv_request_state.setText("未通过");
            tv_request_state.setTextColor(getResources().getColor(R.color.tv_red));
            ll_request_not_pass_reason.setVisibility(View.VISIBLE);
            ll_meeting_request_pass_notice.setVisibility(View.GONE);
            ll_meeting_request_not_pass_reason.setVisibility(View.GONE);
        }else if(type.equals("会见未通过")){
            ll_request_pass_notice.setVisibility(View.GONE);
            tv_request_state.setText("未通过");
            tv_request_state.setTextColor(getResources().getColor(R.color.tv_red));
            ll_request_not_pass_reason.setVisibility(View.GONE);
            ll_meeting_request_not_pass_reason.setVisibility(View.VISIBLE);
            ll_meeting_request_pass_notice.setVisibility(View.GONE);
        }else if(type.equals("会见已通过")){
            ll_request_pass_notice.setVisibility(View.GONE);
            tv_request_state.setText("已通过");
            tv_request_state.setTextColor(getResources().getColor(R.color.tv_green));
            ll_request_not_pass_reason.setVisibility(View.GONE);
            ll_meeting_request_not_pass_reason.setVisibility(View.GONE);
            ll_meeting_request_pass_notice.setVisibility(View.VISIBLE);
        }
    }
}
