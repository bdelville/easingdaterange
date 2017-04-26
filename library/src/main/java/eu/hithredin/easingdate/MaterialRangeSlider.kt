package eu.hithredin.easingdate

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import java.util.*

interface RangeSliderListener {
    fun onUpperChanged(newValue: Int)

    fun onLowerChanged(newValue: Int)

    fun onEndAction(lower: Int, upper: Int)
}

/**
 * Slider following Material Design with two movable targets
 * that allow user to select a range of integers.
 */
class MaterialRangeSlider : View {

    private var deviceHelper: DeviceDateRange

    private val DEFAULT_TOUCH_TARGET_SIZE = 50
    private val DEFAULT_UNPRESSED_RADIUS = 15
    private val DEFAULT_PRESSED_RADIUS = 40
    private val DEFAULT_INSIDE_RANGE_STROKE_WIDTH = 8
    private val DEFAULT_OUTSIDE_RANGE_STROKE_WIDTH = 4
    private val HORIZONTAL_PADDING = (DEFAULT_PRESSED_RADIUS / 2) + DEFAULT_OUTSIDE_RANGE_STROKE_WIDTH

    val DEFAULT_MAX: Int = 1000

    private var unpressedRadius: Float = 0.toFloat()
    private var pressedRadius: Float = 0.toFloat()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lineStartX: Int = 0
    private var lineEndX: Int = 0
    private var lineLength: Int = 0
    private var minTargetRadius = 0f
    private var maxTargetRadius = 0f
    private var minPosition = 0
    private var maxPosition = 0
    private var midY = 0
    //List of event IDs touching targets
    private val isTouchingMinTarget = HashSet<Int>()
    private val isTouchingMaxTarget = HashSet<Int>()

    var min: Int = 0
        set(min) {
            field = min
            range = max - min
        }

    var max: Int = DEFAULT_MAX
        set(max) {
            field = max
            range = max - min
        }

    var range: Int = 0; private set

    private var convertFactor: Float = 0.toFloat()
    var rangeSliderListener: RangeSliderListener? = null
    private var targetColor: Int = 0
    private var insideRangeColor: Int = 0
    private var outsideRangeColor: Int = 0
    private var colorControlNormal: Int = 0
    private var colorControlHighlight: Int = 0
    private var insideRangeLineStrokeWidth: Float = 0.toFloat()
    private var outsideRangeLineStrokeWidth: Float = 0.toFloat()
    private var minAnimator: ObjectAnimator
    private var maxAnimator: ObjectAnimator
    internal var lastTouchedMin: Boolean = false

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        deviceHelper = DeviceDateRange(context)
        getDefaultColors()
        getDefaultMeasurements()

