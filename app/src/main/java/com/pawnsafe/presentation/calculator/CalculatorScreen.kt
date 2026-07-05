package com.pawnsafe.presentation.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawnsafe.ui.theme.*

@Composable
fun CalculatorScreen() {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var waitingForOperand by remember { mutableStateOf(false) }

    fun inputDigit(digit: String) {
        if (waitingForOperand) {
            display = digit
            waitingForOperand = false
        } else {
            display = if (display == "0") digit else display + digit
        }
    }

    fun inputDecimal() {
        if (waitingForOperand) {
            display = "0."
            waitingForOperand = false
            return
        }
        if (!display.contains(".")) display += "."
    }

    fun calculate(): Double {
        val op2 = display.toDoubleOrNull() ?: 0.0
        val op1 = operand1 ?: 0.0
        return when (operator) {
            "+" -> op1 + op2
            "-" -> op1 - op2
            "×" -> op1 * op2
            "÷" -> if (op2 != 0.0) op1 / op2 else 0.0
            else -> op2
        }
    }

    fun inputOperator(op: String) {
        if (operand1 != null && !waitingForOperand) {
            val result = calculate()
            display = if (result == result.toLong().toDouble()) result.toLong().toString() else "%.8f".format(result).trimEnd('0').trimEnd('.')
            operand1 = result
        } else {
            operand1 = display.toDoubleOrNull()
        }
        operator = op
        waitingForOperand = true
    }

    fun equals() {
        if (operand1 == null || operator == null) return
        val result = calculate()
        display = if (result == result.toLong().toDouble()) result.toLong().toString() else "%.8f".format(result).trimEnd('0').trimEnd('.')
        operand1 = null
        operator = null
        waitingForOperand = false
    }

    fun clear() {
        display = "0"
        operand1 = null
        operator = null
        waitingForOperand = false
    }

    fun backspace() {
        if (display.length > 1) display = display.dropLast(1)
        else display = "0"
    }

    fun toggleSign() {
        val value = display.toDoubleOrNull() ?: return
        display = if (-value == (-value).toLong().toDouble()) (-value).toLong().toString() else (-value).toString()
    }

    fun percent() {
        val value = display.toDoubleOrNull() ?: return
        val result = value / 100.0
        display = if (result == result.toLong().toDouble()) result.toLong().toString() else "%.8f".format(result).trimEnd('0').trimEnd('.')
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPeach)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarmPeachCard)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text("Sri Nanjundeshwara Jewellers", fontSize = 11.sp, color = Terracotta, fontWeight = FontWeight.Medium)
            Text("Calculator", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }

        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (operator != null) {
                    Text(
                        text = "${operand1?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: ""} $operator",
                        fontSize = 16.sp,
                        color = TextMuted
                    )
                }
                Text(
                    text = display,
                    fontSize = if (display.length > 10) 32.sp else 48.sp,
                    fontWeight = FontWeight.Light,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcButton("AC", Modifier.weight(1f), backgroundColor = NeutralCard, textColor = TerracottaDark) { clear() }
                CalcButton("+/-", Modifier.weight(1f), backgroundColor = NeutralCard, textColor = TerracottaDark) { toggleSign() }
                CalcButton("%", Modifier.weight(1f), backgroundColor = NeutralCard, textColor = TerracottaDark) { percent() }
                CalcButton("÷", Modifier.weight(1f), backgroundColor = Terracotta, textColor = SurfaceWhite) { inputOperator("÷") }
            }
            // Row 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcButton("7", Modifier.weight(1f)) { inputDigit("7") }
                CalcButton("8", Modifier.weight(1f)) { inputDigit("8") }
                CalcButton("9", Modifier.weight(1f)) { inputDigit("9") }
                CalcButton("×", Modifier.weight(1f), backgroundColor = Terracotta, textColor = SurfaceWhite) { inputOperator("×") }
            }
            // Row 3
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcButton("4", Modifier.weight(1f)) { inputDigit("4") }
                CalcButton("5", Modifier.weight(1f)) { inputDigit("5") }
                CalcButton("6", Modifier.weight(1f)) { inputDigit("6") }
                CalcButton("-", Modifier.weight(1f), backgroundColor = Terracotta, textColor = SurfaceWhite) { inputOperator("-") }
            }
            // Row 4
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcButton("1", Modifier.weight(1f)) { inputDigit("1") }
                CalcButton("2", Modifier.weight(1f)) { inputDigit("2") }
                CalcButton("3", Modifier.weight(1f)) { inputDigit("3") }
                CalcButton("+", Modifier.weight(1f), backgroundColor = Terracotta, textColor = SurfaceWhite) { inputOperator("+") }
            }
            // Row 5
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcButton("⌫", Modifier.weight(1f), backgroundColor = NeutralCard, textColor = TerracottaDark) { backspace() }
                CalcButton("0", Modifier.weight(1f)) { inputDigit("0") }
                CalcButton(".", Modifier.weight(1f)) { inputDecimal() }
                CalcButton("=", Modifier.weight(1f), backgroundColor = TerracottaDark, textColor = SurfaceWhite) { equals() }
            }
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = SurfaceWhite,
    textColor: androidx.compose.ui.graphics.Color = TextPrimary,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}