package com.gaoyy.customrefreshlayoutdemo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
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
public class MaterialRefreshLayout extends FrameLayout
{
    private static final String LOG_TAG = MaterialRefreshLayout.class.getSimpleName();

    private WaveView mWaveView;
    private View mHeaderLayout;
    private TextView mTip;
    private ProgressBar mProgressBar;
    private ImageView mArrow;

    //子布局
    private View mChildView;

    //头部的高度
    protected float mHeadHeight = 100;

    //贝塞尔曲线控制点Y轴坐标值
    protected float mControlY = 180;

    //刷新的状态
    protected boolean isRefreshing;

    //触摸获得Y的位置
    private float mTouchY;

    //当前Y的位置
    private float mCurrentY;

    //当前头部布局高度
    protected int mCurrentHeaderHeight = 0;
    //子view在Y轴上移动的距离
    protected float offsetY = 0;
    //刷新回调接口
    private OnMaterialRefreshListener onMaterialRefreshListener;


    public boolean isRefreshing()
    {
        return isRefreshing;
    }

    public void setRefreshing(boolean refreshing)
    {
        isRefreshing = refreshing;
        /**
         * 当需要设置正在处于刷新状态时，layout 初始化时mChildView为null
         * 通过打印日志是先执行setRefreshing在执行onAttachedToWindow，所以为null
         * 所以setRefreshing还需要放在onAttachedToWindow()方法里面
         */
        if (isRefreshing)
        {
            if (mChildView == null)
            {
                return;
            }
            mChildView.animate().translationY(mHeadHeight).start();
            mWaveView.setHeadHeight((int) mHeadHeight);
            mWaveView.setControlY(0);
            mWaveView.setControlX(1);
            mWaveView.invalidate();
            mProgressBar.setVisibility(View.VISIBLE);
            mTip.setText("正在加载");
            mTip.setVisibility(View.VISIBLE);

        }
    }

    public OnMaterialRefreshListener getOnMaterialRefreshListener()
    {
        return onMaterialRefreshListener;
    }

    public void setOnMaterialRefreshListener(OnMaterialRefreshListener onMaterialRefreshListener)
    {
        this.onMaterialRefreshListener = onMaterialRefreshListener;
    }

    public MaterialRefreshLayout(Context context)
    {
        this(context, null, 0);
    }

