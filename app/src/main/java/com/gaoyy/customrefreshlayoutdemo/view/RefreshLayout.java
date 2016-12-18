package com.gaoyy.customrefreshlayoutdemo.view;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gaoyy.customrefreshlayoutdemo.R;

/**
 * Created by gaoyy on 2016/12/16 0016.
 */
public class RefreshLayout extends FrameLayout
{

    //头部layout
    private FrameLayout mHeadLayout;

    private WaveView waveView;
    private View headerLayout;
    private TextView tip;
    private ProgressBar progressBar;
    private ImageView arrow;
    //子布局
    private View mChildView;

    //头部的高度
    protected float mHeadHeight = 100;
    protected float mWaveHeight = 120;

    private static final String LOG_TAG = RefreshLayout.class.getSimpleName();

    //刷新的状态
    protected boolean isRefreshing;

    //触摸获得Y的位置
    private float mTouchY;

    //当前Y的位置
    private float mCurrentY;

    private DecelerateInterpolator decelerateInterpolator;

    public boolean isRefreshing()
    {
        return isRefreshing;
    }

    public void setRefreshing(boolean refreshing)
    {
        isRefreshing = refreshing;
    }

    private PullWaveListener pullWaveListener;
    private PullToRefreshListener pullToRefreshListener;

    public void setPullWaveListener(PullWaveListener pullWaveListener)
    {
        this.pullWaveListener = pullWaveListener;
    }

    public void setPullToRefreshListener(PullToRefreshListener pullToRefreshListener)
    {
        this.pullToRefreshListener = pullToRefreshListener;
    }

    public RefreshLayout(Context context)
    {
        this(context, null, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            mChildView.animate().translationY(0).start();
            setRefreshing(false);
        }
    };

