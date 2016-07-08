package com.gkzxhn.gkprison.userport.activity;

import android.content.SharedPreferences;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.view.pb.NumberProgressBar;
import com.gkzxhn.gkprison.utils.Log;

/**
 * 监狱简介
 */
public class PrisonIntroductionActivity extends BaseActivity {

    private WebView wv_news_detail;
    private NumberProgressBar npb_loading;
    private int id;
    private SharedPreferences sp;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_prison_introduction, null);
        wv_news_detail = (WebView) view.findViewById(R.id.wv_news_detail);
        npb_loading = (NumberProgressBar) view.findViewById(R.id.npb_loading);
        return view;
    }
    // http://10.93.1.10:3000/api/v1/news?access_token=d56e241a101d011c399211e9e24b0acd&jail_id=1
    @Override
    protected void initData() {
        setTitle("");
        setBackVisibility(View.VISIBLE);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        id = sp.getInt("jail_id",1);
        wv_news_detail.loadUrl(Constants.RESOURSE_HEAD+"/jails/" + id);
        Log.i("jail_id is :", id + "");
        wv_news_detail.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        WebSettings webSettings = wv_news_detail.getSettings();
        webSettings.setJavaScriptEnabled(true);
//        webSettings.setBuiltInZoomControls(true);
//        webSettings.setSupportZoom(true);
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
    public void onBackPressed() {
        if(wv_news_detail.canGoBack()){
            wv_news_detail.goBack();
        }else {
            super.onBackPressed();
        }
    }
}