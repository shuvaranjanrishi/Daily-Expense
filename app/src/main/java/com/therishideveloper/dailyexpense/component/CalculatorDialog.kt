package com.therishideveloper.dailyexpense.component

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.therishideveloper.dailyexpense.R
import com.therishideveloper.dailyexpense.ui.theme.tealColor
import com.therishideveloper.dailyexpense.util.NumberUtils
import java.util.Locale
import kotlin.math.roundToInt

private const val PREF_CALCULATOR = "calculator_prefs"
private const val KEY_CALC_SCALE = "saved_scale"
private const val DEFAULT_CALC_SCALE = 1.0f
private const val MIN_CALC_SCALE = 0.78f
private const val MAX_CALC_SCALE = 1.38f
private const val BASE_CALC_WIDTH_DP = 256f

private fun loadSavedCalcScale(context: Context): Float {
    val prefs = context.getSharedPreferences(PREF_CALCULATOR, Context.MODE_PRIVATE)
    return prefs.getFloat(KEY_CALC_SCALE, DEFAULT_CALC_SCALE)
        .coerceIn(MIN_CALC_SCALE, MAX_CALC_SCALE)
}

private fun saveCalcScale(context: Context, scale: Float) {
    context.getSharedPreferences(PREF_CALCULATOR, Context.MODE_PRIVATE)
        .edit()
        .putFloat(KEY_CALC_SCALE, scale.coerceIn(MIN_CALC_SCALE, MAX_CALC_SCALE))
        .apply()
}

private fun resetCalcScale(context: Context) {
    context.getSharedPreferences(PREF_CALCULATOR, Context.MODE_PRIVATE)
        .edit()
        .remove(KEY_CALC_SCALE)
        .apply()
}

