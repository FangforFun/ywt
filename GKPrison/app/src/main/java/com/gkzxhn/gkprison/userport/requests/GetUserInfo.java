package com.gkzxhn.gkprison.userport.requests;

import com.gkzxhn.gkprison.userport.bean.PrisonerUserInfo;

import java.util.List;
import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * author:huangzhengneng
 * email:943852572@qq.com
 * date: 2016/8/1.
 * function:获取用户信息
 */

public interface GetUserInfo {

    @GET("prisoner")
    Observable<PrisonerUserInfo> getUserInfo(@Query("phone") String ph, @QueryMap Map<String, String> map);
}
