package com.TAP.TAP;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

import java.util.jar.Attributes;

public class NoScrollViewPager extends ViewPager {
    private boolean noScroll = false;

    public NoScrollViewPager(Context context, AttributeSet attr) {
        super(context, attr);
        setOffscreenPageLimit(4);
    }

    public NoScrollViewPager(Context context) {
        super(context);
    }

    public void setNoScroll(boolean noScroll) {
        this.noScroll = noScroll;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(noScroll) return false;
        else return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(noScroll) return false;
        else return super.onInterceptTouchEvent(event);
    }
}
