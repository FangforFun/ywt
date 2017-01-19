package com.gkzxhn.gkprison.userport.activity;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.base.BaseActivityNew;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.view.pb.NumberProgressBar;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 监狱简介
 */
public class PrisonIntroductionActivity extends BaseActivityNew {

    @BindView(R.id.wv_news_detail) WebView wv_news_detail;
    @BindView(R.id.npb_loading) NumberProgressBar npb_loading;
    @BindView(R.id.tv_title) TextView tv_title;
    @BindView(R.id.rl_back) RelativeLayout rl_back;

    // http://10.93.1.10:3000/api/v1/news?access_token=d56e241a101d011c399211e9e24b0acd&jail_id=1

    @Override
    public int setLayoutResId() {
        return R.layout.activity_prison_introduction;
    }

    @Override
    protected void initUiAndListener() {
        ButterKnife.bind(this);
        tv_title.setText(R.string.prison_introduction);
        rl_back.setVisibility(View.VISIBLE);
        int id = (int) SPUtil.get(this, SPKeyConstants.JAIL_ID, -1);
        wv_news_detail.loadUrl(Constants.RESOURSE_HEAD+"/jails/" + id);
        Log.i("jail_id is :", id + "");
        WebSettings webSettings = wv_news_detail.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);// 开启Dom storage API 功能
        webSettings.setSupportZoom(true);
        npb_loading.setVisibility(View.VISIBLE);
        npb_loading.setReachedBarHeight(10);
        npb_loading.setUnreachedBarHeight(8);
        npb_loading.setProgressTextSize(24);
        wv_news_detail.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                npb_loading.setProgress(newProgress);
                Log.i("loading web view progress ", newProgress + "");
                if(newProgress == 100){
                    npb_loading.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    @Override
    protected void initInjector() {

    }

    @Override
    protected boolean isApplyStatusBarColor() {
        return true;
    }

    @Override
    protected boolean isApplyTranslucentStatus() {
        return true;
    }

    @Override
    public void onBackPressed() {
        if(wv_news_detail.canGoBack()){
            wv_news_detail.goBack();
        }else {
            super.onBackPressed();
        }
    }
}