package com.gkzxhn.gkprison.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.PayService;
import com.gkzxhn.gkprison.api.okhttp.OkHttpUtils;
import com.gkzxhn.gkprison.api.rx.RxUtils;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.constant.WeixinConstants;
import com.gkzxhn.gkprison.userport.ui.main.MainActivity;
import com.gkzxhn.gkprison.userport.ui.pay.PaymentActivity;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Author: Huang ZN
 * Date: 2016/12/28
 * Email:943852572@qq.com
 * Description:微信支付结果回调页面
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = WXPayEntryActivity.class.getSimpleName();
    private IWXAPI api;
    private String token;
    private String times;
    @BindView(R.id.iv_pay_result_icon) ImageView iv_pay_result_icon;
    @BindView(R.id.tv_pay_result) TextView tv_pay_result;
    @BindView(R.id.tv_title) TextView tv_title;
    @BindView(R.id.tv_send_goods) TextView tv_send_goods;
    private int resultCode;// 结果码

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        ButterKnife.bind(this);
        api = WXAPIFactory.createWXAPI(this, WeixinConstants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        tv_title.setText(R.string.pay_result);
        resultCode = resp.errCode;
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            Log.i(TAG, "wx pay response error code : " + resp.errCode + ", error info: " + resp.errStr);
            if (resp.errCode == 0) {
                token = (String) SPUtil.get(this, SPKeyConstants.ACCESS_TOKEN, "");
                times = PaymentActivity.times;
                sendOrderToServer();// 发送订单至服务器  更新
                tv_send_goods.setVisibility(View.VISIBLE);
                tv_pay_result.setTextColor(getResources().getColor(R.color.theme));
            } else if (resp.errCode == -2) {
                iv_pay_result_icon.setImageResource(R.drawable.payfail);
                tv_pay_result.setText(getString(R.string.pay_failed));
                tv_send_goods.setVisibility(View.GONE);
                tv_pay_result.setTextColor(getResources().getColor(R.color.tv_red));
            }else if (resp.errCode == -1){
                ToastUtil.showShortToast(getString(R.string.please_use_release_keystore));
                finish();
            }
        }
    }

    @OnClick(R.id.bt_ok)
    public void onClick(View view){
        Intent intent = new Intent(WXPayEntryActivity.this, MainActivity.class);
        if (resultCode == 0) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (resultCode == -2) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("times", times);
        }
        startActivity(intent);
        finish();
    }

    /**
     * 发送订单至服务器  更新
     */
    private void sendOrderToServer() {
        String trade_no = PaymentActivity.TradeNo;
        String orderInfo = "{\"order\":{\"trade_no\":\"" + trade_no + "\",\"status\":\"WAIT_FOR_NOTIFY\"}}";
        Log.d(TAG, "wx pay order info: " + orderInfo);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        PayService payService = retrofit.create(PayService.class);
        synchronizeSubscription = payService.sendWXPayOrder(token, OkHttpUtils.getRequestBody(orderInfo))
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<ResponseBody>(){
                    @Override public void onError(Throwable e) {
                        ToastUtil.showShortToast(getString(R.string.order_synchronize_failed));
                        Log.i(TAG, "order synchronize failed : " + e.getMessage());
                    }

                    @Override public void onNext(ResponseBody responseBody) {
                        ToastUtil.showShortToast(getString(R.string.pay_success));
                        try {
                            String result = responseBody.string();
                            Log.i(TAG, "order synchronize success : " + result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private Subscription synchronizeSubscription;

    @Override
    protected void onDestroy() {
        RxUtils.unSubscribe(synchronizeSubscription);
        super.onDestroy();
    }
}