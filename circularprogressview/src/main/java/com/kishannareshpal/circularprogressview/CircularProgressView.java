package com.kishannareshpal.circularprogressview;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class CircularProgressView extends View {

    Context ctx;
    private int fullWidth, fullHeight;
    private int circle_sweepAngle;
    private float strokeWidth = 12f; // this width will expand +4 with animation. So in the end it's value'll be 12;
    private StrokePlacement strokePlacement;

    private RectF strokeOval;
    private Paint main_paint, stroke_paint;
    private ValueAnimator valueAnimator;


    public CircularProgressView(Context context) {
        super(context);
        init(context, null);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);

    }

    public void init(Context ctx, @Nullable AttributeSet attributeSet){
        this.ctx = ctx;
        strokeOval = new RectF();

        TypedArray ta = ctx.obtainStyledAttributes(attributeSet, R.styleable.CircularProgressView);
        int backgroundColor = ta.getResourceId(R.styleable.CircularProgressView_backgroundColor, R.color.cpv_backgroundColor);
        int strokeColor = ta.getResourceId(R.styleable.CircularProgressView_progressStrokeColor, R.color.cpv_strokeColor);
        strokePlacement = StrokePlacement.fromId(ta.getInt(R.styleable.CircularProgressView_progressStrokePlacement, StrokePlacement.INSIDE.getId()));

        // This paint is for every icon background
        main_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        main_paint.setColor(ContextCompat.getColor(ctx, backgroundColor));

        // This paint is mainly used on the PROGRESS's rotating icon stroke
        stroke_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke_paint.setStrokeCap(Paint.Cap.ROUND);
        stroke_paint.setStyle(Paint.Style.STROKE);
        stroke_paint.setColor(ContextCompat.getColor(ctx, strokeColor));

        valueAnimator = ValueAnimator.ofInt(1, 270);
        valueAnimator.setDuration(1500);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circle_sweepAngle = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        start();

        if (attributeSet != null) {
            ta.recycle();
        }
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fullWidth = w;
        fullHeight = h;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // get the size
        int width = fullWidth - getPaddingLeft() - getPaddingRight();
        int height = fullWidth - getPaddingTop() - getPaddingBottom();

        float cx            = fullWidth / 2; // The x-coordinate of the center of the main circle to be drawn
        float cy            = fullHeight / 2; // The y-coordinate of the center of the main circle to be drawn
        float circle_radius = width / 2; // the main circle radius.

        // Bounds of the stroke (progress indicator)
        strokeOval.top = strokeWidth / 2;
        strokeOval.left = strokeWidth / 2;
        strokeOval.right = width - (strokeWidth/2);
        strokeOval.bottom = height - (strokeWidth/2);

        strokeWidth = circle_radius * (20.0f / 100.0f); // 25% of the circle_radius.
        stroke_paint.setStrokeWidth(strokeWidth);

        // First draw the background
        switch (strokePlacement) {
            case INSIDE:
                // will draw the stroke (progress indicator) inside the main background.
                circle_radius = width / 2;
                break;

            case OUTSIDE:
                circle_radius -= strokeWidth - 1; // i've added -1 because it was showing a thin white pixel on the circle edges while indicator was rotating.
                break;

            case CENTER:
                circle_radius -= (strokeWidth /2);
                break;
        }

        canvas.drawCircle(cx, cy, circle_radius, main_paint);

        // Then draw the stroke (progress indicator) on top of the main circle.
        canvas.rotate(circle_sweepAngle * 4, cx, cy); // rotation anim.
        canvas.drawArc(strokeOval, 70, circle_sweepAngle, false, stroke_paint); // stroke (progress indicator)
    }



    /** User Interface Methods **/
    /**
     * Change the main circle color.
     * @param backgroundColor color
     */
    public void setBackgroundColor(int backgroundColor) {
        if (main_paint != null) {
            main_paint.setColor(backgroundColor);
            invalidate();
        }
    }

    /**
     * Change the stroke color (progress indicator).
     * @param strokeColor color
     */
    public void setStrokeColor(int strokeColor) {
        if (stroke_paint != null) {
            stroke_paint.setColor(strokeColor);
            invalidate();
        }
    }

    /**
     * Change the stroke (progress indicator) to be positioned either outside or inside the main circle.
     * @param strokePlacement the placement
     */
    public void setStrokePlacement(StrokePlacement strokePlacement) {
        this.strokePlacement = strokePlacement;
        invalidate();
    }




    /** User Experience methods **/
    /**
     * Starts the progress indicator animation.
     */
    public void start() {
        if (valueAnimator != null) {
            valueAnimator.start();
            invalidate();
        }
    }

    /**
     * Stops the progress indicator animation.
     */
    public void stop() {
        if (valueAnimator != null) {
            valueAnimator.end();
            invalidate();
        }
    }


}
