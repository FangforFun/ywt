package com.gkzxhn.gkprison.activity;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.gkzxhn.gkprison.R;
import com.weiwangcn.betterspinner.library.BetterSpinner;

/**
 * created by hzn 2015/12/15
 * 申请二维码页面
 */
public class ApplyBarcodeActivity extends BaseActivity {

    private final String[] PRISONS = {"监狱1", "监狱2", "监狱3", "监狱4"};

    private BetterSpinner bs_prison_choose;// 监狱选择
    private ArrayAdapter prisonAdapter;// 下拉适配器
    private EditText et_name;// 姓名
    private EditText et_ic_card;// 身份证号
    private EditText et_phone_num;// 手机号
    private EditText et_relationship_with_prisoner;// 与服刑人员关系
    private EditText et_prisoner_name;// 服刑人员姓名
    private EditText et_identifying_code;// 验证码
    private Button bt_send_identifying_code;// 发送验证码
    private Button bt_apply;// 提交申请
    private ImageView iv_add_photo_01;
    private ImageView iv_add_photo_02;
    private String name = "";
    private String ic_card = "";
    private String phone_num = "";
    private String relationship_with_prisoner = "";
    private String prisoner_name = "";
    private String identifying_code = "";
    private AlertDialog dialog;


    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_apply_barcode, null);
        bs_prison_choose = (BetterSpinner) view.findViewById(R.id.bs_prison_choose);
        et_name = (EditText) view.findViewById(R.id.et_name);
        et_ic_card = (EditText) view.findViewById(R.id.et_ic_card);
        et_phone_num = (EditText) view.findViewById(R.id.et_phone_num);
        et_relationship_with_prisoner = (EditText) view.findViewById(R.id.et_relationship_with_prisoner);
        et_prisoner_name = (EditText) view.findViewById(R.id.et_prisoner_name);
        et_identifying_code = (EditText) view.findViewById(R.id.et_identifying_code);
        bt_send_identifying_code = (Button) view.findViewById(R.id.bt_send_identifying_code);
        bt_apply = (Button) view.findViewById(R.id.bt_apply);
        iv_add_photo_01 = (ImageView) view.findViewById(R.id.iv_add_photo_01);
        iv_add_photo_02 = (ImageView) view.findViewById(R.id.iv_add_photo_02);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("申请二维码");
        setBackVisibility(View.VISIBLE);
        prisonAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, PRISONS);
        bs_prison_choose.setAdapter(prisonAdapter);
        name = et_name.getText().toString().trim();
        ic_card = et_ic_card.getText().toString().trim();
        phone_num = et_phone_num.getText().toString().trim();
        relationship_with_prisoner = et_relationship_with_prisoner.getText().toString().trim();
        prisoner_name = et_prisoner_name.getText().toString().trim();
        identifying_code = et_identifying_code.getText().toString().trim();
        bt_apply.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_apply:
//                if(TextUtils.isEmpty(name)){
//                    showToastMsgShort("姓名为空");
//                    return;
//                }else if(TextUtils.isEmpty(ic_card)){
//                    showToastMsgShort("身份证号为空");
//                    return;
//                }else if(TextUtils.isEmpty(phone_num)){
//                    showToastMsgShort("手机号为空");
//                    return;
//                }else if(TextUtils.isEmpty(relationship_with_prisoner)){
//                    showToastMsgShort("与服刑人员关系为空");
//                    return;
//                }else if(TextUtils.isEmpty(prisoner_name)){
//                    showToastMsgShort("服刑人员姓名为空");
//                    return;
//                }else if(TextUtils.isEmpty(identifying_code)){
//                    showToastMsgShort("验证码为空");
//                    return;
//                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(false);
                    View view = View.inflate(this, R.layout.apply_barcode_dialog, null);
                    Button bt_ok = (Button) view.findViewById(R.id.bt_ok);
                    bt_ok.setOnClickListener(this);
                    dialog = builder.create();
                    builder.setView(view);
                    builder.show();
//                }
                break;
            case R.id.bt_ok:
                dialog.dismiss();
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(dialog != null && dialog.isShowing()){
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
