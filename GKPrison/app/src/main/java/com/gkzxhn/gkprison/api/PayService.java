package com.gkzxhn.gkprison.api;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Author: Huang ZN
 * Date: 2017/1/9
 * Email:943852572@qq.com
 * Description:
 */

public interface PayService {

    /**
     * 发送支付方式至服务器
     * @param map
     * @param body
     * @return
     */
    @POST("prepay")
    Observable<ResponseBody> sendPaymentType(
            @QueryMap Map<String, String> map,
            @Body RequestBody body);

}
