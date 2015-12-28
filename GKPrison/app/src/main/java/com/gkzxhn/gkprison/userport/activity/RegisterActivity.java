package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.google.gson.Gson;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * created by hzn 2015/12/15
 * 申请二维码页面
 */
public class RegisterActivity extends BaseActivity {

    private final String[] PRISONS = {"监狱1", "监狱2", "监狱3", "监狱4"};
    private String url = "http://10.93.1.10:3000/api/v1/apply";
    private BetterSpinner bs_prison_choose;// 监狱选择
    private ArrayAdapter prisonAdapter;// 下拉适配器
    private EditText et_name;// 姓名
    private EditText et_ic_card;// 身份证号
    private EditText et_phone_num;// 手机号
    private EditText et_relationship_with_prisoner;// 与服刑人员关系
    private EditText et_prisoner_number;// 服刑人员囚号

    private EditText et_prison_chooes;//监狱选择
    private EditText et_identifying_code;// 验证码
    private Button bt_send_identifying_code;// 发送验证码
    private Button bt_register;// 提交申请
    private TextView tv_read;
    private CheckBox cb_agree_disagree;

   private  Map<String,String> map = new LinkedHashMap<String,String>();
    private ImageView iv_add_photo_01;
    private ImageView iv_add_photo_02;
    private TextView tv_software_protocol;
    private Gson gson;
    private String name = "";
    private String apply = "";
    private JSONObject jsonObject = new JSONObject();
    private int jail_id = 1;
    private int type_id = 3;
    private String ic_card = "";
    private String phone_num = "";
    private String prisoner_iccardnum = "";
    private String relationship_with_prisoner = "";
    private String prisoner_number = "";
    private String prison_name = "";
    private String identifying_code = "";
    private AlertDialog dialog;
    private AlertDialog agreement_dialog;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_register, null);
      //  bs_prison_choose = (BetterSpinner) view.findViewById(R.id.bs_prison_choose);
        et_name = (EditText) view.findViewById(R.id.et_name);
        et_ic_card = (EditText) view.findViewById(R.id.et_ic_card);
        et_phone_num = (EditText) view.findViewById(R.id.et_phone_num);
        et_relationship_with_prisoner = (EditText) view.findViewById(R.id.et_relationship_with_prisoner);
        et_prisoner_number = (EditText) view.findViewById(R.id.et_prisoner_num);
        et_identifying_code = (EditText) view.findViewById(R.id.et_identifying_code);
        et_prison_chooes = (EditText)view.findViewById(R.id.et_prison_choose);
        bt_send_identifying_code = (Button) view.findViewById(R.id.bt_send_identifying_code);
        bt_register = (Button) view.findViewById(R.id.bt_register);
        iv_add_photo_01 = (ImageView) view.findViewById(R.id.iv_add_photo_01);
        iv_add_photo_02 = (ImageView) view.findViewById(R.id.iv_add_photo_02);
        tv_software_protocol = (TextView) view.findViewById(R.id.tv_software_protocol);
        tv_read = (TextView) view.findViewById(R.id.tv_read);
        cb_agree_disagree = (CheckBox) view.findViewById(R.id.cb_agree_disagree);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("注册");
        setBackVisibility(View.VISIBLE);
        prisonAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, PRISONS);
        tv_software_protocol.setOnClickListener(this);
        tv_read.setOnClickListener(this);
        cb_agree_disagree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    bt_register.setEnabled(true);
                    bt_register.setBackground(getResources().getDrawable(R.drawable.theme_bg_bt_selector));
                }else {
                    bt_register.setEnabled(false);
                    bt_register.setBackground(getResources().getDrawable(R.drawable.gray_bg_bt_selector));
                }
            }
        });
        bt_register.setOnClickListener(this);
    }

    /**
     * 发送注册请求至服务端
     */
    private void sendRegisterToServer() {
        name = et_name.getText().toString().trim();
        ic_card = et_ic_card.getText().toString().trim();
        phone_num = et_phone_num.getText().toString().trim();
        relationship_with_prisoner = et_relationship_with_prisoner.getText().toString().trim();
        prisoner_number = et_prisoner_number.getText().toString().trim();
        prison_name = et_prison_chooes.getText().toString().trim();
        identifying_code = et_identifying_code.getText().toString().trim();
        Map<String,Map> map1 = new HashMap<String, Map>();
        map.put("name",name);
        map.put("uuid",ic_card);
        map.put("phone",phone_num);
        map.put("relationship",relationship_with_prisoner);
        map.put("prisoner_number",prisoner_number);
        map.put("jail_id",jail_id+"");
        map.put("type_id",type_id+"");
        map1.put("apply",map);
        apply = JSONValue.toJSONString(map1);
        new Thread(){
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);
                try {
                    String str = jsonObject.toString();
//                    Log.d("MainActivity", apply);
                   StringEntity entity = new StringEntity(apply);
                    post.setEntity(entity);
                    HttpResponse httpResponse = httpClient.execute(post);
                    if (httpResponse.getStatusLine().getStatusCode() == 200){
                        String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
//                        Log.d("MainActivity",result);
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
//        Toast.makeText(getApplicationContext(), "请等待审核通过，系统将会发短信给您", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_register:
                if(TextUtils.isEmpty(name)){
                    showToastMsgShort("姓名为空");
                    return;
                }else if(TextUtils.isEmpty(ic_card)){
                   showToastMsgShort("身份证号为空");
                   return;
                }else if(TextUtils.isEmpty(phone_num)){
                   showToastMsgShort("手机号为空");
                    return;
                }else if(TextUtils.isEmpty(relationship_with_prisoner)){
                    showToastMsgShort("与服刑人员关系为空");
                    return;
                }else if(TextUtils.isEmpty(identifying_code)){
                    showToastMsgShort("验证码为空");
                    return;
                }else {
                    sendRegisterToServer();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(false);
                    View view = View.inflate(this, R.layout.register_commit_success_dialog, null);
                    Button bt_ok = (Button) view.findViewById(R.id.bt_ok);
                    bt_ok.setOnClickListener(this);
                    dialog = builder.create();
                    builder.setView(view);
                    builder.show();
                  }
                break;
            case R.id.bt_ok:
                dialog.dismiss();
                finish();
                break;
            case R.id.tv_software_protocol:
                AlertDialog.Builder agreement_builder = new AlertDialog.Builder(this);
                View agreement_view = View.inflate(this, R.layout.software_agreement_dialog, null);
                agreement_dialog = agreement_builder.create();
                agreement_builder.setView(agreement_view);
                agreement_builder.show();
                break;
            case R.id.tv_read:
                if(cb_agree_disagree.isChecked()){
                    cb_agree_disagree.setChecked(false);
                }else {
                    cb_agree_disagree.setChecked(true);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(dialog != null && dialog.isShowing()){
            return true;
        }else if(agreement_dialog != null && agreement_dialog.isShowing()){
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
