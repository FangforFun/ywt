package com.gkzxhn.gkprison.userport.requests;

import com.gkzxhn.gkprison.userport.bean.Letter;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * author:huangzhengneng
 * email:943852572@qq.com
 * date: 2016/8/1.
 * function:写信请求
 */

public interface WriteMessage {

    @POST("mail_boxes")
    Observable<String> sendMessage(
            @Query("jail_id") int id,
            @Query("access_token") String access_token,
            @Body String msg);
}
