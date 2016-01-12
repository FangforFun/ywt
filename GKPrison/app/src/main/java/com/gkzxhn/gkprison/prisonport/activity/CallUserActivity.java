package com.gkzxhn.gkprison.prisonport.activity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.AVChatActivity;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.lidroid.xutils.BitmapUtils;

/**
 * 准备呼叫页面
 */
public class CallUserActivity extends BaseActivity {

    private Button bt_call;
    private FrameLayout fl_video_view;
    private String meeting_name;
    private String accid;
    private String image_url;
    private LinearLayout ll_id_card_photo;
    private ImageView iv_id_card_01;
    private ImageView iv_id_card_02;
    private RelativeLayout rl_getting;
    private BitmapUtils bitmapUtil;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_call_user, null);
        bt_call = (Button) view.findViewById(R.id.bt_call);
        fl_video_view = (FrameLayout) view.findViewById(R.id.fl_video_view);
        ll_id_card_photo = (LinearLayout) view.findViewById(R.id.ll_id_card_photo);
        iv_id_card_01 = (ImageView) view.findViewById(R.id.iv_id_card_01);
        iv_id_card_02 = (ImageView) view.findViewById(R.id.iv_id_card_02);
        rl_getting = (RelativeLayout) view.findViewById(R.id.rl_getting);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.getScreenWidthHeight(this)[0] - DensityUtil.dip2px(this, 80));
        fl_video_view.setLayoutParams(params);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("远程会见");
        setBackVisibility(View.VISIBLE);
        bitmapUtil = new BitmapUtils(this);
        meeting_name = getIntent().getStringExtra("申请人");
        accid = getIntent().getStringExtra("accid");
        image_url = getIntent().getStringExtra("image_url");
        Log.i("tupian", image_url);
        if(image_url.contains("|")) {
            String[] img_urls = image_url.split("\\|");
            bitmapUtil.display(iv_id_card_01, Constants.URL_HEAD + img_urls[0]);
            bitmapUtil.display(iv_id_card_02, Constants.URL_HEAD + img_urls[1]);
            Log.i("tupian", Constants.URL_HEAD + img_urls[0] + "---" + Constants.URL_HEAD + img_urls[1]);
        }
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
