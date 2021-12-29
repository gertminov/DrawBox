package io.ak1.drawbox

import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

/**
 * Created by akshay on 24/12/21
 * https://ak1.io
 */

fun setStrokeColor(color: Color) {
    strokeColor = color
}

fun setStrokeWidth(width: Float) {
    strokeWidth = width
}

fun unDo() {
    if (undoStack.isNotEmpty()) {
        val last = undoStack.last()
        redoStack.add(last)
        undoStack.remove(last)
        bitmap?.eraseColor(android.graphics.Color.TRANSPARENT)
    }
    (state as MutableStateFlow).tryEmit("${undoStack.size}")
}

fun reDo() {
    if (redoStack.isNotEmpty()) {
        val last = redoStack.last()
        undoStack.add(last)
        redoStack.remove(last)
        bitmap?.eraseColor(android.graphics.Color.TRANSPARENT)
    }
    (state as MutableStateFlow).tryEmit("${undoStack.size}")
}

fun reset() {
    redoStack.clear()
    undoStack.clear()
    bitmap?.eraseColor(android.graphics.Color.TRANSPARENT)
    (state as MutableStateFlow).tryEmit(UUID.randomUUID().toString())
}


fun getDrawBoxBitmap() = bitmap

internal fun DrawScope.drawSomePath(
    path: Path,
    color: Color = strokeColor,
    width: Float = strokeWidth
) = drawPath(
    path,
    color,
    style = Stroke(width, miter = 0f, join = StrokeJoin.Round, cap = StrokeCap.Round),
)

internal fun Canvas.drawSomePath(
    path: Path,
    color: Color = strokeColor,
    width: Float = strokeWidth
) = this.drawPath(path, Paint().apply {
    this.style = PaintingStyle.Stroke
    this.isAntiAlias = true
    this.color = color
    this.strokeJoin = StrokeJoin.Round
    this.strokeCap = StrokeCap.Round
    this.strokeWidth = width
})

internal fun MotionEvent.getRect() =
    Rect(this.x - 0.5f, this.y - 0.5f, this.x + 0.5f, this.y + 0.5f)

internal fun generateCanvas(size: IntSize): Canvas? = if (size.width > 0 && size.height > 0) {
    bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
    Canvas(bitmap!!.asImageBitmap())
} else null


//Model
data class PathWrapper(val path: Path, val strokeWidth: Float = 5f, val strokeColor: Color)