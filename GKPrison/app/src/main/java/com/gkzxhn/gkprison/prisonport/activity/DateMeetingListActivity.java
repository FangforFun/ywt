package com.gkzxhn.gkprison.prisonport.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import com.gkzxhn.gkprison.prisonport.adapter.CalendarViewAdapter;
import com.gkzxhn.gkprison.prisonport.bean.MeetingInfo;
import com.gkzxhn.gkprison.prisonport.view.CalendarCard;
import com.gkzxhn.gkprison.prisonport.view.CustomDate;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.squareup.okhttp.Call;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * created by hzn 2015.12.22
 */
public class DateMeetingListActivity extends BaseActivity implements CalendarCard.OnCellClickListener{

    private final String[] MEETING_NAMES = {"张三", "李四", "王五", "赵六"};
    private final String[] MEETING_TIMES = {"9:00-9:20", "9:30-9:50", "10:00-10:20", "10:30-10:50"};
    private final String[] MEETING_AREAS = {"第一监区", "第二监区", "第三监区", "第四监区"};
    private final String[] MEETING_IDS = {"18774810958", "18670341296", "13647491573", "18670732143"};

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
    private List<MeetingInfo> meetingInfoList;
    private ScrollView scrollView;
    private SharedPreferences sp;
    private CustomDate mDate;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    String result = (String) msg.obj;
                    if(!TextUtils.isEmpty(result)){
                        parseResult(result);
                        showToastMsgShort("刷新成功");
                        ll_loading.setVisibility(View.GONE);
                        if(meetingListAdapter == null) {
                            meetingListAdapter = new MeetingListAdapter();
                            lv_meeting_list.setAdapter(meetingListAdapter);
                        }else {
                            meetingListAdapter.notifyDataSetChanged();
                        }
                        DensityUtil.setListViewHeightBasedOnChildren(lv_meeting_list);
                        lv_meeting_list.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };

