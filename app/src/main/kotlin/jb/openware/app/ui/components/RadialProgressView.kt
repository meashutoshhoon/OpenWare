package jb.openware.app.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

class RadialProgressView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    View(context, attrs) {
    private val circleRect = RectF()
    private val decelerateInterpolator: DecelerateInterpolator
    private val accelerateInterpolator: AccelerateInterpolator
    private val progressPaint: Paint
    private var lastUpdateTime: Long = 0
    private var radOffset = 0f
    private var currentCircleLength = 0f
    private var risingCircleLength = false
    private var currentProgressTime = 0f
    private var useSelfAlpha = false
    private var drawingCircleLength = 0f
    private var progressColor: Int
    private var size: Int
    private var currentProgress = 0f
    private var progressAnimationStart = 0f
    private var progressTime = 0
    private var animatedProgress = 0f
    private var toCircle = false
    private var toCircleProgress = 0f
    private var noProgress = true

    init {
        size = dp(45f)
        progressColor = -0x948d88
        decelerateInterpolator = DecelerateInterpolator()
        accelerateInterpolator = AccelerateInterpolator()
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeCap = Paint.Cap.ROUND
        progressPaint.strokeWidth = dp(3f).toFloat()
        progressPaint.color = progressColor
    }

    fun setUseSelfAlpha(value: Boolean) {
        useSelfAlpha = value
    }

    override fun setAlpha(alpha: Float) {
        super.setAlpha(alpha)
        if (useSelfAlpha) {
            progressPaint.alpha = (alpha * 255).toInt()
        }
    }

    fun setNoProgress(value: Boolean) {
        noProgress = value
    }

    fun setProgress(value: Float) {
        currentProgress = value
        if (animatedProgress > value) {
            animatedProgress = value
        }
        progressAnimationStart = animatedProgress
        progressTime = 0
    }

    private fun updateAnimation() {
        val newTime = System.currentTimeMillis()
        var dt = newTime - lastUpdateTime
        if (dt > 17) {
            dt = 17
        }
        lastUpdateTime = newTime
        radOffset += 360 * dt / ROTATION_TIME
        val count = (radOffset / 360).toInt()
        radOffset -= (count * 360).toFloat()
        if (toCircle && toCircleProgress != 1f) {
            toCircleProgress += 16 / 220f
            if (toCircleProgress > 1f) {
                toCircleProgress = 1f
            }
        } else if (!toCircle && toCircleProgress != 0f) {
            toCircleProgress -= 16 / 400f
            if (toCircleProgress < 0) {
                toCircleProgress = 0f
            }
        }
        if (noProgress) {
            if (toCircleProgress == 0f) {
                currentProgressTime += dt.toFloat()
                if (currentProgressTime >= RISING_TIME) {
                    currentProgressTime = RISING_TIME
                }
                currentCircleLength = if (risingCircleLength) {
                    4 + 266 * accelerateInterpolator.getInterpolation(currentProgressTime / RISING_TIME)
                } else {
                    4 - 270 * (1.0f - decelerateInterpolator.getInterpolation(currentProgressTime / RISING_TIME))
                }
                if (currentProgressTime == RISING_TIME) {
                    if (risingCircleLength) {
                        radOffset += 270f
                        currentCircleLength = -266f
                    }
                    risingCircleLength = !risingCircleLength
                    currentProgressTime = 0f
                }
            } else {
                val old = currentCircleLength
                if (risingCircleLength) {
                    currentCircleLength =
                        4 + 266 * accelerateInterpolator.getInterpolation(currentProgressTime / RISING_TIME)
                    currentCircleLength += 360 * toCircleProgress
                    val dx = old - currentCircleLength
                    if (dx > 0) {
                        radOffset += old - currentCircleLength
                    }
                } else {
                    currentCircleLength = 4 - 270 * (1.0f - decelerateInterpolator.getInterpolation(
                        currentProgressTime / RISING_TIME
                    ))
                    currentCircleLength -= 364 * toCircleProgress
                    val dx = old - currentCircleLength
                    if (dx > 0) {
                        radOffset += old - currentCircleLength
                    }
                }
            }
        } else {
            val progressDiff = currentProgress - progressAnimationStart
            if (progressDiff > 0) {
                progressTime += dt.toInt()
                if (progressTime >= 200.0f) {
                    progressAnimationStart = currentProgress
                    animatedProgress = progressAnimationStart
                    progressTime = 0
                } else {
                    animatedProgress =
                        progressAnimationStart + progressDiff * decelerateInterpolator.getInterpolation(
                            progressTime / 200.0f
                        )
                }
            }
            currentCircleLength = 4f.coerceAtLeast(360 * animatedProgress)
        }
        invalidate()
    }

    fun setSize(value: Int) {
        size = value
        invalidate()
    }

    fun setStrokeWidth(value: Float) {
        progressPaint.strokeWidth = dp(value).toFloat()
    }

    fun setProgressColor(color: Int) {
        progressColor = color
        progressPaint.color = progressColor
    }

    fun toCircle(toCircle: Boolean, animated: Boolean) {
        this.toCircle = toCircle
        if (!animated) {
            toCircleProgress = if (toCircle) 1f else 0f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = dp(size.toFloat())
        val width = resolveSize(desiredSize, widthMeasureSpec)
        val height = resolveSize(desiredSize, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val x = (width - size) / 2
        val y = (height - size) / 2
        circleRect[x.toFloat(), y.toFloat(), (x + size).toFloat()] = (y + size).toFloat()
        canvas.drawArc(
            circleRect,
            radOffset,
            currentCircleLength.also { drawingCircleLength = it },
            false,
            progressPaint
        )
        updateAnimation()
    }

    private fun dp(px: Float): Int {
        return (context.resources.displayMetrics.density * px).toInt()
    }

    companion object {
        private const val ROTATION_TIME = 2000f
        private const val RISING_TIME = 500f
    }
}