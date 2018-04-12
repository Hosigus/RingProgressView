package com.hosigus.ringprogressview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by 某只机智 on 2018/3/21.
 * 用于绘制环形统计/进度图
 * ————————————————————
 * 建议定义部分：
 * 1.ringCount 环数
 * 2.Paint(主要是颜色)
 *      RingP - 背景环
 *      StrokeP - 进度环外部
 *      FillP - 进度环内部
 *      HintP - 提示文本
 * 3.progressList 进度值
 * 4.explainList 对环说明
 * ——————————
 * 可自定义部分:
 * 1.ringHeight 单环高度
 * 2.ringMargin 环间距
 * 3.needProgressHint 显示进度提示
 * 4.needRingExplain 显示对环说明
 * 5.onDrawListener 绘制完成的回调
 * ————————————————
 * 定义完后请调用 reDraw() 方法,刷新以及展现动态效果
 */

public class RingProgressView extends View {
    private int ringCount;
    /**
     * RingHeight 单环高度
     */
    private int ringHeight;
    /**
     * RingMargin 环间距
     */
    private int ringMargin;
    private int maxSize;

    /**
     * needProgressHint: 设置是否显示环上百分比提示
     * needRingExplain: 设置是否显示底部对颜色的说明
     */
    private boolean isHeightSeted, isMarginSeted,needProgressHint, needRingExplain;

    private List<Paint> ringPList,strokePList, fillPList, hintPList;
    /**
     * 进度 , float 0-1
     */
    private List<Float> progressList;
    /**
     * 底部对颜色的说明,应当尽量简短
     */
    private List<String> explainList;

    private Path path = new Path();

