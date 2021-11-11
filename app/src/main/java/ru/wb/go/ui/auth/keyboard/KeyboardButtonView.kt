package ru.wb.go.ui.auth.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import ru.wb.go.R

class KeyboardButtonView : AppCompatTextView {

    var customValue = "0"
    private var animationRadius = 0
    private var color = 0
    private var strokeWidth = 0f
    private var backgroundCircle = false
    private var circlePaint: Paint = Paint()
    private var animationPaint: Paint = Paint()

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        attrs?.let { initAttributes(it) }
        initUI()
        initComponents()
    }

    private fun initComponents() {
        initCirclePaint()
        initAnimationPaint()
    }

    private fun initCirclePaint() {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = strokeWidth
        circlePaint.isAntiAlias = true
        circlePaint.color = color
    }

    private fun initAnimationPaint() {
        animationPaint = Paint()
        animationPaint.style = Paint.Style.FILL
        animationPaint.strokeWidth = strokeWidth
        animationPaint.isAntiAlias = true
        animationPaint.color = ContextCompat.getColor(context, R.color.keyboard_ripple_color)
    }

    private fun initUI() {
        gravity = Gravity.CENTER
        animationRadius = height
    }

    private fun initAttributes(attrs: AttributeSet) {
        val attributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.KeyboardButtonView, 0, 0
        )
        try {
            setBackgroundCircle(
                attributes.getBoolean(
                    R.styleable.KeyboardButtonView_isBackgroundCircle,
                    false
                )
            )
            customValue = attributes.getString(R.styleable.KeyboardButtonView_value) ?: ""
            setStrokeWidth(
                attributes.getDimension(
                    R.styleable.KeyboardButtonView_strokeWidth,
                    0f
                )
            )
        } finally {
            attributes.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (backgroundCircle) {
            val halfHeight = (height / 2).toFloat()
            val radius = halfHeight - strokeWidth / 2
            canvas.drawCircle(halfHeight, halfHeight, radius, circlePaint)
        }
        drawBg(canvas)
    }

    fun startAnimation() {
        animationRadius = 0
        postInvalidateDelayed(1)
    }

    private fun drawBg(canvas: Canvas) {
        animationRadius += 10
        if (animationRadius < radius) {
            canvas.drawCircle(
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                animationRadius.toFloat(),
                animationPaint
            )
            postInvalidateDelayed(1)
        }
    }

    private val radius: Int
        get() = if (width > height) height / 2 else width / 2

    private fun setBackgroundCircle(backgroundCircle: Boolean) {
        this.backgroundCircle = backgroundCircle
    }

    fun setColor(color: Int) {
        this.color = color
        setTextColor(this.color)
    }

    private fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth
    }

}