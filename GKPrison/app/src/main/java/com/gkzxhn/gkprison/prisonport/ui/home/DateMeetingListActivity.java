package com.gkzxhn.gkprison.prisonport.ui.home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.app.module.ApiModule;
import com.gkzxhn.gkprison.base.BaseActivityNew;
import com.gkzxhn.gkprison.widget.view.calendar.CalendarViewAdapter;
import com.gkzxhn.gkprison.prisonport.bean.MeetingInfo;
import com.gkzxhn.gkprison.prisonport.ui.CallUserActivity;
import com.gkzxhn.gkprison.widget.view.calendar.CalendarCard;
import com.gkzxhn.gkprison.widget.view.calendar.CustomDate;
import com.gkzxhn.gkprison.userport.ui.main.MainUtils;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.gkzxhn.gkprison.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * created by hzn 2015.12.22
 * 平板端会见列表页面
 */
public class DateMeetingListActivity extends BaseActivityNew implements CalendarCard.OnCellClickListener, DateMeetingContract.View, AdapterView.OnItemClickListener {

    private static final java.lang.String TAG = "DateMeetingListActivity";
    @BindView(R.id.pb_loading) ProgressBar pb_loading;
    @BindView(R.id.tv_loading) TextView tv_loading;
    @BindView(R.id.ll_loading) LinearLayout ll_loading;
    @BindView(R.id.tvCurrentMonth) TextView monthText;
    @BindView(R.id.vp_calendar) ViewPager mViewPager;
    @BindView(R.id.ll_calendar) LinearLayout ll_calendar;
    @BindView(R.id.lv_meeting_list) ListView lv_meeting_list;
    @BindView(R.id.tv_no_list) TextView tv_no_list;
    @BindView(R.id.scrollView) ScrollView scrollView;
    @BindView(R.id.tv_title) TextView tv_title;// 标题
    @BindView(R.id.rl_refresh) RelativeLayout rl_refresh;// 刷新
    @BindView(R.id.iv_refresh) ImageView iv_refresh;
    @BindView(R.id.tv_logout) TextView tv_logout;// 注销

    private int mCurrentIndex = 498;
    private CalendarViewAdapter<CalendarCard> adapter;
    private SildeDirection mDirection = SildeDirection.NO_SILDE;
    private MeetingListAdapter meetingListAdapter;
    private long mExitTime;//add by hzn 退出按键时间间隔
    private CalendarCard[] views;
    private CustomDate mDate;
    private RotateAnimation ra;
    private ProgressDialog progressDialog;
    private List<MeetingInfo> meetingInfos;
    private AlertDialog cancel_meeting_dialog;// 取消会见对话框
    private Handler handler = new Handler();

    private AlertDialog confirmDialog;

    @Inject DateMeetingPresenter mPresenter;

    private Runnable dismissProgressDialogTask = new Runnable() {
        @Override
        public void run() {
            progressDialog.dismiss();
            // 重新刷新数据
            mPresenter.requestDataList(mDate.getYear() + "-"
                    + mDate.getMonth() + "-" + mDate.getDay());// 请求数据
        }
    };

    @Override
    public void clickDate(CustomDate date) {
        mDate = date;
        if ((date.getYear() + "年" + date.getMonth() + "月").equals(monthText.getText().toString())) {
            // 点击的是当月的
            scrollView.scrollTo(0, 0);// 刷新时滑到顶端
            mPresenter.requestDataList(date.getYear() + "-" + date.getMonth() + "-" + date.getDay());// 请求数据
        } else if (date.getMonth() < Integer.parseInt(monthText.getText().toString().split("年")[1].
                substring(0, monthText.getText().toString().split("年")[1].length() - 1))) {
            showToast(getString(R.string.left_sliding));
        } else if (date.getMonth() > Integer.parseInt(monthText.getText().toString().split("年")[1].
                substring(0, monthText.getText().toString().split("年")[1].length() - 1))) {
            showToast(getString(R.string.right_sliding));
        }
    }

    /**
     * 开启此activity
     * @param mContext
     */
    public static void startActivity(Context mContext){
        Intent intent = new Intent(mContext, DateMeetingListActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    public void changeDate(CustomDate date) {
        monthText.setText(date.getYear() + "年" + date.getMonth() + "月");
    }

    @Override
    public void showToast(String msg) {
        ToastUtil.showShortToast(msg);
    }

    @Override
    public void requestDataStart() {
        handler.post(rotateTask);
        ll_loading.setVisibility(View.VISIBLE);
        tv_loading.setText(getString(R.string.pull_to_refresh_refreshing_label));
        pb_loading.setVisibility(View.VISIBLE);
        for (int i = 0; i < 3; i++) {
            views[i].setEnabled(false);
        }
    }

    @Override
    public void requestDataComplete(List<MeetingInfo> meetingInfoList) {
        meetingInfos = new ArrayList<>();
        meetingInfos.clear();
        for (MeetingInfo info : meetingInfoList){
            meetingInfos.add(info);
            Log.i(TAG, info.toString());
        }
        showToast(getString(R.string.refresh_success));
        checkDataSize();// 检查数据判断是否为空
        for (int i = 0; i < 3; i++) {
            views[i].setEnabled(true);
        }
        ra.cancel();
    }

    @Override
    public void requestDataFailed() {
        showToast(getString(R.string.refresh_data_failed));
        tv_loading.setText(R.string.click_to_refresh);
        pb_loading.setVisibility(View.GONE);
        for (int i = 0; i < 3; i++) {
            views[i].setEnabled(true);
        }
        ra.cancel(); // 取消刷新图标的旋转任务
    }

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
        }
    }

