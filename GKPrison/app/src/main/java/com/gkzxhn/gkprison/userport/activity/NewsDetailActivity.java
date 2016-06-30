package com.gkzxhn.gkprison.userport.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;

/**
 * 新闻详情页
 */
public class NewsDetailActivity extends BaseActivity {

    private WebView wv_news_detail;
    private FrameLayout fl_loading;
    private String webUrl;
    private int type;

    private LinearLayout ll_comment;
    private EditText et_comment;
    private Button bt_comment;
    private TextView tv_comments;

    // 评论内容
    private String comment_content;
    private boolean has_comment = true;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_news_detail, null);
        wv_news_detail = (WebView) view.findViewById(R.id.wv_news_detail);
        fl_loading = (FrameLayout) view.findViewById(R.id.fl_loading);
        ll_comment = (LinearLayout) view.findViewById(R.id.ll_comment);
        et_comment = (EditText) view.findViewById(R.id.et_comment);
        bt_comment = (Button) view.findViewById(R.id.bt_comment);
        tv_comments = (TextView) view.findViewById(R.id.tv_comments);
        Drawable[] drawables = tv_comments.getCompoundDrawables();
        drawables[0].setBounds(7, 0, DensityUtil.dip2px(this, 20), DensityUtil.dip2px(this, 20));
        tv_comments.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("");
        setBackVisibility(View.VISIBLE);
        int id = getIntent().getIntExtra("id",-1);
        // type=0 首页轮播图  type=1 新闻  默认为1
        type = getIntent().getIntExtra("type", 1);
        has_comment = getIntent().getBooleanExtra("has_comment", true);
        Log.i("------------", has_comment + "");
        if(type == 1) {
            webUrl = Constants.RESOURSE_HEAD + "/news/" + id;
        }else {
            int index = getIntent().getIntExtra("index", 1);
            webUrl = "https://www.fushuile.com/app/" + index;
        }
        WebSettings webSettings = wv_news_detail.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);// 开启Dom storage API 功能
        wv_news_detail.loadUrl(webUrl);
        fl_loading.setVisibility(View.VISIBLE);
        wv_news_detail.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                fl_loading.setVisibility(View.GONE);
                if(!has_comment) {
                    ll_comment.setVisibility(View.VISIBLE);
                }
                super.onPageFinished(view, url);
            }
        });
        bt_comment.setOnClickListener(this);
        tv_comments.setOnClickListener(this);
        et_comment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bt_comment.setVisibility(View.VISIBLE);
                    tv_comments.setVisibility(View.GONE);
                } else {
                    bt_comment.setVisibility(View.GONE);
                    tv_comments.setVisibility(View.VISIBLE);
                }
            }
        });
        if(!has_comment) {
            ll_comment.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if(wv_news_detail.canGoBack()){
            wv_news_detail.goBack();
        }else {
            super.onBackPressed();
        }
    }

    private Handler handler = new Handler();

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_comment:
                comment_content = et_comment.getText().toString().trim();
                if(TextUtils.isEmpty(comment_content)){
                    showToastMsgShort("输入您要评论的内容吧");
                    return;
                }
                final ProgressDialog dialog = new ProgressDialog(this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setMessage("正在提交...");
                dialog.show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        showToastMsgShort("评论成功");
                        et_comment.setText("");
                    }
                }, 1000);
                break;
            case R.id.tv_comments:
                Intent intent = new Intent(this, CommentsDetailsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}