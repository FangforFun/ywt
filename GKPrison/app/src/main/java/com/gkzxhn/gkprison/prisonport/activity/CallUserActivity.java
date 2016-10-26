package com.gkzxhn.gkprison.prisonport.activity;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.prisonport.bean.FamilyMeetingInfo;
import com.gkzxhn.gkprison.prisonport.requests.ApiService;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.keda.vconf.dialog.P2PCallDialog;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * created by huangzhengneng on 2016/1/12
 * 准备呼叫页面
 */
public class CallUserActivity extends BaseActivity {

    private static final java.lang.String TAG = "CallUserActivity";

    @BindView(R.id.iv_id_card_01)
    ImageView iv_id_card_01;
    @BindView(R.id.iv_id_card_02)
    ImageView iv_id_card_02;
    @BindView(R.id.ll_id_card_photo)
    LinearLayout ll_id_card_photo;
    @BindView(R.id.rl_getting)
    RelativeLayout rl_getting;
    @BindView(R.id.rl_video_view)
    RelativeLayout rl_video_view;
    @BindView(R.id.tv_meeting_notice)
    TextView tv_meeting_notice;
    @BindView(R.id.rl_meeting_notice)
    RelativeLayout rl_meeting_notice;
    @BindView(R.id.bt_call)
    Button bt_call;// 呼叫按钮  默认是不可用的  当成功解析详细会见信息后恢复可用
    private int family_id;// 家属id
    private FamilyMeetingInfo familyMeetingInfo;// 会见详情信息

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_call_user, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("远程会见");
        setBackVisibility(View.VISIBLE);
        family_id = getIntent().getIntExtra("family_id", 0);
        Log.i(TAG, "family_id : " + family_id);
        getMeetingDetailInfo(family_id);// 获取会见对象详情信息
    }

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
        apiService.getMeetingDetailInfo(family_id, (String) SPUtil.get(CallUserActivity.this, "token", ""))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FamilyMeetingInfo>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "get detail info failed : " + e.getMessage());
                        rl_getting.setVisibility(View.GONE);
                        showToastMsgShort("获取身份证照片失败");
                    }

                    @Override
                    public void onNext(FamilyMeetingInfo familyMeetingInfo) {
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
            Log.i(TAG, "detail info img : " + Constants.RESOURSE_HEAD + img_urls[0] + "---" + Constants.URL_HEAD + img_urls[1]);
            SPUtil.put(this, "img_url_01", Constants.RESOURSE_HEAD + img_urls[0]);
            SPUtil.put(this, "img_url_02", Constants.RESOURSE_HEAD + img_urls[1]);
            SPUtil.put(this, "img_url_03", Constants.RESOURSE_HEAD + img_urls[2]);
            bt_call.setEnabled(true);
            rl_getting.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.bt_call)
    public void onClick() {
        if (Utils.isNetworkAvailable(this)) {
            SPUtil.put(CallUserActivity.this, "family_accid", familyMeetingInfo.getAccid());
            Log.i(TAG, "Call User Activity ---> " + familyMeetingInfo.getAccid());
            // 呼叫
            new P2PCallDialog(this).show();
        } else {
            showToastMsgShort("没有网络，请检查网络设置");
        }
    }
}
