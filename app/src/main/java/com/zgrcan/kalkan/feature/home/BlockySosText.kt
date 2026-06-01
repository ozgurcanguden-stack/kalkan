package com.zgrcan.kalkan.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val LETTER_S = arrayOf(
    "11111",
    "10001",
    "10000",
    "11110",
    "00001",
    "10001",
    "11111",
)

private val LETTER_O = arrayOf(
    "01110",
    "10001",
    "10001",
    "10001",
    "10001",
    "10001",
    "01110",
)

@Composable
fun BlockySosText(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    letterHeight: Dp = 52.dp,
    letterSpacing: Dp = 10.dp,
) {
    val letterWidth = letterHeight * 0.78f
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(letterSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BlockyLetter(pattern = LETTER_S, color = color, modifier = Modifier.size(letterWidth, letterHeight))
        BlockyLetter(pattern = LETTER_O, color = color, modifier = Modifier.size(letterWidth, letterHeight))
        BlockyLetter(pattern = LETTER_S, color = color, modifier = Modifier.size(letterWidth, letterHeight))
    }
}

@Composable
private fun BlockyLetter(
    pattern: Array<String>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val rows = pattern.size
        val cols = pattern.first().length
        val gapRatio = 0.12f
        val cellW = size.width / cols
        val cellH = size.height / rows
        val blockW = cellW * (1f - gapRatio)
        val blockH = cellH * (1f - gapRatio)
        val corner = minOf(blockW, blockH) * 0.08f

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (pattern[row][col] != '1') continue
                val left = col * cellW + (cellW - blockW) / 2f
                val top = row * cellH + (cellH - blockH) / 2f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(blockW, blockH),
                    cornerRadius = CornerRadius(corner, corner),
                )
            }
        }
    }
}
