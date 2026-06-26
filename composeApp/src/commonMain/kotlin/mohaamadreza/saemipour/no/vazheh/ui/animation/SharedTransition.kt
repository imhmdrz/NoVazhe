package mohaamadreza.saemipour.no.vazheh.ui.animation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Plumbing for cross-screen shared-element transitions.
 *
 * [SharedTransitionScope] is provided once around the whole `NavHost` (see App.kt), while each
 * navigation destination that participates in a shared transition provides its own
 * [AnimatedVisibilityScope] (the `AnimatedContentScope` receiver of its `composable { }` block).
 *
 * Both are exposed as CompositionLocals so deeply-nested composables (e.g. a category tile inside
 * `ChildScreen`, and the category hero inside `GameScreen`) can opt in to a shared element with a
 * single modifier call, without threading the scopes through every parameter list.
 *
 * Destinations that don't provide the locals get a no-op modifier, so the helpers are safe to call
 * anywhere.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Marks this composable as a shared element identified by [key]. The element with the same [key]
 * on the previous/next screen morphs (position + size) into this one during the nav transition.
 * Use when both screens render the *same* content (e.g. the same category image).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedElementKey(key: String): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        this@sharedElementKey.sharedElement(
            rememberSharedContentState(key = key),
            visibilityScope,
        )
    }
}

/**
 * Like [sharedElementKey] but for a *container* whose contents differ between screens; the bounds
 * morph while the children cross-fade.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBoundsKey(key: String): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        this@sharedBoundsKey.sharedBounds(
            rememberSharedContentState(key = key),
            visibilityScope,
        )
    }
}
