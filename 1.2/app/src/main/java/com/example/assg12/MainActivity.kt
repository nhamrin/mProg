package com.example.assg12

import android.os.Bundle
import android.view.SoundEffectConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.graphics.shapes.Morph
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.graphics.shapes.toPath
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.platform.LocalView
import com.example.assg12.ui.theme.Assg12Theme

/**
 * The main activity class
 *
 * This class starts the program and draws polygon with 6 corners
 *
 * This program draws a polygon that morphs to another polygon when clicked on. Press and hold the
 * polygon to, temporarily, retain its new shape.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Assg12Theme {
                Polygon(6) //NOTE: At least 3
            }
        }
    }
}

/**
 * The morphing class
 *
 * This class morphs a polygon between two different set numbers of vertices
 */
class MorphPolygonShape(
    private val morph: Morph,
    private val percentage: Float
) : Shape {
    private val matrix = Matrix()
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) : Outline {
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)

        val path = morph.toPath(progress = percentage).asComposePath()
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

/**
 * The polygon function
 *
 * This function draws a polygon of with a specific amount of vertices. Currently, it is set to
 * remember the polygon with 6 vertices (set in the call in MainActivity) and a polygon with a
 * random amount (between 3 to 10) of vertices. This random amount is reset everytime the app is
 * launched.
 *
 * @param vert an integer passed from MainActivity that sets the vertices in the base polygon
 */
@Composable
fun Polygon(vert: Int) {
    var vertices = vert

    if (vertices < 3) {
        vertices = 3
    }

    val shapeA = remember {
        RoundedPolygon(
            vertices,
            rounding = CornerRounding(0.2f, smoothing = 0.1f)
        )
    }

    val shapeB = remember {
        var corner = (3..10).random()
        if (corner == vertices) corner += 1

        RoundedPolygon(
            corner,
            rounding = CornerRounding(0.2f, smoothing = 0.1f)
        )
    }

    val morph = remember {
        Morph(shapeA, shapeB)
    }

    val interactionSrc = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSrc.collectIsPressedAsState()

    val animatedProgress = animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        label = "progress",
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium) //Change this to modify the transition
    )

    val view = LocalView.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(400.dp)
                .padding(8.dp)
                .clip(MorphPolygonShape(morph, animatedProgress.value))
                .background(Color(0xFFDDFF94))
                .clickable(interactionSource = interactionSrc, indication = null) {
                    view.playSoundEffect(SoundEffectConstants.CLICK) //This should play a click sound but does not seem to work that well...
                }
        ) {
            Text("Click me!", //The text on the polygon
                fontSize = 30.sp,
                modifier = Modifier.align(Alignment.Center))
        }
    }
}
