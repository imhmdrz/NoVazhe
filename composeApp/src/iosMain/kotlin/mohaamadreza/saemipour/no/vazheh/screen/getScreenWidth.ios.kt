package mohaamadreza.saemipour.no.vazheh.screen

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectGetWidth
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class)
actual fun getScreenWidth(): Float {
    val bounds = UIScreen.mainScreen.bounds
    return CGRectGetWidth(bounds).toFloat()
}