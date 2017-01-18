package com.gkzxhn.gkprison.prisonport.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.app.PerActivity;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.prisonport.bean.MeetingInfo;
import com.gkzxhn.gkprison.prisonport.requests.ApiService;
import com.gkzxhn.gkprison.userport.ui.login.LoginActivity;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.gkzxhn.gkprison.utils.UIUtils;
import com.keda.sky.app.TruetouchGlobal;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Author: Huang ZN
 * Date: 2017/1/17
 * Email:943852572@qq.com
 * Description:
 */
@PerActivity
public class DateMeetingPresenter implements DateMeetingContract.Presenter {

    private static final String TAG = DateMeetingPresenter.class.getSimpleName();
    private DateMeetingContract.View meetingListView;
    private ApiService mService;
    private Context mContext;

    private Subscription requestListSubscription;
    private AlertDialog kickoutDialog;

    @Inject
    public DateMeetingPresenter(Context context, ApiService apiService){
        mService = apiService;
        mContext = context;
    }

    @Override
    public void attachView(@NonNull DateMeetingContract.View view) {
        meetingListView = view;
    }

    @Override
    public void detachView() {
        meetingListView = null;
        UIUtils.dismissAlertDialog(kickoutDialog);
    }

    @Override
    public void checkStatusAndRequestData() {
        StatusCode code = NIMClient.getStatus();
        checkStatusCode(code);// 判断当前用户状态方法
        String formatDate = StringUtils.formatTime(System.currentTimeMillis(), "yyyy-MM-dd");
        if (code == StatusCode.LOGINED) {// 已登录状态才刷新数据
            requestDataList(formatDate);
        }
    }

    /**
     * 请求数据列表
     * @param formatDate
     */
    public void requestDataList(String formatDate) {
        if (SystemUtil.isNetWorkUnAvailable()) {
            meetingListView.showToast(mContext.getString(R.string.net_broken));
            return;
        }
        meetingListView.requestDataStart();
        String accid = (String) SPUtil.get(mContext, SPKeyConstants.USERNAME, "");
        Log.i(TAG, formatDate + "---" + accid);
        requestListSubscription = mService.getMeetingList(accid, formatDate).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<List<MeetingInfo>>(){
            @Override public void onError(Throwable e) {
                Log.i(TAG, "request meeting list failed: " + e.getMessage());
                meetingListView.requestDataFailed();
            }

            @Override public void onNext(List<MeetingInfo> meetingInfoList) {
                meetingListView.requestDataComplete(meetingInfoList);
            }
        });
    }

    /**
     * 判断当前云信id状态
     */
    private void checkStatusCode(StatusCode code) {
        if (code == StatusCode.KICKOUT) {// 被其他端挤掉
            showKickoutDialog();
        } else if (code == StatusCode.CONNECTING) {// 正在连接
            meetingListView.showToast(mContext.getString(R.string.connecting));
        } else if (code == StatusCode.LOGINING) {// 正在登录
            meetingListView.showToast(mContext.getString(R.string.logining));
        } else if (code == StatusCode.NET_BROKEN) { // 网络连接已断开
            meetingListView.showToast(mContext.getString(R.string.net_broken));
        } else if (code == StatusCode.UNLOGIN) {// 未登录
            meetingListView.showToast(mContext.getString(R.string.not_login));
        }
    }

    /**
     * 被挤下线
     */
    private void showKickoutDialog() {
        kickoutDialog = UIUtils.showReLoginDialog(mContext, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                LoginActivity.startActivityClearTask(mContext);
                SPUtil.clear(mContext);
                NIMClient.getService(AuthService.class).logout();
                TruetouchGlobal.logOff();
            }
        });
    }
}
