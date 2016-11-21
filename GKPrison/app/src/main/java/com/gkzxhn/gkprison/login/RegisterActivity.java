package com.gkzxhn.gkprison.login;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.login.adapter.AutoTextAdapater;
import com.gkzxhn.gkprison.login.requests.LoginService;
import com.gkzxhn.gkprison.login.view.AlertView;
import com.gkzxhn.gkprison.login.view.OnItemClickListener;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.userport.bean.Register;
import com.gkzxhn.gkprison.userport.bean.Uuid_images_attributes;
import com.gkzxhn.gkprison.userport.view.sweet_alert_dialog.SweetAlertDialog;
import com.gkzxhn.gkprison.utils.Base64;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.ImageTools;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * created by huangzhengneng 2015/12/15
 * 注册页面
 */
public class RegisterActivity extends BaseActivity {

    private static final java.lang.String TAG = "RegisterActivity";
    @BindView(R.id.et_name)
    EditText et_name;// 姓名
    @BindView(R.id.rb_male)
    RadioButton rb_male;
    @BindView(R.id.rb_female)
    RadioButton rb_female;
    @BindView(R.id.rg_sex)
    RadioGroup rg_sex;// 性别
    @BindView(R.id.iv_user_icon)
    ImageView iv_user_icon;// 头像
    @BindView(R.id.et_ic_card)
    EditText et_ic_card;// 身份证号
    @BindView(R.id.et_phone_num)
    EditText et_phone_num;// 手机号
    @BindView(R.id.et_relationship_with_prisoner)
    EditText et_relationship_with_prisoner;// 与服刑人员关系
    @BindView(R.id.et_prisoner_num)
    EditText et_prisoner_num;// 服刑人员囚号
    @BindView(R.id.actv_prison_choose)
    AutoCompleteTextView actv_prison_choose;//监狱选择
    @BindView(R.id.et_identifying_code)
    EditText et_identifying_code;// 验证码
    @BindView(R.id.bt_send_identifying_code)
    Button bt_send_identifying_code;// 发送验证码
    @BindView(R.id.iv_add_photo_01)
    ImageView iv_add_photo_01;
    @BindView(R.id.iv_add_photo_02)
    ImageView iv_add_photo_02;
    @BindView(R.id.cb_agree_disagree)
    CheckBox cb_agree_disagree;// 我已阅读复选框
    @BindView(R.id.tv_read)
    TextView tv_read;// 我已阅读协议
    @BindView(R.id.tv_software_protocol)
    TextView tv_software_protocol;// 蓝色软件协议
    @BindView(R.id.bt_register)
    Button bt_register;// 提交申请
    @BindView(R.id.pb_register)
    ProgressBar pb_register;
    @BindView(R.id.tv_register)
    TextView tv_register;
    @BindView(R.id.rl_register)
    RelativeLayout rl_register;

    private static final int CROP_SMALL_PICTURE = 2;
    private Gson gson;
    private List<String> suggest;// 自动提示的集合
    private Map<String, Integer> prison_map;
    private AutoTextAdapater autoTextAdapater; // actv_prison_choose适配器
    private String data; // 监狱选择访问服务器返回的字符串
    private String name = "";
    private String apply = "";
    private String ic_card = "";
    private String phone_num = "";
    private String relationship_with_prisoner = "";
    private String prisoner_number = "";// 囚号输入框内容
    private String prison_chooes = "";// 监狱选择输入框内容
    private String identifying_code = "";// 验证码输入框的内容

    // 对话框相关
    private SweetAlertDialog sadDialog;
    private AlertDialog dialog; // 注册成功对话框
    private AlertDialog agreement_dialog;// 协议
    private AlertView alertView;

    // 头像身份证图片相关
    private static final int TAKE_PHOTO = 0; //imageview1照相;
    private static final int CHOOSE_PHOTO = 1;//imageview1选图片;
    private static final int SCALE = 20;// 照片缩小比例
    private String uploadFile1 = "";
    private String uploadFile2 = "";
    private String uploadFile3 = "";
    private int imageclick = 0;// 判断点击的是哪个添加图片的加号
    private Bitmap newBitmap1;
    private Bitmap newBitmap2;
    private Bitmap newBitmap3;
    private List<Uuid_images_attributes> uuid_images = new ArrayList<>();

