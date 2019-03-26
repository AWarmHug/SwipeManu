package com.warm.swipemenu;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by warm on 17/5/14.
 * 侧滑按钮
 * 一、
 * Scroller:处理滑动
 * 首先注意这两个方法
 * {@link #scrollBy(int, int)} }这是移动(x,y)距离 改变初始位置
 * {@link #scrollTo(int, int)}这是移动到(x,y) 不改变初始位置
 * 1. 创建Scroller的实例
 * 2. 在需要滑动的时候 调用{@link Scroller#startScroll(int, int, int, int, int)} 方法来初始化滚动数据并刷新界面
 * 3. 重写{@link #computeScroll()}方法，并在其内部完成平滑滚动的逻辑
 * <p>
 * <p>
 * 二、
 * VelocityTracker:处理滑动速度
 * 1、在{@link #onTouchEvent(MotionEvent)}方法中进行初始化和接管MotionEvent操作
 * 2、在{@link #onTouchEvent(MotionEvent)} ACTION_MOVE状态
 * 使用{@link VelocityTracker#computeCurrentVelocity(int, float)} 计算实时速{@link VelocityTracker#getXVelocity(int)}
 * <p>
 * <p>
 * 三、
 * 处理RecycleView滑动冲突 {@link #requestDisallowInterceptTouchEvent(boolean)}
 * 四、
 * 在RecycleView.Adapter中因为点击事件已经被拦截所以需要以子控件为Item。
 */

public class SwipeMenu extends ViewGroup {


    private static SwipeMenu menu;

    private static final String TAG = "SideslipLayout";


    private int menuLength;

    private Scroller mScroller;

    /**
     * 当前状态 {@link #isOpen()}
     */
    private int state;

    /**
     * 用于修改当前状态 {@link #setState(int)}
     */
    public static final int OPEN = 0x110;

    /**
     * 同上
     */
    public static final int CLOSE = 0x111;

    /**
     * 同上，滑动中
     */
    private final int MOVING = 0x112;

    /**
     * 判定为拖动的最小移动像素数
     */
    private int mTouchSlop;


    /**
     * 获取滑动速率的类
     */
    private VelocityTracker mVelocityTracker;

    /**
     * 最大滑动速率，用于计算当前的滑动速度 {@link VelocityTracker#computeCurrentVelocity(int, float)}
     */
    private int maxVelocity;

    /**
     * 默认滑动过关速率
     */
    private final int mVelocity = 1000;


    private float velocityX, velocityY;

    private int mPointerId;

    /**
     * 存放按钮的那一块
     */
    private RectF mRectF = new RectF();


    public void setState(int state, int time) {
        this.state = state;
        if (state == OPEN) {
            open(time);
        } else {
            close(time);
        }
    }

    public void setState(int state) {
        this.setState(state, 250);
    }

    public SwipeMenu(Context context) {
        this(context, null);
    }

    public SwipeMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScroller = new Scroller(getContext());
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        setFocusable(true);
        setFocusableInTouchMode(true);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setClickable(true);
        int measureWidth = doMeasureWidth(widthMeasureSpec);
        int measureHeight = doMeasureHeight(heightMeasureSpec);
        setMeasuredDimension(measureWidth, measureHeight);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    private int doMeasureWidth(int measureSpec) {

        int result = 0;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:
                result = getChildAt(0).getMeasuredWidth();
                break;
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        return result;
    }

    private int doMeasureHeight(int measureSpec) {

        int result = 0;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:

                result = getChildAt(0).getMeasuredHeight();
                Log.d(TAG, "doMeasureHeight: " + result);
                break;
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int left = getPaddingLeft();
        int top = getPaddingTop();

        if (changed) {

            //需要手动变0 防止重复测量
            menuLength = 0;

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.setClickable(true);
                child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
                left += child.getMeasuredWidth();

                if (i != 0) {
                    menuLength += child.getMeasuredWidth();

                }
            }


        }
    }

    private float downX;

    private float lastMoveX;


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:


                mPointerId = ev.getPointerId(0);
                mRectF.set(getMeasuredWidth() - menuLength, 0, getMeasuredWidth(), getMeasuredHeight());
                downX = ev.getX();
                if (menu != null && menu != this && (menu.isOpen())) {
                    menu.setState(CLOSE);
                    break;
                }

                if (menu == this && menu.isOpen() && !menu.mRectF.contains(ev.getX(), ev.getY())) {
                    menu.setState(CLOSE);
                    return true;
                }


            case MotionEvent.ACTION_MOVE:
                //如果超过最小滑动距离拦截
                if (Math.abs(downX - ev.getX()) > mTouchSlop) {
                    if (!isOpen()) {
                        if (ev.getX() - downX < 0) {
                            lastMoveX = ev.getX();
                            getParent().requestDisallowInterceptTouchEvent(true);
                            return true;
                        }

                    } else {
                        if (ev.getX() - downX < 0) {

                            return false;

                        } else {

                            lastMoveX = ev.getX();
                            getParent().requestDisallowInterceptTouchEvent(true);

                            return true;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:

                break;
        }

        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        acquireVelocityTracker(event);
        final VelocityTracker verTracker = mVelocityTracker;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:

                if (!isOpen()) {

                    scrollTo(Math.max(0, Math.min((int) (lastMoveX - event.getX()), menuLength)), 0);

                } else {

                    scrollTo(Math.min(menuLength, Math.max((int) ((lastMoveX - event.getX()) + menuLength), 0)), 0);

                }
                break;
            case MotionEvent.ACTION_UP:
                verTracker.computeCurrentVelocity(1000, maxVelocity);
                velocityX = verTracker.getXVelocity(mPointerId);
                velocityY = verTracker.getYVelocity(mPointerId);
//                if (getScrollX() >= 0) {
                if (!isOpen()) {
                    //打开
                    if (getScrollX() > menuLength / 2 || velocityX < -mVelocity) {

                        setState(OPEN);
                    } else {
                        setState(CLOSE);
                    }
                } else {
                    //关闭
                    if (getScrollX() < menuLength / 2 || velocityX > mVelocity) {
                        setState(CLOSE);
                    } else {
                        setState(OPEN);
                    }

                }
                releaseVelocityTracker();

                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }


        return super.onTouchEvent(event);

    }


    //恢复关闭
    private void close(int time) {

        startScroll(getScrollX(), -getScrollX(), time);

        if (menu != null) {

            menu = null;
        }
    }

    public void init() {
        startScroll(getScrollX(), -getScrollX(), 0);
    }


    //恢复打开
    private void open(int time) {

        startScroll(getScrollX(), menuLength - getScrollX(), time);
        menu = this;
    }

    public static SwipeMenu getMenu() {
        return menu;
    }

    private void startScroll(int x, int dx, int time) {
        mScroller.startScroll(x, 0, dx, 0, time);
        invalidate();
    }


    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }

    }

    public boolean isOpen() {
        return state == OPEN;
    }


    /**
     * @param event 向VelocityTracker添加MotionEvent
     * @see VelocityTracker#obtain()
     * @see VelocityTracker#addMovement(MotionEvent)
     */
    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * * 释放VelocityTracker
     *
     * @see VelocityTracker#clear()
     * @see VelocityTracker#recycle()
     */
    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (menu == this) {
            menu.setState(CLOSE);
        }
        super.onDetachedFromWindow();
    }
}