    enum SildeDirection {
        RIGHT, LEFT, NO_SILDE
    }

    @Override
    public int setLayoutResId() {
        int[] screenWidthHeight = DensityUtil.getScreenWidthHeight(this);// 屏幕宽带
        if (screenWidthHeight[0] == 1280 && screenWidthHeight[1] == 720) {
            return R.layout.activity_date_meeting_list;
        } else {
            return R.layout.activity_date_meeting_list_tablet;
        }
    }

    @Override
    protected void initUiAndListener() {
        ButterKnife.bind(this);
        mPresenter.attachView(this);
        rl_refresh.setVisibility(View.VISIBLE);
        tv_logout.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams params = new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DensityUtil.getScreenWidthHeight(this)[0]);
        ll_calendar.setLayoutParams(params);
        tv_title.setText(R.string.meeting_list);
        views = new CalendarCard[3];
        for (int i = 0; i < 3; i++) {
            views[i] = new CalendarCard(this, this);
        }
        adapter = new CalendarViewAdapter<>(views);
        setViewPager();
        mDate = CalendarCard.mShowDate;
        lv_meeting_list.setOnItemClickListener(this);
        mPresenter.checkStatusAndRequestData();
    }

    @Override
    protected void initInjector() {
        DaggerDateMeetingComponent.builder()
                .appComponent(getAppComponent())
                .activityModule(getActivityModule())
                .apiModule(new ApiModule())
                .build()
                .inject(this);
    }

    @Override
    protected boolean isApplyStatusBarColor() {
        return true;
    }

    @Override
    protected boolean isApplyTranslucentStatus() {
        return true;
    }

    @Override
    protected void onDestroy() {
        UIUtils.dismissAlertDialog(confirmDialog);
        UIUtils.dismissProgressDialog(progressDialog);
        mPresenter.detachView();
        super.onDestroy();
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
     * 检查数据是否为空  显示UI
     */
    private void checkDataSize() {
        ll_loading.setVisibility(View.GONE);
        if (meetingInfos.size() == 0) {
            tv_no_list.setVisibility(View.VISIBLE);
            lv_meeting_list.setVisibility(View.GONE);
            return;
        } else {
            tv_no_list.setVisibility(View.GONE);
            lv_meeting_list.setVisibility(View.VISIBLE);
            if (meetingListAdapter == null) {
                meetingListAdapter = new MeetingListAdapter(this, meetingInfos, new com.gkzxhn.gkprison.prisonport.ui.home.MeetingListAdapter.OnCancelSuccessListener() {
                    @Override
                    public void onSuccess(int position) {
                        handler.postDelayed(dismissProgressDialogTask, 1500);
                        meetingInfos.remove(position);
                    }
                });
                lv_meeting_list.setAdapter(meetingListAdapter);
            } else {
                meetingListAdapter.notifyDataSetChanged();
            }
            DensityUtil.setListViewHeightBasedOnChildren(lv_meeting_list);
        }
    }

    /**
     * 设置日历页面viewpager
     */
    private void setViewPager() {
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(498);
        mViewPager.addOnPageChangeListener(new SimplePageChangeListener(){
            @Override public void onPageSelected(int position) {
                measureDirection(position);
                updateCalendarView(position);
            }
        });
    }

    @OnClick({R.id.btnPreMonth, R.id.btnNextMonth, R.id.rl_refresh, R.id.tv_logout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPreMonth:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                break;
            case R.id.btnNextMonth:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                break;
            case R.id.ll_loading:
                if (views != null)
                    mPresenter.requestDataList(CalendarCard.mShowDate.getYear() + "-"
                            + CalendarCard.mShowDate.getMonth() + "-" + CalendarCard.mShowDate.getDay());
                break;
            case R.id.rl_refresh:
                scrollView.scrollTo(0, 0);
                mPresenter.requestDataList(mDate.getYear() + "-" + mDate.getMonth() + "-" + mDate.getDay());// 请求数据
                break;
            case R.id.tv_logout:
                confirmDialog = MainUtils.showConfirmDialog(this);
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
        CalendarCard[] mShowViews = adapter.getAllItems();
        if (mDirection == SildeDirection.RIGHT) {
            mShowViews[arg0 % mShowViews.length].rightSlide();
        } else if (mDirection == SildeDirection.LEFT) {
            mShowViews[arg0 % mShowViews.length].leftSlide();
        }
        mDirection = SildeDirection.NO_SILDE;
    }

    @Override
    public void onBackPressed() {
        if (confirmDialog != null && confirmDialog.isShowing()){
            confirmDialog.dismiss();
            return;
        }
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
}
