package com.gkzxhn.gkprison.api.wrap;

import com.gkzxhn.gkprison.api.ApiRequest;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.News;
import com.gkzxhn.gkprison.userport.bean.PrisonerUserInfo;

import java.util.List;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Author: Huang ZN
 * Date: 2016/12/28
 * Email:943852572@qq.com
 * Description:主页面相关网络操作简单封装
 */

public class MainWrap {

    /**
     * 获取囚犯信息
     * @param request
     * @param map
     * @param observer
     * @return
     */
    public static Subscription getPrisonerUserInfo(ApiRequest request,
           Map<String, String> map, Observer<PrisonerUserInfo> observer){
        return request.getUserInfo(map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 主页的获取新闻
     * @param jail_id
     * @param observer
     * @return
     */
    public static Subscription getMainNews(int jail_id, Observer<List<News>> observer){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiRequest apiRequest = retrofit.create(ApiRequest.class);
        return apiRequest.getNews(jail_id)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
