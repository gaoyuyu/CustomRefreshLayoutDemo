package com.gaoyy.customrefreshlayoutdemo.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.gaoyy.customrefreshlayoutdemo.R;

public class WaveView extends View
{

    private int mWidth;
    private int mHeight;

    private int headHeight;
    private int waveHeight;
    private int waveX;



    public int getHeadHeight()
    {
        return headHeight;
    }

    public void setHeadHeight(int headHeight)
    {
        this.headHeight = headHeight;
    }

    public int getWaveHeight()
    {
        return waveHeight;
    }

    public void setWaveHeight(int waveHeight)
    {
        this.waveHeight = waveHeight;
    }

    public int getWaveX()
    {
        return waveX;
    }

    public void setWaveX(int waveX)
    {
        this.waveX = waveX;
    }

    //画笔
    private Paint paint;
    //Path
    private Path path;

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
    private void init()
    {
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.colorPrimaryDark));
    }



    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        path.reset();
        path.lineTo(0, headHeight);
        path.quadTo(waveX, headHeight + waveHeight, mWidth,headHeight);
        path.lineTo(mWidth, 0);
        canvas.drawPath(path, paint);


        Paint p1 = new Paint();
        p1.setStrokeWidth(5f);
        p1.setColor(Color.GREEN);
        canvas.drawPoints(new float[]{          //绘制一组点，坐标位置由float数组指定
                0, headHeight,
                waveX, headHeight + waveHeight,
                mWidth,headHeight
        },p1);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }
}
