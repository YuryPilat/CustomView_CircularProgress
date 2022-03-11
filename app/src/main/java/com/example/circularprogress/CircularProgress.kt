package com.example.circularprogress

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.*

private const val ARC_FULL_ANGLE = 360f
private const val PERCENTAGE_VALUE_HOLDER = "percentage"
private const val TICK_AMOUNT = 28
private const val MAX_PERCENT = 100
private const val COMMON_MARGIN = 40
private const val STROKE_WIDTH = 20f
private const val BACKGROUND_ARC_START_ANGLE = 0f
private const val PROGRESS_ARC_START_ANGLE = 270f
private const val BACKGROUND_STROKE_WIDTH = 25f
private const val ROUND_CAP_PADDING = 0.98f
private const val DEFAULT_PADDING_COEFF = 0.05
private const val ANIMATION_DURATION = 1000L

class CircularProgress(
    context: Context?,
    attrs: AttributeSet?
) : View(context, attrs) {

    private var horizontalCenter = 0f
    private var verticalCenter = 0f
    private var currentProgress = 0
    private val viewBounds = RectF()
    private val backgroundProgressColor = Color.LTGRAY
    private val fillProgressColor =  Color.YELLOW

    private val backgroundProgressPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = backgroundProgressColor
        strokeWidth = BACKGROUND_STROKE_WIDTH
    }

    private val fillProgressPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = fillProgressColor
        strokeWidth = STROKE_WIDTH
        strokeCap = Paint.Cap.ROUND
    }

    private val pointerPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.MAGENTA
    }

    private val tickBackground = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = backgroundProgressColor
        strokeWidth = STROKE_WIDTH/2
        strokeCap = Paint.Cap.ROUND
    }

    private val tickFill = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = fillProgressColor
        strokeWidth = STROKE_WIDTH/2
        strokeCap = Paint.Cap.ROUND
    }

    private lateinit var onProgressChangedListener : OnProgressChangedListener

    interface OnProgressChangedListener {
        fun getProgressPercent(percent : Int)
    }

    fun setOnProgressChangedListener(listener: OnProgressChangedListener) {
        this.onProgressChangedListener = listener
    }


    override fun onDraw(canvas: Canvas) {
        drawBackgroundArc(canvas)
        drawProgressArc(canvas)
        drawTicks(canvas)
        drawPointer(canvas)
    }

    private fun drawPointer(canvas: Canvas) {
        val radius = viewBounds.width()/2
        val toRadians = (getCurrentSweepAngle() - 90) * (Math.PI / 180.0)
        val x = (radius * cos(toRadians)).toInt()
        val y = (radius * sin(toRadians)).toInt()
        canvas.drawCircle(
            ((x + width / 2)).toFloat(),
            ((y + height / 2)).toFloat(),
            STROKE_WIDTH,
            pointerPaint
        )
    }

    private fun drawTicks(canvas: Canvas) {
        val innerTickRadius = viewBounds.height()/2 + COMMON_MARGIN
        (0 until TICK_AMOUNT).forEach { tickIndex ->
            val tickRotation = (tickIndex * Math.PI * 2 / TICK_AMOUNT).toFloat()
            val startX = sin(tickRotation.toDouble()).toFloat() * innerTickRadius
            val startY = (-cos(tickRotation.toDouble())).toFloat() * innerTickRadius
            val stopX = sin(tickRotation.toDouble()).toFloat() * horizontalCenter
            val stopY = (-cos(tickRotation.toDouble())).toFloat() * horizontalCenter
            val paint = if (tickPercent(tickIndex) < currentProgress) tickFill else tickBackground
            canvas.drawLine(
                horizontalCenter + startX,
                verticalCenter + startY,
                horizontalCenter + stopX*ROUND_CAP_PADDING,
                verticalCenter + stopY*ROUND_CAP_PADDING,
                paint
            )
        }
    }

    private fun tickPercent(tick : Int) : Int {
        return ((tick * MAX_PERCENT)/ TICK_AMOUNT)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        horizontalCenter = (width/2).toFloat()
        verticalCenter = horizontalCenter
        val ovalSize = verticalCenter - COMMON_MARGIN*2
        viewBounds.set(
            horizontalCenter - ovalSize,
            verticalCenter - ovalSize,
            horizontalCenter + ovalSize,
            verticalCenter + ovalSize
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val deviceWidth = MeasureSpec.getSize(widthMeasureSpec)
        val deviceHeight = MeasureSpec.getSize(heightMeasureSpec)
        val viewPadding = (deviceWidth*DEFAULT_PADDING_COEFF).roundToInt()
        setMeasuredDimension(deviceWidth - viewPadding, deviceWidth- viewPadding)
    }

    private fun drawBackgroundArc(canvas: Canvas) {
        canvas.drawArc(viewBounds, BACKGROUND_ARC_START_ANGLE, ARC_FULL_ANGLE, false, backgroundProgressPaint)
    }

    private fun drawProgressArc(canvas: Canvas) {
        canvas.drawArc(viewBounds, PROGRESS_ARC_START_ANGLE, getCurrentSweepAngle(), false, fillProgressPaint)
    }

    private fun getCurrentSweepAngle() : Float {
        return ARC_FULL_ANGLE * (currentProgress / MAX_PERCENT.toFloat())
    }

    private  fun animateProgress() {
        val valuesHolder = PropertyValuesHolder.ofFloat("percentage", 0f, currentProgress.toFloat())
        val animator = ValueAnimator().apply {
            setValues(valuesHolder)
            duration = ANIMATION_DURATION
            interpolator = LinearInterpolator()
            addUpdateListener {
                val percentage = it.getAnimatedValue(PERCENTAGE_VALUE_HOLDER) as Float
                currentProgress = percentage.toInt()
                postInvalidate()
            }
        }
        animator.start()
    }

    private fun getAngle(touchX: Double, touchY: Double): Double {
        val angle: Double
        val x2 = touchX - horizontalCenter
        val y2 = touchY - verticalCenter
        val d1 = sqrt((verticalCenter * verticalCenter).toDouble())
        val d2 = sqrt((x2 * x2 + y2 * y2))
        angle = if (touchX >= horizontalCenter) {
            Math.toDegrees(acos((-verticalCenter * y2) / (d1 * d2)))
        } else
            ARC_FULL_ANGLE - Math.toDegrees(acos((-verticalCenter * y2) / (d1 * d2)))
        return angle
    }

    private fun countPercent(angle : Double) {
        currentProgress = ((angle * MAX_PERCENT)/ARC_FULL_ANGLE).roundToInt()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_MOVE) {
            if(viewBounds.contains(event.x, event.y)) {
                countPercent(getAngle(event.x.toDouble(), event.y.toDouble()))
                onProgressChangedListener.getProgressPercent(currentProgress)
                postInvalidate()
            }
        }
        return true
    }

    fun setTimerProgress(progress : Int) {
        currentProgress = progress
        animateProgress()
    }
}