    public RingProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RingProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingProgressView);
        ringCount = typedArray.getInteger(R.styleable.RingProgressView_ringCount,1);
        ringHeight = typedArray.getDimensionPixelSize(R.styleable.RingProgressView_ringHeight, -1);
        ringMargin = typedArray.getDimensionPixelSize(R.styleable.RingProgressView_ringMargin, -1);
        needProgressHint = typedArray.getBoolean(R.styleable.RingProgressView_nendProgressHint, false);
        needRingExplain = typedArray.getBoolean(R.styleable.RingProgressView_needRingExplain, true);
        typedArray.recycle();

        isHeightSeted = ringHeight != -1;
        isMarginSeted = ringMargin != -1;

        initValue();
        initPaint();
    }

    private void initSize(){
        int perHeight = 2 * maxSize / (ringCount + 2) / 5;
        if (!isHeightSeted) {
            ringHeight = 14 * perHeight / 17;
        }
        if (!isMarginSeted) {
            ringMargin = 3 * perHeight / 17;
        }
    }

    private void initValue() {
        int add = 0;
        if (progressList==null){
            progressList = new ArrayList<>();
            explainList = new ArrayList<>();
            add = ringCount;
        }else {
            add = ringCount - progressList.size();
        }
        for (int i = 0; i < add; i++) {
            progressList.add(0f);
            explainList.add("");
        }
    }

    private void initPaint(){
        int add = 0;
        if (ringPList==null){
            ringPList = new ArrayList<>();
            strokePList = new ArrayList<>();
            fillPList = new ArrayList<>();
            hintPList = new ArrayList<>();
            add = ringCount;
        }else {
            add = ringCount - ringPList.size();
        }
        for (int i = 0; i < add; i++) {
            Paint p1 = new Paint(), p2 = new Paint(), p3 = new Paint(), p4 = new Paint();
            p1.setStyle(Paint.Style.STROKE);
            p2.setStyle(Paint.Style.STROKE);
            p3.setStyle(Paint.Style.FILL);
            p1.setStrokeWidth(dp2px(2));
            p2.setStrokeWidth(dp2px(2));
            p1.setAntiAlias(true);
            p2.setAntiAlias(true);
            p3.setAntiAlias(true);
            p1.setColor(Color.rgb(228,248,254));
            p2.setColor(Color.rgb(130,200,240));
            p3.setColor(Color.rgb(184,228,253));
            p4.setColor(Color.rgb(130,200,240));
            ringPList.add(p1);
            strokePList.add(p2);
            fillPList.add(p3);
            hintPList.add(p4);
        }
    }

    //14:3
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = View.MeasureSpec.getSize(widthMeasureSpec),h = View.MeasureSpec.getSize(heightMeasureSpec);

        maxSize = w < h ? w : h;
        initSize();

        if (needRingExplain){
            if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST) {
                int bestSpace = (ringHeight + ringMargin * 2) * ((ringCount + 1) / 2 + 1);
                h = h < bestSpace ? h : bestSpace;
            }
            setMeasuredDimension(maxSize, h);
        }else {
            setMeasuredDimension(maxSize,maxSize);
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawOutRing(canvas);
        drawInRing(canvas);
        if (needProgressHint) {
            drawProgressHint(canvas);
        }
        if (needRingExplain) {
            drawExplain(canvas);
        }
    }

    private void drawExplain(Canvas canvas) {
        if (getMeasuredHeight() == maxSize)
            return;

        float perH = (getMeasuredHeight() - maxSize) / (ringCount / 2 + 2),
                center = maxSize / 2,
                perW = (ringHeight + ringMargin) * (ringCount + 2),
                r = ringHeight / 4,
                y = maxSize + perH / 2,
                xa = center - perW / 8,
                xb = center + perW / 8;
        for (int i = 0; i < ringCount; i++) {
            path.reset();
            if (i%2==0) {
                path.addCircle(xa, y, r, Path.Direction.CW);
                Paint p = hintPList.get(i);
                p.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(explainList.get(i), xa - 2 * r, y + r, p);
                p.setTextAlign(Paint.Align.LEFT);
            } else {
                path.addCircle(xb, y, r, Path.Direction.CW);
                canvas.drawText(explainList.get(i), xb + 2 * r, y + r, hintPList.get(i));
                y += perH;
            }
            canvas.drawPath(path, strokePList.get(i));
            canvas.drawPath(path, fillPList.get(i));
        }
    }

    private void drawProgressHint(Canvas canvas) {
        float center = maxSize / 2;
        float radius = (ringHeight + ringMargin) * (ringCount + 2);
        for (int i = 0; i < ringCount; i++) {
            hintPList.get(i).setTextSize(dp2px(ringHeight/5));
            canvas.drawText(String.format(Locale.CHINA,"%.1f",progressList.get(i)*100)+"%",
                    center-ringHeight*2.2f,
                    center-radius+ringHeight*7/9f, hintPList.get(i));
            radius -= ringHeight + ringMargin;
        }
    }

    private void drawInRing(Canvas canvas) {
        float center = maxSize / 2;
        float radius = (ringHeight + ringMargin) * (ringCount + 2);
        int smallRadius = ringHeight / 2;
        float startLeft = center - smallRadius, startRight = startLeft + ringHeight;
        for (int i = 0; i < ringCount; i++) {
            path.reset();
            float angle = 360 * progressList.get(i);

            float leftAndTop = center - radius, rightAndBottom = center + radius;
            RectF r = new RectF(leftAndTop, leftAndTop, rightAndBottom, rightAndBottom);
            path.arcTo(r, 270, angle);

            float endCx = (float)(center + (radius - smallRadius) * Math.sin(2 * Math.PI * progressList.get(i))),
                    endCy = (float)(center - (radius - smallRadius) * Math.cos(2 * Math.PI * progressList.get(i)));
            r = new RectF(endCx - smallRadius, endCy - smallRadius, endCx + smallRadius, endCy + smallRadius);
            path.arcTo(r, 270+angle, 180);

            leftAndTop = center - radius + ringHeight;
            rightAndBottom = center + radius - ringHeight;
            r = new RectF(leftAndTop, leftAndTop, rightAndBottom, rightAndBottom);
            path.arcTo(r, 270+angle, -angle);

            r = new RectF(startLeft, center - radius, startRight, center - radius + ringHeight);
            path.arcTo(r, 90, 180);

            canvas.drawPath(path, strokePList.get(i));
            canvas.drawPath(path, fillPList.get(i));

            radius -= ringHeight + ringMargin;
        }
    }

    private void drawOutRing(Canvas canvas) {
        float center = maxSize / 2;
        float radius = (ringHeight + ringMargin) * (ringCount + 2);
        for (int i = 0; i < ringCount; i++) {
            path.reset();
            path.addCircle(center,center,radius, Path.Direction.CW);
            radius -= ringHeight;
            path.addCircle(center,center,radius, Path.Direction.CW);
            canvas.drawPath(path, ringPList.get(i));
            radius -= ringMargin;
        }
    }

    private int dp2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 重绘,以及动态效果
     */
    public void reDraw(final OnDrawListener onDrawListener){
        final Float[] realList = progressList.toArray(new Float[progressList.size()]);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float rate = animation.getAnimatedFraction();
                for (int i = 0; i < ringCount; i++) {
                    progressList.set(i, realList[i] * rate);
                }
                invalidate();
                if (rate == 1 && onDrawListener != null) {
                    onDrawListener.onDrawEnd();
                }
            }
        });
        valueAnimator.start();
    }
    public void reDraw(){
        reDraw(null);
    }

    /**
     * 设置层数后将重置画笔和值
     * @param ringCount 新层数
     */
    public void setRingCount(int ringCount) {
        this.ringCount = ringCount;
        initSize();
        initPaint();
        initValue();
    }
    public int getRingCount() {
        return ringCount;
    }

    public int getRingHeight() {
        return ringHeight;
    }
    public int getRingMargin() {
        return ringMargin;
    }
    public void setRingHeight(int ringHeight) {
        this.ringHeight = ringHeight;
        isHeightSeted = true;
        initSize();
    }
    public void setRingMargin(int ringMargin) {
        this.ringMargin = ringMargin;
        isMarginSeted = true;
        initSize();
    }

    /**
     * 取消设置RingHeight
     */
    public void setRingHeightFree(){
        isHeightSeted = false;
        initSize();
    }
    /**
     * 取消设置RingMargin
     */
    public void setRingMarginFree(){
        isMarginSeted = false;
        initSize();
    }

    public List<Float> getProgressList() {
        return progressList;
    }
    public void setProgressList(List<Float> progressList) {
        this.progressList = progressList;
    }
    public void setProgress(int position,Float progress) {
        progressList.set(position, progress);
    }

    public void setExplainList(List<String> explainList) {
        this.explainList = explainList;
    }
    public void setExplain(int position,String explain) {
        explainList.set(position, explain);
    }

    public boolean isNeedRingExplain() {
        return needRingExplain;
    }
    public void setNeedRingExplain(boolean needRingExplain) {this.needRingExplain = needRingExplain;}

    public boolean isNeedProgressHint() {
        return needProgressHint;
    }
    public void setNeedProgressHint(boolean needProgressHint) {this.needProgressHint = needProgressHint;}

