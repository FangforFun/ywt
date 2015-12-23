package com.gkzxhn.gkprison.prisonport.activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.utils.DensityUtil;

/**
 * 准备呼叫页面
 */
public class CallUserActivity extends BaseActivity {

    private Button bt_call;
    private TextView tv_meeting_request_name;
    private FrameLayout fl_video_view;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_call_user, null);
        bt_call = (Button) view.findViewById(R.id.bt_call);
        tv_meeting_request_name = (TextView) view.findViewById(R.id.tv_meeting_request_name);
        fl_video_view = (FrameLayout) view.findViewById(R.id.fl_video_view);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.getScreenWidthHeight(this)[0] - DensityUtil.dip2px(this, 80));
        fl_video_view.setLayoutParams(params);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("远程会见");
        setBackVisibility(View.VISIBLE);
        bt_call.setOnClickListener(this);
        String name = getIntent().getStringExtra("申请人");
        tv_meeting_request_name.setText(name);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_call:
                showToastMsgShort("呼叫");
                break;
        }
    }
}
