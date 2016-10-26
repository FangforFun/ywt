package com.gkzxhn.gkprison.prisonport.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.gkzxhn.gkprison.prisonport.adapter.CalendarViewAdapter;
import com.gkzxhn.gkprison.prisonport.bean.MeetingInfo;
import com.gkzxhn.gkprison.prisonport.requests.ApiService;
import com.gkzxhn.gkprison.prisonport.view.CalendarCard;
import com.gkzxhn.gkprison.prisonport.view.CustomDate;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.keda.sky.app.TruetouchGlobal;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
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
 * created by hzn 2015.12.22
 * 平板端会见列表页面
 */
public class DateMeetingListActivity extends BaseActivity implements CalendarCard.OnCellClickListener {

    private static final java.lang.String TAG = "DateMeetingListActivity";
    @BindView(R.id.pb_loading)
    ProgressBar pb_loading;
    @BindView(R.id.tv_loading)
    TextView tv_loading;
    @BindView(R.id.ll_loading)
    LinearLayout ll_loading;
    @BindView(R.id.btnPreMonth)
    ImageButton preImgBtn;
    @BindView(R.id.btnNextMonth)
    ImageButton nextImgBtn;
    @BindView(R.id.tvCurrentMonth)
    TextView monthText;
    @BindView(R.id.vp_calendar)
    ViewPager mViewPager;
    @BindView(R.id.ll_calendar)
    LinearLayout ll_calendar;
    @BindView(R.id.tv_meeting_num)
    TextView tv_meeting_num;
    @BindView(R.id.lv_meeting_list)
    ListView lv_meeting_list;
    @BindView(R.id.tv_no_list)
    TextView tv_no_list;
    @BindView(R.id.fl_transparent)
    FrameLayout fl_transparent;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    private int mCurrentIndex = 498;
    private CalendarCard[] mShowViews;
    private CalendarViewAdapter<CalendarCard> adapter;
    private SildeDirection mDirection = SildeDirection.NO_SILDE;
    private MeetingListAdapter meetingListAdapter;
    private long mExitTime;//add by hzn 退出按键时间间隔
    private CalendarCard[] views;
    private SharedPreferences sp;
    private CustomDate mDate;
    private RotateAnimation ra;
    private ProgressDialog progressDialog;
    private List<MeetingInfo> meetingInfos;
    private AlertDialog cancel_meeting_dialog;// 取消会见对话框
    private int[] screenWidthHeight;// 屏幕宽带
    private Handler handler = new Handler();

