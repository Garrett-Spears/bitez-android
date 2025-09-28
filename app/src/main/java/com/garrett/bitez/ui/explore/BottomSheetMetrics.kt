package com.garrett.bitez.ui.explore

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

// Non-changing values for bottom sheet to reuse
data class BottomSheetMetrics(
    val heightOfHalfExpandedBottomSheet: Int,
    val heightOfFullyExpandedBottomSheet: Int,
    val peekHeight: Int,
    val halfSlideOffset: Float
) {
    // Static method to calculate fixed UI bottom sheet metrics and return obj
    companion object {
        fun calcBottomSheetMetrics(bottomSheet: View): BottomSheetMetrics {
            val bottomSheetBehavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(bottomSheet)

            // NOTE: Android coordinate system fixes Y = 0 at top of parent and increases as you go down

            // Percentage of parent view that bottom sheet fills up when half expanded
            val halfExpandedRatio: Float = bottomSheetBehavior.halfExpandedRatio

            // Percentage of parent view that bottom sheet does not cover when half-expanded
            val topHalfExpandedRatio: Float = 1 - halfExpandedRatio

            // Height of entire explore page fragment
            val parentHeight: Int = (bottomSheet.parent as View).height

            // Y-axis distance from top of parent view to top of bottom sheet when fully expanded
            val expandedTopDist: Int = bottomSheetBehavior.expandedOffset

            // Y-axis distance from top of parent view to top of bottom sheet when fully collapsed
            val collapsedTopDist: Int = parentHeight - bottomSheetBehavior.peekHeight

            // Y-axis distance from top of parent view to top of bottom sheet when half-expanded
            val halfExpandedTopDist: Int = (parentHeight * topHalfExpandedRatio).toInt()

            // Height of bottom sheet when half-expanded
            // NOTE: Calculated as Y-axis distance from top of half-expanded bottom sheet to top of fully-collapsed bottom sheet
            val heightOfHalfExpandedBottomSheet: Int = collapsedTopDist - halfExpandedTopDist

            // Height of bottom sheet when fully-expanded
            // NOTE: Calculated as Y-axis distance from top of fully-expanded bottom sheet to top of fully-collapsed bottom sheet
            val heightOfFullyExpandedBottomSheet: Int = collapsedTopDist - expandedTopDist

            // NOTE: This is == slideOffset for half-expanded bottom sheet since this is the percentage
            //       at which the bottom covers its total height share
            val halfSlideOffset = heightOfHalfExpandedBottomSheet.toFloat() / heightOfFullyExpandedBottomSheet.toFloat()


            return BottomSheetMetrics(
                heightOfHalfExpandedBottomSheet,
                heightOfFullyExpandedBottomSheet,
                bottomSheetBehavior.peekHeight,
                halfSlideOffset
            )
        }
    }
}