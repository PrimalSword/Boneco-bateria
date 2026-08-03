package com.primalsword.voltinho.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import com.primalsword.voltinho.model.BatteryMood
import com.primalsword.voltinho.model.BatterySnapshot
import com.primalsword.voltinho.model.MascotKind
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Original, code-drawn mascot animation. No external artwork or animation license is required.
 */
class MascotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var snapshot: BatterySnapshot = BatterySnapshot()
        set(value) {
            field = value
            invalidate()
        }

    var mascotKind: MascotKind = MascotKind.PINGO
        set(value) {
            field = value
            invalidate()
        }

    var showPercentage: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val path = Path()
    private val rect = RectF()
    private val startedAt = SystemClock.uptimeMillis()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat().coerceAtLeast(1f)
        val h = height.toFloat().coerceAtLeast(1f)
        val phase = ((SystemClock.uptimeMillis() - startedAt) % 4_000L) / 4_000f * (Math.PI * 2).toFloat()
        val mood = snapshot.mood

        val bounce = when (mood) {
            BatteryMood.CELEBRATING -> abs(sin(phase * 2f)) * h * 0.08f
            BatteryMood.CHARGING -> sin(phase * 1.7f) * h * 0.03f
            BatteryMood.ENERGETIC -> sin(phase * 1.4f) * h * 0.035f
            BatteryMood.CONTENT -> sin(phase) * h * 0.018f
            BatteryMood.TIRED -> sin(phase * 0.55f) * h * 0.008f
            BatteryMood.CRITICAL -> sin(phase * 7f) * w * 0.018f
        }

        canvas.save()
        canvas.translate(if (mood == BatteryMood.CRITICAL) bounce else 0f, if (mood == BatteryMood.CRITICAL) 0f else -bounce)
        when (mascotKind) {
            MascotKind.PINGO -> drawPingo(canvas, w, h, phase, mood)
            MascotKind.BYTE -> drawByte(canvas, w, h, phase, mood)
            MascotKind.MIMO -> drawMimo(canvas, w, h, phase, mood)
        }
        drawEffects(canvas, w, h, phase, mood)
        if (showPercentage) drawPercentage(canvas, w, h)
        canvas.restore()

        postInvalidateOnAnimation()
    }

    private fun drawPingo(canvas: Canvas, w: Float, h: Float, phase: Float, mood: BatteryMood) {
        val bodyTop = h * 0.12f
        val bodyBottom = h * 0.72f
        val cx = w * 0.5f

        path.reset()
        path.moveTo(cx, bodyTop)
        path.cubicTo(w * 0.78f, h * 0.16f, w * 0.88f, h * 0.43f, w * 0.76f, bodyBottom)
        path.cubicTo(w * 0.68f, h * 0.86f, w * 0.32f, h * 0.86f, w * 0.24f, bodyBottom)
        path.cubicTo(w * 0.12f, h * 0.43f, w * 0.22f, h * 0.16f, cx, bodyTop)
        fillPaint.color = colorForMood(mood)
        fillPaint.style = Paint.Style.FILL
        canvas.drawPath(path, fillPaint)

        drawFace(canvas, w, h, phase, mood, eyeY = h * 0.42f, mouthY = h * 0.57f)

        strokePaint.color = Color.rgb(9, 17, 31)
        strokePaint.strokeWidth = w * 0.045f
        val armSwing = sin(phase * 2f) * h * 0.06f
        canvas.drawLine(w * 0.23f, h * 0.48f, w * 0.08f, h * 0.55f + armSwing, strokePaint)
        canvas.drawLine(w * 0.77f, h * 0.48f, w * 0.92f, h * 0.55f - armSwing, strokePaint)
    }

    private fun drawByte(canvas: Canvas, w: Float, h: Float, phase: Float, mood: BatteryMood) {
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = colorForMood(mood)
        rect.set(w * 0.18f, h * 0.18f, w * 0.82f, h * 0.72f)
        canvas.drawRoundRect(rect, w * 0.15f, w * 0.15f, fillPaint)

        strokePaint.color = Color.rgb(9, 17, 31)
        strokePaint.strokeWidth = w * 0.045f
        canvas.drawLine(w * 0.5f, h * 0.18f, w * 0.5f, h * 0.08f, strokePaint)
        fillPaint.color = Color.rgb(168, 255, 53)
        canvas.drawCircle(w * 0.5f, h * 0.07f, w * 0.055f, fillPaint)

        fillPaint.color = Color.rgb(9, 17, 31)
        rect.set(w * 0.27f, h * 0.31f, w * 0.73f, h * 0.56f)
        canvas.drawRoundRect(rect, w * 0.07f, w * 0.07f, fillPaint)

        drawFace(canvas, w, h, phase, mood, eyeY = h * 0.405f, mouthY = h * 0.50f, lightFace = true)

        strokePaint.strokeWidth = w * 0.05f
        val armSwing = cos(phase * 1.8f) * h * 0.05f
        canvas.drawLine(w * 0.18f, h * 0.48f, w * 0.06f, h * 0.55f + armSwing, strokePaint)
        canvas.drawLine(w * 0.82f, h * 0.48f, w * 0.94f, h * 0.55f - armSwing, strokePaint)
    }

    private fun drawMimo(canvas: Canvas, w: Float, h: Float, phase: Float, mood: BatteryMood) {
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = colorForMood(mood)

        path.reset()
        path.moveTo(w * 0.25f, h * 0.28f)
        path.lineTo(w * 0.31f, h * 0.09f)
        path.lineTo(w * 0.44f, h * 0.25f)
        path.lineTo(w * 0.56f, h * 0.25f)
        path.lineTo(w * 0.69f, h * 0.09f)
        path.lineTo(w * 0.75f, h * 0.28f)
        path.cubicTo(w * 0.88f, h * 0.38f, w * 0.85f, h * 0.72f, w * 0.66f, h * 0.78f)
        path.cubicTo(w * 0.57f, h * 0.84f, w * 0.43f, h * 0.84f, w * 0.34f, h * 0.78f)
        path.cubicTo(w * 0.15f, h * 0.72f, w * 0.12f, h * 0.38f, w * 0.25f, h * 0.28f)
        canvas.drawPath(path, fillPaint)

        drawFace(canvas, w, h, phase, mood, eyeY = h * 0.43f, mouthY = h * 0.60f)

        fillPaint.color = Color.rgb(9, 17, 31)
        path.reset()
        path.moveTo(w * 0.46f, h * 0.52f)
        path.lineTo(w * 0.54f, h * 0.52f)
        path.lineTo(w * 0.50f, h * 0.57f)
        path.close()
        canvas.drawPath(path, fillPaint)
    }

    private fun drawFace(
        canvas: Canvas,
        w: Float,
        h: Float,
        phase: Float,
        mood: BatteryMood,
        eyeY: Float,
        mouthY: Float,
        lightFace: Boolean = false,
    ) {
        val faceColor = if (lightFace) Color.rgb(168, 255, 53) else Color.rgb(9, 17, 31)
        fillPaint.color = faceColor
        fillPaint.style = Paint.Style.FILL

        val blink = if ((phase % (Math.PI * 2).toFloat()) > 5.75f) 0.15f else 1f
        val tiredScale = if (mood == BatteryMood.TIRED || mood == BatteryMood.CRITICAL) 0.32f else 1f
        val eyeHeight = h * 0.045f * blink * tiredScale
        canvas.drawOval(
            RectF(w * 0.35f - w * 0.035f, eyeY - eyeHeight, w * 0.35f + w * 0.035f, eyeY + eyeHeight),
            fillPaint,
        )
        canvas.drawOval(
            RectF(w * 0.65f - w * 0.035f, eyeY - eyeHeight, w * 0.65f + w * 0.035f, eyeY + eyeHeight),
            fillPaint,
        )

        strokePaint.color = faceColor
        strokePaint.strokeWidth = w * 0.035f
        path.reset()
        when (mood) {
            BatteryMood.CELEBRATING, BatteryMood.CHARGING, BatteryMood.ENERGETIC -> {
                path.moveTo(w * 0.39f, mouthY)
                path.quadTo(w * 0.50f, mouthY + h * 0.11f, w * 0.61f, mouthY)
            }
            BatteryMood.CONTENT -> {
                path.moveTo(w * 0.42f, mouthY)
                path.quadTo(w * 0.50f, mouthY + h * 0.05f, w * 0.58f, mouthY)
            }
            BatteryMood.TIRED -> {
                path.moveTo(w * 0.43f, mouthY)
                path.lineTo(w * 0.57f, mouthY)
            }
            BatteryMood.CRITICAL -> {
                path.moveTo(w * 0.40f, mouthY + h * 0.05f)
                path.quadTo(w * 0.50f, mouthY - h * 0.06f, w * 0.60f, mouthY + h * 0.05f)
            }
        }
        canvas.drawPath(path, strokePaint)
    }

    private fun drawEffects(canvas: Canvas, w: Float, h: Float, phase: Float, mood: BatteryMood) {
        when (mood) {
            BatteryMood.CHARGING -> {
                fillPaint.color = Color.rgb(255, 222, 68)
                path.reset()
                val shift = sin(phase * 2f) * h * 0.03f
                path.moveTo(w * 0.78f, h * 0.05f + shift)
                path.lineTo(w * 0.65f, h * 0.25f + shift)
                path.lineTo(w * 0.76f, h * 0.25f + shift)
                path.lineTo(w * 0.64f, h * 0.43f + shift)
                path.lineTo(w * 0.90f, h * 0.18f + shift)
                path.lineTo(w * 0.78f, h * 0.18f + shift)
                path.close()
                canvas.drawPath(path, fillPaint)
            }
            BatteryMood.CELEBRATING -> {
                val colors = intArrayOf(Color.rgb(168, 255, 53), Color.rgb(255, 222, 68), Color.rgb(103, 198, 255))
                repeat(6) { index ->
                    val angle = phase * 1.4f + index * (Math.PI.toFloat() / 3f)
                    fillPaint.color = colors[index % colors.size]
                    canvas.drawCircle(
                        w * 0.5f + cos(angle) * w * 0.43f,
                        h * 0.37f + sin(angle) * h * 0.25f,
                        w * 0.025f,
                        fillPaint,
                    )
                }
            }
            BatteryMood.TIRED -> {
                textPaint.color = Color.argb(190, 9, 17, 31)
                textPaint.textSize = w * 0.16f
                canvas.drawText("z", w * 0.83f, h * (0.24f - sin(phase) * 0.04f), textPaint)
            }
            BatteryMood.CRITICAL -> {
                fillPaint.color = Color.rgb(255, 105, 120)
                val sweatY = h * (0.28f + abs(sin(phase)) * 0.08f)
                canvas.drawOval(RectF(w * 0.78f, sweatY, w * 0.84f, sweatY + h * 0.11f), fillPaint)
            }
            else -> Unit
        }
    }

    private fun drawPercentage(canvas: Canvas, w: Float, h: Float) {
        fillPaint.color = Color.argb(225, 9, 17, 31)
        rect.set(w * 0.22f, h * 0.78f, w * 0.78f, h * 0.99f)
        canvas.drawRoundRect(rect, h * 0.1f, h * 0.1f, fillPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = w * 0.18f
        val baseline = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText("${snapshot.level}%", rect.centerX(), baseline, textPaint)
    }

    private fun colorForMood(mood: BatteryMood): Int = when (mood) {
        BatteryMood.CELEBRATING -> Color.rgb(168, 255, 53)
        BatteryMood.CHARGING -> Color.rgb(103, 198, 255)
        BatteryMood.ENERGETIC -> Color.rgb(168, 255, 53)
        BatteryMood.CONTENT -> Color.rgb(255, 222, 68)
        BatteryMood.TIRED -> Color.rgb(255, 174, 84)
        BatteryMood.CRITICAL -> Color.rgb(255, 105, 120)
    }
}