    /**
     * 解析结果
     */
    private void parseResult(String result) {
        meetingInfoList = new ArrayList<>();
        meetingInfoList.clear();
        try {
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MeetingInfo meetingInfo = new MeetingInfo();
                meetingInfo.setName(jsonObject.getString("name"));
                meetingInfo.setPhone(jsonObject.getString("phone"));
                meetingInfo.setPrisoner_number(jsonObject.getString("prisoner_number"));
                meetingInfo.setRelationship(jsonObject.getString("relationship"));
                meetingInfo.setUuid(jsonObject.getString("uuid"));
                meetingInfo.setAccess_token(jsonObject.getString("access_token"));
//                meetingInfo.setPrison_area(jsonObject.getString("prison_area"));
//                meetingInfo.setReply_date(jsonObject.getString("reply_date"));
                meetingInfo.setPrisoner_name(jsonObject.getString("prisoner_name"));
                meetingInfo.setImage_url(TextUtils.isEmpty(jsonObject.getString("image_url")) ? "url错误" : jsonObject.getString("image_url"));
                meetingInfoList.add(meetingInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Message msg;

    @Override
    public void clickDate(CustomDate date) {
        mDate = date;
        if((date.getYear() + "年" + date.getMonth() + "月").equals(monthText.getText().toString())){
            // 点击的是当月的
//            showToastMsgShort(date.getYear() + "年" + date.getMonth() + "月" + date.getDay() + "日");
            scrollView.scrollTo(0,0);// 刷新时滑到顶端
            requestData(date.getYear() + "-" + date.getMonth() + "-" + date.getDay());
        }else if(date.getMonth() < Integer.parseInt(monthText.getText().toString().split("年")[1].substring(0, monthText.getText().toString().split("年")[1].length() - 1))){
            showToastMsgShort("左滑至下个月份");
        }else if(date.getMonth() > Integer.parseInt(monthText.getText().toString().split("年")[1].substring(0, monthText.getText().toString().split("年")[1].length() - 1))){
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
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("is_first", false);
        editor.commit();
        StatusCode code = NIMClient.getStatus();
        Log.i("监狱端进主页啦", code + sp.getString("password", "") + "---" + sp.getString("token", ""));
        setTitle("会见列表");
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
                if(mDate != null) {
                    if((mDate.getDay() + "").equals(formatDate.substring(formatDate.length() - 2, formatDate.length()))){
                        Intent intent = new Intent(DateMeetingListActivity.this, CallUserActivity.class);
                        intent.putExtra("申请人", meetingInfoList.get(position).getName());
                        intent.putExtra("accid", meetingInfoList.get(position).getAccess_token());
                        intent.putExtra("image_url", meetingInfoList.get(position).getImage_url());
                        startActivity(intent);
                        showToastMsgShort(mDate.getDay() + "" + formatDate.substring(formatDate.length() - 3, formatDate.length()));
                    }else {
                        showToastMsgShort(mDate.getYear() + "-" + mDate.getMonth() + "-" + mDate.getDay() + "才能会见哦");
                    }
                    Log.i("时间", (mDate.getDay() + "") + "---" + (formatDate.substring(formatDate.length() - 3, formatDate.length())));
                }
            }
        });
    }

    /**
     * 请求会见列表数据
     */
    private void requestData(final String date) {
        ll_loading.setVisibility(View.VISIBLE);
        tv_loading.setText("正在刷新...");
        pb_loading.setVisibility(View.VISIBLE);
        new Thread(){
            @Override
            public void run() {
                SystemClock.sleep(2000);
                HttpUtils httpUtils = new HttpUtils();
                msg = handler.obtainMessage();
                Log.i("会见列表请求", Constants.URL_HEAD +
                        Constants.PRISON_PORT_MEETING_LIST_URL + date);
                httpUtils.send(HttpRequest.HttpMethod.GET, Constants.URL_HEAD +
//                        "---hahah" +
                        Constants.PRISON_PORT_MEETING_LIST_URL + date, new RequestCallBack<Object>() {
                    @Override
                    public void onSuccess(ResponseInfo<Object> responseInfo) {
                        Log.i("请求成功", responseInfo.result.toString());
                        msg.obj = responseInfo.result.toString();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Log.i("请求失败", s + "---" + e.getMessage());
                        showToastMsgShort("刷新数据失败");
                        tv_loading.setText("点击刷新");
                        pb_loading.setVisibility(View.GONE);
                        ll_loading.setOnClickListener(DateMeetingListActivity.this);
                    }
                });
            }
        }.start();
    }

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
                if(views != null) {
                    requestData(CalendarCard.mShowDate.getYear() + "-" + CalendarCard.mShowDate.getMonth() + "-" + CalendarCard.mShowDate.getDay());
                }
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
            return meetingInfoList.size();
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
            if(convertView == null){
                convertView = View.inflate(DateMeetingListActivity.this, R.layout.meeting_list_item, null);
                holder = new ViewHolder();
                holder.tv_meeting_name = (TextView) convertView.findViewById(R.id.tv_meeting_name);
                holder.tv_meeting_time = (TextView) convertView.findViewById(R.id.tv_meeting_time);
                holder.tv_meeting_prison_area = (TextView) convertView.findViewById(R.id.tv_meeting_prison_area);
                holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_meeting_name.setText(meetingInfoList.get(position).getName());
            holder.tv_meeting_time.setText(MEETING_TIMES[0]);
            holder.tv_meeting_prison_area.setText(MEETING_AREAS[0]);
            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showToastMsgShort("删除" + holder.tv_meeting_name.getText().toString() + position);
                    AlertDialog.Builder builder = new AlertDialog.Builder(DateMeetingListActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("确定取消" + holder.tv_meeting_name.getText().toString() + "的会见？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showToastMsgShort("已取消" + holder.tv_meeting_name.getText().toString() + "的会见");
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showToastMsgShort("取消");
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            });
            return convertView;
        }
    }

    private static class ViewHolder{
        TextView tv_meeting_time;
        TextView tv_meeting_name;
        TextView tv_meeting_prison_area;
        ImageView iv_delete;
    }

    /**
     * 按兩次返回退出程序   add by hzn
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
