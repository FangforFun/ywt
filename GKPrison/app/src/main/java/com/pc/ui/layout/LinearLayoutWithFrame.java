/**
 * @ FrameLinearLayout.java 2013-9-3
 */

package com.pc.ui.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.pc.utils.StringUtils;

/**
 * 该布局可绘制分割线
 * @author ryf
 * @date 2013-9-3
 */

public class LinearLayoutWithFrame extends LinearLayout {

	private final String NS = "http://kedacom.com/tt/android/LinearLayoutWithFrame";
	private Paint mPaint;
	private Location mLocation;

	enum Location {
		TOP, BOTTOM, CENTER;
	}

	public LinearLayoutWithFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		String str = attrs.getAttributeValue(NS, "location");
		if (StringUtils.equals("top", str)) {
			mLocation = Location.TOP;
		} else if (StringUtils.equals("center", str)) {
			mLocation = Location.CENTER;
		} else if (StringUtils.equals("bottom", str)) {
			mLocation = Location.BOTTOM;
		}
		init();
		setBackgroundColor(0x00000000);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	private void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.parseColor("#ffAEAEAE"));
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(3);
		mPaint.setTextSize(25);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawLine(canvas);
	}

	private void drawLine(Canvas canvas) {
		canvas.drawLine(0, 0, 0, getHeight(), mPaint);// 左侧竖线
		canvas.drawLine(getWidth(), 0, getWidth(), getHeight(), mPaint);// 右侧竖线
		if (mLocation == Location.TOP) {
			canvas.drawLine(0, 0, getWidth(), 0, mPaint);// 上横线
		}
		canvas.drawLine(0, getHeight(), getWidth(), getHeight(), mPaint);// 下横线
	}

}