    private int countdown = 60; // 倒计时
    private boolean isRunning = false;

    private Handler handler = new Handler();

    /**
     * 成功
     * @param titleText
     * @param delayTime
     */
    private void setSuccessDialog(String titleText, long delayTime) {
        sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_green));
        sadDialog.setTitleText(titleText)
                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sadDialog.dismiss();
            }
        }, delayTime);
    }

    /**
     * 设置失败对话框
     * @param titleText
     */
    private void showFailedDialog(String titleText) {
        sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
        sadDialog.setTitleText(titleText).setConfirmText("确定").changeAlertType(SweetAlertDialog.ERROR_TYPE);
        sadDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
    }

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_register, null);
        ButterKnife.bind(this, view);
        iv_add_photo_01.setTag(1);
        iv_add_photo_02.setTag(2);
        // 设置图标
        Drawable[] drawables = rb_male.getCompoundDrawables();
        drawables[0].setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 30), DensityUtil.dip2px(getApplicationContext(), 30));
        rb_male.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        Drawable[] drawables2 = rb_female.getCompoundDrawables();
        drawables2[0].setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 30), DensityUtil.dip2px(getApplicationContext(), 30));
        rb_female.setCompoundDrawables(drawables2[0], drawables2[1], drawables2[2], drawables2[3]);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("注册");
        setBackVisibility(View.VISIBLE);
        cb_agree_disagree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bt_register.setEnabled(true);
                } else {
                    bt_register.setEnabled(false);
                }
            }
        });
        prison_map = new HashMap<>();
        actv_prison_choose.setThreshold(1);
        actv_prison_choose.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                new GetSuggestData().execute(newText);
            }

            @Override public void afterTextChanged(Editable s) {}
        });
        setBackPress();
    }

    /**
     * 设置UI返回按钮
     */
    private void setBackPress() {
        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEditTextContent();
                if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(ic_card) || !TextUtils.isEmpty(phone_num) || !TextUtils.isEmpty(relationship_with_prisoner)
                        || !TextUtils.isEmpty(prisoner_number) || !TextUtils.isEmpty(prison_chooes) || !TextUtils.isEmpty(identifying_code)
                        || newBitmap1 != null || newBitmap2 != null || newBitmap3 != null) {
                    setReminder();// 设置退出提醒
                } else {
                    RegisterActivity.this.finish();
                }
            }
        });
    }

    @OnClick({R.id.bt_send_identifying_code, R.id.bt_register,R.id.tv_software_protocol,R.id.tv_read,
            R.id.iv_add_photo_01, R.id.iv_add_photo_02,R.id.iv_user_icon})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_read:
                if (cb_agree_disagree.isChecked()) {
                    cb_agree_disagree.setChecked(false);
                } else {
                    cb_agree_disagree.setChecked(true);
                }
                break;
            case R.id.tv_software_protocol: showSoftProtocolDialog();break;// 协议对话框
            case R.id.iv_add_photo_01: showPhotoPicker(this, true);imageclick = 1;break;
            case R.id.iv_add_photo_02: showPhotoPicker(this, true);imageclick = 2;break;
            case R.id.iv_user_icon: showPhotoPicker(this, false);imageclick = -1;break;
            case R.id.bt_send_identifying_code:
                phone_num = et_phone_num.getText().toString().trim();
                // 判断手机号码是否合法
                if (TextUtils.isEmpty(phone_num)) {
                    showToastMsgShort("手机号为空");
                } else {
                    if (!Utils.isMobileNO(phone_num)) {
                        showToastMsgShort("请输入正确的手机号码");
                    } else {
                        if (Utils.isNetworkAvailable()) {
                            String phone_str = "{\"apply\":{\"phone\":\"" + phone_num + "\"}}";
                            initAndShowDialog("正在发送...");
                            getVerificationCode(phone_str);
                        } else {
                            showToastMsgShort("没有网络");
                            return;
                        }
                    }
                }
                bt_send_identifying_code.setEnabled(false);
                bt_send_identifying_code.setBackgroundColor(getResources().getColor(R.color.tv_gray));
                bt_send_identifying_code.setText(countdown + "秒后可重发");
                handler.postDelayed(identifying_Code_Task, 1000);
                break;
            case R.id.bt_register:
                getEditTextContent();
                // 判断姓名是否都是汉字组成
                if (judgeName()) return;
                // 判断身份证号是否合法
                if (judgeIDCard()) return;
                // 判断手机号码是否合法
                if (judgePhoneNumber()) return;
                // 判断输入的与服刑人员关系是否都是汉字
                if (judgeRelativeShip()) return;
                //判断囚号
                if (judgePrisonerNumber()) return;
                //判断监狱选择
                // 判断验证码是否正确
                // 判断是否上传身份证正反面照
                // 判断是否上传头像
                if (judgeNullInput()) return;
                if (Utils.isNetworkAvailable()) {
                    showConfirmDialog();
                } else {
                    showToastMsgShort("没有网络");
                }
                break;