//      设置画笔：
//      RingP - 背景环
//      StrokeP - 进度环外部
//      FillP - 进度环内部
//      HintP - 提示文本
    public void setRingPList(List<Paint> ringPList) {
        this.ringPList = ringPList;
    }
    public void setStrokePList(List<Paint> strokePList) {
        this.strokePList = strokePList;
    }
    public void setFillPList(List<Paint> fillPList) {
        this.fillPList = fillPList;
    }
    public void setHintPList(List<Paint> hintPList) {
        this.hintPList = hintPList;
    }
    public void setRingP(int position,Paint ringP) {
        ringPList.set(position, ringP);
    }
    public void setStrokeP(int position,Paint strokeP) {
        strokePList.set(position, strokeP);
    }
    public void setFillP(int position,Paint fillP) {
        fillPList.set(position, fillP);
    }
    public void setProgressP(int position,Paint progressP) {
        hintPList.set(position, progressP);}

//      设置画笔颜色：
//      RingP - 背景环
//      StrokeP - 进度环外部
//      FillP - 进度环内部
//      HintP - 提示文本
    public void setRingColor(int position,int color) {
        ringPList.get(position).setColor(color);
    }
    public void setStrokeColor(int position,int color) {strokePList.get(position).setColor(color);}
    public void setFillColor(int position,int color) {
        fillPList.get(position).setColor(color);
    }
    public void setProgressColor(int position,int color) {
        hintPList.get(position).setColor(color);}

    /**
     * 重绘完成后的回调接口
     */
    public interface OnDrawListener {
        void onDrawEnd();
    }
}