@Composable
fun CalculatorDialog(
    initialExpression: String,
    initialResult: String,
    onMinimize: (String, String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val resizeDesc = stringResource(R.string.desc_resize_calculator)

    var expression by remember { mutableStateOf(initialExpression) }
    var result by remember { mutableStateOf(initialResult) }

    // Drag, Resize, and Scroll states
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var scale by rememberSaveable { mutableFloatStateOf(loadSavedCalcScale(context)) }
    var isDragging by remember { mutableStateOf(false) }
    var isResizing by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val handleMinimize = {
        saveCalcScale(context, scale)
        onMinimize(expression, result)
    }

    val handleClose = {
        resetCalcScale(context)
        onClose()
    }

    // Auto scroll to end when expression changes
    LaunchedEffect(expression) { scrollState.animateScrollTo(scrollState.maxValue) }

    // Reusable Button List Configuration
    val buttonRows = remember {
        listOf(
            listOf(
                CalcButtonData("7", CalcAction.Number("7")),
                CalcButtonData("8", CalcAction.Number("8")),
                CalcButtonData("9", CalcAction.Number("9")),
                CalcButtonData("/", CalcAction.Operation("/"))
            ),
            listOf(
                CalcButtonData("4", CalcAction.Number("4")),
                CalcButtonData("5", CalcAction.Number("5")),
                CalcButtonData("6", CalcAction.Number("6")),
                CalcButtonData("*", CalcAction.Operation("*"))
            ),
            listOf(
                CalcButtonData("1", CalcAction.Number("1")),
                CalcButtonData("2", CalcAction.Number("2")),
                CalcButtonData("3", CalcAction.Number("3")),
                CalcButtonData("-", CalcAction.Operation("-"))
            ),
            listOf(
                CalcButtonData(".", CalcAction.Decimal),
                CalcButtonData("0", CalcAction.Number("0")),
                CalcButtonData("Del", CalcAction.Delete),
                CalcButtonData("+", CalcAction.Operation("+"))
            ),
            listOf(
                CalcButtonData("C", CalcAction.Clear),
                CalcButtonData("=", CalcAction.Calculate, weight = 2f)
            )
        )
    }

    Dialog(
        onDismissRequest = handleMinimize,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val maxAllowedScale = remember(maxWidth) {
                (maxWidth / BASE_CALC_WIDTH_DP.dp).coerceIn(MIN_CALC_SCALE, MAX_CALC_SCALE)
            }
            val currentMaxScale by rememberUpdatedState(maxAllowedScale)
            val clampedScale = scale.coerceIn(MIN_CALC_SCALE, maxAllowedScale)
            val dialogWidth = BASE_CALC_WIDTH_DP.dp * clampedScale
            val baseWidthPx = with(density) { BASE_CALC_WIDTH_DP.dp.toPx() }
            val bodyScrollState = rememberScrollState()
            val idleGripColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)

            Surface(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .width(dialogWidth),
                shape = RoundedCornerShape((20f * clampedScale).dp),
                color = MaterialTheme.colorScheme.surface.copy(
                    alpha = if (isDragging || isResizing) 0.88f else 0.98f
                ),
                shadowElevation = 12.dp,
                border = if (isDragging || isResizing) {
                    BorderStroke(1.2.dp, tealColor.copy(0.65f))
                } else {
                    null
                }
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Drag Handle (outside verticalScroll so vertical dragging always moves the dialog)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("calculator_drag_handle")
                                .padding(
                                    top = (12f * clampedScale).dp,
                                    bottom = (10f * clampedScale).dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((36f * clampedScale).dp, 4.dp)
                                    .background(Color.LightGray.copy(0.55f), CircleShape)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(bodyScrollState, enabled = bodyScrollState.maxValue > 0)
                                .padding(
                                    start = (12f * clampedScale).dp,
                                    end = (12f * clampedScale).dp,
                                    bottom = (14f * clampedScale).dp
                                )
                        ) {
                            // Display Section
                            DisplayArea(expression, result, scrollState, clampedScale)

                            Spacer(modifier = Modifier.height((8f * clampedScale).dp))

                            // Buttons Grid
                            buttonRows.forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = (3f * clampedScale).dp),
                                    horizontalArrangement = Arrangement.spacedBy((6f * clampedScale).dp)
                                ) {
                                    row.forEach { btn ->
                                        CalculatorKey(
                                            data = btn,
                                            scale = clampedScale,
                                            modifier = Modifier.weight(btn.weight),
                                            onClick = {
                                                handleCalculatorAction(
                                                    btn.action,
                                                    expression
                                                ) { newExp, newRes ->
                                                    expression = newExp
                                                    if (newRes.isNotEmpty()) result = newRes
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height((10f * clampedScale).dp))

                            // Bottom Actions
                            DialogFooter(
                                scale = clampedScale,
                                onClose = handleClose,
                                onMinimize = handleMinimize
                            )
                        }
                    }

                    // Bottom-Right Corner Resize Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(48.dp)
                            .testTag("calculator_resize_handle")
                            .semantics { contentDescription = resizeDesc }
                            .pointerInput(baseWidthPx) {
                                detectDragGestures(
                                    onDragStart = { isResizing = true },
                                    onDragEnd = { isResizing = false },
                                    onDragCancel = { isResizing = false }
                                ) { change, dragAmount ->
                                    change.consume()
                                    val delta = (dragAmount.x + dragAmount.y) / (baseWidthPx * 1.35f)
                                    scale = (scale + delta).coerceIn(MIN_CALC_SCALE, currentMaxScale)
                                }
                            },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Canvas(
                            modifier = Modifier
                                .padding(end = 7.dp, bottom = 7.dp)
                                .size(14.dp)
                        ) {
                            val strokeWidth = 1.8.dp.toPx()
                            val gripColor = if (isResizing) tealColor else idleGripColor
                            drawLine(
                                color = gripColor,
                                start = Offset(size.width * 0.22f, size.height),
                                end = Offset(size.width, size.height * 0.22f),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = gripColor,
                                start = Offset(size.width * 0.56f, size.height),
                                end = Offset(size.width, size.height * 0.56f),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = gripColor,
                                start = Offset(size.width * 0.88f, size.height),
                                end = Offset(size.width, size.height * 0.88f),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun simpleEvaluate(expr: String): String {
    val sanitized = expr.trimEnd('+', '-', '*', '/', '.')
    if (sanitized.isEmpty()) return "0"

    return try {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        for (i in sanitized.indices) {
            val ch = sanitized[i]
            val isUnarySign = (ch == '-' || ch == '+') &&
                (i == 0 || sanitized[i - 1] in charArrayOf('+', '-', '*', '/'))
            if (ch in charArrayOf('+', '-', '*', '/') && !isUnarySign) {
                if (current.isNotEmpty()) {
                    tokens.add(current.toString())
                    current.clear()
                }
                tokens.add(ch.toString())
            } else {
                current.append(ch)
            }
        }
        if (current.isNotEmpty()) {
            tokens.add(current.toString())
        }
        if (tokens.isEmpty()) return "0"

        var total = tokens[0].toDoubleOrNull() ?: return "0"
        var index = 1
        while (index + 1 < tokens.size) {
            val op = tokens[index]
            val next = tokens[index + 1].toDoubleOrNull() ?: break
            when (op) {
                "+" -> total += next
                "-" -> total -= next
                "*" -> total *= next
                "/" -> {
                    if (next == 0.0) return "0"
                    total /= next
                }
            }
            index += 2
        }
        if (total.isNaN() || total.isInfinite()) return "0"
        if (total % 1.0 == 0.0) {
            total.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", total).trimEnd('0').trimEnd('.')
        }
    } catch (_: Exception) {
        "0"
    }
}

// Define calculator button types for better logic handling
sealed class CalcAction {
    data class Number(val value: String) : CalcAction()
    data class Operation(val op: String) : CalcAction()
    object Calculate : CalcAction()
    object Clear : CalcAction()
    object Delete : CalcAction()
    object Decimal : CalcAction()
}

// Data class for representing a Button UI
data class CalcButtonData(
    val label: String,
    val action: CalcAction,
    val weight: Float = 1f
)

// Centralized logic to handle expression updates
private fun handleCalculatorAction(
    action: CalcAction,
    currentExpr: String,
    onUpdate: (String, String) -> Unit
) {
    var newExpr = currentExpr
    var newResult = ""

    when (action) {
        is CalcAction.Clear -> {
            newExpr = ""
            newResult = "0"
        }

        is CalcAction.Delete -> {
            if (newExpr.isNotEmpty()) newExpr = newExpr.dropLast(1)
        }

        is CalcAction.Calculate -> {
            newResult = simpleEvaluate(newExpr)
        }

        is CalcAction.Decimal -> {
            val lastNumber = newExpr.takeLastWhile { it !in charArrayOf('+', '-', '*', '/') }
            if (!lastNumber.contains('.')) {
                newExpr += if (lastNumber.isEmpty()) "0." else "."
            }
        }

        is CalcAction.Number -> newExpr += action.value
        is CalcAction.Operation -> {
            val operators = charArrayOf('+', '-', '*', '/')
            if (newExpr.isEmpty()) {
                if (action.op == "-") newExpr = "-"
            } else if (newExpr == "-") {
                if (action.op == "+") newExpr = ""
            } else if (newExpr.last() in operators) {
                newExpr = newExpr.dropLast(1) + action.op
            } else {
                newExpr += action.op
            }
        }
    }
    onUpdate(newExpr, newResult)
}

@Composable
fun DisplayArea(
    expression: String,
    result: String,
    scrollState: ScrollState,
    scale: Float = 1.0f
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Text(
            text = NumberUtils.formatByLocale(context, expression).ifEmpty { "0" },
            fontSize = (14f * scale).sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.horizontalScroll(scrollState)
        )
        Text(
            text = NumberUtils.formatByLocale(context, result),
            fontSize = (26f * scale).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1
        )
    }
}

@Composable
fun CalculatorKey(
    data: CalcButtonData,
    modifier: Modifier,
    scale: Float = 1.0f,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val keyTealColor = Color(0xFF008080)
    val color = when (data.action) {
        is CalcAction.Calculate -> keyTealColor
        is CalcAction.Clear, is CalcAction.Delete -> Color(0xFFFF5252).copy(0.15f)
        is CalcAction.Operation -> keyTealColor.copy(0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (data.action is CalcAction.Calculate) Color.White
    else if (data.action is CalcAction.Clear || data.action is CalcAction.Delete) Color(0xFFFF5252)
    else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .height((38f * scale).dp)
            .clickable { onClick() },
        shape = RoundedCornerShape((10f * scale).dp),
        color = color
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                NumberUtils.formatByLocale(context, data.label),
                fontSize = (15f * scale).sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun DialogFooter(
    scale: Float = 1.0f,
    onClose: () -> Unit,
    onMinimize: () -> Unit
) {
    val fontSize = (12.5f * scale).coerceAtLeast(10.5f).sp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = (24f * scale).dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onClose,
            modifier = Modifier.testTag("calculator_close_button"),
            contentPadding = PaddingValues(
                horizontal = (10f * scale).dp,
                vertical = (4f * scale).dp
            )
        ) {
            Text(
                stringResource(R.string.btn_close),
                color = Color.Red.copy(0.75f),
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false
            )
        }
        Spacer(modifier = Modifier.width((6f * scale).dp))
        Button(
            onClick = onMinimize,
            modifier = Modifier.testTag("calculator_minimize_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008080)),
            contentPadding = PaddingValues(
                horizontal = (12f * scale).dp,
                vertical = (4f * scale).dp
            )
        ) {
            Text(
                stringResource(R.string.btn_minimize),
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}