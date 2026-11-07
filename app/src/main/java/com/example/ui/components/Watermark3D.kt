package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 3D 50% transparent watermark displaying "RazifApps®"
 * Engineered with 3D embossed/extruded shadow layers and 50% alpha transparency.
 */
@Composable
fun Watermark3D(
    modifier: Modifier = Modifier,
    text: String = "RazifApps®"
) {
    Box(
        modifier = modifier
            .testTag("watermark_3d_razifapps")
            .alpha(0.50f) // Exact 50% transparent watermark requirement
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x33000000),
                        Color(0x1AFFFFFF),
                        Color(0x33000000)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x66FFFFFF),
                        Color(0x1A000000)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        // Deep 3D bottom extrusion shadow
        Text(
            text = text,
            color = Color(0xCC000000),
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.SansSerif,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(3f, 3f),
                    blurRadius = 4f
                )
            )
        )

        // Middle 3D bevel layer
        Text(
            text = text,
            color = Color(0x8800E5FF), // Subtle neon cyan glow
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.SansSerif,
                shadow = Shadow(
                    color = Color(0xFF00E5FF),
                    offset = Offset(1.5f, 1.5f),
                    blurRadius = 2f
                )
            )
        )

        // Foreground 3D embossed highlight text
        Text(
            text = text,
            color = Color(0xFFFFFFFF),
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.SansSerif,
                shadow = Shadow(
                    color = Color(0xAAFFFFFF),
                    offset = Offset(-1f, -1f),
                    blurRadius = 1f
                )
            )
        )
    }
}
