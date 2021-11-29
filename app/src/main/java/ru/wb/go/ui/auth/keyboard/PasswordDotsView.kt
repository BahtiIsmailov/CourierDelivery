package ru.wb.go.ui.auth.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import ru.wb.go.R

class PasswordDotsView : AppCompatEditText {
    private var backgroundColor = 0
    private var color = 0
    private var count = 0
    private var paint: Paint? = null
    private var strokeWidth = 0f
    private var dotSize = 0f

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        paint = Paint()
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DotPasswordView,
            0, 0
        )
        try {
            setCount(
                attributes.getInt(
                    R.styleable.DotPasswordView_dotsPasswordCount,
                    NUMBER_OF_CIRCLES
                )
            )
            setBackgroundColor(
                attributes.getInt(
                    R.styleable.DotPasswordView_dotsPasswordBackgroundColor, ContextCompat.getColor(
                        context, R.color.keyboard_dot_background
                    )
                )
            )
            setColor(
                attributes.getInt(
                    R.styleable.DotPasswordView_dotsPasswordColor, ContextCompat.getColor(
                        context, R.color.keyboard_dot_select
                    )
                )
            )
            setStrokeWidth(
                attributes.getDimensionPixelSize(
                    R.styleable.DotPasswordView_dotsStrokeWidth,
                    1
                ).toFloat()
            )
            setDotSize(
                attributes.getDimensionPixelSize(R.styleable.DotPasswordView_dotsSize, 7).toFloat()
            )
        } finally {
            attributes.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDots(canvas)
    }

    private fun drawDots(canvas: Canvas) {
        val radius =
            (dotSize / 2).toInt()
        for (i in 0 until count) {
            drawBackgroundCircle(canvas, i, radius)
            drawCircle(canvas, i, radius)
        }
    }

    private fun drawBackgroundCircle(canvas: Canvas, position: Int, radius: Int) {
        val startX = width / count
        paint!!.style = Paint.Style.FILL
        paint!!.color = backgroundColor
        paint!!.isAntiAlias = true
        canvas.drawCircle(
            (position * startX + startX / 2).toFloat(),
            (height / 2).toFloat(),
            radius.toFloat(),
            paint!!
        )
    }

    private fun drawCircle(canvas: Canvas, position: Int, radius: Int) {
        val startX = width / count
        setFillStyle(position)
        paint!!.color = color
        paint!!.strokeWidth = strokeWidth
        paint!!.isAntiAlias = true
        canvas.drawCircle(
            (position * startX + startX / 2).toFloat(),
            (height / 2).toFloat(),
            radius.toFloat(),
            paint!!
        )
    }

    private fun setFillStyle(position: Int) {
        if (position < text!!.length) {
            paint!!.style = Paint.Style.FILL
        } else {
            paint!!.style = Paint.Style.STROKE
        }
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
    }

    fun setColor(color: Int) {
        this.color = color
    }

    fun setCount(count: Int) {
        this.count = count
    }

    fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth
    }

    fun setDotSize(dotSize: Float) {
        this.dotSize = dotSize
    }

    companion object {
        const val NUMBER_OF_CIRCLES = 4
    }

}