package com.gkzxhn.gkprison.userport.activity;

import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;

/**
 * 新闻详情页
 */
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
        int id = getIntent().getIntExtra("id",-1);
        WebSettings webSettings = wv_news_detail.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);// 开启Dom storage API 功能
        wv_news_detail.loadUrl(Constants.RESOURSE_HEAD + "/news/" + id);
        fl_loading.setVisibility(View.VISIBLE);
        wv_news_detail.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                fl_loading.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });
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
