package com.gaoyy.customrefreshlayoutdemo.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.gaoyy.customrefreshlayoutdemo.R;

public class WaveView extends View
{
    //屏幕宽度
    private int mWidth;
    //屏幕高度
    private int mHeight;
    //头部矩形高度
    private int headHeight;
    //贝塞尔曲线控制点X坐标值
    private int controlX;
    //控制点Y坐标值
    private int controlY;
    //颜色
    private int waveColor =R.color.colorPrimaryDark;
    //画笔
    private Paint paint;
    //Path
    private Path path;


    public int getHeadHeight()
    {
        return headHeight;
    }

    public void setHeadHeight(int headHeight)
    {
        this.headHeight = headHeight;
    }

    public int getControlX()
    {
        return controlX;
    }

    public void setControlX(int controlX)
    {
        this.controlX = controlX;
    }

    public int getControlY()
    {
        return controlY;
    }

    public void setControlY(int controlY)
    {
        this.controlY = controlY;
    }

    public int getWaveColor()
    {
        return waveColor;
    }

    public void setWaveColor(int waveColor)
    {
        this.waveColor = waveColor;
    }

    public WaveView(Context context)
    {
        this(context, null, 0);
    }

    public WaveView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WaveView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 初始化
     */
    private void init()
    {
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(waveColor));
    }



    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        path.reset();
        path.lineTo(0, headHeight);
        path.quadTo(controlX, headHeight + controlY, mWidth,headHeight);
        path.lineTo(mWidth, 0);
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }
}