//            case R.id.rl_back:
//                getEditTextContent();
//                if (dialog != null && dialog.isShowing()) {
//                    // 没反应
//                } else if (agreement_dialog != null && agreement_dialog.isShowing()) {
//                    // 没反应
//                } else if (alertView != null && alertView.isShowing()) {
//                    alertView.dismiss();
//                } else if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(ic_card) || !TextUtils.isEmpty(phone_num) || !TextUtils.isEmpty(relationship_with_prisoner)
//                        || !TextUtils.isEmpty(prisoner_number) || !TextUtils.isEmpty(prison_chooes) || !TextUtils.isEmpty(identifying_code)
//                        || newBitmap1 != null || newBitmap2 != null || newBitmap3 != null) {
//                    setReminder();// 弹出对话框提醒
//                } else {
//                    super.onBackPressed();
//                }
//                break;
        }
    }

    /**
     * 获取验证码
     * @param phone_str
     */
    private void getVerificationCode(String phone_str) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.URL_HEAD)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create())
                .build();
        LoginService service = retrofit.create(LoginService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), phone_str);
        service.getVerificationCode(body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "get verification code failed : " + e.getMessage());
                        showFailedDialog("验证码请求失败，请稍后再试！");
                    }

                    @Override public void onNext(ResponseBody responseBody) {
                        try {
                            String result = responseBody.string();
                            Log.i(TAG, "get verification success :" + result);
                            JSONObject jsonObject = new JSONObject(result);
                            int code = jsonObject.getInt("code");
                            if(code == 200){
                                setSuccessDialog("已发送", 1000);
                            }else{
                                showFailedDialog("验证码请求失败，请稍后再试！");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            showFailedDialog("验证码请求异常，请稍后再试！");
                        }
                    }
                });
    }

    /**
     * 协议对话框
     */
    private void showSoftProtocolDialog() {
        AlertDialog.Builder agreement_builder = new AlertDialog.Builder(this);
        View agreement_view = View.inflate(this, R.layout.software_agreement_dialog, null);
        LinearLayout ll_explain_content = (LinearLayout) agreement_view.findViewById(R.id.ll_explain_content);
        agreement_dialog = agreement_builder.create();
        agreement_dialog.setCancelable(true);
        agreement_builder.setView(agreement_view);
        agreement_builder.show();
        ll_explain_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agreement_dialog.dismiss();
            }
        });
    }

    /**
     * 显示确认提交注册的提示对话框
     */
    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setMessage("即将提交注册，注册信息将会严格审核，注册信息一旦通过，将不可修改，确定提交？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                verificationCode(); // 发送注册信息至服务器
                String prisoner_name = actv_prison_choose.getText().toString();
                SPUtil.put(getApplication(), "prisonname", prisoner_name);
                dialog.dismiss();
            }
        }).setNegativeButton("再确认一下", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog register_remind_dialog = builder.create();
        register_remind_dialog.show();
    }

    /**
     * 判断相关输入框为空
     * @return
     */
    private boolean judgeNullInput() {
        if (newBitmap3 == null) {
            showToastMsgShort("请选择头像");
            return true;
        }
        if (TextUtils.isEmpty(prison_chooes)) {
            showToastMsgShort("请输入监狱名称");
            return true;
        }
        if (TextUtils.isEmpty(identifying_code)) {
            showToastMsgShort("验证码为空");
            return true;
        }
        if (newBitmap1 == null || newBitmap2 == null) {
            showToastMsgShort("请上传身份证正反面照");
            return true;
        }
        return false;
    }

    /**
     * 判断囚号
     * @return
     */
    private boolean judgePrisonerNumber() {
        if (TextUtils.isEmpty(prisoner_number)) {
            showToastMsgShort("服刑人员囚号为空");
            return true;
        } else {
            // 囚号  数字组成的字符串
            if (!Utils.isNumeric(prisoner_number)) {
                showToastMsgShort("请输入正确的囚号");
                return true;
            }
        }
        return false;
    }

    /**
     * 判断与服刑人员关系
     * @return
     */
    private boolean judgeRelativeShip() {
        if (TextUtils.isEmpty(relationship_with_prisoner)) {
            showToastMsgShort("与服刑人员关系为空");
            return true;
        } else {
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            Matcher m = p.matcher(name);
            if (m.matches()) { // 不全是汉字
                showToastMsgShort("请输入正确的与服刑人员的关系");
                return true;
            }
        }
        return false;
    }

    /**
     * 判断手机号
     * @return
     */
    private boolean judgePhoneNumber() {
        if (TextUtils.isEmpty(phone_num)) {
            showToastMsgShort("手机号为空");
            return true;
        } else {
            if (!Utils.isMobileNO(phone_num)) {
                showToastMsgShort("请输入正确的手机号码");
                return true;
            }
        }
        return false;
    }

    /**
     * 判断身份证是否合法
     * @return
     */
    private boolean judgeIDCard() {
        if (TextUtils.isEmpty(ic_card)) {
            showToastMsgShort("身份证号为空");
            return true;
        } else {
            try {
                if (!Utils.IDCardValidate(ic_card).equals("")) {
                    showToastMsgShort("身份证号不合法");
                    return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 判断姓名是否为空  是否由汉字组成  是否长度小于30
     * @return
     */
    private boolean judgeName() {
        if (TextUtils.isEmpty(name)) {
            showToastMsgShort("姓名为空");
            return true;
        } else if (name.length() <= 1 || name.length() >= 30) {
            showToastMsgShort("姓名长度不合法");
            return true;
        } else {
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            Matcher m = p.matcher(name);
            if (m.matches()) { // 不全是汉字
                showToastMsgShort("姓名不合法");
                return true;
            }
        }
        return false;
    }

    /**
     * 获取输入框内容
     */
    private void getEditTextContent() {
        name = et_name.getText().toString().trim();
        ic_card = et_ic_card.getText().toString().trim().toLowerCase();
        phone_num = et_phone_num.getText().toString().trim();
        relationship_with_prisoner = et_relationship_with_prisoner.getText().toString().trim();
        prisoner_number = et_prisoner_num.getText().toString().trim();
        prison_chooes = actv_prison_choose.getText().toString().trim();
        identifying_code = et_identifying_code.getText().toString().trim();
    }

    /**
     * 监狱查找任务
     */
    private class GetSuggestData extends AsyncTask<String, String, String> {

        @Override protected void onPostExecute(String result) {
            super.onPostExecute(result);
            autoTextAdapater = new AutoTextAdapater(suggest, RegisterActivity.this);
            actv_prison_choose.setAdapter(autoTextAdapater);
        }

        @Override protected String doInBackground(String... key) {
            String newText = key[0];
            newText = newText.trim();
            newText = newText.replace(" ", "+");
            if (Utils.isNetworkAvailable()) {
                try {
                    data = HttpRequestUtil.doHttpsGet(Constants.URL_HEAD + "jails/" + newText);
                    suggest = new ArrayList<>();
                    prison_map.clear();
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray jArray = jsonObject.getJSONArray("jails");
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jsonObject1 = jArray.getJSONObject(i);
                        String suggestKey = jsonObject1.getString("title");
                        int id = jsonObject1.getInt("id");
                        suggest.add(suggestKey);
                        prison_map.put(suggestKey, id);
                    }
                } catch (Exception e) {
                    Log.w("Error", e.getMessage());
                }
            }
            return null;
        }
    }

    /**
     * 发送注册信息
     */
    private void register() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.URL_HEAD).addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build();
        LoginService registerRequest = retrofit.create(LoginService.class);
        getZipPicture();// 获取压缩图片并转换base64
        Register register = setRegisterBean();
        if(register == null){
            showFailedDialog("抱歉，暂未开通此监狱");
            return;
        }
        gson = new Gson();
        String str = gson.toJson(register);
        apply = "{\"apply\":" + str + "}";
        Log.i(TAG, "register info : " + apply);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), apply);
        registerRequest.register(body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        String error = e.getMessage();
                        Log.e(TAG, "send register info failed : " + error);
                        if (error.contains("413")) {
                            showFailedDialog("注册失败，照片体积过大！");
                        }else {
                            showFailedDialog("注册请求失败，请稍后再试！");
                        }
                    }

                    @Override public void onNext(ResponseBody responseBody) {
                        try {
                            String result = responseBody.string();
                            Log.i(TAG, "register success : " + result);
                            JSONObject jsonObject = new JSONObject(result);
                            int code = jsonObject.getInt("code");
                            if(code == 200){// success
                                sadDialog.dismiss();
                                showRegisterSuccess();
                                // 把囚号保存在本地
                                SPUtil.put(getApplicationContext(), "prisoner_number", prisoner_number);
                            }else if(code == 404){
                                showFailedDialog("验证码错误！");
                            }else if(code == 501){
                                JSONObject errors = jsonObject.getJSONObject("errors");
                                JSONArray apply_create = errors.getJSONArray(result.contains("apply_create") ? "apply_create" : "phone");
                                showFailedDialog(apply_create.getString(0));
                            }else {
                                showFailedDialog("注册失败！");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            showFailedDialog("注册失败！");
                        }
                    }
                });
    }

    /**
     * 显示注册成功对话框
     */
    private void showRegisterSuccess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setCancelable(false);
        View view = View.inflate(RegisterActivity.this, R.layout.register_commit_success_dialog, null);
        Button bt_ok = (Button) view.findViewById(R.id.bt_ok);
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RegisterActivity.this.finish();
            }
        });
        dialog = builder.create();
        builder.setView(view);
        builder.show();
    }

    /**
     * 设置注册的bean对象
     * @return
     */
    @NonNull
    private Register setRegisterBean() {
        Register register = new Register();
        register.setName(name);
        register.setUuid(ic_card);
        register.setPhone(phone_num);
        register.setRelationship(relationship_with_prisoner);
        register.setPrisoner_number(prisoner_number);
        register.setGender(rg_sex.getCheckedRadioButtonId() == R.id.rb_male ? "男" : "女");
        if (prison_map.containsKey(prison_chooes)) {
            int jail_id = prison_map.get(prison_chooes);
            register.setJail_id(jail_id);
            Log.i(TAG, "jail_id : " + jail_id);
        } else {
            showToastMsgShort("抱歉，暂未开通此监狱");
            return null;
        }
        register.setType_id(3);
        register.setUuid_images_attributes(uuid_images);
        return register;
    }

    /**
     * 获取压缩图片并转换base64
     */
    private void getZipPicture() {
        ByteArrayOutputStream bao1 = new ByteArrayOutputStream();
        newBitmap1.compress(Bitmap.CompressFormat.PNG, 100, bao1);
        byte[] ba1 = bao1.toByteArray();
        String tu1 = Base64.encode(ba1);
        ByteArrayOutputStream bao2 = new ByteArrayOutputStream();
        newBitmap2.compress(Bitmap.CompressFormat.PNG, 100, bao2);
        byte[] ba2 = bao2.toByteArray();
        String tu2 = Base64.encode(ba2);
        String[] tu = {tu1, tu2};
        uuid_images.clear();
        for (int i = 0; i < tu.length; i++) {
            Uuid_images_attributes uuid_images_attributes = new Uuid_images_attributes();
            uuid_images_attributes.setImage_data(tu[i]);
            uuid_images.add(uuid_images_attributes);
        }
        ByteArrayOutputStream bao3 = new ByteArrayOutputStream();
        newBitmap3.compress(Bitmap.CompressFormat.PNG, 100, bao3);
        byte[] ba3 = bao3.toByteArray();
        String tu3 = Base64.encode(ba3);
        Uuid_images_attributes user_icon = new Uuid_images_attributes();
        user_icon.setImage_data(tu3);
        uuid_images.add(user_icon);
    }

    /**
     * 发送手机号码和验证码
     */
    private void verificationCode() {
        initAndShowDialog("正在注册...");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD).addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build();
        LoginService register = retrofit.create(LoginService.class);
        String code = "{\"session\":{\"phone\":\"" + phone_num + "\",\"code\":\"" + identifying_code + "\"}}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), code);
        register.judgeVerificationCode(body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "send code failed : " + e.getMessage());
                        showFailedDialog("验证码请求失败，请稍后再试！");
                    }

                    @Override public void onNext(ResponseBody responseBody) {
                        try {
                            String result = responseBody.string();
                            JSONObject jsonObject = new JSONObject(result);
                            int code = jsonObject.getInt("code");
                            if(code == 200){
                                register();
                            }else {
                                showFailedDialog("验证码错误！");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            showFailedDialog("异常");
                        }
                    }
                });
    }

    /**
     * 初始化并且显示对话框
     * @param text
     */
    private void initAndShowDialog(String text) {
        sadDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(text);
        sadDialog.setCancelable(false);
        sadDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && isRunning) {
            handler.removeCallbacks(identifying_Code_Task);
            handler = null;
        }
    }

    /**
     * 发送验证码倒计时任务
     */
    private Runnable identifying_Code_Task = new Runnable() {
        @Override
        public void run() {
            isRunning = true;
            countdown--;
            bt_send_identifying_code.setText(countdown + "秒后可重发");
            if (countdown == 0) {
                bt_send_identifying_code.setEnabled(true);
                bt_send_identifying_code.setBackground(getResources().getDrawable(R.drawable.theme_bg_bt_selector));
                bt_send_identifying_code.setText("发送验证码");
                countdown = 60;
                isRunning = false;
            } else {
                handler.postDelayed(identifying_Code_Task, 1000);
            }
        }
    };

    @Override
    public void onBackPressed() {
        getEditTextContent();
        if (dialog != null && dialog.isShowing()) {
            // 没反应
        } else if (agreement_dialog != null && agreement_dialog.isShowing()) {
            // 没反应
        } else if (alertView != null && alertView.isShowing()) {
            alertView.dismiss();
        } else if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(ic_card) || !TextUtils.isEmpty(phone_num) || !TextUtils.isEmpty(relationship_with_prisoner)
                || !TextUtils.isEmpty(prisoner_number) || !TextUtils.isEmpty(prison_chooes) || !TextUtils.isEmpty(identifying_code)
                || newBitmap1 != null || newBitmap2 != null || newBitmap3 != null) {
            setReminder();// 弹出对话框提醒
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 弹出对话框提醒
     */
    private void setReminder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("提示").setMessage("放弃所修改的内容吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                RegisterActivity.this.finish();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog remind_dialog = builder.create();
        remind_dialog.show();
    }

    /**
     * 显示相片操作(0 拍照 / 1 选择相片)
     *
     * @param context
     */
    private void showPhotoPicker(Context context, boolean isTwo) {
        if (isTwo) {
            alertView = new AlertView("上传身份证照片", null, "取消", null, new String[]{"拍照", "从相册中选择"},
                    context, AlertView.Style.ActionSheet, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if(position == TAKE_PHOTO){
                        takePhotoFromCamera();// 拍照
                    }else if(position == CHOOSE_PHOTO){
                        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        openAlbumIntent.setType("image/*");
                        startActivityForResult(openAlbumIntent, CHOOSE_PHOTO);
                    }else if(position == -1){// 取消
                        alertView.dismiss();
                    }
                }
            });
            alertView.show();
        } else {
            alertView = new AlertView("上传头像", null, "取消", null, new String[]{"拍照"},
                    context, AlertView.Style.ActionSheet, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if(position == TAKE_PHOTO){
                        takePhotoFromCamera();// 拍照
                    }else if(position == -1){
                        alertView.dismiss();
                    }
                }
            });
            alertView.show();
        }
    }

    /**
     * 相机拍照
     */
    private void takePhotoFromCamera() {
        Intent openCameraIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        Uri imageUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), "image.jpg"));
        // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(openCameraIntent, TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO:
                    // 将处理过的图片显示在界面上，并保存到本地
                    if (imageclick == 1) {
                        setFirstIDPhoto(); // 设置第一张身份证照片
                        Log.i(TAG, "uploadFile1 : " + uploadFile1);
                    } else if (imageclick == 2) {
                        setSecondIDPhoto(); // 设置第二张身份证照片
                        Log.i(TAG, "uploadFile2 : " + uploadFile2);
                    } else if (imageclick == -1) {
                        Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.jpg"));
                        cropImageUri(imageUri, 300, 300, CROP_SMALL_PICTURE);// 裁剪
                    }
                    break;
                case CHOOSE_PHOTO:
                    ContentResolver resolver = getContentResolver();
                    // 照片的原始资源地址
                    Uri originalUri = data.getData();
                    try {// 使用ContentProvider通过URI获取原始图片
                        Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                        if (photo != null) {
                            setPhotoFromAlbum(photo);
                        }
                    } catch (FileNotFoundException e) {
                        showToastMsgShort("文件不存在");
                    } catch (IOException e) {
                        showToastMsgShort("读取文件出错");
                    }
                    break;
                case CROP_SMALL_PICTURE:
                    // 将保存在本地的图片取出并缩小后显示在界面上
                    Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/image.jpg");
                    newBitmap3 = bitmap;
                    iv_user_icon.setImageBitmap(newBitmap3);
                    break;
            }
        }
    }

    /**
     * 设置从相册获取的图片
     * @param photo
     */
    private void setPhotoFromAlbum(Bitmap photo) {
        if (imageclick == 1) {
            // 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
            newBitmap1 = ImageTools.zoomBitmap(photo, photo.getWidth() / SCALE, photo.getHeight() / SCALE);
            // 释放原始图片占用的内存，防止out of memory异常发生
            photo.recycle();
            iv_add_photo_01.setImageBitmap(newBitmap1);
            uploadFile1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .getAbsolutePath() + "/Camera/" + "emptyphoto.png";
        } else if (imageclick == 2) {
            newBitmap2 = ImageTools.zoomBitmap(photo, photo.getWidth() / SCALE, photo.getHeight() / SCALE);
            // 释放原始图片占用的内存，防止out of memory异常发生
            photo.recycle();
            iv_add_photo_02.setImageBitmap(newBitmap2);
            uploadFile2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .getAbsolutePath() + "/Camera/" + "emptyphoto.png";
        }
    }

    /**
     * 设置第二张身份证照片
     */
    private void setSecondIDPhoto() {
        // 将保存在本地的图片取出并缩小后显示在界面上
        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/image.jpg");
        newBitmap2 = ImageTools.zoomBitmap(bitmap, bitmap.getWidth() / SCALE, bitmap.getHeight() / SCALE);
        // 由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
        bitmap.recycle();
        iv_add_photo_02.setImageBitmap(newBitmap2);
        ImageTools.savePhotoToSDCard(newBitmap2, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .getAbsolutePath() + "/Camera", String.valueOf(System.currentTimeMillis()));
        uploadFile2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .getAbsolutePath() + "/Camera/" + String.valueOf(System.currentTimeMillis()) + ".png";
    }

    /**
     * 设置第一张身份证照片
     */
    private void setFirstIDPhoto() {
        // 将保存在本地的图片取出并缩小后显示在界面上
        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/image.jpg");
        newBitmap1 = ImageTools.zoomBitmap(bitmap, bitmap.getWidth() / SCALE, bitmap.getHeight() / SCALE);
        // 由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
        bitmap.recycle();
        iv_add_photo_01.setImageBitmap(newBitmap1);
        ImageTools.savePhotoToSDCard(newBitmap1, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .getAbsolutePath() + "/Camera", String.valueOf(System.currentTimeMillis()));
        uploadFile1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .getAbsolutePath() + "/Camera/" + String.valueOf(System.currentTimeMillis()) + ".png";
    }

    /**
     * 裁剪照片
     *
     * @param uri         image uri
     * @param outputX     default image width
     * @param outputY     default image height
     * @param requestCode
     */
    private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, requestCode);
    }
}
