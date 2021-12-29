package io.ak1.drawbox

import android.graphics.Bitmap
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by akshay on 10/12/21
 * https://ak1.io
 */


internal var strokeWidth = 5f
internal var strokeColor = Color.Red

// TODO: 25/12/21 convert datatype to Stack
//  currently Stack is internal in 'androidx.compose.runtime'

internal val undoStack = ArrayList<PathWrapper>()
internal val redoStack = ArrayList<PathWrapper>()
internal var bitmap: Bitmap? = null

val state: StateFlow<String> = MutableStateFlow("")

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawBox(
    modifier: Modifier = Modifier.fillMaxSize(),
    trackHistory: (undoCount: Int, redoCount: Int) -> Unit = { _, _ -> }
) {
    val refreshState = UUID.randomUUID().toString()
    var size = remember { mutableStateOf(IntSize.Zero) }
    var path = Path()
    val action: MutableState<Any?> = remember { mutableStateOf(null) }
    var imageBitmapCanvas: Canvas? = null

    LaunchedEffect(refreshState) {
        imageBitmapCanvas = generateCanvas(size.value)
        action.value = UUID.randomUUID().toString()
        state.collect {
            action.value = it
            trackHistory(undoStack.size, redoStack.size)
        }
    }

    Canvas(modifier = modifier
        .pointerInteropFilter {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(it.x, it.y)
                    path.addOval(it.getRect())
                }
                MotionEvent.ACTION_MOVE -> path.lineTo(it.x, it.y)
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    redoStack.clear()
                    undoStack.add(PathWrapper(path, strokeWidth, strokeColor))
                    trackHistory(undoStack.size, redoStack.size)
                    path = Path()
                }
                else -> false
            }
            action.value = "${it.x},${it.y}"
            true
        }
        .onSizeChanged {
            size.value = it
        }) {
        bitmap?.let { bitmap ->
            action.value?.let {
                undoStack.forEach {
                    imageBitmapCanvas?.drawSomePath(
                        path = it.path,
                        color = it.strokeColor,
                        width = it.strokeWidth
                    )
                }
                imageBitmapCanvas?.drawSomePath(path = path)
            }
            this.drawIntoCanvas {
                it.nativeCanvas.drawBitmap(bitmap, 0f, 0f, null)
            }
        }
    }
}




