package ru.wb.go.ui.scanner

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import me.dm7.barcodescanner.core.CameraWrapper
import me.dm7.barcodescanner.core.IViewFinder
import me.dm7.barcodescanner.core.ViewFinderView
import me.dm7.barcodescanner.zxing.ZXingScannerView


open class ZXingScannerZoomView : ZXingScannerView {

    private var cameraCompleteListener: ZXingScannerComplete? = null

    interface ZXingScannerComplete {
        fun onComplete()
    }

    private var cameraWrapper: CameraWrapper? = null
    private var customViewFinderView: CustomViewFinderView? = null

    constructor(context: Context?) : super(context)

    override fun createViewFinderView(context: Context): IViewFinder {
        customViewFinderView = CustomViewFinderView(context)
        return customViewFinderView as CustomViewFinderView
    }

    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)

    fun setCameraCompleteListener(cameraCompleteListener: ZXingScannerComplete?) {
        this.cameraCompleteListener = cameraCompleteListener
    }

    fun setFlashLoaderEnabled() {
        customViewFinderView?.flashLoader()
    }

    fun setFlashLoaderComplete() {
        customViewFinderView?.flashLoaderComplete()
    }

    val maxZoom: Int
        get() {
            var maxZoom = 0
            if (cameraWrapper != null) {
                val parameters = cameraWrapper!!.mCamera.parameters
                maxZoom = parameters.maxZoom
            }
            return maxZoom
        }

    fun zoom(zoom: Int) {
        if (cameraWrapper != null) {
            val parameters = cameraWrapper!!.mCamera.parameters
            val maxZoom = parameters.maxZoom
            if (parameters.isZoomSupported) {
                if (zoom >= 0 && zoom < maxZoom) {
                    parameters.zoom = zoom
                } else {
                    parameters.zoom = maxZoom / 2
                }
                cameraWrapper!!.mCamera.parameters = parameters
            }
        }
    }

    override fun setupCameraPreview(cameraWrapper: CameraWrapper) {
        super.setupCameraPreview(cameraWrapper)
        this.cameraWrapper = cameraWrapper
        if (cameraCompleteListener != null) {
            cameraCompleteListener!!.onComplete()
        }
    }

    private class CustomViewFinderView : ViewFinderView {
        val path = Path()
        var isFlash: Boolean = false
        val paintBorderLoader = Paint()

        constructor(context: Context?) : super(context) {
            init()
        }

        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        private fun init() {
        }

        fun flashLoader() {
            isFlash = true
            invalidate()
        }

        fun flashLoaderComplete() {
            isFlash = false
            invalidate()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun drawViewFinderMask(canvas: Canvas) {
            canvas.save()
            val rectPath = Path()
            rectPath.addRoundRect(
                RectF(
                    framingRect.left.toFloat(),
                    framingRect.top.toFloat(),
                    framingRect.right.toFloat(),
                    framingRect.bottom.toFloat()
                ), CORNER_RADIUS, CORNER_RADIUS, Path.Direction.CCW
            )
            canvas.clipOutPath(rectPath)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mFinderMaskPaint)
            canvas.restore()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setFlash(canvas: Canvas) {

            paintBorderLoader.color = Color.argb(0.8f, 1f, 1f, 1f)
            paintBorderLoader.style = Paint.Style.FILL
            paintBorderLoader.strokeWidth = 5f
            paintBorderLoader.isAntiAlias = true

            paintBorderLoader.pathEffect = CornerPathEffect(CORNER_RADIUS)

            path.moveTo((width / 2).toFloat(), framingRect.top.toFloat())
            path.lineTo(framingRect.right.toFloat(), framingRect.top.toFloat())
            path.lineTo(framingRect.right.toFloat(), framingRect.bottom.toFloat())
            path.lineTo(framingRect.left.toFloat(), framingRect.bottom.toFloat())
            path.lineTo(framingRect.left.toFloat(), framingRect.top.toFloat())
            path.lineTo((width / 2).toFloat(), framingRect.top.toFloat())
            canvas.drawPath(path, paintBorderLoader)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (isFlash) setFlash(canvas)
        }

        companion object {
            const val CORNER_RADIUS = 40f
        }
    }

}