    public MaterialRefreshLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public MaterialRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init()
    {
        //使用isInEditMode解决可视化编辑器无法识别自定义控件的问题
        if (isInEditMode())
        {
            return;
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        Log.i(LOG_TAG, "onAttachedToWindow");

        mHeaderLayout = LayoutInflater.from(getContext()).inflate(R.layout.header, null);
        mWaveView = (WaveView) mHeaderLayout.findViewById(R.id.waveview);
        mTip = (TextView) mHeaderLayout.findViewById(R.id.tip);
        mProgressBar = (ProgressBar) mHeaderLayout.findViewById(R.id.progressbar);
        mArrow = (ImageView) mHeaderLayout.findViewById(R.id.arrow);


        this.addView(mHeaderLayout);

        mChildView = getChildAt(0);
        //此时getChildCount()为2，因为上面调用了addView()，以及还有一个子view，所以有2个。index为0的View为头部布局
        if (getChildCount() > 2)
        {
            throw new RuntimeException("Can only have a child view");
        }

        if (mChildView == null)
        {
            return;
        }

        setRefreshing(isRefreshing);
        
        ViewPropertyAnimator childViewPropertyAnimator = mChildView.animate();
        childViewPropertyAnimator.setInterpolator(new DecelerateInterpolator());
        childViewPropertyAnimator.setUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                int childHeight = (int) mChildView.getTranslationY();
                mHeaderLayout.getLayoutParams().height = childHeight;
                mHeaderLayout.requestLayout();
            }
        });


    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        //刷新状态拦截事件,不做任何处理
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
                float distanceY = currentY - mTouchY;
                if (distanceY > 0 && !canChildScrollUp())
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
        //如何取得贝塞尔曲线的圆弧切点到控制点的距离，假设控制点在正中，正好三角形是等边三角心，根据贝塞尔曲线的原理可以得知就是waveheight/2，三角形中位线定理
        switch (action)
        {
            case MotionEvent.ACTION_MOVE:
                mCurrentY = event.getY();
                float controlX = event.getX();
                float distanceY = mCurrentY - mTouchY;
                distanceY = Math.max(0, distanceY);
                if (mChildView != null)
                {
                    if (distanceY < mHeadHeight)
                    {
                        Log.e(LOG_TAG, "distanceY < mHeadHeight");
                        mWaveView.setHeadHeight((int) distanceY);
                        mWaveView.setControlY(0);
                        mWaveView.setControlX((int) controlX);
                        mWaveView.invalidate();
                        mCurrentHeaderHeight = (int) distanceY;
                        offsetY = mCurrentHeaderHeight;
                        mArrow.setVisibility(View.GONE);
                        mTip.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);

                    }
                    else if (distanceY > mHeadHeight && (distanceY <= (mHeadHeight + mControlY)))
                    {
                        //这里的distanceY选择的范围是(mHeadHeight,mHeadHeight + mControlY],动画效果过渡更加自然，
                        //若是(mHeadHeight,mHeadHeight + mControlY/2]，圆弧一下子撑开，过渡效果粗糙，
                        Log.e(LOG_TAG, "distanceY > mHeadHeight && (distanceY < (mHeadHeight + mControlY/2))");
                        float currentWaveHeight = distanceY - mHeadHeight;
                        mWaveView.setHeadHeight((int) mHeadHeight);
                        mWaveView.setControlY((int) currentWaveHeight);
                        mWaveView.setControlX((int) controlX);
                        mWaveView.invalidate();
                        mCurrentHeaderHeight = (int) mHeadHeight;
                        offsetY = mHeadHeight + currentWaveHeight / 2;
                        mProgressBar.setVisibility(View.GONE);
                        if (currentWaveHeight / mControlY > 0.5f)
                        {
                            mArrow.animate().setListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationStart(Animator animation)
                                {
                                    super.onAnimationStart(animation);
                                    mTip.setText("下拉刷新");
                                }

                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    super.onAnimationEnd(animation);
                                    //动画结束后，显示控件，否则出现不和谐的过渡效果
                                    mArrow.setVisibility(View.VISIBLE);
                                    mTip.setVisibility(View.VISIBLE);

                                }
                            });
                            mArrow.animate()
                                    .rotationX(0)
                                    .setDuration(15)
                                    .start();
                        }
                    }
                    else if (distanceY > (mHeadHeight + mControlY))
                    {
                        Log.e(LOG_TAG, "distanceY > (mHeadHeight + mControlY / 2)");
                        mWaveView.setHeadHeight((int) mHeadHeight);
                        mWaveView.setControlY((int) mControlY);
                        mWaveView.setControlX((int) controlX);
                        mWaveView.invalidate();
                        mCurrentHeaderHeight = (int) mHeadHeight;
                        offsetY = mHeadHeight + mControlY / 2;
                        mProgressBar.setVisibility(View.GONE);

                        mArrow.animate().setListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationStart(Animator animation)
                            {
                                super.onAnimationStart(animation);
                                mTip.setText("释放立即刷新");
                                mArrow.setVisibility(View.VISIBLE);
                                mTip.setVisibility(View.VISIBLE);
                            }
                        });
                        mArrow.animate()
                                .rotationX(180)
                                .setDuration(50)
                                .start();
                    }
                    //设置子View的Y轴偏移量
                    mChildView.setTranslationY(offsetY);
                    //重新设置header的高度
                    mHeaderLayout.getLayoutParams().height = (int) offsetY;
                    //重绘
                    mHeaderLayout.requestLayout();
                }
                return true;
            case MotionEvent.ACTION_UP:
                //当偏移量大于mHeadHeight + mWaveHeight / 2时，刷新
                if (mChildView.getTranslationY() >= (mHeadHeight + mControlY / 2))
                {
                    Log.e(LOG_TAG, "MotionEvent.ACTION_UP mChildView.getTranslationY() >= (mHeadHeight + mControlY / 2)");
                    mChildView.animate().setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationStart(Animator animation)
                        {
                            super.onAnimationStart(animation);
                            mArrow.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.VISIBLE);
                            mTip.setText("正在加载");
                        }
                    });
                    mChildView.animate().translationY(mHeadHeight).start();

                    isRefreshing = true;
                    if (onMaterialRefreshListener != null)
                    {
                        onMaterialRefreshListener.onRefresh(MaterialRefreshLayout.this);
                    }
                }
                else
                {
                    mChildView.animate().setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationStart(Animator animation)
                        {
                            super.onAnimationStart(animation);
                            mArrow.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.GONE);
                            mTip.setVisibility(View.GONE);
                        }
                    });
                    mChildView.animate().translationY(0).start();

                }
                return true;
        }

        return super.onTouchEvent(event);
    }


    /**
     * 完成刷新
     */
    public void finishRefresh()
    {
        if (mChildView != null)
        {
            mChildView.animate().translationY(0).start();
            setRefreshing(false);
        }
    }

    /**
     * 判断是否可以上拉
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