        //get attributes passed in XML
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaterialRangeSlider, 0, 0)
        targetColor = styledAttrs.getColor(R.styleable.MaterialRangeSlider_insideRangeLineColor, colorControlNormal)
        insideRangeColor = styledAttrs.getColor(R.styleable.MaterialRangeSlider_insideRangeLineColor, colorControlNormal)
        outsideRangeColor = styledAttrs.getColor(R.styleable.MaterialRangeSlider_outsideRangeLineColor, colorControlHighlight)
        min = styledAttrs.getInt(R.styleable.MaterialRangeSlider_min, min)
        max = styledAttrs.getInt(R.styleable.MaterialRangeSlider_max, max)

        unpressedRadius = styledAttrs.getDimension(R.styleable.MaterialRangeSlider_unpressedTargetRadius, DEFAULT_UNPRESSED_RADIUS.toFloat())
        pressedRadius = styledAttrs.getDimension(R.styleable.MaterialRangeSlider_pressedTargetRadius, DEFAULT_PRESSED_RADIUS.toFloat())
        insideRangeLineStrokeWidth = styledAttrs.getDimension(R.styleable.MaterialRangeSlider_insideRangeLineStrokeWidth, DEFAULT_INSIDE_RANGE_STROKE_WIDTH.toFloat())
        outsideRangeLineStrokeWidth = styledAttrs.getDimension(R.styleable.MaterialRangeSlider_outsideRangeLineStrokeWidth, DEFAULT_OUTSIDE_RANGE_STROKE_WIDTH.toFloat())

        styledAttrs.recycle()

        minTargetRadius = unpressedRadius
        maxTargetRadius = unpressedRadius
        range = max - min

        minAnimator = getMinTargetAnimator(true)
        maxAnimator = getMaxTargetAnimator(true)
    }

    /**
     * Get default colors from theme.  Compatible with 5.0+ themes and AppCompat themes.
     * Will attempt to get 5.0 colors, if not avail fallback to AppCompat, and if not avail use
     * black and gray.
     * These will be used if colors are not set in xml.
     */
    private fun getDefaultColors() {
        val typedValue = TypedValue()

        val materialStyledAttrs = context.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.colorControlNormal, android.R.attr.colorControlHighlight))

        val appcompatMaterialStyledAttrs = context.obtainStyledAttributes(typedValue.data, intArrayOf(android.support.v7.appcompat.R.attr.colorControlNormal, android.support.v7.appcompat.R.attr.colorControlHighlight))
        colorControlNormal = materialStyledAttrs.getColor(0, appcompatMaterialStyledAttrs.getColor(0, android.R.color.holo_blue_dark))
        colorControlHighlight = materialStyledAttrs.getColor(1, appcompatMaterialStyledAttrs.getColor(1, android.R.color.black))

        targetColor = colorControlNormal
        insideRangeColor = colorControlHighlight

        materialStyledAttrs.recycle()
        appcompatMaterialStyledAttrs.recycle()
    }

    /**
     * Get default measurements to use for radius and stroke width.
     * These are used if measurements are not set in xml.
     */
    private fun getDefaultMeasurements() {
        pressedRadius = Math.round(deviceHelper.dipToPixels(DEFAULT_PRESSED_RADIUS.toFloat()).toFloat()).toFloat()
        unpressedRadius = Math.round(deviceHelper.dipToPixels(DEFAULT_UNPRESSED_RADIUS.toFloat()).toFloat()).toFloat()
        insideRangeLineStrokeWidth = Math.round(deviceHelper.dipToPixels(DEFAULT_INSIDE_RANGE_STROKE_WIDTH.toFloat()).toFloat()).toFloat()
        outsideRangeLineStrokeWidth = Math.round(deviceHelper.dipToPixels(DEFAULT_OUTSIDE_RANGE_STROKE_WIDTH.toFloat()).toFloat()).toFloat()
    }

    private fun getMinTargetAnimator(touching: Boolean): ObjectAnimator {
        val anim = ObjectAnimator.ofFloat(this, "minTargetRadius", minTargetRadius, if (touching) pressedRadius else unpressedRadius)
        anim.addUpdateListener { invalidate() }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                anim.removeAllListeners()
                super.onAnimationEnd(animation)
            }
        })

        anim.interpolator = AccelerateInterpolator()
        return anim
    }

    private fun getMaxTargetAnimator(touching: Boolean): ObjectAnimator {
        val anim = ObjectAnimator.ofFloat(this, "maxTargetRadius", maxTargetRadius, if (touching) pressedRadius else unpressedRadius)
        anim.addUpdateListener { invalidate() }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                anim.removeAllListeners()
            }
        })
        anim.interpolator = AccelerateInterpolator()
        return anim
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val desiredHeight = 96

        var width = widthSize
        var height = desiredHeight

        if (widthMode == View.MeasureSpec.EXACTLY) {
            width = widthSize
        } else if (widthMode == View.MeasureSpec.AT_MOST) {
            width = Math.min(width, widthSize)
        }
        if (heightMode == View.MeasureSpec.EXACTLY) {
            height = heightSize
        } else if (heightMode == View.MeasureSpec.AT_MOST) {
            height = desiredHeight
        }

        lineLength = width - HORIZONTAL_PADDING * 2
        midY = height / 2
        lineStartX = HORIZONTAL_PADDING
        lineEndX = lineLength + HORIZONTAL_PADDING

        calculateConvertFactor()

        selectedMin = min
        selectedMax = max
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        drawEntireRangeLine(canvas)
        drawSelectedRangeLine(canvas)
        drawSelectedTargets(canvas)
    }

    private fun drawEntireRangeLine(canvas: Canvas) {
        paint.color = outsideRangeColor
        paint.strokeWidth = outsideRangeLineStrokeWidth
        canvas.drawLine(lineStartX.toFloat(), midY.toFloat(), lineEndX.toFloat(), midY.toFloat(), paint)
    }

    private fun drawSelectedRangeLine(canvas: Canvas) {
        paint.strokeWidth = insideRangeLineStrokeWidth
        paint.color = insideRangeColor
        canvas.drawLine(minPosition.toFloat(), midY.toFloat(), maxPosition.toFloat(), midY.toFloat(), paint)
    }

    private fun drawSelectedTargets(canvas: Canvas) {
        paint.color = targetColor
        canvas.drawCircle(minPosition.toFloat(), midY.toFloat(), minTargetRadius, paint)
        canvas.drawCircle(maxPosition.toFloat(), midY.toFloat(), maxTargetRadius, paint)
    }

    //user has touched outside the target, lets jump to that position
    private fun jumpToPosition(index: Int, event: MotionEvent) {
        if (event.getX(index) > maxPosition && event.getX(index) <= lineEndX) {
            maxPosition = event.getX(index).toInt()
            invalidate()
            callMaxChangedCallbacks()
        } else if (event.getX(index) < minPosition && event.getX(index) >= lineStartX) {
            minPosition = event.getX(index).toInt()
            invalidate()
            callMinChangedCallbacks()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled)
            return false

        val actionIndex = event.actionIndex
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> if (lastTouchedMin) {
                if (!checkTouchingMinTarget(actionIndex, event) && !checkTouchingMaxTarget(actionIndex, event)) {
                    jumpToPosition(actionIndex, event)
                }
            } else if (!checkTouchingMaxTarget(actionIndex, event) && !checkTouchingMinTarget(actionIndex, event)) {
                jumpToPosition(actionIndex, event)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                isTouchingMinTarget.remove(event.getPointerId(actionIndex))
                isTouchingMaxTarget.remove(event.getPointerId(actionIndex))
                if (isTouchingMinTarget.isEmpty()) {
                    minAnimator.cancel()
                    minAnimator = getMinTargetAnimator(false)
                    minAnimator.start()
                }
                if (isTouchingMaxTarget.isEmpty()) {
                    maxAnimator.cancel()
                    maxAnimator = getMaxTargetAnimator(false)
                    maxAnimator.start()
                }
                rangeSliderListener?.onEndAction(selectedMin, selectedMax)
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0..event.pointerCount - 1) {
                    if (isTouchingMinTarget.contains(event.getPointerId(i))) {
                        var touchX = event.getX(i).toInt()
                        touchX = clamp(touchX, lineStartX, lineEndX)
                        if (touchX >= maxPosition) {
                            maxPosition = touchX
                            callMaxChangedCallbacks()
                        }
                        minPosition = touchX
                        callMinChangedCallbacks()
                    }
                    if (isTouchingMaxTarget.contains(event.getPointerId(i))) {
                        var touchX = event.getX(i).toInt()
                        touchX = clamp(touchX, lineStartX, lineEndX)
                        if (touchX <= minPosition) {
                            minPosition = touchX
                            callMinChangedCallbacks()
                        }
                        maxPosition = touchX
                        callMaxChangedCallbacks()
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_POINTER_DOWN -> for (i in 0..event.pointerCount - 1) {
                if (lastTouchedMin) {
                    if (!checkTouchingMinTarget(i, event) && !checkTouchingMaxTarget(i, event)) {
                        jumpToPosition(i, event)
                    }
                } else if (!checkTouchingMaxTarget(i, event) && !checkTouchingMinTarget(i, event)) {
                    jumpToPosition(i, event)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                isTouchingMinTarget.clear()
                isTouchingMaxTarget.clear()
            }
        }

        return true
    }

    /**
     * Checks if given index is touching the min target.  If touching start animation.
     */
    private fun checkTouchingMinTarget(index: Int, event: MotionEvent): Boolean {
        if (isTouchingMinTarget(index, event)) {
            lastTouchedMin = true
            isTouchingMinTarget.add(event.getPointerId(index))
            if (!minAnimator.isRunning) {
                minAnimator = getMinTargetAnimator(true)
                minAnimator.start()
            }
            return true
        }
        return false
    }

    /**
     * Checks if given index is touching the max target.  If touching starts animation.
     */
    private fun checkTouchingMaxTarget(index: Int, event: MotionEvent): Boolean {
        if (isTouchingMaxTarget(index, event)) {
            lastTouchedMin = false
            isTouchingMaxTarget.add(event.getPointerId(index))
            if (!maxAnimator.isRunning) {
                maxAnimator = getMaxTargetAnimator(true)
                maxAnimator.start()
            }
            return true
        }
        return false
    }

    private fun callMinChangedCallbacks() {
        rangeSliderListener?.onLowerChanged(selectedMin)
    }

    private fun callMaxChangedCallbacks() {
        rangeSliderListener?.onUpperChanged(selectedMax)
    }

    private fun isTouchingMinTarget(pointerIndex: Int, event: MotionEvent): Boolean {
        return event.getX(pointerIndex) > minPosition - DEFAULT_TOUCH_TARGET_SIZE
                && event.getX(pointerIndex) < minPosition + DEFAULT_TOUCH_TARGET_SIZE
                && event.getY(pointerIndex) > midY - DEFAULT_TOUCH_TARGET_SIZE
                && event.getY(pointerIndex) < midY + DEFAULT_TOUCH_TARGET_SIZE
    }

    private fun isTouchingMaxTarget(pointerIndex: Int, event: MotionEvent): Boolean {
        return event.getX(pointerIndex) > maxPosition - DEFAULT_TOUCH_TARGET_SIZE
                && event.getX(pointerIndex) < maxPosition + DEFAULT_TOUCH_TARGET_SIZE
                && event.getY(pointerIndex) > midY - DEFAULT_TOUCH_TARGET_SIZE
                && event.getY(pointerIndex) < midY + DEFAULT_TOUCH_TARGET_SIZE
    }

    private fun calculateConvertFactor() {
        convertFactor = range.toFloat() / lineLength
    }

    var selectedMin: Int
        get() = Math.round((minPosition - lineStartX) * convertFactor + min)
        private set(selectedMin) {
            minPosition = Math.round((selectedMin - min) / convertFactor + lineStartX)
            callMinChangedCallbacks()
        }

    var selectedMax: Int
        get() = Math.round((maxPosition - lineStartX) * convertFactor + min)
        private set(selectedMax) {
            maxPosition = Math.round((selectedMax - min) / convertFactor + lineStartX)
            callMaxChangedCallbacks()
        }

    /**
     * Resets selected values to MIN and MAX.
     */
    fun reset() {
        minPosition = lineStartX
        maxPosition = lineEndX
        rangeSliderListener?.onLowerChanged(selectedMin)
        rangeSliderListener?.onUpperChanged(selectedMax)
        invalidate()
    }

    /**
     * Keeps Number value inside min/max bounds by returning min or max if outside of
     * bounds.  Otherwise will return the value without altering.
     */
    private fun <T : Number> clamp(value: T, min: T, max: T): T {
        if (value.toDouble() > max.toDouble()) {
            return max
        } else if (value.toDouble() < min.toDouble()) {
            return min
        }
        return value
    }
}