package com.gkzxhn.gkprison.userport.activity;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

/**
 * created by hzn 2016/1/20
 * 闹钟提醒页面
 */
public class AlarmActivity extends BaseActivity {

    private ImageView iv_alarm_icon;
    private TextView tv_alarm_time;
    private TextView tv_alarm_stop;
    private TextView tv_alarm_remind;
    private Vibrator vibrator;
    private MediaPlayer player;
    private Handler handler = new Handler();
    private int index = 0;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_alarm, null);
        iv_alarm_icon = (ImageView) view.findViewById(R.id.iv_alarm_icon);
        tv_alarm_time = (TextView) view.findViewById(R.id.tv_alarm_time);
        tv_alarm_stop = (TextView) view.findViewById(R.id.tv_alarm_stop);
        tv_alarm_remind = (TextView) view.findViewById(R.id.tv_alarm_remind);
        return view;
    }

    @Override
    protected void initData() {
        setActionBarGone(View.GONE);
        handler.post(alarm_Task);
        player = MediaPlayer.create(this, R.raw.alarm);
        player.setVolume(1.0f, 1.0f);
        player.setLooping(true);
        player.start();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {500, 500, 500, 500};
        vibrator.vibrate(pattern, 2);
        tv_alarm_stop.setOnClickListener(this);
    }

    /**
     * 闹钟动画任务
     */
    private Runnable alarm_Task = new Runnable() {
        @Override
        public void run() {
            if(index % 2 == 0){
                iv_alarm_icon.setImageResource(R.drawable.alarm_right);
            }else {
                iv_alarm_icon.setImageResource(R.drawable.alarm_left);
            }
            index++;
            handler.postDelayed(alarm_Task, 100);
        }
    };

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.tv_alarm_stop:
                player.stop();
                vibrator.cancel();
                handler.removeCallbacks(alarm_Task);
                AlarmActivity.this.finish();
                break;
        }
    }
}
