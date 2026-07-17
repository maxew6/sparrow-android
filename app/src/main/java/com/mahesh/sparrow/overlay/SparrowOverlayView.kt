package com.mahesh.sparrow.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.mahesh.sparrow.R
import com.mahesh.sparrow.domain.model.SparrowAnimationState
import kotlin.math.sin
import kotlin.random.Random

/**
 * Renders Sparrow (an original, code-drawn bird — no imported artwork) plus
 * an optional speech bubble. Deliberately a plain [View] rather than a
 * [android.view.ViewGroup] wrapping Compose: a `ComposeView` inside a raw
 * `WindowManager` overlay needs its own manually-managed
 * `SavedStateRegistryOwner`/`ViewModelStoreOwner`/lifecycle, which is a lot
 * of extra failure surface for a window this simple.
 */
class SparrowOverlayView(context: Context) : View(context) {

    /** Diameter, in px, of the bird itself (excludes speech-bubble headroom). */
    var birdSizePx: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    var isDarkTheme: Boolean = false

    private var animationState: SparrowAnimationState = SparrowAnimationState.ENTERING
    private var reducedMotion: Boolean = false

    private var wingPhase = 0f
    private var bobPhase = 0f
    private var isBlinking = false

    private var speechText: String? = null
    private val hideBubbleHandler = Handler(Looper.getMainLooper())
    private val hideBubbleRunnable = Runnable { showSpeechBubble(null) }

    private val bodyPaint = colorPaint(R.color.sparrow_body_brown)
    private val bellyPaint = colorPaint(R.color.sparrow_belly_cream)
    private val wingPaint = colorPaint(R.color.sparrow_wing_brown)
    private val tailPaint = colorPaint(R.color.sparrow_tail_dark)
    private val beakPaint = colorPaint(R.color.sparrow_beak_orange)
    private val eyePaint = colorPaint(R.color.sparrow_eye_charcoal)

    private val bubbleBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bubbleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private var wingAnimator: ValueAnimator? = null
    private var bobAnimator: ValueAnimator? = null
    private var blinkRunnableScheduled = false

