package com.app.feng.PM25_displayer.View;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.app.feng.PM25_displayer.R;

/**
 * Created by feng on 2015/9/13.
 */
public class CircleProgressBar extends View {

    //圆环半径(小圆的半径) 单位dp
    private int mRadius;
    //圆环宽度
    private int mWidth;
    //圆环起始角度
    private int mStartAngle;
    //圆环最大刻度
    private int mMax;
    //进度条颜色
    private int mColor;
    //进度条背景瑶瑟
    private final int mBackgroudColor;
    //画笔
    private Paint mPaint;
    //当前刻度
    private int mProgress;
    //矩形区域
    private RectF mRectF;
    //圆环直径
    private int mDiameter;
    //圆环宽度的屏幕像素值
    private int width_dimension;
    //字体画笔
    private Paint mTextPaint;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBackgroudColor = Color.parseColor("#CFCFCF");
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, defStyleAttr, 0);
        int num = array.getIndexCount();
        for (int i = 0; i < num; i++) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.CircleProgressBar_startAngle)
                mStartAngle = array.getInt(attr, 135);
            else if (attr == R.styleable.CircleProgressBar_max)
                mMax = array.getInt(attr, 100);
            else if (attr == R.styleable.CircleProgressBar_radius)
                mRadius = array.getInt(attr, 115);
            else if (attr == R.styleable.CircleProgressBar_width)
                mWidth = array.getInt(attr, 15);
            else if (attr == R.styleable.CircleProgressBar_ArcColor)
                mColor = array.getColor(attr, Color.parseColor("#42AE7C"));

        }
        initProgressBar();
    }

    /**
     * 此方法用于适配不同屏幕下的控件显示数值
     */
    private void initProgressBar() {
        Resources resources = getResources();
        //返回屏幕像素
        DisplayMetrics metrics = resources.getDisplayMetrics();
        //通过TypedValue转换dp到屏幕像素
        //参数1 转换单位，参数2 转换数值，参数三 当前屏幕
        int radius_dimension = (int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mRadius, metrics));

        width_dimension = (int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mWidth, metrics));

        Paint paint = new Paint();
        //抗锯齿
        paint.setAntiAlias(true);
        //样式--结束时为一个半圆
        paint.setStrokeCap(Paint.Cap.ROUND);
        //样式--只绘制边框
        paint.setStyle(Paint.Style.STROKE);
        //设置画笔宽度
        paint.setStrokeWidth(width_dimension);
        //设置颜色
        paint.setColor(mColor);

        mPaint = paint;

        mTextPaint = new Paint(paint);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(radius_dimension / 1.5f);

        //矩形区域
        mDiameter = (int) Math.ceil(radius_dimension * 2.0f);

        mRectF = new RectF(width_dimension / 2f, width_dimension / 2f, mDiameter + width_dimension / 2f, mDiameter + width_dimension / 2f);

        mProgress = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制背景条 起始默认为135
        mPaint.setAlpha(0x7f);
        mPaint.setColor(mBackgroudColor);
        canvas.drawArc(mRectF, mStartAngle, 300, false, mPaint);
        //绘制当前条
        if (mProgress != 0) {
            mPaint.setAlpha(0xff);
            mPaint.setColor(mColor);
            float degree = (300 * mProgress) / mMax * 1.0f;

            canvas.drawArc(mRectF, mStartAngle, degree, false, mPaint);
        }

        //绘制圆环中的字
        String txt = String.valueOf(mProgress);
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);
        float mTxtWidth = mTextPaint.measureText(txt, 0, txt.length());
        mTextPaint.setColor(mColor);

        canvas.drawText(txt, getWidth() / 2 - (mTxtWidth / 2), getHeight() / 2 + (mTxtHeight / 4), mTextPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mDiameter + width_dimension, mDiameter + width_dimension);
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    public void postProgress(final int progress) {
        this.post(new Runnable() {
            @Override
            public void run() {
                setProgress(progress);
            }
        });
    }

    public void setColor(int color) {
        mColor = color;
    }
}
