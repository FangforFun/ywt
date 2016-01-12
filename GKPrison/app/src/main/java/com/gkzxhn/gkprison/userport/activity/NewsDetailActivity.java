package com.gkzxhn.gkprison.userport.activity;

import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;


public class NewsDetailActivity extends BaseActivity {

    private WebView wv_news_detail;
    private FrameLayout fl_loading;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_news_detail, null);
        wv_news_detail = (WebView) view.findViewById(R.id.wv_news_detail);
        fl_loading = (FrameLayout) view.findViewById(R.id.fl_loading);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("");
        setBackVisibility(View.VISIBLE);
        wv_news_detail.loadUrl("http://192.168.169.5:3000/jails/1");
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
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
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
