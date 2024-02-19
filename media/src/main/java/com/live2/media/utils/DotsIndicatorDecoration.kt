package com.live2.media.utils

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

class DotsIndicatorDecoration(
    @ColorInt colorInactive: Int,
    @ColorInt colorActive: Int
) : RecyclerView.ItemDecoration() {

    private val lineLength = 40.dp
    private val indicatorHeight: Int = 20.dp
    private val indicatorItemPadding: Int = 4.dp
    private val radius: Int = 2.dp
    private val inactivePaint = Paint()
    private val activePaint = Paint()

    init {
        inactivePaint.style = Paint.Style.FILL
        inactivePaint.isAntiAlias = true
        inactivePaint.color = colorInactive
        activePaint.style = Paint.Style.FILL_AND_STROKE
        activePaint.strokeCap = Paint.Cap.ROUND
        activePaint.strokeWidth = radius.toFloat() * 2
        activePaint.isAntiAlias = true
        activePaint.color = colorActive
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val adapter = parent.adapter ?: return
        val itemCount = adapter.itemCount

        // center horizontally, calculate width and subtract half from center
        val totalLength = (this.radius * 2 * itemCount).toFloat()
        val paddingBetweenItems = (0.coerceAtLeast(itemCount - 1) * indicatorItemPadding).toFloat()
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        // center vertically in the allotted space
        val indicatorPosY = parent.height - indicatorHeight / 2f

        val activePosition: Int = when (parent.layoutManager) {
            is LinearLayoutManager -> {
                (parent.layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()
            }
            else -> {
                // not supported layout manager
                return
            }
        }
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        drawInactiveDots(c, indicatorStartX, indicatorPosY, itemCount, activePosition)
        drawActiveLine(c, indicatorStartX, indicatorPosY, activePosition)
    }

    private fun drawInactiveDots(
        c: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        itemCount: Int,
        activeIndex: Int,
    ) {
        // width of item indicator including padding
        val itemWidth = (this.radius * 2 + indicatorItemPadding).toFloat()
        var start = indicatorStartX + radius
        for (i in 0 until itemCount) {
            if (i == activeIndex) {
                start += lineLength / 2
                continue
            }
            c.drawCircle(start, indicatorPosY, radius.toFloat(), inactivePaint)
            start += itemWidth
        }
    }

    private fun drawActiveLine(
        c: Canvas, indicatorStartX: Float, indicatorPosY: Float,
        highlightPosition: Int
    ) {
        // width of item indicator including padding
        val itemWidth = (lineLength + indicatorItemPadding).toFloat() / 2
        val circleWidth = (radius * 2) + indicatorItemPadding
        val highlightStart = indicatorStartX + circleWidth * highlightPosition + radius
        c.drawLine(
            highlightStart,
            indicatorPosY,
            highlightStart + itemWidth - indicatorItemPadding / 2,
            indicatorPosY,
            activePaint
        )
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = indicatorHeight
    }
}