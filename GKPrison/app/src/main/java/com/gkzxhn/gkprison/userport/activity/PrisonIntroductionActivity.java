package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.userport.view.RollViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 监狱简介
 */
public class PrisonIntroductionActivity extends BaseActivity {

    private WebView wv_news_detail;
    private FrameLayout fl_loading;
    private int id;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_prison_introduction, null);
        wv_news_detail = (WebView) view.findViewById(R.id.wv_news_detail);
        fl_loading = (FrameLayout) view.findViewById(R.id.fl_loading);
        return view;
    }
    // http://10.93.1.10:3000/api/v1/news?access_token=d56e241a101d011c399211e9e24b0acd&jail_id=1
    @Override
    protected void initData() {
        setTitle("");
        setBackVisibility(View.VISIBLE);
        id = getIntent().getIntExtra("id",1);
        wv_news_detail.loadUrl("http://10.93.1.116:3000/jails/1");
        fl_loading.setVisibility(View.VISIBLE);
        wv_news_detail.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                fl_loading.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });
        WebSettings webSettings = wv_news_detail.getSettings();
        webSettings.setJavaScriptEnabled(true);
//        webSettings.setBuiltInZoomControls(true);
//        webSettings.setSupportZoom(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && wv_news_detail.canGoBack()){
            wv_news_detail.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

