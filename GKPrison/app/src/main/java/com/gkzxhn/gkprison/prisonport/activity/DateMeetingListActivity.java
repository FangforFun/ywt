package com.gkzxhn.gkprison.prisonport.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.login.LoadingActivity;
import com.gkzxhn.gkprison.prisonport.http.HttpPatch;
import com.gkzxhn.gkprison.prisonport.adapter.CalendarViewAdapter;
import com.gkzxhn.gkprison.prisonport.bean.MeetingInfo;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.prisonport.view.CalendarCard;
import com.gkzxhn.gkprison.prisonport.view.CustomDate;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * created by hzn 2015.12.22
 */
public class DateMeetingListActivity extends BaseActivity implements CalendarCard.OnCellClickListener {

    private ViewPager mViewPager;
    private int mCurrentIndex = 498;
    private CalendarCard[] mShowViews;
    private CalendarViewAdapter<CalendarCard> adapter;
    private SildeDirection mDirection = SildeDirection.NO_SILDE;
    private ListView lv_meeting_list;
    private MeetingListAdapter meetingListAdapter;
    private LinearLayout ll_calendar;
    private ImageButton preImgBtn, nextImgBtn;
    private TextView monthText;
    private long mExitTime;//add by hzn 退出按键时间间隔
    private LinearLayout ll_loading;// 刷新
    private ProgressBar pb_loading;
    private TextView tv_loading;
    private CalendarCard[] views;
    private ScrollView scrollView;
    private SharedPreferences sp;
    private CustomDate mDate;
    private TextView tv_no_list;
    private RotateAnimation ra;
    private ProgressDialog progressDialog;
    private List<MeetingInfo> meetingInfos;
    private HttpClient httpClient;
    private AlertDialog cancel_meeting_dialog;// 取消会见对话框
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String result = (String) msg.obj;
                    if (!TextUtils.isEmpty(result)) {
                        parseResult(result);
                        showToastMsgShort("刷新成功");
                        ll_loading.setVisibility(View.GONE);
                        if (meetingListAdapter == null) {
                            meetingListAdapter = new MeetingListAdapter();
                            lv_meeting_list.setAdapter(meetingListAdapter);
                        } else {
                            meetingListAdapter.notifyDataSetChanged();
                        }
                        if (meetingInfos.size() == 0) {
                            tv_no_list.setVisibility(View.VISIBLE);
                        } else {
                            tv_no_list.setVisibility(View.GONE);
                        }
                        DensityUtil.setListViewHeightBasedOnChildren(lv_meeting_list);
                        lv_meeting_list.setVisibility(View.VISIBLE);
                        for (int i = 0; i < 3; i++) {
                            views[i].setEnabled(true);
                        }
                    }
                    ra.cancel();
                    break;
                case 1: // 取消视频成功
                    String cancel_result = (String) msg.obj;
                    try {
                        JSONObject jsonObject = new JSONObject(cancel_result);
                        int result_code = jsonObject.getInt("code");
                        if(result_code == 200){
                            //成功
//                            showToastMsgShort("取消成功");
                            progressDialog.setMessage("取消成功");
                            handler.postDelayed(dismissProgressDialogTask, 1500);
                            meetingListAdapter.notifyDataSetChanged();
                        }else {
                            // 失败 code为500
                            showToastMsgLong("取消失败，请稍后再试");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:// 取消视频失败
                    showToastMsgLong("发送取消失败，请稍后再试");
                    break;
                case 3:// 视频取消异常
                    showToastMsgLong("取消异常，请稍后再试");
                    break;
            }
        }
    };

    private Runnable dismissProgressDialogTask = new Runnable() {
        @Override
        public void run() {
            progressDialog.dismiss();
            // 重新刷新数据
            long currentDate = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(currentDate);
            String formatDate = format.format(date);
            requestData(formatDate);// 请求数据
        }
    };

    /**
     * 解析结果
     */
    private void parseResult(String result) {
        meetingInfos = new ArrayList<>();
        meetingInfos.clear();
        try {
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MeetingInfo meetingInfo = new MeetingInfo();
                meetingInfo.setId(jsonObject.getInt("id"));
                meetingInfo.setFamily_id(jsonObject.getInt("family_id"));
                meetingInfo.setMeeting_started(jsonObject.getString("meeting_started"));
                meetingInfo.setMeeting_finished(jsonObject.getString("meeting_finished"));
                meetingInfo.setName(jsonObject.getString("name"));
                meetingInfo.setPrison_area(TextUtils.isEmpty(jsonObject.getString("prison_area")) ? "默认监区" : jsonObject.getString("prison_area"));
                meetingInfo.setPrisoner_number(jsonObject.getString("prisoner_number"));
                meetingInfos.add(meetingInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clickDate(CustomDate date) {
        mDate = date;
        if ((date.getYear() + "年" + date.getMonth() + "月").equals(monthText.getText().toString())) {
            // 点击的是当月的
            scrollView.scrollTo(0, 0);// 刷新时滑到顶端
            requestData(date.getYear() + "-" + date.getMonth() + "-" + date.getDay());
        } else if (date.getMonth() < Integer.parseInt(monthText.getText().toString().split("年")[1].substring(0, monthText.getText().toString().split("年")[1].length() - 1))) {
            showToastMsgShort("左滑至下个月份");
        } else if (date.getMonth() > Integer.parseInt(monthText.getText().toString().split("年")[1].substring(0, monthText.getText().toString().split("年")[1].length() - 1))) {
            showToastMsgShort("右滑至上个月份");
        }
    }

    @Override
    public void changeDate(CustomDate date) {
        monthText.setText(date.getYear() + "年" + date.getMonth() + "月");
    }

    enum SildeDirection {
        RIGHT, LEFT, NO_SILDE;
    }

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_date_meeting_list, null);
        mViewPager = (ViewPager) view.findViewById(R.id.vp_calendar);
        preImgBtn = (ImageButton) view.findViewById(R.id.btnPreMonth);
        nextImgBtn = (ImageButton) view.findViewById(R.id.btnNextMonth);
        monthText = (TextView) view.findViewById(R.id.tvCurrentMonth);
        ll_calendar = (LinearLayout) view.findViewById(R.id.ll_calendar);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.getScreenWidthHeight(this)[0]);
        ll_calendar.setLayoutParams(params);
        lv_meeting_list = (ListView) view.findViewById(R.id.lv_meeting_list);
        ll_loading = (LinearLayout) view.findViewById(R.id.ll_loading);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        tv_loading = (TextView) view.findViewById(R.id.tv_loading);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        tv_no_list = (TextView) view.findViewById(R.id.tv_no_list);
        return view;
    }

    @Override
    protected void initData() {
        httpClient = HttpRequestUtil.initHttpClient(null);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("is_first", false);
        editor.commit();
        setRefreshVisibility(View.VISIBLE);
        StatusCode code = NIMClient.getStatus();
        showToastMsgShort("" + code);
        Log.i("监狱端进主页啦", code + sp.getString("password", "") + "---" + sp.getString("token", ""));
        setTitle("会见列表");
        setLogoutVisibility(View.VISIBLE);
        preImgBtn.setOnClickListener(this);
        nextImgBtn.setOnClickListener(this);
        views = new CalendarCard[3];
        for (int i = 0; i < 3; i++) {
            views[i] = new CalendarCard(this, this);
        }
        adapter = new CalendarViewAdapter<>(views);
        setViewPager();
        mDate = CalendarCard.mShowDate;
        long currentDate = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(currentDate);
        final String formatDate = format.format(date);
        requestData(formatDate);// 请求数据
        Log.i("时间", (CalendarCard.mShowDate.getDay() + "") + "---" + (formatDate.substring(formatDate.length() - 3, formatDate.length())));
        lv_meeting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mDate != null) {
//                    if ((mDate.getDay() + "").equals(formatDate.substring(formatDate.length() - 2, formatDate.length()))) {
                        Intent intent = new Intent(DateMeetingListActivity.this, CallUserActivity.class);
                        intent.putExtra("family_id", meetingInfos.get(position).getFamily_id());
                        startActivity(intent);
//                        showToastMsgShort(mDate.getDay() + "" + formatDate.substring(formatDate.length() - 3, formatDate.length()));
//                    } else {
//                        showToastMsgShort(mDate.getYear() + "-" + mDate.getMonth() + "-" + mDate.getDay() + "才能会见哦");
//                    }
                    Log.i("时间", (mDate.getDay() + "") + "---" + (formatDate.substring(formatDate.length() - 3, formatDate.length())));
                }
            }
        });
        rl_refresh.setOnClickListener(this);
        bt_logout.setOnClickListener(this);
    }

    /**
     * 刷新图标旋转动画任务
     */
    private Runnable rotateTask = new Runnable() {
        @Override
        public void run() {
            ra = new RotateAnimation(0, -360 * 100, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            ra.setDuration(1500 * 50);
            LinearInterpolator linearInterpolator = new LinearInterpolator();
            ra.setInterpolator(linearInterpolator);// 匀速
            iv_refresh.startAnimation(ra);
        }
    };

    /**
     * 请求会见列表数据
     */
    private void requestData(final String date) {
        if (Utils.isNetworkAvailable()) {
            handler.post(rotateTask);
            ll_loading.setVisibility(View.VISIBLE);
            tv_loading.setText("正在刷新...");
            pb_loading.setVisibility(View.VISIBLE);
            for (int i = 0; i < 3; i++) {
                views[i].setEnabled(false);
            }
            new Thread() {
                @Override
                public void run() {
                    SystemClock.sleep(2000);// 休眠2秒  模拟网络环境
//                    HttpUtils httpUtils = new HttpUtils();
                    Message msg = handler.obtainMessage();
//                    Log.i("会见列表请求", Constants.URL_HEAD +
//                            Constants.PRISON_PORT_MEETING_LIST_URL + sp.getString("username", "") + "&app_date=" + date);
//                    httpUtils.send(HttpRequest.HttpMethod.GET, Constants.URL_HEAD +
//                            Constants.PRISON_PORT_MEETING_LIST_URL + sp.getString("username", "") + "&app_date=" + date
//                            , new RequestCallBack<Object>() {
//                        @Override
//                        public void onSuccess(ResponseInfo<Object> responseInfo) {
//                            Log.i("请求成功", responseInfo.result.toString());
//                            msg.obj = responseInfo.result.toString();
//                            msg.what = 0;
//                            handler.sendMessage(msg);
//                        }
//
//                        @Override
//                        public void onFailure(HttpException e, String s) {
//                            Log.i("请求失败", s + "---" + e.getMessage());
//                            showToastMsgShort("刷新数据失败");
//                            tv_loading.setText("点击刷新");
//                            pb_loading.setVisibility(View.GONE);
//                            ll_loading.setOnClickListener(DateMeetingListActivity.this);
//                            for (int i = 0; i < 3; i++) {
//                                views[i].setEnabled(true);
//                            }
//                            ra.cancel();
//                        }
//                    });
                    try {
                        String result = HttpRequestUtil.doHttpsGet(Constants.URL_HEAD +
                                Constants.PRISON_PORT_MEETING_LIST_URL + sp.getString("username", "") + "&app_date=" + date);
                        if(result.contains("StatusCode is ")){
                            Log.i("请求失败", result);
                            showToastMsgShort("刷新数据失败");
                            tv_loading.setText("点击刷新");
                            pb_loading.setVisibility(View.GONE);
                            ll_loading.setOnClickListener(DateMeetingListActivity.this);
                            for (int i = 0; i < 3; i++) {
                                views[i].setEnabled(true);
                            }
                            ra.cancel();
                        }else {
                            Log.i("请求成功", result);
                            msg.obj = result;
                            msg.what = 0;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            showToastMsgShort("没有网络");
        }
    }

    /**
     * 设置日历页面viewpager
     */
    private void setViewPager() {
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(498);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                measureDirection(position);
                updateCalendarView(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnPreMonth:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                break;
            case R.id.btnNextMonth:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                break;
            case R.id.ll_loading:
                if (views != null) {
                    requestData(CalendarCard.mShowDate.getYear() + "-" + CalendarCard.mShowDate.getMonth() + "-" + CalendarCard.mShowDate.getDay());
                }
                break;
            case R.id.rl_refresh:
                scrollView.scrollTo(0, 0);
                long currentDate = System.currentTimeMillis();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date(currentDate);
                String formatDate = format.format(date);
                requestData(formatDate);// 请求数据
                break;
            case R.id.bt_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View logout_dialog_view = View.inflate(DateMeetingListActivity.this, R.layout.msg_ok_cancel_dialog, null);
                TextView tv_cancel = (TextView) logout_dialog_view.findViewById(R.id.tv_cancel);
                TextView tv_ok = (TextView) logout_dialog_view.findViewById(R.id.tv_ok);
                builder.setView(logout_dialog_view);
                final AlertDialog dialog = builder.create();
                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                tv_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DateMeetingListActivity.this, LoadingActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear();
                        editor.commit();
                        startActivity(intent);
                        NIMClient.getService(AuthService.class).logout();
                    }
                });
                dialog.show();
                break;
            default:
                break;
        }
    }

    /**
     * 计算方向
     *
     * @param arg0
     */
    private void measureDirection(int arg0) {

        if (arg0 > mCurrentIndex) {
            mDirection = SildeDirection.RIGHT;

        } else if (arg0 < mCurrentIndex) {
            mDirection = SildeDirection.LEFT;
        }
        mCurrentIndex = arg0;
    }

    // 更新日历视图
    private void updateCalendarView(int arg0) {
        mShowViews = adapter.getAllItems();
        if (mDirection == SildeDirection.RIGHT) {
            mShowViews[arg0 % mShowViews.length].rightSlide();
        } else if (mDirection == SildeDirection.LEFT) {
            mShowViews[arg0 % mShowViews.length].leftSlide();
        }
        mDirection = SildeDirection.NO_SILDE;
    }

    public class MeetingListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return meetingInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(DateMeetingListActivity.this, R.layout.meeting_list_item, null);
                holder = new ViewHolder();
                holder.tv_meeting_name = (TextView) convertView.findViewById(R.id.tv_meeting_name);
                holder.tv_meeting_time = (TextView) convertView.findViewById(R.id.tv_meeting_time);
                holder.tv_meeting_prison_area = (TextView) convertView.findViewById(R.id.tv_meeting_prison_area);
                holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_meeting_name.setText(meetingInfos.get(position).getName());
            String meeting_time_start = meetingInfos.get(position).getMeeting_started();
            String meeting_time_finished = meetingInfos.get(position).getMeeting_finished();
            Log.i("会见时间段",meeting_time_start.split(" ")[1].substring(0, meeting_time_start.split(" ")[1].lastIndexOf(":")) + "-" + meeting_time_finished.split(" ")[1].substring(0, meeting_time_finished.split(" ")[1].lastIndexOf(":")));
            holder.tv_meeting_time.setText(meeting_time_start.split(" ")[1].substring(0, meeting_time_start.split(" ")[1].lastIndexOf(":")) + "-" + meeting_time_finished.split(" ")[1].substring(0, meeting_time_finished.split(" ")[1].lastIndexOf(":")));
            holder.tv_meeting_prison_area.setText(meetingInfos.get(position).getPrison_area());
            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DateMeetingListActivity.this);
                    View cancel_dialog = View.inflate(DateMeetingListActivity.this, R.layout.cancel_meeting_dialog, null);
                    TextView tv_cancel_name = (TextView) cancel_dialog.findViewById(R.id.tv_cancel_name);
                    tv_cancel_name.setText(holder.tv_meeting_name.getText().toString().trim());
                    final EditText et_cancel_reason = (EditText) cancel_dialog.findViewById(R.id.et_cancel_reason);
                    TextView tv_cancel = (TextView) cancel_dialog.findViewById(R.id.tv_cancel);
                    TextView tv_ok = (TextView) cancel_dialog.findViewById(R.id.tv_ok);
                    cancel_meeting_dialog = builder.create();
                    cancel_meeting_dialog.setView(cancel_dialog);
                    tv_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String reason = et_cancel_reason.getText().toString().trim();
                            if (!TextUtils.isEmpty(reason)) {
                                sendCancelMeetingToServer(position, meetingInfos.get(position).getId(), reason);
                                cancel_meeting_dialog.dismiss();
                            } else {
                                showToastMsgShort("请输入理由");
                            }
                        }
                    });
                    tv_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cancel_meeting_dialog.dismiss();
                        }
                    });
                    cancel_meeting_dialog.show();
                }
            });
            return convertView;
        }
    }

    /**
     * 发送取消会见至服务器
     */
    private void sendCancelMeetingToServer(final int position, final int id, final String reason) {
        if (Utils.isNetworkAvailable()) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("正在提交，请稍后");
            progressDialog.show();
            new Thread() {
                @Override
                public void run() {
//                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPatch httpPatch = new HttpPatch(Constants.URL_HEAD + "applies/" + id);
                    try {
                        StringEntity entity = new StringEntity("{\"accept_apply\":{\"status\":\"cancel\",\"reason\":\"" + reason + "\"}}", HTTP.UTF_8);
                        entity.setContentType("application/json");
                        httpPatch.setEntity(entity);
                        HttpResponse response = httpClient.execute(httpPatch);
                        Log.i("取消会见", "{\"accept_apply\":{\"status\":\"cancel\",\"reason\":\"" + reason + "\"}}");
                        if (response.getStatusLine().getStatusCode() == 200) {
                            String result = EntityUtils.toString(response.getEntity(), "utf-8");
                            Log.i("取消会见2", result);
                            meetingInfos.remove(meetingInfos.get(position));
                            Message message = handler.obtainMessage();
                            message.what = 1;
                            message.obj = result;
                            handler.sendMessage(message);
                        } else {
                            String result = EntityUtils.toString(response.getEntity(), "utf-8");
                            Log.i("取消会见1", result);
                            Message message = handler.obtainMessage();
                            message.what = 2;
                            message.obj = result;
                            handler.sendMessage(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(3);
                    }
                }
            }.start();
        } else {
            showToastMsgShort("没有网络");
        }
    }

    private static class ViewHolder {
        TextView tv_meeting_time;
        TextView tv_meeting_name;
        TextView tv_meeting_prison_area;
        ImageView iv_delete;
    }

    /**
     * 按兩次返回退出程序   add by hzn
     *
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
