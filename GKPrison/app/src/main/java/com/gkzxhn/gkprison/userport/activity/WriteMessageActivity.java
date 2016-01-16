package com.gkzxhn.gkprison.userport.activity;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.Letter;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class WriteMessageActivity extends BaseActivity {

    private EditText et_theme;
    private EditText et_content;
    private Button bt_commit_write_message;
    private String theme;
    private String contents;
    private Gson gson;
    private String token;
    private String url = Constants.URL_HEAD + "mail_boxes?jail_id=1&access_token=";
    private SharedPreferences sp;
    private int family_id = 0;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_write_message, null);
        et_theme = (EditText) view.findViewById(R.id.et_theme);
        et_content = (EditText) view.findViewById(R.id.et_content);
        bt_commit_write_message = (Button) view.findViewById(R.id.bt_commit_write_message);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("写信");
        setBackVisibility(View.VISIBLE);
        bt_commit_write_message.setOnClickListener(this);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        family_id = sp.getInt("family_id",1);
        token = sp.getString("token","");
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_commit_write_message:
                sendMessage();
                finish();
                break;
        }
    }

    private void sendMessage(){
        theme = et_theme.getText().toString();
        contents = et_content.getText().toString();

        Letter letter = new Letter();
        letter.setTheme(theme);
        letter.setContents(contents);
        letter.setJail_id(1);
        letter.setFamily_id(family_id);
        gson = new Gson();
        String message = gson.toJson(letter);
        final String sendmessage = "{\"message\":"+message+"}";
        new Thread(){
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url+token);
                try {
                    StringEntity entity = new StringEntity(sendmessage,HTTP.UTF_8);
                    entity.setContentType("application/json");
                    post.setEntity(entity);
                    HttpResponse response = httpClient.execute(post);
                    if (response.getStatusLine().getStatusCode()==200){
                        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                        Log.d("MainActivity", result);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }
}
