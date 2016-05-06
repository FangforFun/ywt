package com.gkzxhn.gkprison.prisonport.activity;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.AVChatActivity;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.prisonport.bean.FamilyMeetingInfo;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.tool.Log;
import com.gkzxhn.gkprison.utils.Utils;
import com.lidroid.xutils.BitmapUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * created by huangzhengneng on 2016/1/12
 * 准备呼叫页面
 */
public class CallUserActivity extends BaseActivity {

    private Button bt_call; // 呼叫按钮  默认是不可用的  当成功解析详细会见信息后恢复可用
    private RelativeLayout rl_video_view;
    private LinearLayout ll_id_card_photo;// 身份证正反面
    private ImageView iv_id_card_01;
    private ImageView iv_id_card_02;
    private RelativeLayout rl_getting;
    private BitmapUtils bitmapUtil;
    private SharedPreferences sp;
    private int family_id;
    private TextView tv_meeting_notice;
    private String prisoner_name;
    private FamilyMeetingInfo familyMeetingInfo;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    String result = (String) msg.obj;
                    parseMeetingInfo(result);
                    break;
                case 1: // 失败
                    rl_getting.setVisibility(View.GONE);
                    showToastMsgShort("获取身份证照片失败");
                    break;
                case 2:// 异常
                    rl_getting.setVisibility(View.GONE);
                    showToastMsgShort("获取身份证照片异常");
                    break;
            }
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_call_user, null);
        bt_call = (Button) view.findViewById(R.id.bt_call);
        rl_video_view = (RelativeLayout) view.findViewById(R.id.rl_video_view);
        ll_id_card_photo = (LinearLayout) view.findViewById(R.id.ll_id_card_photo);
        iv_id_card_01 = (ImageView) view.findViewById(R.id.iv_id_card_01);
        iv_id_card_02 = (ImageView) view.findViewById(R.id.iv_id_card_02);
        rl_getting = (RelativeLayout) view.findViewById(R.id.rl_getting);
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.getScreenWidthHeight(this)[0] - DensityUtil.dip2px(this, 80));
//        rl_video_view.setLayoutParams(params);
        tv_meeting_notice = (TextView) view.findViewById(R.id.tv_meeting_notice);
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        setTitle("远程会见");
        setBackVisibility(View.VISIBLE);
        bitmapUtil = new BitmapUtils(this);
        family_id = getIntent().getIntExtra("family_id", 0);
        prisoner_name = getIntent().getStringExtra("prisoner_name");
        Log.i("family_id", family_id + "");
        getMeetingDetailInfo(family_id);
        bt_call.setOnClickListener(this);
        int[] screenWidthHeight = DensityUtil.getScreenWidthHeight(this);
        Log.i("screenWidthHeight is : ", screenWidthHeight[0] + "---" + screenWidthHeight[1]);
//        String filename = sp.getString("错误信息文件名...", "");
//        try {
//            if (!TextUtils.isEmpty(filename)) {
//                Log.i("错误信息文件名...", filename);
//                File file = new File(Environment.getExternalStorageDirectory() + "/crash/" + filename);
//                if (file.isFile() && file.exists()) { //判断文件是否存在
//                    InputStreamReader read = new InputStreamReader(
//                            new FileInputStream(file));//考虑到编码格式
//                    BufferedReader bufferedReader = new BufferedReader(read);
//                    String lineTxt = null;
//                    while ((lineTxt = bufferedReader.readLine()) != null) {
//                        System.out.println(lineTxt);
//                        tv_meeting_notice.setText(lineTxt);
//                    }
//                    read.close();
//                }
//            }else {
//                showToastMsgShort("找不到文件...");
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }
    }

    /**
     * 获取会见的详细信息
     * @param family_id  家属idtr
     */
    private void getMeetingDetailInfo(final int family_id) {
        rl_getting.setVisibility(View.VISIBLE);
        new Thread(){
            @Override
            public void run() {
                try {
                    String result = HttpRequestUtil.doHttpsGet(Constants.URL_HEAD + "families/" + family_id
                            + "?access_token=" + sp.getString("token", ""));
                    Log.i("会见详细请求成功", result);
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    msg.obj = result;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(2);
                }
            }
        }.start();
    }

    /**
     * 解析会见详情信息
     * @param result
     */
    private void parseMeetingInfo(String result) {
        familyMeetingInfo = new FamilyMeetingInfo();
        try {
            JSONObject jsonObject = new JSONObject(result);
            familyMeetingInfo.setCode(jsonObject.getInt("code"));
            familyMeetingInfo.setAccid(jsonObject.getString("accid"));
            JSONObject jsonObject1 = jsonObject.getJSONObject("family");
            FamilyMeetingInfo.Family family = familyMeetingInfo.new Family();
            family.setId(jsonObject1.getInt("id"));
            family.setPrisoner_id(jsonObject1.getInt("prisoner_id"));
            family.setName(jsonObject1.getString("name"));
            family.setPhone(jsonObject1.getString("phone"));
            family.setRelationship(jsonObject1.getString("relationship"));
            family.setUuid(jsonObject1.getString("uuid"));
            family.setCreated_at(jsonObject1.getString("created_at"));
            family.setUpdated_at(jsonObject1.getString("updated_at"));
            family.setImage_url(jsonObject1.getString("image_url"));
            familyMeetingInfo.setFamily(family);
            setImageResourse();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置身份证正反面照
     */
    private void setImageResourse() {
        String image_url = familyMeetingInfo.getFamily().getImage_url();
        if(image_url.contains("|")) {
            String[] img_urls = image_url.split("\\|");
            bitmapUtil.display(iv_id_card_01, Constants.RESOURSE_HEAD + img_urls[0]);
            bitmapUtil.display(iv_id_card_02, Constants.RESOURSE_HEAD + img_urls[1]);
            Log.i("tupian", Constants.RESOURSE_HEAD + img_urls[0] + "---" + Constants.URL_HEAD + img_urls[1]);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("img_url_01", Constants.RESOURSE_HEAD + img_urls[0]);
            editor.putString("img_url_02", Constants.RESOURSE_HEAD + img_urls[1]);
            editor.putString("img_url_03", Constants.RESOURSE_HEAD + img_urls[2]);
            editor.commit();
            bt_call.setEnabled(true);
            rl_getting.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_call:
                if(Utils.isNetworkAvailable()) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("family_accid", familyMeetingInfo.getAccid());
                    editor.putString("prisoner_name", prisoner_name);
                    editor.commit();
                    Log.i("Call User Activity ---> ", familyMeetingInfo.getAccid() + "");
                    AVChatActivity.start(this,
                            familyMeetingInfo.getAccid()
                            , 2, AVChatActivity.FROM_INTERNAL); // 2 视频通话  1语音
                }else {
                    showToastMsgShort("没有网络，请检查网络设置");
                }
                break;
        }
    }
}
