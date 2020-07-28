package com.kishannareshpal.circularprogressview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class CircularProgressView extends View {

    // contants
    private final static float DEFAULT_MAXIMUM_DETERMINATE_PROGRESS_VALUE = 100.0F; // when no max value is added, use this to simulate 100%

    Context ctx;
    private int fullWidth, fullHeight;
    private float strokeWidth; // default: 30% of the circle radius.
    private boolean isIndeterminateStrokeAnimating;

    private float progressSweepAngle;
    private float lastProgressSweepAngle;

    // properties
    private StrokePlacement strokePlacement; // default: .INSIDE
    private ProgressType progressType; // default: .INDETERMINATE
    private int[] progressStrokeColorInts; // gradient between two colors.
    private int backgroundColor, borderColor;


    // FOR DETERMINATE PROGRESS TYPE
    // int minProgressValue; todo
    float maxDeterminateProgressValue; // default: DEFAULT_MAXIMUM_PROGRESS_VALUE
    float currentDeterminateProgressValuePercentage;
    float currentDeterminateProgressValue;

    private RectF strokeOval;
    private Paint main_paint, progressStroke_paint, border_paint;
    private ValueAnimator indeterminateValueAnimator, determinateValueAnimator;
    private boolean isStrokeColorGradient;
    private LinearGradient gradient;


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
        this.backgroundColor = ta.getResourceId(R.styleable.CircularProgressView_backgroundColor, ContextCompat.getColor(ctx, R.color.cpv_backgroundColor)); // transparent
        this.progressStrokeColorInts = new int[] { ta.getResourceId(R.styleable.CircularProgressView_progressStrokeColor, ContextCompat.getColor(ctx, R.color.cpv_strokeColor))}; // black
        this.borderColor = ta.getResourceId(R.styleable.CircularProgressView_borderColor, ContextCompat.getColor(ctx, R.color.cpv_borderColor)); // transparent
        this.strokePlacement = StrokePlacement.fromId(ta.getInt(R.styleable.CircularProgressView_progressStrokePlacement, StrokePlacement.INSIDE.getId()));
        this.progressType = ProgressType.fromId(ta.getInt(R.styleable.CircularProgressView_progressType, ProgressType.INDETERMINATE.getId()));
        if (ta.hasValue(R.styleable.CircularProgressView_determinateProgressValue) || ta.hasValue(R.styleable.CircularProgressView_determinateProgressValuePercentage)) {
            if (!ta.hasValue(R.styleable.CircularProgressView_maxDeterminateProgressValue)) {
                throw new RuntimeException("You must supply the maxDeterminateProgressValue attribute when you use either maxDeterminateProgressValue or determinateProgressValuePercentage attributes. Or use the setter methods to set all these attributes.");
            } else {
                this.maxDeterminateProgressValue = ta.getFloat(R.styleable.CircularProgressView_maxDeterminateProgressValue, DEFAULT_MAXIMUM_DETERMINATE_PROGRESS_VALUE);
                this.currentDeterminateProgressValue = ta.getFloat(R.styleable.CircularProgressView_determinateProgressValue, 0.0F);
                this.currentDeterminateProgressValuePercentage = ta.getFloat(R.styleable.CircularProgressView_determinateProgressValuePercentage, 0.0F);
            }
        }

        // This paint is for every icon background
        main_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // This paint is mainly used on the PROGRESS's rotating stroke
        progressStroke_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressStroke_paint.setStrokeCap(Paint.Cap.ROUND);
        progressStroke_paint.setStyle(Paint.Style.STROKE);

        border_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        border_paint.setStyle(Paint.Style.STROKE);

        indeterminateValueAnimator = ValueAnimator.ofInt(1, 270);
        indeterminateValueAnimator.setDuration(1500);
        indeterminateValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        indeterminateValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        indeterminateValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        indeterminateValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progressSweepAngle = (int) animation.getAnimatedValue();
                invalidate();
            }
        });

        // setup determinate value animator.
        determinateValueAnimator = new ValueAnimator(); // float
        determinateValueAnimator.setDuration(234);
        determinateValueAnimator.setInterpolator(new OvershootInterpolator());
        determinateValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progressSweepAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });


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
        // make sure the the layout_width and layout_height are specified with the same size on booth. E.g: width: 24dp - height: 24dp
        float width = fullWidth - getPaddingLeft() + getPaddingRight() - 2f; // added -2f to prevent corner overlap
        float height = fullHeight - getPaddingTop() + getPaddingBottom() - 2f; // added -2f to prevent corner overlap

        float cx            = (width / 2); // The x-coordinate of the center of the main circle to be drawn
        float cy            = (height / 2); // The y-coordinate of the center of the main circle to be drawn
        float circle_radius = (width / 2); // the main circle radius.
        this.strokeWidth = (circle_radius * (30.0f / 100.0f)); // 30% of the circle_radius.

        // First draw the background
        switch (strokePlacement) {
            case INSIDE:
                // will draw the stroke (progress indicator) inside the main background.
                circle_radius = width / 2;
                break;

            case OUTSIDE:
                circle_radius -= strokeWidth;
                break;

            case CENTER:
                circle_radius -= (strokeWidth / 2);
                break;
        }
        main_paint.setColor(backgroundColor);
        canvas.drawCircle(cx, cy, circle_radius, main_paint);

        // Bounds of the stroke (progress indicator)
        this.strokeOval.top = strokeWidth / 2;
        this.strokeOval.left = strokeWidth / 2;
        this.strokeOval.right = width - (strokeWidth/2);
        this.strokeOval.bottom = height - (strokeWidth/2);


        if (isStrokeColorGradient) {
            gradient = new LinearGradient(progressSweepAngle, 0, progressSweepAngle, height, progressStrokeColorInts, null, Shader.TileMode.CLAMP);
            progressStroke_paint.setShader(gradient);

        } else {
            progressStroke_paint.setColor(progressStrokeColorInts[0]);
        }
        progressStroke_paint.setStrokeWidth(strokeWidth);

        // Draw a border
        border_paint.setStrokeWidth(strokeWidth);
        border_paint.setColor(borderColor);
        canvas.drawArc(strokeOval, 0, 360, false, border_paint);

        // Then draw the stroke (progress indicator) on top of the main circle.
        if (progressType == ProgressType.INDETERMINATE) {
            if (!indeterminateValueAnimator.isStarted()) resumeIndeterminateAnimation();
            canvas.rotate(progressSweepAngle * 4, cx, cy); // indefinite rotation anim.
            float startAngle = lastProgressSweepAngle;
            canvas.drawArc(strokeOval, startAngle, progressSweepAngle, false, progressStroke_paint); // stroke (progress indicator)

        } else if (progressType == ProgressType.DETERMINATE) {
            canvas.drawArc(strokeOval, -90, progressSweepAngle, false, progressStroke_paint); // stroke (progress indicator)
        }
    }



    /** Getter Methods **/
    public boolean isIndeterminate() {
        return progressType == ProgressType.INDETERMINATE;
    }


    /* Setter Methods */
    /**
     * Change the main circle color.
     * @param backgroundColor color
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public void setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

    /**
     * Change the stroke color (progress indicator).
     *
     * @param strokeColorInts if one color is passed, the color will be a solid color. if multiple colors are passed (>1) than the color will be linear gradient.
     */
    public void setProgressStrokeColor(@ColorInt int... strokeColorInts) {
        if (strokeColorInts.length < 1) throw new IllegalArgumentException("Must supply at least one color.");
        this.progressStrokeColorInts = strokeColorInts;
        this.isStrokeColorGradient = strokeColorInts.length > 1;
        invalidate();
    }

    /**
     * Change the stroke (progress indicator) to be positioned either outside or inside the main circle.
     * @param strokePlacement the placement
     */
    public void setStrokePlacement(StrokePlacement strokePlacement) {
        this.strokePlacement = strokePlacement;
        invalidate();
    }
    public void setProgressType(ProgressType progressType) {
        changeProgressType(progressType);
        invalidate();
    }


    /**
     * Stops or paused the animation of the indeterminate state stroke.
     *
     * @param hideStroke should also hide the stroke.
     * @see #resumeIndeterminateAnimation() for resuming.
     */
    public void pauseIndeterminateAnimation(boolean hideStroke) {
        if (progressType == ProgressType.INDETERMINATE) {
            if (indeterminateValueAnimator == null) return;
            this.isIndeterminateStrokeAnimating = false;
            lastProgressSweepAngle = (int) indeterminateValueAnimator.getAnimatedValue();
            if (hideStroke) indeterminateValueAnimator.setIntValues(0);
            indeterminateValueAnimator.cancel();
        }
    }

    /**
     * Resumes the animation of the indeterminate state stroke, if paused.
     * @see #pauseIndeterminateAnimation(boolean) for how to stop it.
     */
    public void resumeIndeterminateAnimation() {
        if (progressType == ProgressType.INDETERMINATE) {
            if (indeterminateValueAnimator == null) return;
            this.isIndeterminateStrokeAnimating = true;
            indeterminateValueAnimator.setIntValues(1, 270);
            indeterminateValueAnimator.start();
        }
    }

    /**
     * Toggles the indeterminate state stroke animation.
     * If its already animating, stop it with {@link #pauseIndeterminateAnimation(boolean)}
     * otherwise start it with {@link #resumeIndeterminateAnimation()}
     */
    public void toggleIndeterminateAnimation() {
        if (!this.isIndeterminate()) {
            this.setProgressType(ProgressType.INDETERMINATE);
        }

        if (progressType == ProgressType.INDETERMINATE) {
            if (indeterminateValueAnimator == null) return;
            if (isIndeterminateStrokeAnimating) {
                // If its animating, stop it!
                pauseIndeterminateAnimation(false);
            } else {
                // If its not animating, start it!
                resumeIndeterminateAnimation();
            }
        }
    }



    /**
     * Changes the maximum progress value.
     * The 100% equivalent value.
     * - The progress will always start from 0 to the specified maximumProgressValue.
     *
     * E.g:
     *  - If you were to download a file, you would put here the maximum file size in bytes.
     *
     * @param maximumProgressValue maximum value.
     */
    public void setRange(int maximumProgressValue) {
        this.maxDeterminateProgressValue = maximumProgressValue;
    }

    /**
     * Sets the current progress to the specified value. Does not do anything if the progress bar is in indeterminate mode.
     * This method will immediately update the visual position of the progress indicator.
     *
     * @param progressPercentage specified progress value.
     * @param animated if the progress should update to the target value with animation.
     *
     * @see #setProgess(float) for setting without the animation.
     * @see #calcProgressValuePercentageOf(int, int) Use this method for calculating the progressPercentage used here.
     */
    public void setProgress(float progressPercentage, boolean animated) {
        changeProgress(progressPercentage, animated);
    }

    public void setProgess(float progressPercentage) {
        // sets the progress instantly.
        changeProgress(progressPercentage, false);
    }





    // Convenience
    /**
     * Calculates the percentage of a value out of the maximum value
     *
     * @param value the value out of the max value.
     * @param maxValue the max value.
     * @return the percentage of the value out of the maxValue.
     */
    public static float calcProgressValuePercentageOf(int value, int maxValue) {
        // max = 2300
        // val = 20
        // perc = ?

        // val = max * 10/100    > will get u 10% of max value

        // val = max * perc/100
        // 20 = 2300 * perc/100;
        // 2300 * perc/100 = 20
        // perc/100 = 20/2300

        // perc = 20*100/2300    > the formula!
        return (value*100F) / maxValue;
    }



    // Private
    void changeProgress(float progressValuePercentage, boolean animated) {
        this.currentDeterminateProgressValuePercentage = progressValuePercentage;

        // mock
        // max = 2400
        // progress = 20
        // 2400 (20/100)

        // 480 <-> 2400
        // x <-> 360
        // x = (360*480)/2400     > the formula!

        // x% of max value

        int CIRCLE_MAX_ANGLE = 360;
        float xPercentOfMax = maxDeterminateProgressValue * (progressValuePercentage / 100F); // x% of max value.
        float newCircleSweepAngle = (CIRCLE_MAX_ANGLE * xPercentOfMax) / maxDeterminateProgressValue;

        if (animated) {
            determinateValueAnimator.setFloatValues(progressSweepAngle, newCircleSweepAngle);
            if (determinateValueAnimator.isRunning()) determinateValueAnimator.cancel();
            determinateValueAnimator.start();
            progressSweepAngle = newCircleSweepAngle;
            lastProgressSweepAngle = progressSweepAngle;
        } else {
            if (determinateValueAnimator != null) determinateValueAnimator.cancel();
            progressSweepAngle = newCircleSweepAngle;
            invalidate();
        }
    }

    private void changeProgressType(ProgressType progressType) {
        switch (progressType) {
            case INDETERMINATE:
                this.progressType = progressType;
                resumeIndeterminateAnimation();
                break;

            case DETERMINATE:
                pauseIndeterminateAnimation(false);
                this.progressType = progressType;
                break;
        }
    }

}
