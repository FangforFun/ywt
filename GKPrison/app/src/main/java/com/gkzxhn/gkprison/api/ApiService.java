package com.gkzxhn.gkprison.api;

import com.gkzxhn.gkprison.prisonport.bean.FamilyMeetingInfo;
import com.gkzxhn.gkprison.prisonport.bean.MeetingInfo;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * author:huangzhengneng
 * email:943852572@qq.com
 * date: 2016/8/4.
 * function:监狱端请求service
 */

public interface ApiService {

    /**
     * 获取会见列表
     * @param username
     * @param date
     * @return
     */
    @GET("applies")
    Observable<List<MeetingInfo>> getMeetingList(
            @Query("accid") String username,
            @Query("app_date") String date);

    /**
     * 取消视频会见
     * @param id
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @PATCH("applies/{id}")
    Observable<ResponseBody> cancelMeeting(
            @Path("id") int id,
            @Body RequestBody body);

    /**
     * 获取会见详情信息
     * @param id
     * @param token
     * @return
     */
    @GET("families/{family_id}")
    Observable<FamilyMeetingInfo> getMeetingDetailInfo(
            @Path("family_id") int id,
            @Query("access_token") String token);
}
