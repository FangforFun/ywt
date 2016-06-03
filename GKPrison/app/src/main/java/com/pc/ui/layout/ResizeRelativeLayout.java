/**
 * @(#)ResizeRelativeLayout.java 2013-11-15 Copyright 2013 it.kedacom.com, Inc.
 *                               All rights reserved.
 */

package com.pc.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * @author chenjian
 * @date 2013-11-15
 */

public class ResizeRelativeLayout extends RelativeLayout {

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ResizeRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ResizeRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @param context
	 */
	public ResizeRelativeLayout(Context context) {
		super(context);
	}

	private IOnResizeListener mListener;

	public void setOnResizeListener(IOnResizeListener l) {
		mListener = l;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mListener != null) {
			mListener.OnResize(w, h, oldw, oldh);
		}
	}

}
