package com.kalkan.app.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class TurkishPhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            digits.forEachIndexed { i, c ->
                append(c)
                if (i == 2 || i == 5 || i == 7) append(' ')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 2 -> offset
                offset <= 5 -> offset + 1
                offset <= 7 -> offset + 2
                offset <= 10 -> offset + 3
                else -> formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 3 -> offset
                offset <= 7 -> offset - 1
                offset <= 10 -> offset - 2
                offset <= 14 -> offset - 3
                else -> digits.length
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