    private void init()
    {
        //使用isInEditMode解决可视化编辑器无法识别自定义控件的问题
        if (isInEditMode())
        {
            return;
        }

        if (getChildCount() > 1)
        {
            throw new RuntimeException("只能拥有一个子控件");
        }

        decelerateInterpolator = new DecelerateInterpolator(10);
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        Log.i(LOG_TAG, "onAttachedToWindow");

        headerLayout = LayoutInflater.from(getContext()).inflate(R.layout.header, null);
        waveView = (WaveView) headerLayout.findViewById(R.id.waveview);
        tip = (TextView) headerLayout.findViewById(R.id.tip);
        progressBar = (ProgressBar) headerLayout.findViewById(R.id.progressbar);
        arrow = (ImageView) headerLayout.findViewById(R.id.arrow);


        this.addView(headerLayout);

        mChildView = getChildAt(0);
        //此时getChildCount()为2，因为上面调用了addView()，以及还有一个textview，所以有2个。index为0的View为头部布局


        if (mChildView == null)
        {
            return;
        }

        ViewPropertyAnimator childViewPropertyAnimator = mChildView.animate();
        childViewPropertyAnimator.setInterpolator(new DecelerateInterpolator());
        childViewPropertyAnimator.setUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                int childHeight = (int) mChildView.getTranslationY();
                Log.e(LOG_TAG, "onAnimationUpdate getTranslationY-->" + childHeight);
                headerLayout.getLayoutParams().height = childHeight;
                headerLayout.requestLayout();
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        //刷新状态不拦截事件
        if (isRefreshing)
        {
            return true;
        }
        int action = ev.getAction();
        switch (action)
        {
            //单点触摸按下动作
            case MotionEvent.ACTION_DOWN:
                mTouchY = ev.getY();
                break;
            //单点触摸离开动作
            case MotionEvent.ACTION_UP:
                break;
            //单点触摸移动动作
            case MotionEvent.ACTION_MOVE:
                float currentY = ev.getY();
                float dy = currentY - mTouchY;
                if (dy > 0 && !canChildScrollUp())
                {
                    return true;
                }
                break;

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //刷新状态不响应事件
        if (isRefreshing)
        {
            return super.onTouchEvent(event);
        }


        int action = event.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_MOVE:
                Log.e(LOG_TAG, "onTouchEvent MotionEvent.ACTION_MOVE:");
                mCurrentY = event.getY();
                Log.i(LOG_TAG, "mCurrentY-->" + mCurrentY);
                float waveX = event.getX();
                float dy = mCurrentY - mTouchY;
                Log.e(LOG_TAG, "onTouchEvent MotionEvent.ACTION_MOVE: dy-->" + dy);
                dy = Math.max(0,dy);

                Log.e(LOG_TAG, "onTouchEvent MotionEvent.ACTION_MOVE: mWaveHeight-->" + mWaveHeight);
                Log.e(LOG_TAG, "onTouchEvent MotionEvent.ACTION_MOVE: mHeadHeight-->" + mHeadHeight);
                Log.e(LOG_TAG, "onTouchEvent MotionEvent.ACTION_MOVE: after select dy-->" + dy);

                if(mChildView != null)
                {
                    int headerHeight = 0;
                    float offsetY = 0;
                    if(dy < mHeadHeight)
                    {
                        waveView.setHeadHeight((int) dy);
                        waveView.setWaveHeight(0);
                        waveView.setWaveX((int) waveX);
                        waveView.invalidate();
                        headerHeight = (int )dy;
                        offsetY = headerHeight;
                        Log.e(LOG_TAG,"dy < mHeadHeight");
                        Log.e(LOG_TAG,"dy = "+dy);
                        Log.e(LOG_TAG,"headerHeight = "+headerHeight);
                        Log.e(LOG_TAG,"offsetY  =  "+offsetY);

                        arrow.setVisibility(View.GONE);
                        tip.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                    else if(dy>mHeadHeight && (dy <(mHeadHeight+mWaveHeight)))
                    {
                        float currentWaveHeight = dy-mHeadHeight;
                        waveView.setHeadHeight((int)mHeadHeight);
                        waveView.setWaveHeight((int)currentWaveHeight);
                        waveView.setWaveX((int) waveX);
                        waveView.invalidate();
                        headerHeight = (int )mHeadHeight;
                        offsetY = (float)(mHeadHeight+currentWaveHeight);
                        Log.e(LOG_TAG,"dy>mHeadHeight && (dy <(mHeadHeight+mWaveHeight))");
                        Log.e(LOG_TAG,"dy = "+dy);
                        Log.e(LOG_TAG,"headerHeight = "+headerHeight);
                        Log.e(LOG_TAG,"offsetY  =  "+offsetY);
                        if(currentWaveHeight/mWaveHeight > 0.5f)
                        {
                            tip.setText("下拉刷新");
                            arrow.setVisibility(View.VISIBLE);
                            tip.setVisibility(View.VISIBLE);
                            arrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_downward_black_24dp));
                        }
                    }
                    else if(dy > (mHeadHeight+mWaveHeight))
                    {
                        waveView.setHeadHeight((int)mHeadHeight);
                        waveView.setWaveHeight((int)mWaveHeight);
                        waveView.setWaveX((int) waveX);
                        waveView.invalidate();
                        headerHeight = (int )mHeadHeight;
                        offsetY = mHeadHeight+mWaveHeight;
                        Log.e(LOG_TAG,"dy>mHeadHeight && (dy <(mHeadHeight+mWaveHeight))");
                        Log.e(LOG_TAG,"dy = "+dy);
                        Log.e(LOG_TAG,"headerHeight = "+headerHeight);
                        Log.e(LOG_TAG,"offsetY  =  "+offsetY);

                        tip.setText("释放立即刷新");
                        arrow.setVisibility(View.VISIBLE);
                        tip.setVisibility(View.VISIBLE);
                        arrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_upward_black_24dp));
                    }

                    mChildView.setTranslationY(offsetY);
                    headerLayout.getLayoutParams().height = (int)offsetY;
                    headerLayout.requestLayout();
                }
                return true;
            case MotionEvent.ACTION_UP:
                Log.e(LOG_TAG, "onTouchEvent MotionEvent.ACTION_UP:");
                if (mChildView.getTranslationY() >= (mHeadHeight+mWaveHeight))
                {
                    mChildView.animate().translationY(mHeadHeight).start();

                    arrow.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    tip.setText("正在加载");

                    isRefreshing = true;
                    handler.sendEmptyMessageDelayed(0, 3000);
//                    if (pullToRefreshListener != null)
//                    {
//                        pullToRefreshListener.onRefresh(this);
//                    }
                }
                else
                {
                    mChildView.animate().translationY(0).start();
                }
                return true;
        }

        return super.onTouchEvent(event);
    }


    /**
     * 用来判断是否可以上拉
     *
     * @return boolean
     */
    public boolean canChildScrollUp()
    {
        if (mChildView == null)
        {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14)
        {
            if (mChildView instanceof AbsListView)
            {
                final AbsListView absListView = (AbsListView) mChildView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            }
            else
            {
                return ViewCompat.canScrollVertically(mChildView, -1) || mChildView.getScrollY() > 0;
            }
        }
        else
        {
            return ViewCompat.canScrollVertically(mChildView, -1);
        }
    }
}
