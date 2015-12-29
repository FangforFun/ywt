package com.gkzxhn.gkprison.prisonport.activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.AVChatActivity;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.utils.DensityUtil;

/**
 * 准备呼叫页面
 */
public class CallUserActivity extends BaseActivity {

    private Button bt_call;
    private FrameLayout fl_video_view;
    private String meeting_name;
    private String accid;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_call_user, null);
        bt_call = (Button) view.findViewById(R.id.bt_call);
        fl_video_view = (FrameLayout) view.findViewById(R.id.fl_video_view);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.getScreenWidthHeight(this)[0] - DensityUtil.dip2px(this, 80));
        fl_video_view.setLayoutParams(params);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("远程会见");
        setBackVisibility(View.VISIBLE);
        meeting_name = getIntent().getStringExtra("申请人");
        accid = getIntent().getStringExtra("accid");
        bt_call.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_call:
                showToastMsgShort("呼叫");
                AVChatActivity.start(this, accid, 2, AVChatActivity.FROM_INTERNAL); // 2 视频通话
                break;
        }
    }
}
