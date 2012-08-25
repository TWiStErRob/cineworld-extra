package com.twister.cineworld.ui.components;

// source: http://blog.svpino.com/2011/08/disabling-pagingswiping-on-android.html

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ExtendedViewPager extends ViewPager {

	private boolean	enabled;

	public ExtendedViewPager(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.enabled = true;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (this.enabled) {
			return super.onTouchEvent(event);
		}

		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent event) {
		if (this.enabled) {
			return super.onInterceptTouchEvent(event);
		}

		return false;
	}

	public void setPagingEnabled(final boolean enabled) {
		this.enabled = enabled;
	}
}
