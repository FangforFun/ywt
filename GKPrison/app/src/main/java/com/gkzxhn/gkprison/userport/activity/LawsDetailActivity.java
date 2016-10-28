package com.gkzxhn.gkprison.userport.activity;

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
import com.keda.sky.app.PcAppStackManager;

/**
 * 法律法规详情页
 */
public class LawsDetailActivity extends BaseActivity {

    private WebView wv_news_detail;
    private NumberProgressBar npb_loading;
    private int id;

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(this, R.layout.activity_laws_detail, null);
        wv_news_detail = (WebView) view.findViewById(R.id.wv_news_detail);
        npb_loading = (NumberProgressBar) view.findViewById(R.id.npb_loading);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("");
        setBackVisibility(View.VISIBLE);
        id = getIntent().getIntExtra("id", 1);
        wv_news_detail.loadUrl(Constants.RESOURSE_HEAD + "/laws/" + id + "");
        wv_news_detail.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        npb_loading.setVisibility(View.VISIBLE);
        npb_loading.setReachedBarHeight(10);
        npb_loading.setUnreachedBarHeight(8);
        npb_loading.setProgressTextSize(24);
        WebSettings webSettings = wv_news_detail.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);// 开启Dom storage API 功能
        webSettings.setSupportZoom(true);
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
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
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