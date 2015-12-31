package com.gkzxhn.gkprison.avchat;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.Chronometer;

import com.gkzxhn.gkprison.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengneng on 2015/12/30.
 */
public class Anticlockwise extends Chronometer{

    public Anticlockwise(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO 自动生成的构造函数存根
        mTimeFormat = new SimpleDateFormat("mm:ss");
        this.setOnChronometerTickListener(listener);
        mContext = context;
    }

    private long mTime;
    private long mNextTime;
    private OnTimeCompleteListener mListener;
    private SimpleDateFormat mTimeFormat;
    private Context mContext;

    public Anticlockwise(Context context) {
        super(context);

    }

    /**
     * 重新启动计时
     */
    public void reStart(long _time_s) {
        if (_time_s == -1)
        {
            mNextTime = mTime;
        } else
        {
            mTime = mNextTime = _time_s;
        }
        this.start();
    }

    public void reStart() {
        reStart(-1);
    }

    /**
     * 继续计时
     */
    public void onResume() {
        this.start();
    }

    /**
     * 暂停计时
     */
    public void onPause() {
        this.stop();
    }

    /**
     * 设置时间格式
     *
     * @param pattern
     *            计时格式
     */
    public void setTimeFormat(String pattern) {
        mTimeFormat = new SimpleDateFormat(pattern);
    }

    public void setOnTimeCompleteListener(OnTimeCompleteListener l) {
        mListener = l;
    }

    Chronometer.OnChronometerTickListener listener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer)
        {
            if (mNextTime <= 0)
            {
                if (mNextTime == 0)
                {
                    Anticlockwise.this.stop();
                    if (null != mListener)
                        mListener.onTimeComplete();
                }
                mNextTime = 0;
                updateTimeText();
                return;
            }
            mNextTime--;
            updateTimeText();
            mListener.onTimeChanged(mNextTime);
        }
    };

    /**
     * 初始化时间
     * @param _time_s
     */
    public void initTime(long _time_s) {
        mTime = mNextTime = _time_s;
        updateTimeText();
    }

    private void updateTimeText() {
        this.setText(mTimeFormat.format(new Date(mNextTime * 1000)));
    }

    interface OnTimeCompleteListener {
        void onTimeComplete();

        void onTimeChanged(long ms);
    }
}
