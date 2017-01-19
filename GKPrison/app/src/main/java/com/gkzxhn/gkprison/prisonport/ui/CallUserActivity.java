package com.gkzxhn.gkprison.prisonport.ui;

import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.rx.RxUtils;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.base.BaseActivityNew;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.prisonport.bean.FamilyMeetingInfo;
import com.gkzxhn.gkprison.api.ApiService;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.keda.vconf.dialog.P2PCallDialog;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * created by huangzhengneng on 2016/1/12
 * 准备呼叫页面
 */
public class CallUserActivity extends BaseActivityNew {

    private static final java.lang.String TAG = "CallUserActivity";

    public static final String fileName = Environment.getExternalStorageDirectory() + "/avatar.png";

    @BindView(R.id.tv_title) TextView tv_title;
    @BindView(R.id.rl_back) RelativeLayout rl_back;
    @BindView(R.id.iv_id_card_01) ImageView iv_id_card_01;
    @BindView(R.id.iv_id_card_02) ImageView iv_id_card_02;
    @BindView(R.id.ll_id_card_photo) LinearLayout ll_id_card_photo;
    @BindView(R.id.rl_getting) RelativeLayout rl_getting;
    @BindView(R.id.rl_video_view) RelativeLayout rl_video_view;
    @BindView(R.id.tv_meeting_notice) TextView tv_meeting_notice;
    @BindView(R.id.rl_meeting_notice) RelativeLayout rl_meeting_notice;
    @BindView(R.id.bt_call) Button bt_call;// 呼叫按钮  默认是不可用的  当成功解析详细会见信息后恢复可用
    private FamilyMeetingInfo familyMeetingInfo;// 会见详情信息

    private Subscription getInfo;
    private P2PCallDialog p2PCallDialog;

    /**
     * 获取会见的详细信息
     *
     * @param family_id 家属id
     */
    private void getMeetingDetailInfo(final int family_id) {
        rl_getting.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        getInfo = apiService.getMeetingDetailInfo(family_id, (String) SPUtil.get(CallUserActivity.this, SPKeyConstants.ACCESS_TOKEN, ""))
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<FamilyMeetingInfo>() {
                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "get detail info failed : " + e.getMessage());
                        rl_getting.setVisibility(View.GONE);
                        ToastUtil.showShortToast(getString(R.string.get_id_photo_failed));
                    }

                    @Override public void onNext(FamilyMeetingInfo familyMeetingInfo) {
                        Log.i(TAG, "get detail info success : " + familyMeetingInfo.toString());
                        CallUserActivity.this.familyMeetingInfo = familyMeetingInfo;
                        setImageResources();
                    }
                });
    }

    /**
     * 设置身份证正反面照
     */
    private void setImageResources() {
        String image_url = familyMeetingInfo.getFamily().getImage_url();
        if (image_url.contains("|")) {
            String[] img_urls = image_url.split("\\|");
            Picasso.with(this).load(Constants.RESOURSE_HEAD + img_urls[0]).error(R.drawable.default_img).into(iv_id_card_01);
            Picasso.with(this).load(Constants.RESOURSE_HEAD + img_urls[1]).error(R.drawable.default_img).into(iv_id_card_02);
            Log.i(TAG, "detail info img : " + Constants.RESOURSE_HEAD + img_urls[0] + "---" + Constants.RESOURSE_HEAD + img_urls[1]);
            SPUtil.put(this, "img_url_01", Constants.RESOURSE_HEAD + img_urls[0]);
            SPUtil.put(this, "img_url_02", Constants.RESOURSE_HEAD + img_urls[1]);
            SPUtil.put(this, "img_url_03", Constants.RESOURSE_HEAD + img_urls[2]); // 头像
            bt_call.setEnabled(true);
            rl_getting.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.bt_call, R.id.rl_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_call:
                if (!SystemUtil.isNetWorkUnAvailable()) {
                    SPUtil.put(CallUserActivity.this, "family_accid", familyMeetingInfo.getAccid());
                    Log.i(TAG, "Call User Activity ---> " + familyMeetingInfo.getAccid());
                    // 呼叫
                    p2PCallDialog = new P2PCallDialog(this);
                    p2PCallDialog.show();
                } else {
                    ToastUtil.showShortToast(getString(R.string.net_broken));
                }
                break;
            case R.id.rl_back:
                finish();
                break;
        }
    }

    @Override
    public int setLayoutResId() {
        return R.layout.activity_call_user;
    }

    @Override
    protected void initUiAndListener() {
        ButterKnife.bind(this);
        tv_title.setText(R.string.remote_meeting);
        rl_back.setVisibility(View.VISIBLE);
        int family_id = getIntent().getIntExtra(SPKeyConstants.FAMILY_ID, 0);
        Log.i(TAG, "family_id : " + family_id);
        getMeetingDetailInfo(family_id);// 获取会见对象详情信息
    }

    @Override protected void initInjector() {}

    @Override
    protected boolean isApplyStatusBarColor() {
        return true;
    }

    @Override
    protected boolean isApplyTranslucentStatus() {
        return true;
    }

    @Override
    protected void onDestroy() {
        if (p2PCallDialog != null){
            if (p2PCallDialog.isShowing()){
                p2PCallDialog.dismiss();
            }
            p2PCallDialog = null;
        }
        RxUtils.unSubscribe(getInfo);
        super.onDestroy();
    }
}
