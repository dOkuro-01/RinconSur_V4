package cl.duoc.rinconsur.view.utils



import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavBackStackEntry

 









@Composable
fun AnimacionEntradaFormulario(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 },
        content = content
    )
}





@Composable
fun Modifier.pulsoYRotacion(): Modifier {
    val transition = rememberInfiniteTransition(label = "pulsoYRotacion")
    val scale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val rotation by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "rotation"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationZ = rotation
    }
}




private const val DURACION_NAV = 400

 

@Composable
fun EstadoProbadorAnimado(estado: String) {
    val color by animateColorAsState(
        targetValue = colorEstado(estado), animationSpec = tween(650), label = "colorProbador")
    AnimatedContent(
        targetState = estado,
        transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
        label = "estadoProbador"
    ) { estadoVisible ->
        Text(estadoVisible, color = color,
            modifier = Modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

val navEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION_NAV)) +
        fadeIn(tween(DURACION_NAV))
}

val navExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION_NAV)) +
        fadeOut(tween(DURACION_NAV))
}

val navPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION_NAV)) +
        fadeIn(tween(DURACION_NAV))
}

val navPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION_NAV)) +
        fadeOut(tween(DURACION_NAV))
}