    private Runnable dismissProgressDialogTask = new Runnable() {
        @Override
        public void run() {
            progressDialog.dismiss();
            // 重新刷新数据
            long currentDate = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = new Date(currentDate);
            String formatDate = format.format(date);
            requestData(formatDate);// 请求数据
        }
    };

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
        screenWidthHeight = DensityUtil.getScreenWidthHeight(this);
        View view;
        if (screenWidthHeight[0] == 1280 && screenWidthHeight[1] == 720) {
            view = View.inflate(this, R.layout.activity_date_meeting_list, null);
            ButterKnife.bind(this, view);
        } else {
            view = View.inflate(this, R.layout.activity_date_meeting_list_tablet, null);
            ButterKnife.bind(this, view);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.getScreenWidthHeight(this)[0]);
        ll_calendar.setLayoutParams(params);
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        SPUtil.put(this, "is_first", false);
        setRefreshVisibility(View.VISIBLE);
        StatusCode code = NIMClient.getStatus();
        checkStatusCode(code);// 判断当前用户状态方法
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date(currentDate);
        final String formatDate = format.format(date);
        if (code == StatusCode.LOGINED) {// 已登录状态才刷新数据
            requestData(formatDate);// 请求数据
        }
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
        fl_transparent.setOnClickListener(this);
    }

    /**
     * 判断当前云信id状态
     */
    private void checkStatusCode(StatusCode code) {
        if (code == StatusCode.KICKOUT) {// 被其他端挤掉
            showKickoutDialog();
        } else if (code == StatusCode.CONNECTING) {// 正在连接
            showToastMsgShort("正在连接...");
        } else if (code == StatusCode.LOGINING) {// 正在登录
            showToastMsgShort("正在登录...");
        } else if (code == StatusCode.NET_BROKEN) { // 网络连接已断开
            showToastMsgLong("网络连接已断开，请检查网络设置");
        } else if (code == StatusCode.UNLOGIN) {// 未登录
            showToastMsgShort("未登录");
        } else {

        }
    }

    /**
     * 云信id在其他设备登录
     */
    private void showKickoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("账号下线提示");
        builder.setCancelable(false);
        builder.setMessage("您的账号" + sp.getString("token", "") + "在其他设备登录，点击重新登录。");
        builder.setPositiveButton("重新登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DateMeetingListActivity.this, LoadingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                // 防止不重新登录直接退出当再次进来还需要经过欢迎页面
                SPUtil.put(DateMeetingListActivity.this, "is_first", false);
                startActivity(intent);
                NIMClient.getService(AuthService.class).logout();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
    private void requestData(String date) {
        if (Utils.isNetworkAvailable(this)) {
            handler.post(rotateTask);
            ll_loading.setVisibility(View.VISIBLE);
            tv_loading.setText("正在刷新...");
            pb_loading.setVisibility(View.VISIBLE);
            fl_transparent.setVisibility(View.VISIBLE);
            for (int i = 0; i < 3; i++) {
                views[i].setEnabled(false);
            }
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.URL_HEAD)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiService api = retrofit.create(ApiService.class);
            Map<String, String> map = new HashMap<>();
            map.put("app_date", date);
            String accid = (String) SPUtil.get(this, "username", "");
            Log.i(TAG, date + "---" + accid);
            api.getMeetingList(accid, map)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<MeetingInfo>>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "request meeting list failed ：" + e.getMessage());
                            showToastMsgShort("刷新数据失败");
                            tv_loading.setText("点击刷新");
                            pb_loading.setVisibility(View.GONE);
                            ll_loading.setOnClickListener(DateMeetingListActivity.this);
                            fl_transparent.setVisibility(View.GONE);
                            for (int i = 0; i < 3; i++) {
                                views[i].setEnabled(true);
                            }
                            ra.cancel(); // 取消刷新图标的旋转任务
                        }

                        @Override
                        public void onNext(List<MeetingInfo> infos) {
                            meetingInfos = new ArrayList<>();
                            meetingInfos.clear();
                            for (MeetingInfo info : infos){
                                meetingInfos.add(info);
                                Log.i(TAG, info.toString());
                            }
                            showToastMsgShort("刷新成功");
                            checkDataSize();// 检查数据判断是否为空
                            DensityUtil.setListViewHeightBasedOnChildren(lv_meeting_list);
                            ll_loading.setVisibility(View.GONE);
                            lv_meeting_list.setVisibility(View.VISIBLE);
                            for (int i = 0; i < 3; i++) {
                                views[i].setEnabled(true);
                            }
                            ra.cancel();
                            fl_transparent.setVisibility(View.GONE); // 刷新完数据隐藏
                        }
                    });
        } else {
            showToastMsgShort("没有网络");
        }
    }

    /**
     * 检查数据是否为空  显示UI
     */
    private void checkDataSize() {
        if (meetingInfos.size() == 0) {
            tv_no_list.setVisibility(View.VISIBLE);
        } else {
            tv_no_list.setVisibility(View.GONE);
        }
        if (meetingListAdapter == null) {
            meetingListAdapter = new MeetingListAdapter();
            lv_meeting_list.setAdapter(meetingListAdapter);
        } else {
            meetingListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置日历页面viewpager
     */
    private void setViewPager() {
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(498);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                measureDirection(position);
                updateCalendarView(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {}

            @Override
            public void onPageScrollStateChanged(int arg0) {}
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
                        SPUtil.clear(DateMeetingListActivity.this);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear();
                        editor.apply();
                        startActivity(intent);
                        NIMClient.getService(AuthService.class).logout();
                        TruetouchGlobal.logOff();
                    }
                });
                dialog.show();
                break;
            case R.id.fl_transparent:

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
            holder.tv_meeting_time.setText(meeting_time_start.split(" ")[1].substring(0, meeting_time_start.split(" ")[1].lastIndexOf(":")) + "-" + meeting_time_finished.split(" ")[1].substring(0, meeting_time_finished.split(" ")[1].lastIndexOf(":")));
            holder.tv_meeting_prison_area.setText(meetingInfos.get(position).getPrison_area());
            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCancelDialog(holder, position);
                }
            });
            return convertView;
        }
    }

    /**
     * 取消会见对话框
     * @param holder
     * @param position
     */
    private void showCancelDialog(ViewHolder holder, final int position) {
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

    /**
     * 发送取消会见至服务器
     */
    private void sendCancelMeetingToServer(final int position, int id, String reason) {
        if (Utils.isNetworkAvailable(this)) {
            initAndShowDialog();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.URL_HEAD)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiService apiService = retrofit.create(ApiService.class);
            String msg = "{\"accept_apply\":{\"status\":\"cancel\",\"reason\":\"" + reason + "\"}}";
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), msg);
            apiService.cancelMeeting(id, body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "cancel meeting failed : " + e.getMessage());
                            showToastMsgLong("取消失败，请稍后再试");
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            meetingInfos.remove(meetingInfos.get(position));
                            try {
                                String cancel_result = responseBody.string();
                                JSONObject jsonObject = new JSONObject(cancel_result);
                                int result_code = jsonObject.getInt("code");
                                Log.i(TAG, "cancel meeting : " + cancel_result);
                                if (result_code == 200) {
                                    //成功
                                    progressDialog.setMessage("取消成功");
                                    handler.postDelayed(dismissProgressDialogTask, 1500);
                                    meetingListAdapter.notifyDataSetChanged();
                                } else {
                                    // 失败 code为500
                                    showToastMsgLong("取消失败，请稍后再试");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } else {
            showToastMsgShort("没有网络");
        }
    }

    private void initAndShowDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("正在提交，请稍后");
        progressDialog.show();
    }

    private static class ViewHolder {
        TextView tv_meeting_time;
        TextView tv_meeting_name;
        TextView tv_meeting_prison_area;
        ImageView iv_delete;
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
}
