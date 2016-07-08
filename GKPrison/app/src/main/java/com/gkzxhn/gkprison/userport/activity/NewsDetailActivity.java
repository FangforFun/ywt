package com.gkzxhn.gkprison.userport.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
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
import com.gkzxhn.gkprison.userport.view.pb.NumberProgressBar;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;

/**
 * 新闻详情页
 */
public class NewsDetailActivity extends BaseActivity {

    private WebView wv_news_detail;
    private NumberProgressBar npb_loading;
    private String webUrl;
    private int type;

    // 评论相关
    private LinearLayout ll_comment;
    private EditText et_comment;
    private Button bt_comment;
    private TextView tv_comments;

    // 评论内容
    private String comment_content;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_news_detail, null);
        wv_news_detail = (WebView) view.findViewById(R.id.wv_news_detail);
        npb_loading = (NumberProgressBar) view.findViewById(R.id.npb_loading);
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
        webSettings.setSupportZoom(true);
        wv_news_detail.loadUrl(webUrl);
        npb_loading.setVisibility(View.VISIBLE);
        npb_loading.setReachedBarHeight(10);
        npb_loading.setUnreachedBarHeight(8);
        npb_loading.setProgressTextSize(24);
        wv_news_detail.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if(type == 1)
                    ll_comment.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }
        });
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