    private fun colorPaint(colorRes: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, colorRes)
        style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
        bubbleTextPaint.textSize = birdSizePx * 0.22f
    }

    fun applyMotionState(state: SparrowAnimationState, reducedMotion: Boolean) {
        this.animationState = state
        this.reducedMotion = reducedMotion
        updateAnimators()
    }

    fun showSpeechBubble(text: String?, durationMs: Long = 4000L) {
        hideBubbleHandler.removeCallbacks(hideBubbleRunnable)
        speechText = text
        invalidate()
        if (text != null) {
            hideBubbleHandler.postDelayed(hideBubbleRunnable, durationMs)
        }
    }

    /** A quick, non-looping bounce played on tap. */
    fun playTapHop() {
        if (reducedMotion) return
        ValueAnimator.ofFloat(0f, 1f, 0f).apply {
            duration = 260
            addUpdateListener {
                bobPhase = (it.animatedValue as Float) * 6f
                invalidate()
            }
        }.start()
    }

    private fun updateAnimators() {
        wingAnimator?.cancel()
        bobAnimator?.cancel()

        if (reducedMotion) {
            wingPhase = 0f
            bobPhase = 0f
            invalidate()
            return
        }

        when (animationState) {
            SparrowAnimationState.FLYING, SparrowAnimationState.ENTERING -> {
                wingAnimator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
                    duration = 450
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        wingPhase = it.animatedValue as Float
                        invalidate()
                    }
                }.also { it.start() }
            }
            SparrowAnimationState.IDLE -> {
                wingPhase = 0f
                bobAnimator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
                    duration = 2600
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        bobPhase = it.animatedValue as Float
                        invalidate()
                    }
                }.also { it.start() }
                scheduleBlinkIfNeeded()
            }
            else -> {
                wingPhase = 0f
                bobPhase = 0f
                invalidate()
            }
        }
    }

    private fun scheduleBlinkIfNeeded() {
        if (blinkRunnableScheduled) return
        blinkRunnableScheduled = true
        val delay = Random.nextLong(1800, 4200)
        hideBubbleHandler.postDelayed({
            blinkRunnableScheduled = false
            if (animationState == SparrowAnimationState.IDLE && !reducedMotion) {
                isBlinking = true
                invalidate()
                hideBubbleHandler.postDelayed({
                    isBlinking = false
                    invalidate()
                    scheduleBlinkIfNeeded()
                }, 120)
            }
        }, delay)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (birdSizePx <= 0) return

        val bubbleReserve = (height - birdSizePx).coerceAtLeast(0)
        val bobOffset = if (reducedMotion) 0f else sin(bobPhase) * birdSizePx * 0.03f
        val originX = width / 2f
        val originY = bubbleReserve + birdSizePx / 2f + bobOffset

        drawBird(canvas, originX, originY, birdSizePx.toFloat())

        val text = speechText
        if (text != null && bubbleReserve > 0) {
            drawSpeechBubble(canvas, text, originX, originY - birdSizePx / 2f, bubbleReserve.toFloat())
        }
    }

    private fun drawBird(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val scale = size / 108f
        fun sx(v: Float) = cx + (v - 54f) * scale
        fun sy(v: Float) = cy + (v - 60f) * scale

        val wingLift = if (!reducedMotion) sin(wingPhase) * 8f else 0f

        // Tail
        val tailPath = android.graphics.Path().apply {
            moveTo(sx(32f), sy(52f))
            lineTo(sx(16f), sy(46f))
            lineTo(sx(30f), sy(62f))
            close()
        }
        canvas.drawPath(tailPath, tailPaint)

        // Body
        canvas.drawCircle(sx(52f), sy(60f), 22f * scale, bodyPaint)

        // Wing (lifts during flight)
        canvas.drawCircle(sx(46f), sy(54f - wingLift), 11f * scale, wingPaint)

        // Belly
        canvas.drawCircle(sx(48f), sy(70f), 13f * scale, bellyPaint)

        // Beak
        val beakPath = android.graphics.Path().apply {
            moveTo(sx(72f), sy(58f))
            lineTo(sx(86f), sy(60f))
            lineTo(sx(72f), sy(66f))
            close()
        }
        canvas.drawPath(beakPath, beakPaint)

        // Eye (skips when blinking)
        if (!isBlinking) {
            canvas.drawCircle(sx(60f), sy(52f), 3f * scale, eyePaint)
        } else {
            val eyeLinePaint = Paint(eyePaint).apply { strokeWidth = 2f * scale }
            canvas.drawLine(sx(57f), sy(52f), sx(63f), sy(52f), eyeLinePaint)
        }
    }

    private fun drawSpeechBubble(canvas: Canvas, text: String, anchorX: Float, anchorBottom: Float, maxHeight: Float) {
        bubbleBgPaint.color = ContextCompat.getColor(
            context,
            if (isDarkTheme) R.color.bubble_dark_bg else R.color.bubble_light_bg
        )
        bubbleTextPaint.color = ContextCompat.getColor(
            context,
            if (isDarkTheme) R.color.bubble_dark_text else R.color.bubble_light_text
        )

        val padding = birdSizePx * 0.12f
        val bubbleWidth = (width - padding).coerceAtMost(width * 0.92f)
        val bubbleHeight = (maxHeight - padding).coerceAtLeast(birdSizePx * 0.5f)
        val left = anchorX - bubbleWidth / 2f
        val top = (anchorBottom - bubbleHeight - padding / 2f).coerceAtLeast(padding / 2f)
        val rect = RectF(left, top, left + bubbleWidth, top + bubbleHeight)

        canvas.drawRoundRect(rect, 24f, 24f, bubbleBgPaint)
        canvas.drawText(
            text,
            rect.centerX(),
            rect.centerY() - (bubbleTextPaint.ascent() + bubbleTextPaint.descent()) / 2f,
            bubbleTextPaint
        )
    }

    fun release() {
        hideBubbleHandler.removeCallbacksAndMessages(null)
        wingAnimator?.cancel()
        bobAnimator?.cancel()
    }
}
