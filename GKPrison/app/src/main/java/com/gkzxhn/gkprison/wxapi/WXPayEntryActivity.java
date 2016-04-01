package com.gkzxhn.gkprison.wxapi;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.constant.WeixinConstants;
import com.gkzxhn.gkprison.prisonport.http.HttpPatch;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.activity.WeixinPayActivity;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import com.gkzxhn.gkprison.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.XMLFormatter;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	
	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";
    private IWXAPI api;
	private String tradeno;
	private SharedPreferences sp;
	private String token;
	private String times;
	private String partnerid = "1320273701";
	StringBuffer sb;
	private String paystatus;
	StringBuffer xml;
	private String URL= "https://api.mch.weixin.qq.com/pay/orderquery";
	public static final MediaType JSON
			= MediaType.parse("application/json; charset=utf-8");
	OkHttpClient client;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 1:
					String result = (String)msg.obj;
					Log.d("ag", result);
					break;
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
    	api = WXAPIFactory.createWXAPI(this, WeixinConstants.APP_ID);
        api.handleIntent(getIntent(), this);
		Log.d("ff", token);


    }



	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if (resp.errCode == 0){
				sp = getSharedPreferences("config", MODE_PRIVATE);
				token = sp.getString("token", "");
				times = WeixinPayActivity.times;
				tradeno = WeixinPayActivity.tradeno;
				/**
				SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
				String type = "微信支付";
				String sql = "update Cart set isfinish = 1,payment_type = '" + type + "‘ where time = '" + times + "'";
				db.execSQL(sql);
				 **/
				final String str = "{\"order\":{\"trade_no\":\"" + tradeno + "\",\"status\":\"WAIT_FOR_NOTIFY\"}}";
				Log.d("dds", str);
				new Thread() {
					@Override
					public void run() {

						Looper.prepare();
						Message msg = handler.obtainMessage();
						HttpClient httpClient = new DefaultHttpClient();
						HttpPatch httpPatch = new HttpPatch(Constants.URL_HEAD + "payment_status?access_token=" + token);
						Log.d("asd", Constants.URL_HEAD + "payment_status?access_token=" + token);
						try {
							StringEntity entity = new StringEntity(str, HTTP.UTF_8);
							entity.setContentType("application/json");
							httpPatch.setEntity(entity);
							HttpResponse response = httpClient.execute(httpPatch);
							if (response.getStatusLine().getStatusCode() == 200) {
								String result = EntityUtils.toString(response.getEntity(), "utf-8");
								msg.obj = result;
								msg.what = 1;
								handler.sendMessage(msg);
							} else {
								Toast.makeText(getApplicationContext(), "通知服务器失败", Toast.LENGTH_SHORT).show();
							}
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							Looper.loop();
						}
					}
				}.start();
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				View view = this.getLayoutInflater().inflate(R.layout.weixinpay_dialog,null);
				Button button = (Button)view.findViewById(R.id.btn_payfinish);
				builder.setView(view);
				button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(WXPayEntryActivity.this, MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("times", times);
						startActivity(intent);
						finish();
					}
				});
				builder.show();
			}
		}
	}



	private String genAppSign(List<NameValuePair> params) {
		sb = new StringBuffer();
		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		com.gkzxhn.gkprison.utils.Log.d("sa", sb.toString());
		sb.append("key=");
		sb.append("d75699d893882dea526ea05e9c7a4090");
		com.gkzxhn.gkprison.utils.Log.d("dd", sb.toString());
		//  sb.append("sign str\n" + sb.toString() + "\n\n");
		String appSign = MD5Utils.ecoder(sb.toString()).toUpperCase();
		com.gkzxhn.gkprison.utils.Log.d("orion1", appSign);
		return appSign;
	}
	private String getRandomString() {
		String suiji = "";
		int len = 32;
		char[] chars = new char[len];
		Random random = new Random();
		for (int i = 0;i < len;i++){
			if (random.nextBoolean() == true){
				chars[i] = (char)(random.nextInt(25) + 97);
			}else {
				chars[i] = (char)(random.nextInt(9) + 48);
			}
		}
		suiji = new String(chars);
		return suiji;
	}


}