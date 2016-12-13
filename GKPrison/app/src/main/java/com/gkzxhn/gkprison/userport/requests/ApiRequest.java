package com.gkzxhn.gkprison.userport.requests;

import com.gkzxhn.gkprison.userport.bean.Balance;
import com.gkzxhn.gkprison.userport.bean.News;
import com.gkzxhn.gkprison.userport.bean.PrisonerInfo;
import com.gkzxhn.gkprison.userport.bean.PrisonerUserInfo;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * author:huangzhengneng
 * email:943852572@qq.com
 * date: 2016/8/2.
 * function:retrofit所需service
 */
public interface ApiRequest {

    /**
     * 获取用户对应囚犯信息
     * @param ph 电话
     * @param map uuid
     * @return
     */
    @GET("prisoner")
    Observable<PrisonerUserInfo> getUserInfo(@Query("phone") String ph, @QueryMap Map<String, String> map);


    /**
     * 发送意见反馈
     * @param token
     * @param msg 反馈内容
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("feedback")
    Observable<Object> sendOpinion(@Query("access_token") String token, @Body RequestBody msg);

    /**
     * 写信
     * @param id 监狱id
     * @param access_token
     * @param msg 内容
     * @return
     */
    @Headers({"Content-Type:application/json"})
    @POST("mail_boxes")
    Observable<Object> sendMessage(
            @Query("jail_id") int id,
            @Query("access_token") String access_token,
            @Body RequestBody msg);

    /**
     * 获取新闻
     * @param jail_id
     * @return
     */
    @GET("news")
    Observable<List<News>> getNews(@Query("jail_id") int jail_id);

    /**
     * 获取剩余可会见次数
     * @param f_id
     * @return
     */
    @GET("families/{family_id}")
    Observable<Balance> getBalance(@Path("family_id") int f_id);

    /**
     * 发送远程会见申请
     * @param token
     * @param body
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("apply")
    Observable<ResponseBody> sendMeetingRequest(
            @Query("access_token") String token,
            @Body RequestBody body);

    /**
     * 发送支付方式
     * @param jail_id
     * @param access_token
     * @param body
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("prepay")
    Observable<ResponseBody> sendPaymentType(
            @Query("jail_id") String jail_id,
            @Query("access_token") String access_token,
            @Body RequestBody body
    );

    /**
     * 家属服务 获取囚犯信息
     * @param access_token
     * @return
     */
    @GET("services")
    Observable<PrisonerInfo> getPrisonerInfo(
            @Query("access_token") String access_token
    );

    /**
     * 汇款订单
     * @param jail_id
     * @param token
     * @param body
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("orders")
    Observable<ResponseBody> sendOrder(
            @Query("jail_id") int jail_id,
            @Query("access_token") String token,
            @Body RequestBody body
    );
}
