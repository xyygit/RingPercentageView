package com.xyy.customview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.Nullable;

/**
 * 环形百分比
 * Created by yayun.xia on 2020/4/21.
 */
public class RingPercentageView extends View {
    //进度条最大值
    private static final int MAX_VALUE = 100;
    //底环画笔
    private Paint mRingBgPaint;
    //圆环进度条画笔
    private Paint mProgressRingPaint;
    //文本画笔
    private Paint mTextPaint;
    //圆环旋转角度
    private float mSweepAngle;
    //圆环宽度
    private float mRingWidth;
    //底环颜色,默认 #E6E9EC
    private int mRingBgColor;
    //进度条圆环开始颜色，进度条圆环是渐变的
    private int mProgressRingStartColor;
    //进度条圆环结束颜色，进度条圆环是渐变的
    private int mProgressRingEndColor;
    //圆环半径,默认：Math.min(getHeight()/2,getWidth()/2)
    private float mCircleRadius;
    //文字颜色
    private int mTextColor;
    //文字大小
    private float mTextSize;
    //圆环渐变色
    private int[] arcColors = {};
    //初始化进度
    private int progress = 0;

    public RingPercentageView(Context context) {
        this(context, null);
    }

    public RingPercentageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RingPercentageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RingPercentageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        //获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingPercentageView);
        mRingWidth = typedArray.getDimension(R.styleable.RingPercentageView_ring_width, dipToPx(5));
        mRingBgColor = typedArray.getColor(R.styleable.RingPercentageView_ring_color, Color.parseColor("#E6E9EC"));
        mProgressRingStartColor = typedArray.getColor(R.styleable.RingPercentageView_progress_ring_start_color,
                Color.parseColor("#C797FF"));
        mProgressRingEndColor = typedArray.getColor(R.styleable.RingPercentageView_progress_ring_end_color,
                Color.parseColor("#766FFE"));
        mTextColor = typedArray.getColor(R.styleable.RingPercentageView_text_color, Color.parseColor("#956FFE"));
        mTextSize = typedArray.getDimension(R.styleable.RingPercentageView_text_size, dipToPx(14));

        //用完记得回收
        typedArray.recycle();

        //初始化
        init();
    }

    private void init() {
        //渐变颜色，为了让颜色过度自然，起止颜色设成一致
        arcColors = new int[]{
                mProgressRingStartColor,
                mProgressRingEndColor,
                mProgressRingStartColor};

        //初始化底环
        mRingBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        mRingBgPaint.setStyle(Paint.Style.STROKE);//描边
        mRingBgPaint.setStrokeWidth(mRingWidth);//设置圆环宽度
        mRingBgPaint.setColor(mRingBgColor);//底环颜色

        //初始化进度条圆环
        mProgressRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressRingPaint.setStrokeWidth(mRingWidth);
        mProgressRingPaint.setStyle(Paint.Style.STROKE);
        mProgressRingPaint.setStrokeCap(Paint.Cap.ROUND);//设置圆弧两头圆角化
        mProgressRingPaint.setDither(true); //防抖动

        //初始化文字画笔
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
    }

    private void setProgress(int value) {
        if (value > MAX_VALUE) {//百分比值范围0-100
            value = MAX_VALUE;
        }
        if (value < 0) {
            value = 0;
        }
        this.progress = value;
        //角度范围0~359
        mSweepAngle = 359 * progress / 100F;
        postInvalidate();
    }

    public void setPercentData(float data) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(progress, data);
        valueAnimator.setDuration((long) (Math.abs(progress - data) * 20));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                progress = (Math.round(value * 10)) / 10;
                setProgress(progress);
            }
        });
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureView(dipToPx(62), widthMeasureSpec),
                measureView(dipToPx(62), heightMeasureSpec));
    }

    /**
     * 重新测量尺寸
     *
     * @param defaultSize 默认尺寸
     * @param measureSpec 尺寸规格
     * @return 重新计算后的尺寸
     */
    private int measureView(int defaultSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize;//UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //计算半径
        mCircleRadius = Math.min(getWidth(), getHeight()) / 2F;
        //画灰色圆环背景
        onDrawRingBg(canvas);
        //绘制进度条
        onDrawProgressRing(canvas);
        //绘制百分比
        onDrawText(canvas);
    }

    /**
     * 绘制底环
     *
     * @param canvas 画布
     */
    private void onDrawRingBg(Canvas canvas) {
        //圆环是有宽度的，绘制时半径减去圆环宽度的一半
        float radius = mCircleRadius - mRingWidth / 2F;
        canvas.drawCircle(mCircleRadius, mCircleRadius, radius, mRingBgPaint);
    }

    /**
     * 绘制进度条圆环
     *
     * @param canvas 画布
     */
    private void onDrawProgressRing(Canvas canvas) {
        //设置颜色渐变
        Shader mProgressRingShader = new SweepGradient(mCircleRadius, mCircleRadius, arcColors, null);
        mProgressRingPaint.setShader(mProgressRingShader);
        //限制圆环进度条绘制范围
        RectF rectF = new RectF(mRingWidth / 2F,
                mRingWidth / 2F,
                mCircleRadius * 2 - mRingWidth / 2F,
                mCircleRadius * 2 - mRingWidth / 2F);
        //需求是从顶点开始绘制，默认绘制位置从0度开始
        //所以这里起始位置逆时针旋转90度
        canvas.drawArc(rectF, -90, mSweepAngle, false, mProgressRingPaint);
    }

    /**
     * 绘制文本
     *
     * @param canvas 画布
     */
    private void onDrawText(Canvas canvas) {
        String text = progress + "%";
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        //获取文本的宽度
        float textWidth = mTextPaint.measureText(text);
        //获取文本的高度
        float textHeight = fm.bottom - fm.top;
        //获取文字的绘制起点X坐标，加点偏移调整居中
        float textCenterVerticalBaselineX = mCircleRadius - textWidth / 2F + dipToPx(2);
        //获取文字的绘制起点Y坐标
        float textCenterVerticalBaselineY = mCircleRadius - fm.descent + textHeight / 2;
        //绘制文字
        canvas.drawText(progress + "%", textCenterVerticalBaselineX, textCenterVerticalBaselineY, mTextPaint);
    }

    private int dipToPx(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
    }
}
