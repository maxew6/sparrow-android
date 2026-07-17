package com.mahesh.sparrow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.mahesh.sparrow.ui.theme.SparrowBeakOrange
import com.mahesh.sparrow.ui.theme.SparrowBrown
import com.mahesh.sparrow.ui.theme.SparrowCream
import com.mahesh.sparrow.ui.theme.SparrowEyeCharcoal
import com.mahesh.sparrow.ui.theme.SparrowTailDark
import com.mahesh.sparrow.ui.theme.SparrowWingBrown

/**
 * A static rendering of Sparrow for onboarding, drawn with the same
 * proportions as [com.mahesh.sparrow.overlay.SparrowOverlayView] so the pet
 * looks consistent between setup and the floating overlay.
 */
@Composable
fun SparrowIllustration(modifier: Modifier = Modifier, sizeDp: Int = 120) {
    Canvas(modifier = modifier.size(sizeDp.dp)) {
        val scale = size.minDimension / 108f
        fun sx(v: Float) = (v - 54f) * scale + size.width / 2f
        fun sy(v: Float) = (v - 60f) * scale + size.height / 2f

        val tailPath = Path().apply {
            moveTo(sx(32f), sy(52f))
            lineTo(sx(16f), sy(46f))
            lineTo(sx(30f), sy(62f))
            close()
        }
        drawPath(tailPath, color = SparrowTailDark)

        drawCircle(color = SparrowBrown, radius = 22f * scale, center = Offset(sx(52f), sy(60f)))
        drawCircle(color = SparrowWingBrown, radius = 11f * scale, center = Offset(sx(46f), sy(54f)))
        drawCircle(color = SparrowCream, radius = 13f * scale, center = Offset(sx(48f), sy(70f)))

        val beakPath = Path().apply {
            moveTo(sx(72f), sy(58f))
            lineTo(sx(86f), sy(60f))
            lineTo(sx(72f), sy(66f))
            close()
        }
        drawPath(beakPath, color = SparrowBeakOrange)

        drawCircle(color = SparrowEyeCharcoal, radius = 3f * scale, center = Offset(sx(60f), sy(52f)))
    }
}
