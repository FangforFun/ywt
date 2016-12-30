package com.gkzxhn.gkprison.app.module;

import com.gkzxhn.gkprison.api.ApiRequest;
import com.gkzxhn.gkprison.api.LoginService;
import com.gkzxhn.gkprison.constant.Constants;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Author: Huang ZN
 * Date: 2016/12/20
 * Email:943852572@qq.com
 * Description:
 */
@Module
public class ApiModule {

    @Provides
    public OkHttpClient provideOkHttpClient(){
        OkHttpClient mOkHttpClient = new OkHttpClient();
        OkHttpClient.Builder builder = mOkHttpClient.newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        builder.interceptors().clear();
        return builder.build();
    }

    @Provides
    public Retrofit provideRetrofit(OkHttpClient okHttpClient){
        return new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    @Provides
    public LoginService provideLoginService(Retrofit retrofit){
        return retrofit.create(LoginService.class);
    }

    @Provides
    public ApiRequest provideApiRequest(Retrofit retrofit){
        return retrofit.create(ApiRequest.class);
    }
}
