package com.pc.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ResizeLinearLayout extends LinearLayout {

	private IOnResizeListener mListener;

	public void setOnResizeListener(IOnResizeListener l) {
		mListener = l;
	}

	public ResizeLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mListener != null) {
			mListener.OnResize(w, h, oldw, oldh);
		}
	}

}
