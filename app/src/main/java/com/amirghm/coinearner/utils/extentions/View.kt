package com.amirghm.coinearner.utils.extentions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.view.View

/**
 * Created by Amir Hossein Ghasemi since 07/07/20
 *
 * Usage: All extension functions related to [View]
 *
 */

fun View.getLocationOnScreen(): Point {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return Point(location[0], location[1])
}

/**
 * This function represents the Absolute X coordinate of the view in the page
 * @return The X Absolute coordinated of the view
 */
fun View.absX(): Int {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return location[0]
}

/**
 * This function represents the Absolute Y coordinate of the view in the page
 * @return The Y Absolute coordinated of the view
 */
fun View.absY(): Int {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return location[1]
}