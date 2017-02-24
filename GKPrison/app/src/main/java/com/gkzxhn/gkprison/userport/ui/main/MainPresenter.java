package com.gkzxhn.gkprison.userport.ui.main;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.ApiRequest;
import com.gkzxhn.gkprison.api.rx.RxUtils;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.api.wrap.MainWrap;
import com.gkzxhn.gkprison.app.PerActivity;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.userport.bean.PrisonerUserInfo;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.gkzxhn.gkprison.prisonport.ui.CallUserActivity.fileName;
import static com.gkzxhn.gkprison.userport.ui.main.MainUtils.getXml;

/**
 * Author: Huang ZN
 * Date: 2016/12/28
 * Email:943852572@qq.com
 * Description:
 */
@PerActivity
public class MainPresenter implements MainContract.Presenter {

    private static final String TAG = MainPresenter.class.getName();
    private MainContract.View mainView;

    private OkHttpClient okHttpClient;
    private ApiRequest apiRequest;
    private Context mContext;
    private boolean isRegisterUser;

    private Subscription updateOrderSubscription;
    private Subscription getUserInfoSubscription;

    @Inject
    public MainPresenter(OkHttpClient okHttpClient, ApiRequest apiRequest, Context context){
        this.okHttpClient = okHttpClient;
        this.apiRequest = apiRequest;
        mContext = context;
    }


    @Override
    public void attachView(@NonNull MainContract.View view) {
        mainView = view;
    }

    @Override
    public void detachView() {
        mainView = null;
        RxUtils.unSubscribe(getUserInfoSubscription, updateOrderSubscription);
    }

    @Override
    public void checkStatus() {
//        if (SystemUtil.isNetWorkUnAvailable()){
//            // ToDo 没有网络显示默认布局 不进行下一步操作
//            return;
//        }
        StatusCode status = NIMClient.getStatus();
        Log.i(TAG, "nim status is ：" + status);
        isRegisterUser = (boolean) SPUtil.get(mContext, SPKeyConstants.IS_REGISTERED_USER, false);
        Log.i(TAG, "user type ：" + isRegisterUser);
        if (isRegisterUser){
            mainView.showProgress(mContext.getString(R.string.loading));
            // 获取用户信息
            String phone = (String) SPUtil.get(mContext, SPKeyConstants.USERNAME, "");
            String uuid = (String) SPUtil.get(mContext, SPKeyConstants.PASSWORD, "");
            Map<String, String> map = new HashMap<>();
            map.put("phone", phone);
            map.put("uuid", uuid);
            getUserInfoSubscription = MainWrap.getPrisonerUserInfo(apiRequest, map, new SimpleObserver<PrisonerUserInfo>(){
                @Override public void onNext(PrisonerUserInfo prisonerUserInfo) {
                    Log.i(TAG, prisonerUserInfo.getResult().toString());
                    savePrisonerInfo(prisonerUserInfo);
                    mainView.dismissProgress();
                    mainView.getUserInfoSuccess();
                }

                @Override public void onError(Throwable e) {
                    Log.i(TAG, "get user info failed : " + e.getMessage());
                    // 获取用户信息失败  重新登录
                    mainView.dismissProgress();
                    ((MainActivity)mainView).addHomeFragment();
//                    mainView.reLoginNotGetUserInfo();
                }
            });
        }else {
            // 弹出监狱选择框
            mainView.fastLoginWithoutAccount();
        }
        if(status == StatusCode.KICKOUT){
            mainView.accountKickout();// 其他设备登录
        }
    }

    @Override
    public void downloadAvatar(String path) {
        if (!isRegisterUser || SystemUtil.isNetWorkUnAvailable()){
            return;
        }
        Picasso.with(mContext).load(path).into(new Target() {
            @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    File file = new File(fileName);
//                    if (file.exists()){
//                        boolean isDeleteSuccess = file.delete();
//                        Log.i(TAG, "delete exists avatar result: " + isDeleteSuccess);
//                    }
                    FileOutputStream fos =new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                    Log.i(TAG, "avatar已下载至:" + fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "avatar下载异常");
                }
            }

            @Override public void onBitmapFailed(Drawable errorDrawable) {
                Log.i(TAG, "avatar下载失败");
            }

            @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.i(TAG, "avatar准备加载");
            }
        });
    }

    /**
     * 保存囚犯信息
     * @param info
     */
    private void savePrisonerInfo(PrisonerUserInfo info) {
        putSP(SPKeyConstants.PRISON_TERM_STARTED_AT, info.getResult().get(0).getPrison_term_started_at());
        putSP(SPKeyConstants.PRISON_TERM_ENDED_AT, info.getResult().get(0).getPrison_term_ended_at());
        putSP(SPKeyConstants.GENDER, info.getResult().get(0).getGender());
        putSP(SPKeyConstants.PRISONER_NAME, info.getResult().get(0).getName());
        putSP(SPKeyConstants.JAIL_ID, info.getResult().get(0).getJail_id());
        putSP(SPKeyConstants.PRISONER_NUMBER, info.getResult().get(0).getPrisoner_number());
    }

    /**
     * 更新微信订单
     */
    public void doWXPayController(final String times, final SQLiteDatabase db) {
        if (times != null){
            final String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            final String str = getXml();
            updateOrderSubscription = Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    RequestBody body = RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), str);
                    Request request = new Request.Builder().url(url).post(body).build();
                    try {
                        Response response = okHttpClient.newCall(request).execute();
                        String result = response.body().string();
                        subscriber.onNext(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(new Throwable("更新失败:" + e.getMessage()));
                    }
                }
            }).subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<String>(){
                        @Override public void onError(Throwable e) {
                            Log.i(TAG, e.getMessage());
                        }

                        @Override public void onNext(String s) {
                            Log.i(TAG, "update weixin pay order success : " + s);
                            String type = "微信支付";
                            String sql = "update Cart set isfinish = 1,payment_type = '"
                                    +type+"' where time = '" + times + "'";
                            db.execSQL(sql);
                        }
                    });

        }
    }

    /**
     * 存SP
     * @param key
     * @param defaultValue
     */
    private void putSP(String key, Object defaultValue){
        SPUtil.put(mContext, key, defaultValue);
    }
}
