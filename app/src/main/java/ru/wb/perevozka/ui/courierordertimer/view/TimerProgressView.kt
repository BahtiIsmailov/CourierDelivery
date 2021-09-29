package ru.wb.perevozka.ui.courierordertimer.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.FloatRange
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.wb.perevozka.R
import java.util.concurrent.TimeUnit

class TimerProgressView : View {
    private var canvas: Canvas? = null
    private val pCenter = Point(0, 0)
    private val rectBox = RectF()
    private val rectScale = RectF()
    private var currentAngle = 0

    @FloatRange(from = MIN_PROGRESS_SCALE.toDouble(), to = MAX_PROGRESS_SCALE.toDouble())
    private var progress = DEFAULT_PROGRESS_VALUE
    private var progressWidth = DEFAULT_STROKE_WIDTH_SCALE
    private var isDividerScale = DEFAULT_IS_DIVIDER_SCALE

    private var scaleCurrentColor = DEFAULT_SCALE_CURRENT_COLOR
    private var scaleWaitColor = DEFAULT_SCALE_WAIT_COLOR
    private var backgroundColor1 = DEFAULT_PROGRESS_BACKGROUND_COLOR
    private var foregroundColor = DEFAULT_PROGRESS_FOREGROUND_COLOR
    private var displayMetrics: DisplayMetrics? = null
    private var timer: Disposable? = null

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        initAttributes(attrs)
        initFields()
        recalculate()
    }

    private fun initAttributes(attrs: AttributeSet?) {
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.TimerProgressView)
            try {
                val styleProgress = array.getFloat(
                    R.styleable.TimerProgressView_ap_progress,
                    DEFAULT_PROGRESS_VALUE
                )
                progress = Math.min(styleProgress, MAX_ANGLE_SCALE.toFloat())
                progressWidth = array.getInteger(
                    R.styleable.TimerProgressView_ap_progress_width,
                    DEFAULT_STROKE_WIDTH_SCALE
                )

                isDividerScale = array.getBoolean(
                    R.styleable.TimerProgressView_ap_is_divider,
                    DEFAULT_IS_DIVIDER_SCALE
                )

                scaleCurrentColor = array.getColor(
                    R.styleable.TimerProgressView_ap_progress_scale_current_color,
                    DEFAULT_SCALE_CURRENT_COLOR
                )
                scaleWaitColor = array.getColor(
                    R.styleable.TimerProgressView_ap_progress_scale_wait_color,
                    DEFAULT_SCALE_WAIT_COLOR
                )
                backgroundColor1 = array.getColor(
                    R.styleable.TimerProgressView_ap_progress_background_color,
                    DEFAULT_PROGRESS_BACKGROUND_COLOR
                )
                foregroundColor = array.getColor(
                    R.styleable.TimerProgressView_ap_progress_foreground_color,
                    DEFAULT_PROGRESS_FOREGROUND_COLOR
                )
            } finally {
                array.recycle()
            }
        }
    }

    private fun initFields() {
        displayMetrics = context.resources.displayMetrics
    }

    private fun recalculate() {
        calculateAnimationAngle(progress)
    }

    private fun calculateRect() {}

    private fun calculateAnimationAngle(animationProgress: Float) {
        val factor = MAX_ANGLE_SCALE.toFloat() / MAX_PROGRESS_SCALE
        currentAngle = (factor * animationProgress).toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        var h = h
        h = w / 2
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = measuredWidth
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        initParam(canvas)
        drawDivider()
        drawArc(currentAngle)
    }

    private fun drawDivider() {
        if (!isDividerScale) return
        val scaleWidth = dpToPx(progressWidth).toFloat() / 2
        val backgroundPaint = Paint()
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.isAntiAlias = true
        backgroundPaint.strokeCap = Paint.Cap.ROUND
        backgroundPaint.color = scaleWaitColor
        backgroundPaint.strokeWidth = scaleWidth
        val pRadiusScale = radiusBox * RADIUS_SCALE
        canvas!!.drawLine(
            pCenter.x.toFloat() - pRadiusScale,
            pCenter.y.toFloat(),
            pCenter.x.toFloat() + pRadiusScale,
            pCenter.y.toFloat(),
            backgroundPaint
        )
    }

    private fun initParam(canvas: Canvas) {
        this.canvas = canvas
        val width = width
        val height = height
        rectBox[0f, 0f, width.toFloat()] = height.toFloat()
        pCenter[width / 2] = height / 2
    }

    private fun drawArc(currentAngle: Int) {
        val pRadiusScale = radiusBox * RADIUS_SCALE
        val scaleWidth = dpToPx(progressWidth).toFloat()
        val backgroundPaint = Paint()
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.isAntiAlias = true
        backgroundPaint.strokeCap = Paint.Cap.ROUND
        backgroundPaint.color = scaleWaitColor
        backgroundPaint.strokeWidth = scaleWidth
        val foregroundPaint = Paint()
        foregroundPaint.style = Paint.Style.STROKE
        foregroundPaint.isAntiAlias = true
        foregroundPaint.strokeCap = Paint.Cap.ROUND
        foregroundPaint.color = scaleCurrentColor
        foregroundPaint.strokeWidth = scaleWidth
        rectScale[pCenter.x - pRadiusScale, pCenter.y - pRadiusScale, pCenter.x + pRadiusScale] =
            pCenter.y + pRadiusScale
        canvas!!.drawArc(
            rectScale,
            START_ARC_ANGLE.toFloat(),
            MAX_ANGLE_SCALE.toFloat(),
            false,
            backgroundPaint
        )
        canvas!!.drawArc(
            rectScale,
            START_ARC_ANGLE.toFloat(),
            currentAngle.toFloat(),
            false,
            foregroundPaint
        )
    }

    private val radiusBox: Float
        private get() = Math.min(rectBox.height(), rectBox.width()) / 2

    @FloatRange(from = MIN_PROGRESS_SCALE.toDouble(), to = MAX_PROGRESS_SCALE.toDouble())
    fun getProgress(): Float {
        return progress
    }

    fun setProgress(
        @FloatRange(
            from = MIN_PROGRESS_SCALE.toDouble(),
            to = MAX_PROGRESS_SCALE.toDouble()
        ) progress: Float
    ) {
        this.progress = progress
        recalculate()
        invalidate()
    }

    fun animateProgress(
        @FloatRange(
            from = MIN_PROGRESS_SCALE.toDouble(),
            to = MAX_PROGRESS_SCALE.toDouble()
        ) progress: Float
    ) {
        calculateRect()
        this.progress = progress
        clearTimer()
        startTimer()
    }

    private fun clearTimer() {
        if (timer != null) {
            timer!!.dispose()
        }
    }

    private fun startTimer() {
        val frameCount = frameCount(progress)
        val stepArcProgress = stepArcProgress(progress, frameCount)
        timer = Flowable.interval(
            FRAMES_PER_SECOND.toLong(),
            TimeUnit.MILLISECONDS,
            AndroidSchedulers.mainThread()
        )
            .take(frameCount.toLong())
            .map { frame: Long -> frame + 1 }
            .subscribe { frame: Long -> animateArc(frameCount, stepArcProgress, frame.toInt()) }
    }

    private fun frameCount(progress: Float): Int {
        val count = (progress * FRAME_FACTOR).toInt()
        return if (count == 0) 1 else count
    }

    private fun stepArcProgress(progress: Float, frameCount: Int): Int {
        return if (frameCount == 0) 0 else progress.toInt() / frameCount
    }

    private fun animateArc(frameCount: Int, stepArcProgress: Int, frame: Int) {
        val animationProgress = stepArcProgress * frame
        val angle: Float = if (frame >= frameCount) progress else animationProgress.toFloat()
        calculateAnimationAngle(angle)
        invalidate()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * displayMetrics!!.density + 0.5).toInt()
    }

    companion object {
        private const val DEFAULT_SCALE_CURRENT_COLOR = -0x98c549
        private const val DEFAULT_SCALE_WAIT_COLOR = -0x191c15
        private const val DEFAULT_PROGRESS_BACKGROUND_COLOR = -0x1f221c
        private const val DEFAULT_PROGRESS_FOREGROUND_COLOR = -0x9bd35d
        private const val FRAMES_PER_SECOND = 20
        private const val FRAME_FACTOR = 0.20f
        private const val MAX_ANGLE_SCALE = 360
        private const val MIN_PROGRESS_SCALE = 0f
        private const val MAX_PROGRESS_SCALE = 100f
        private const val DEFAULT_PROGRESS_VALUE = 0f
        private const val START_ARC_ANGLE = -90

        private const val DEFAULT_STROKE_WIDTH_SCALE = 5
        private const val DEFAULT_IS_DIVIDER_SCALE = false
        private const val RADIUS_SCALE = 0.95f
    }
}