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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.view.pb.NumberProgressBar;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 新闻详情页
 */
public class NewsDetailActivity extends BaseActivity {

    private static final String TAG = "NewsDetailActivity";
    private WebView wv_news_detail;
    private NumberProgressBar npb_loading;
    private String webUrl;
    private int type;
    private int id;// 新闻id

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
        id = getIntent().getIntExtra("id",-1);
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
//        et_comment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    bt_comment.setVisibility(View.VISIBLE);
//                    tv_comments.setVisibility(View.GONE);
//                } else {
//                    bt_comment.setVisibility(View.GONE);
//                    tv_comments.setVisibility(View.VISIBLE);
//                }
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        if(wv_news_detail.canGoBack()){
            wv_news_detail.goBack();
        }else {
            super.onBackPressed();
        }
    }

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
                OkHttpClient client = new OkHttpClient();
                String param = "{\"family_id\":" + SPUtil.get(this, "family_id", -1) + ",\"content\":\"" + comment_content + "\"}";
                Log.i(TAG, param);
                RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), param);
                Request request = new Request.Builder()
                        .url(Constants.URL_HEAD + "news/" + id + "/comments?access_token=" + SPUtil.get(this, "token", ""))
                        .post(body)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                showToastMsgShort("评论失败，请稍后再试");
                                Log.i(TAG, e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        try {
                            String result = response.body().string();
                            /**
                             * {"code":200,"msg":"Comment success"}
                             */
                            Log.i(TAG, result);
                            JSONObject jsonObject = new JSONObject(result);
                            int code = jsonObject.getInt("code");
                            if(code == 200){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        showToastMsgShort("评论成功！");
                                        et_comment.setText("");
                                    }
                                });
                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        showToastMsgShort("评论失败，请稍后再试");
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    showToastMsgShort("评论失败，请稍后再试");
                                }
                            });
                        }
                    }
                });
                break;
            case R.id.tv_comments:
                Intent intent = new Intent(this, CommentsDetailsActivity.class);
                intent.putExtra("news_id", id);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}