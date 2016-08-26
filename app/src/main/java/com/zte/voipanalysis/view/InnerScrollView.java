package com.zte.voipanalysis.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class InnerScrollView extends ScrollView
{
    private ScrollView parentScrollView; // 父列表的视图

    // 子列表滚动是否将父列表滚动无效,
    // true-子列表滚动到头,不会传递给父列表;
    // false-会传递给父列表
    private boolean isChildDisableParentScroll;

    public void setParentScrollView(ScrollView parentScrollView)
    {
        this.parentScrollView = parentScrollView;
    }

    public void setChildDiableParentScroll(boolean isChildDisableParentScroll)
    {
        this.isChildDisableParentScroll = isChildDisableParentScroll;
    }

    public InnerScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

    }

    private int lastScrollDelta = 0;

    public void resume()
    {
        overScrollBy(0, -lastScrollDelta, 0, getScrollY(), 0, getScrollRange(), 0, 0, true);
        lastScrollDelta = 0;
    }

    int mTop = 10;


    public void scrollTo(View targetView)
    {

        int oldScrollY = getScrollY();
        int top = targetView.getTop() - mTop;
        int delatY = top - oldScrollY;
        lastScrollDelta = delatY;
        overScrollBy(0, delatY, 0, getScrollY(), 0, getScrollRange(), 0, 0, true);
    }

    private int getScrollRange()
    {
        int scrollRange = 0;
        if (getChildCount() > 0)
        {
            View child = getChildAt(0);
            scrollRange = Math.max(0, child.getHeight() - (getHeight()));
        }
        return scrollRange;
    }

    int currentY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if (parentScrollView == null)
        {
            return super.onInterceptTouchEvent(ev);
        }
        else
        {
            if (ev.getAction() == MotionEvent.ACTION_DOWN)
            {
                // 将父scrollview的滚动事件拦截
                currentY = (int) ev.getY();
                setParentScrollAble(false);
                return super.onInterceptTouchEvent(ev);
            }
            else if (ev.getAction() == MotionEvent.ACTION_UP)
            {
                // 把滚动事件恢复给父Scrollview
                setParentScrollAble(true);
            }
            else if (ev.getAction() == MotionEvent.ACTION_MOVE)
            {
            }
        }
        return super.onInterceptTouchEvent(ev);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        View child = getChildAt(0);
        if (parentScrollView != null)
        {
            if (ev.getAction() == MotionEvent.ACTION_MOVE)
            {
                int height = child.getMeasuredHeight();
                height = height - getMeasuredHeight();

                // System.out.println("height=" + height);
                int scrollY = getScrollY();
                // System.out.println("scrollY" + scrollY);
                int y = (int) ev.getY();

                // 手指向下滑动
                if (currentY < y)
                {
                    if (scrollY <= 0)
                    {
                        // 如果向下滑动到头，就把滚动交给父Scrollview
                        setParentScrollAble(true);
                        return false;
                    }
                    else
                    {
                        setParentScrollAble(false);

                    }
                }
                else if (currentY > y)
                {
                    if (scrollY >= height)
                    {
                        // 如果向上滑动到头，就把滚动交给父Scrollview
                        setParentScrollAble(true);
                        return false;
                    }
                    else
                    {
                        setParentScrollAble(false);
                    }
                }
                currentY = y;
            }
        }

        return super.onTouchEvent(ev);
    }

    private void setParentScrollAble(boolean flag)
    {
        if (isChildDisableParentScroll)
        {
            flag = false;
        }

        parentScrollView.requestDisallowInterceptTouchEvent(!flag);
    }

}