package com.example.administrator.locationtest.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 设置多个TextView跑马灯效果，多个焦点
 * Created by Administrator on 2016/3/10.
 */
public class MarqueeView extends TextView {
    public MarqueeView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect)
    {
        // TODO Auto-generated method stub
        if(focused) super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
        // TODO Auto-generated method stub
        if(hasWindowFocus) super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    public boolean isFocused()
    {
        return true;
    }
}
