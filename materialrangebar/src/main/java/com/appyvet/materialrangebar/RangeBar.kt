/*
 * Copyright 2013, Edmodo, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.appyvet.materialrangebar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginRight
import com.appyvet.materialrangebar.RangeBar.OnRangeBarChangeListener
import java.util.*
import kotlin.math.abs

/*
 * Copyright 2015, Appyvet, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */ /**
 * The MaterialRangeBar is a single or double-sided version of a [android.widget.SeekBar]
 * with discrete values. Whereas the thumb for the SeekBar can be dragged to any
 * position in the bar, the RangeBar only allows its thumbs to be dragged to
 * discrete positions (denoted by tick marks) in the bar. When released, a
 * RangeBar thumb will snap to the nearest tick mark.
 * This version is forked from edmodo range bar
 * https://github.com/edmodo/range-bar.git
 * Clients of the RangeBar can attach a
 * [OnRangeBarChangeListener] to be notified when the pins
 * have
 * been moved.
 */
class RangeBar : View {
    // Instance variables for all of the customizable attributes
    private var mTickHeight = DEFAULT_TICK_HEIGHT_DP
    private var mTickStart = DEFAULT_TICK_START
    private var mTickEnd = DEFAULT_TICK_END
    private var mTickInterval = DEFAULT_TICK_INTERVAL
    private var mMinIndexDistance = 0
    private var mDesiredMinDistance = -1f
    private var mBarWeight = DEFAULT_BAR_WEIGHT_DP
    private var mIsBarRounded = false
    private var mBarColor = DEFAULT_BAR_COLOR
    private var mPinColor = DEFAULT_PIN_COLOR
    private var mTextColor = DEFAULT_TEXT_COLOR
    private var mConnectingLineWeight = DEFAULT_CONNECTING_LINE_WEIGHT_DP
    private var mConnectingLineColors: ArrayList<Int>? = ArrayList()
    private var mThumbRadiusDP = DEFAULT_EXPANDED_PIN_RADIUS_DP
    private var mTickDefaultColor = DEFAULT_TICK_COLOR
    private var mTickColors: ArrayList<Int>? = ArrayList()
    private var mTickLabelColor = DEFAULT_TICK_LABEL_COLOR
    private var mTickLabelSelectedColor = DEFAULT_TICK_LABEL_SELECTED_COLOR
    private var mActiveTickLabelColor = 0
    private var mActiveTickLabelSelectedColor = 0
    private var mTickLabelSize = DEFAULT_TICK_LABEL_FONT_SP
    private var mTickBottomLabels: Array<CharSequence>? = null
    private var mTickTopLabels: Array<CharSequence>? = null
    private var mTickDefaultLabel: String? = DEFAULT_TICK_LABEL
    private var mExpandedPinRadius = DEFAULT_EXPANDED_PIN_RADIUS_DP
    private var mThumbColor = DEFAULT_CONNECTING_LINE_COLOR
    private var mThumbColorLeft = 0
    private var mThumbColorRight = 0
    private var mThumbBoundaryColor = DEFAULT_CONNECTING_LINE_COLOR
    private var mThumbBoundarySize = DEFAULT_CIRCLE_BOUNDARY_SIZE_DP
    private var mThumbSize = DEFAULT_CIRCLE_SIZE_DP
    private var mMinPinFont = DEFAULT_MIN_PIN_FONT_SP
    private var mMaxPinFont = DEFAULT_MAX_PIN_FONT_SP

    // setTickCount only resets indices before a thumb has been pressed or a
    // setThumbIndices() is called, to correspond with intended usage
    private var mFirstSetTickCount = true
    private val mDisplayMetrices = resources.displayMetrics
    private val mDefaultWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, mDisplayMetrices).toInt()
    private val mDefaultHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75f, mDisplayMetrices).toInt()

    /**
     * Gets the tick count.
     *
     * @return the tick count
     */
    var tickCount = ((mTickEnd - mTickStart) / mTickInterval).toInt() + 1
        private set
    private var mLeftThumb: PinView? = null
    private var mRightThumb: PinView? = null
    private var mBar: Bar? = null
    private var mConnectingLine: ConnectingLine? = null
    private var mListener: OnRangeBarChangeListener? = null
    private var mPinTextListener: OnRangeBarTextListener? = null
    private var mTickMap: HashMap<Float, String>? = null

    /**
     * Gets the index of the left-most pin.
     *
     * @return the 0-based index of the left pin
     */
    private var leftIndex: Int = 0

    fun getLeftIndex(): Int {
        return leftIndex
    }

    fun setLeftIndex(index: Int) {
        leftIndex = index
    }

    fun setThumbsIndex(leftIndex: Int, rightIndex: Int) {
        if (isRangeBar) {
            mLeftThumb!!.x = marginLeft + leftIndex / (tickCount - 1).toFloat() * barLength
            mLeftThumb!!.setXValue(getPinValue(leftIndex))
        }

        mRightThumb!!.x = marginLeft + rightIndex / (tickCount - 1).toFloat() * barLength
        mRightThumb!!.setXValue(getPinValue(rightIndex))

        mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                getPinValue(leftIndex),
                getPinValue(rightIndex))

        invalidate()

    }

    /**
     * Gets the index of the right-most pin.
     *
     * @return the 0-based index of the right pin
     */
    private var rightIndex = 0

    fun getRightIndex(): Int {
        return rightIndex
    }

    fun setRightIndex(index: Int) {
        rightIndex = index
    }

    /**
     * Gets the type of the bar.
     *
     * @return true if rangebar, false if seekbar.
     */
    var isRangeBar = true
        private set
    private var mPinPadding = DEFAULT_PIN_PADDING_DP
    private var mBarPaddingBottom = DEFAULT_BAR_PADDING_BOTTOM_DP
    private var mActiveConnectingLineColor = 0
    private var mActiveConnectingLineColors = ArrayList<Int>()
    private var mActiveBarColor = 0
    private var mActiveTickDefaultColor = 0
    private var mActiveTickColors = ArrayList<Int>()
    private var mActiveCircleColor = 0
    private var mActiveCircleColorLeft = 0
    private var mActiveCircleColorRight = 0
    private var mActiveCircleBoundaryColor = 0

    //Used for ignoring vertical moves
    private var mDiffX = 0
    private var mDiffY = 0
    private var mLastX = 0f
    private var mLastY = 0f
    private var mFormatter: IRangeBarFormatter? = null
    private var drawTicks = true
    private var mArePinsTemporary = true
    private var mOnlyOnDrag = false
    private var mDragging = false
    private var mIsInScrollingContainer = false
    private var mPinTextFormatter: PinTextFormatter = object : PinTextFormatter {
        override fun getText(value: String): String {
            return if (value.length > 4) {
                value.substring(0, 4)
            } else {
                value
            }
        }
    }
    private var mLeftBoundX = 0f
    private var mRightBoundX = 0f

    constructor(context: Context?) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        rangeBarInit(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        rangeBarInit(context, attrs)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        bundle.putInt("TICK_COUNT", tickCount)
        bundle.putFloat("TICK_START", mTickStart)
        bundle.putFloat("TICK_END", mTickEnd)
        bundle.putFloat("TICK_INTERVAL", mTickInterval)
        bundle.putInt("TICK_COLOR", mTickDefaultColor)
        bundle.putIntegerArrayList("TICK_COLORS", mTickColors)
        bundle.putInt("TICK_LABEL_COLOR", mTickLabelColor)
        bundle.putInt("TICK_LABEL_SELECTED_COLOR", mTickLabelSelectedColor)
        bundle.putCharSequenceArray("TICK_TOP_LABELS", mTickTopLabels)
        bundle.putCharSequenceArray("TICK_BOTTOM_LABELS", mTickBottomLabels)
        bundle.putString("TICK_DEFAULT_LABEL", mTickDefaultLabel)
        bundle.putFloat("TICK_HEIGHT_DP", mTickHeight)
        bundle.putFloat("BAR_WEIGHT", mBarWeight)
        bundle.putBoolean("BAR_ROUNDED", mIsBarRounded)
        bundle.putInt("BAR_COLOR", mBarColor)
        bundle.putFloat("CONNECTING_LINE_WEIGHT", mConnectingLineWeight)
        bundle.putIntegerArrayList("CONNECTING_LINE_COLOR", mConnectingLineColors)
        bundle.putFloat("CIRCLE_SIZE", mThumbSize)
        bundle.putInt("CIRCLE_COLOR", mThumbColor)
        bundle.putInt("CIRCLE_COLOR_LEFT", mThumbColorLeft)
        bundle.putInt("CIRCLE_COLOR_RIGHT", mThumbColorRight)
        bundle.putInt("CIRCLE_BOUNDARY_COLOR", mThumbBoundaryColor)
        bundle.putFloat("CIRCLE_BOUNDARY_WIDTH", mThumbBoundarySize)
        bundle.putFloat("THUMB_RADIUS_DP", mThumbRadiusDP)
        bundle.putFloat("EXPANDED_PIN_RADIUS_DP", mExpandedPinRadius)
        bundle.putFloat("PIN_PADDING", mPinPadding)
        bundle.putFloat("BAR_PADDING_BOTTOM", mBarPaddingBottom)
        bundle.putBoolean("IS_RANGE_BAR", isRangeBar)
        bundle.putBoolean("IS_ONLY_ON_DRAG", mOnlyOnDrag)
        bundle.putBoolean("ARE_PINS_TEMPORARY", mArePinsTemporary)
        bundle.putInt("LEFT_INDEX", leftIndex)
        bundle.putInt("RIGHT_INDEX", rightIndex)
        bundle.putInt("MIN_INDEX_DISTANCE", mMinIndexDistance)
        bundle.putBoolean("FIRST_SET_TICK_COUNT", mFirstSetTickCount)
        bundle.putFloat("MIN_PIN_FONT", mMinPinFont)
        bundle.putFloat("MAX_PIN_FONT", mMaxPinFont)
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state
            tickCount = bundle.getInt("TICK_COUNT")
            mTickStart = bundle.getFloat("TICK_START")
            mTickEnd = bundle.getFloat("TICK_END")
            mTickInterval = bundle.getFloat("TICK_INTERVAL")
            mTickDefaultColor = bundle.getInt("TICK_COLOR")
            mTickColors = bundle.getIntegerArrayList("TICK_COLORS")
            mTickLabelColor = bundle.getInt("TICK_LABEL_COLOR")
            mTickLabelSelectedColor = bundle.getInt("TICK_LABEL_SELECTED_COLOR")
            mTickTopLabels = bundle.getCharSequenceArray("TICK_TOP_LABELS")
            mTickBottomLabels = bundle.getCharSequenceArray("TICK_BOTTOM_LABELS")
            mTickDefaultLabel = bundle.getString("TICK_DEFAULT_LABEL")
            mTickHeight = bundle.getFloat("TICK_HEIGHT_DP")
            mBarWeight = bundle.getFloat("BAR_WEIGHT")
            mIsBarRounded = bundle.getBoolean("BAR_ROUNDED", false)
            mBarColor = bundle.getInt("BAR_COLOR")
            mThumbSize = bundle.getFloat("CIRCLE_SIZE")
            mThumbColor = bundle.getInt("CIRCLE_COLOR")
            mThumbColorLeft = bundle.getInt("CIRCLE_COLOR_LEFT")
            mThumbColorRight = bundle.getInt("CIRCLE_COLOR_RIGHT")
            mThumbBoundaryColor = bundle.getInt("CIRCLE_BOUNDARY_COLOR")
            mThumbBoundarySize = bundle.getFloat("CIRCLE_BOUNDARY_WIDTH")
            mConnectingLineWeight = bundle.getFloat("CONNECTING_LINE_WEIGHT")
            mConnectingLineColors = bundle.getIntegerArrayList("CONNECTING_LINE_COLOR")
            mThumbRadiusDP = bundle.getFloat("THUMB_RADIUS_DP")
            mExpandedPinRadius = bundle.getFloat("EXPANDED_PIN_RADIUS_DP")
            mPinPadding = bundle.getFloat("PIN_PADDING")
            mBarPaddingBottom = bundle.getFloat("BAR_PADDING_BOTTOM")
            isRangeBar = bundle.getBoolean("IS_RANGE_BAR")
            mOnlyOnDrag = bundle.getBoolean("IS_ONLY_ON_DRAG")
            mArePinsTemporary = bundle.getBoolean("ARE_PINS_TEMPORARY")
            leftIndex = bundle.getInt("LEFT_INDEX")
            rightIndex = bundle.getInt("RIGHT_INDEX")
            mFirstSetTickCount = bundle.getBoolean("FIRST_SET_TICK_COUNT")
            mMinIndexDistance = bundle.getInt("MIN_INDEX_DISTANCE")
            mMinPinFont = bundle.getFloat("MIN_PIN_FONT")
            mMaxPinFont = bundle.getFloat("MAX_PIN_FONT")
            setRangePinsByIndices(leftIndex, rightIndex)
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width: Int
        val height: Int

        // Get measureSpec mode and size values.
        val measureWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)

        // The RangeBar width should be as large as possible.
        width = when (measureWidthMode) {
            MeasureSpec.AT_MOST -> {
                measureWidth
            }
            MeasureSpec.EXACTLY -> {
                measureWidth
            }
            else -> {
                mDefaultWidth
            }
        }

        // The RangeBar height should be as small as possible.
        height = when (measureHeightMode) {
            MeasureSpec.AT_MOST -> {
                Math.min(mDefaultHeight, measureHeight)
            }
            MeasureSpec.EXACTLY -> {
                measureHeight
            }
            else -> {
                mDefaultHeight
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val ctx = context

        // This is the initial point at which we know the size of the View.

        // Create the two thumb objects and position line in view
        val density = mDisplayMetrices.density
        val expandedPinRadius = mExpandedPinRadius / density
        val yPos = h - mBarPaddingBottom
        if (isRangeBar) {
            mLeftThumb = PinView(ctx)
            mLeftThumb!!.setFormatter(mFormatter)
            mLeftThumb!!.init(ctx, yPos, expandedPinRadius, mPinColor, mTextColor, mThumbSize,
                    mThumbColorLeft, mThumbBoundaryColor, mThumbBoundarySize, mMinPinFont, mMaxPinFont, mArePinsTemporary)
        }
        mRightThumb = PinView(ctx)
        mRightThumb!!.setFormatter(mFormatter)
        mRightThumb!!.init(ctx, yPos, expandedPinRadius, mPinColor, mTextColor, mThumbSize,
                mThumbColorRight, mThumbBoundaryColor, mThumbBoundarySize, mMinPinFont, mMaxPinFont, mArePinsTemporary)

        // Create the underlying bar.
        val marginLeft = Math.max(mExpandedPinRadius, mThumbSize)
        val barLength = w - 2 * marginLeft
        mBar = Bar(ctx, marginLeft, yPos, barLength, tickCount, mTickHeight, mTickDefaultColor, mTickColors,
                mBarWeight, mBarColor, mIsBarRounded, mTickLabelColor, mTickLabelSelectedColor,
                mTickTopLabels, mTickBottomLabels, mTickDefaultLabel, mTickLabelSize)

        // Initialize thumbs to the desired indices
        if (isRangeBar) {
            updateThumbBounds()
            mLeftThumb!!.x = marginLeft + leftIndex / (tickCount - 1).toFloat() * barLength
            mLeftThumb!!.setXValue(getPinValue(leftIndex))
        }
        mRightThumb!!.x = marginLeft + rightIndex / (tickCount - 1).toFloat() * barLength
        mRightThumb!!.setXValue(getPinValue(rightIndex))

        // Set the thumb indices.
        val newLeftIndex = if (isRangeBar) mBar!!.getNearestTickIndex(mLeftThumb!!) else 0
        val newRightIndex = mBar!!.getNearestTickIndex(mRightThumb!!)

        // Call the listener.
        if (newLeftIndex != leftIndex || newRightIndex != rightIndex) {
            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex),
                        getPinValue(rightIndex))
            }
        }

        // Create the line connecting the two thumbs.
        mConnectingLine = mConnectingLineColors?.let {
            ConnectingLine(yPos, mConnectingLineWeight, it)
        }
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // Cache this value since it only changes if the ViewParent changes
        mIsInScrollingContainer = isInScrollingContainer
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mBar!!.draw(canvas)

        if (isRangeBar) {
            mConnectingLine!!.draw(canvas, mLeftThumb!!, mRightThumb!!)
            if (drawTicks) {
                mBar!!.drawTicks(canvas, mExpandedPinRadius, mRightThumb!!, mLeftThumb)
            }
            mLeftThumb!!.draw(canvas)
        } else {
            mConnectingLine!!.draw(canvas, marginLeft, mRightThumb!!)
            if (drawTicks) {
                mBar!!.drawTicks(canvas, mExpandedPinRadius, mRightThumb!!)
            }
        }
        mRightThumb!!.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        // If this View is not enabled, don't allow for touch interactions.
        if (!isEnabled) {
            return false
        }
        updateThumbBounds()
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDiffX = 0
                mDiffY = 0
                mLastX = event.x
                mLastY = event.y
                // We don't want to change to tick value yet if we're inside a scrolling container.
                // In this case, the user may be trying to scroll the parent.
                if (!mIsInScrollingContainer) {
                    onActionDown(event.x, event.y)
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                // Just release the dragging if we were previously dragging
                // or if it was a click (last touch event coordinates are the same)
                if (mDragging || event.x == mLastX && event.y == mLastY) {
                    this.parent.requestDisallowInterceptTouchEvent(false)
                    onActionUp(event.x, event.y)
                }
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mDragging || event.x == mLastX && event.y == mLastY) {
                    this.parent.requestDisallowInterceptTouchEvent(false)
                    onActionUp(event.x, event.y)
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val curX = event.x
                val curY = event.y
                mDiffX += abs(curX - mLastX).toInt()
                mDiffY += abs(curY - mLastY).toInt()
                mLastX = curX
                mLastY = curY
                if (!mDragging) {
                    return if (mDiffX > mDiffY) {
                        onActionDown(event.x, event.y)
                        true
                    } else {
                        false
                    }
                } else {
                    onActionMove(event.x)
                    this.parent.requestDisallowInterceptTouchEvent(true)
                    if (mDiffX < mDiffY) {
                        //vertical touch
                        // Don't let scrolling parents get this touch event
                        if (!mIsInScrollingContainer) {
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                        return false
                    } else {
                        //horizontal touch (do nothing as it is needed for RangeBar)
                    }
                }
                true
            }
            else -> false
        }
    }
    // Public Methods //////////////////////////////////////////////////////////
    /**
     * Sets if the pins works only when drag it.
     *
     * @param onlyOnDrag boolean specifying if the onlyOnDrag is enabled
     */
    fun setOnlyOnDrag(onlyOnDrag: Boolean) {
        mOnlyOnDrag = onlyOnDrag
    }

    /**
     * Sets a listener to receive notifications of changes to the RangeBar. This
     * will overwrite any existing set listeners.
     *
     * @param listener the RangeBar notification listener; null to remove any
     * existing listener
     */
    fun setOnRangeBarChangeListener(listener: OnRangeBarChangeListener?) {
        mListener = listener
    }

    /**
     * Sets a listener to modify the text
     *
     * @param mPinTextListener the RangeBar pin text notification listener; null to remove any
     * existing listener
     */
    fun setPinTextListener(mPinTextListener: OnRangeBarTextListener?) {
        this.mPinTextListener = mPinTextListener
    }

    fun setFormatter(formatter: IRangeBarFormatter?) {
        if (mLeftThumb != null) {
            mLeftThumb!!.setFormatter(formatter)
        }
        if (mRightThumb != null) {
            mRightThumb!!.setFormatter(formatter)
        }
        mFormatter = formatter
    }

    fun setDrawTicks(drawTicks: Boolean) {
        this.drawTicks = drawTicks
    }

    /**
     * Sets the start tick in the RangeBar.
     *
     * @param tickInterval Integer specifying the number of ticks.
     */
    fun setTickInterval(tickInterval: Float) {
        val tickCount = ((mTickEnd - mTickStart) / tickInterval).toInt() + 1
        if (isValidTickCount(tickCount)) {
            this.tickCount = tickCount
            mTickInterval = tickInterval

            // Prevents resetting the indices when creating new activity, but
            // allows it on the first setting.
            if (mFirstSetTickCount) {
                leftIndex = 0
                rightIndex = this.tickCount - 1
                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                            getPinValue(leftIndex), getPinValue(rightIndex))
                }
            }
            if (indexOutOfRange(leftIndex, rightIndex)) {
                leftIndex = 0
                rightIndex = this.tickCount - 1
                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                            getPinValue(leftIndex), getPinValue(rightIndex))
                }
            }
            createBar()
            createPins()
        } else {
            Log.e(TAG, "tickCount less than 2; invalid tickCount.")
            throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
        }
    }

    /**
     * Sets the height of the ticks in the range bar.
     *
     * @param tickHeight Float specifying the height of each tick mark in dp.
     */
    fun setTickHeight(tickHeight: Float) {
        mTickHeight = tickHeight
        createBar()
    }

    /**
     * Set the weight of the bar line and the tick lines in the range bar.
     *
     * @param barWeight Float specifying the weight of the bar and tick lines in
     * DP.
     */
    fun setBarWeight(barWeight: Float) {
        mBarWeight = barWeight
        createBar()
    }

    /**
     * set the bar with rounded corners
     *
     * @param isBarRounded flag
     */
    var isBarRounded: Boolean
        get() = mIsBarRounded
        set(isBarRounded) {
            mIsBarRounded = isBarRounded
            createBar()
        }

    /**
     * Set the color of the bar line and the tick lines in the range bar.
     *
     * @param barColor Integer specifying the color of the bar line.
     */
    fun setBarColor(barColor: Int) {
        mBarColor = barColor
        createBar()
    }

    /**
     * Set the color of the pins.
     *
     * @param pinColor Integer specifying the color of the pin.
     */
    fun setPinColor(pinColor: Int) {
        mPinColor = pinColor
        createPins()
    }

    /**
     * Set the color of the text within the pin.
     *
     * @param textColor Integer specifying the color of the text in the pin.
     */
    fun setPinTextColor(textColor: Int) {
        mTextColor = textColor
        createPins()
    }

    /**
     * Set if the view is a range bar or a seek bar.
     *
     * @param isRangeBar Boolean - true sets it to rangebar, false to seekbar.
     */
    fun setRangeBarEnabled(isRangeBar: Boolean) {
        this.isRangeBar = isRangeBar
        invalidate()
    }

    /**
     * Set if the pins should dissapear after released
     *
     * @param arePinsTemporary Boolean - true if pins shoudl dissapear after released, false to
     * stay
     * drawn
     */
    fun setTemporaryPins(arePinsTemporary: Boolean) {
        mArePinsTemporary = arePinsTemporary
        invalidate()
    }

    /**
     * Set the default color of the ticks.
     *
     * @param tickDefaultColor Integer specifying the color of the ticks.
     */
    fun setTickDefaultColor(tickDefaultColor: Int) {
        mTickDefaultColor = tickDefaultColor
        setTickColors(tickDefaultColor)
        createBar()
    }

    /**
     * Set the color of the ticks.
     *
     * @param color Integer specifying the color of the ticks.
     */
    fun setTickColors(color: Int) {
        for (i in mTickColors!!.indices) {
            mTickColors!![i] = color
        }
        createBar()
    }

    fun setTickLabelColor(tickLabelColor: Int) {
        mTickLabelColor = tickLabelColor
        createBar()
    }

    fun setTickLabelSelectedColor(tickLabelSelectedColor: Int) {
        mTickLabelSelectedColor = tickLabelSelectedColor
        createBar()
    }

    /**
     * Set the color of the Thumb.
     *
     * @param thumbColor Integer specifying the color of the ticks.
     */
    fun setThumbColor(thumbColor: Int) {
        mThumbColor = thumbColor
        leftThumbColor = thumbColor
        rightThumbColor = thumbColor
        createPins()
    }

    /**
     * Set the color of the Thumb Boundary.
     *
     * @param thumbBoundaryColor Integer specifying the boundary color of the ticks.
     */
    fun setThumbBoundaryColor(thumbBoundaryColor: Int) {
        mThumbBoundaryColor = thumbBoundaryColor

        if (isRangeBar && mLeftThumb != null) {
            mLeftThumb?.changeBoundaryTint(thumbBoundaryColor)
        } else {
            createPins()
        }

        if (mRightThumb != null) {
            mRightThumb?.changeBoundaryTint(thumbBoundaryColor)
        } else {
            createPins()
        }
    }

    /**
     * Set the size of the Thumb Boundary.
     *
     * @param thumbBoundarySize Integer specifying the boundary size of ticks.
     * Value should be in DP
     */
    fun setThumbBoundarySize(thumbBoundarySize: Int) {
        mThumbBoundarySize = thumbBoundarySize.toFloat()
        createPins()
    }

    /**
     * Set the size of the thumb.
     *
     * @param thumbSize Integer specifying the size of ticks.
     * Value should be in DP
     */
    fun setThumbSize(thumbSize: Int) {
        mThumbSize = thumbSize.toFloat()
        createPins()
    }

    /**
     * Set the weight of the connecting line between the thumbs.
     *
     * @param connectingLineWeight Float specifying the weight of the connecting
     * line. Value should be in DP
     */
    fun setConnectingLineWeight(connectingLineWeight: Float) {
        mConnectingLineWeight = connectingLineWeight
        createConnectingLine()
    }

    /**
     * Set the color of the connecting line between the thumbs.
     *
     * @param connectingLineColor Integer specifying the color of the connecting
     * line.
     */
    fun setConnectingLineColor(connectingLineColor: Int) {
        mConnectingLineColors!!.clear()
        mConnectingLineColors!!.add(connectingLineColor)
        createConnectingLine()
    }

    fun setConnectingLineColors(connectingLineColors: ArrayList<Int>?) {
        mConnectingLineColors = ArrayList(connectingLineColors!!)
        createConnectingLine()
    }

    /**
     * If this is set, the thumb images will be replaced with a circle of the
     * specified radius. Default width = 12dp.
     *
     * @param pinRadius Float specifying the radius of the thumbs to be drawn. Value should be in DP
     */
    fun setPinRadius(pinRadius: Float) {
        mExpandedPinRadius = pinRadius
        createPins()
    }

    /**
     * Gets left thumb color
     *
     * @return
     */
    /**
     * Sets left thumb circle color
     *
     * @param colorLeft
     */
    var leftThumbColor: Int
        get() = mThumbColorLeft
        set(colorLeft) {
            mThumbColorLeft = colorLeft
            createPins()
        }

    /**
     * Gets right thumb color
     *
     * @return
     */
    /**
     * Sets Right thumb circle color
     *
     * @param colorRight
     */
    var rightThumbColor: Int
        get() = mThumbColorRight
        set(colorRight) {
            mThumbColorRight = colorRight
            createPins()
        }

    /**
     * Gets the start tick.
     *
     * @return the start tick.
     */// Prevents resetting the indices when creating new activity, but
    // allows it on the first setting.
    /**
     * Sets the start tick in the RangeBar.
     *
     * @param tickStart Integer specifying the number of ticks.
     */
    var tickStart: Float
        get() = mTickStart
        set(tickStart) {
            val tickCount = ((mTickEnd - tickStart) / mTickInterval).toInt() + 1
            if (isValidTickCount(tickCount)) {
                this.tickCount = tickCount
                mTickStart = tickStart

                // Prevents resetting the indices when creating new activity, but
                // allows it on the first setting.
                if (mFirstSetTickCount) {
                    leftIndex = 0
                    rightIndex = this.tickCount - 1
                    if (mListener != null) {
                        mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                                getPinValue(leftIndex),
                                getPinValue(rightIndex))
                    }
                }
                if (indexOutOfRange(leftIndex, rightIndex)) {
                    leftIndex = 0
                    rightIndex = this.tickCount - 1
                    if (mListener != null) {
                        mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                                getPinValue(leftIndex),
                                getPinValue(rightIndex))
                    }
                }
                createBar()
                createPins()
            } else {
                Log.e(TAG, "tickCount less than 2; invalid tickCount.")
                throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
            }
        }

    /**
     * Gets the end tick.
     *
     * @return the end tick.
     */// Prevents resetting the indices when creating new activity, but
    // allows it on the first setting.
    /**
     * Sets the end tick in the RangeBar.
     *
     * @param tickEnd Integer specifying the number of ticks.
     */
    var tickEnd: Float
        get() = mTickEnd
        set(tickEnd) {
            val tickCount = ((tickEnd - mTickStart) / mTickInterval).toInt() + 1
            if (isValidTickCount(tickCount)) {
                this.tickCount = tickCount
                mTickEnd = tickEnd

                // Prevents resetting the indices when creating new activity, but
                // allows it on the first setting.
                if (mFirstSetTickCount) {
                    leftIndex = 0
                    rightIndex = this.tickCount - 1
                    if (mListener != null) {
                        mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                                getPinValue(leftIndex), getPinValue(rightIndex))
                    }
                }
                if (indexOutOfRange(leftIndex, rightIndex)) {
                    leftIndex = 0
                    rightIndex = this.tickCount - 1
                    if (mListener != null) {
                        mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                                getPinValue(leftIndex), getPinValue(rightIndex))
                    }
                }
                createBar()
                createPins()
            } else {
                Log.e(TAG, "tickCount less than 2; invalid tickCount.")
                throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
            }
        }

    /**
     * Gets the tick top labels.
     *
     * @return the tick top labels
     */
    var tickTopLabels: Array<CharSequence>?
        get() = mTickTopLabels
        set(tickLabels) {
            mTickTopLabels = tickLabels
            createBar()
        }

    /**
     * Gets the tick bottom labels.
     *
     * @return the tick bottom labels
     */
    var tickBottomLabels: Array<CharSequence>?
        get() = mTickBottomLabels
        set(tickLabels) {
            mTickBottomLabels = tickLabels
            createBar()
        }

    /**
     * Gets the tick colors.
     *
     * @return List of colors
     */
    /**
     * Set the colors of the ticks.
     *
     * @param tickColors List of Integers specifying the color of the ticks.
     */
    var tickColors: ArrayList<Int>?
        get() = mTickColors
        set(tickColors) {
            mTickColors = ArrayList(tickColors!!)
            createBar()
        }

    /**
     * @param index
     * @return specified color
     */
    fun getTickColor(index: Int): Int {
        return mTickColors!![index]
    }

    /**
     * Sets the location of the pins according by the supplied index.
     * Numbered from 0 to mTickCount - 1 from the left.
     *
     * @param leftPinIndex  Integer specifying the index of the left pin
     * @param rightPinIndex Integer specifying the index of the right pin
     */
    fun setRangePinsByIndices(leftPinIndex: Int, rightPinIndex: Int) {
        if (indexOutOfRange(leftPinIndex, rightPinIndex)) {
            Log.e(TAG,
                    "Pin index left " + leftPinIndex + ", or right " + rightPinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
            throw IllegalArgumentException(
                    "Pin index left " + leftPinIndex + ", or right " + rightPinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
        } else {
            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }
            leftIndex = leftPinIndex
            rightIndex = rightPinIndex
            createPins()
            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex), getPinValue(rightIndex))
            }
        }
        invalidate()
        requestLayout()
    }

    /**
     * Sets the location of pin according by the supplied index.
     * Numbered from 0 to mTickCount - 1 from the left.
     *
     * @param pinIndex Integer specifying the index of the seek pin
     */
    fun setSeekPinByIndex(pinIndex: Int) {
        if (pinIndex < 0 || pinIndex > tickCount) {
            Log.e(TAG,
                    "Pin index " + pinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + 0 + ") and less than the maximum value ("
                            + tickCount + ")")
            throw IllegalArgumentException(
                    "Pin index " + pinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + 0 + ") and less than the maximum value ("
                            + tickCount + ")")
        } else {
            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }
            rightIndex = pinIndex
            createPins()
            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex), getPinValue(rightIndex))
            }
        }
        invalidate()
        requestLayout()
    }

    /**
     * Sets the location of pins according by the supplied values.
     *
     * @param leftPinValue  Float specifying the index of the left pin
     * @param rightPinValue Float specifying the index of the right pin
     */
    fun setRangePinsByValue(leftPinValue: Float, rightPinValue: Float) {
        if (valueOutOfRange(leftPinValue, rightPinValue)) {
            Log.e(TAG,
                    "Pin value left " + leftPinValue + ", or right " + rightPinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
            throw IllegalArgumentException(
                    "Pin value left " + leftPinValue + ", or right " + rightPinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
        } else {
            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }
            leftIndex = ((leftPinValue - mTickStart) / mTickInterval).toInt()
            rightIndex = ((rightPinValue - mTickStart) / mTickInterval).toInt()
            createPins()
            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex), getPinValue(rightIndex))
            }
        }
        if (mListener != null) mListener!!.onTouchEnded(this)
        invalidate()
        requestLayout()
    }

    /**
     * Sets the location of pin according by the supplied value.
     *
     * @param pinValue Float specifying the value of the pin
     */
    fun setSeekPinByValue(pinValue: Float) {
        if (pinValue > mTickEnd || pinValue < mTickStart) {
            Log.e(TAG,
                    "Pin value " + pinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
            throw IllegalArgumentException(
                    "Pin value " + pinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
        } else {
            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }
            rightIndex = ((pinValue - mTickStart) / mTickInterval).toInt()
            createPins()
            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex), getPinValue(rightIndex))
            }
        }
        invalidate()
        requestLayout()
    }

    /**
     * Gets the value of the left pin.
     *
     * @return the string value of the left pin.
     */
    val leftPinValue: String
        get() = getPinValue(leftIndex)

    /**
     * Gets the value of the right pin.
     *
     * @return the string value of the right pin.
     */
    val rightPinValue: String
        get() = getPinValue(rightIndex)

    /**
     * Gets the tick interval.
     *
     * @return the tick interval
     */
    val tickInterval: Double
        get() = mTickInterval.toDouble()

    override fun setEnabled(enabled: Boolean) {
        if (!enabled) {
            mBarColor = DEFAULT_BAR_COLOR
            setConnectingLineColor(DEFAULT_BAR_COLOR)
            mThumbColor = DEFAULT_BAR_COLOR
            mThumbColorLeft = DEFAULT_BAR_COLOR
            mThumbColorRight = DEFAULT_BAR_COLOR
            mThumbBoundaryColor = DEFAULT_BAR_COLOR
            mTickDefaultColor = DEFAULT_BAR_COLOR
            setTickColors(DEFAULT_BAR_COLOR)
            mTickLabelColor = DEFAULT_BAR_COLOR
            mTickLabelSelectedColor = DEFAULT_BAR_COLOR
        } else {
            mBarColor = mActiveBarColor
            setConnectingLineColor(mActiveConnectingLineColor)
            setConnectingLineColors(mActiveConnectingLineColors)
            mThumbColor = mActiveCircleColor
            mThumbColorLeft = mActiveCircleColorLeft
            mThumbColorRight = mActiveCircleColorRight
            mThumbBoundaryColor = mActiveCircleBoundaryColor
            mTickDefaultColor = mActiveTickDefaultColor
            tickColors = mActiveTickColors
            mTickLabelColor = mActiveTickLabelColor
            mTickLabelSelectedColor = mActiveTickLabelSelectedColor
        }
        super.setEnabled(enabled)
        createBar()
        createPins()
        createConnectingLine()
    }

    fun setPinTextFormatter(pinTextFormatter: PinTextFormatter) {
        mPinTextFormatter = pinTextFormatter
    }
    // Private Methods /////////////////////////////////////////////////////////
    /**
     * Does all the functions of the constructor for RangeBar. Called by both
     * RangeBar constructors in lieu of copying the code for each constructor.
     *
     * @param context Context from the constructor.
     * @param attrs   AttributeSet from the constructor.
     */
    private fun rangeBarInit(context: Context, attrs: AttributeSet?) {
        //TODO tick value map
        if (mTickMap == null) {
            mTickMap = HashMap()
        }
        val ta = context.obtainStyledAttributes(attrs, R.styleable.RangeBar, 0, 0)
        try {

            // Sets the values of the user-defined attributes based on the XML
            // attributes.
            val tickStart = ta
                    .getFloat(R.styleable.RangeBar_mrb_tickStart, DEFAULT_TICK_START)
            val tickEnd = ta
                    .getFloat(R.styleable.RangeBar_mrb_tickEnd, DEFAULT_TICK_END)
            val tickInterval = ta
                    .getFloat(R.styleable.RangeBar_mrb_tickInterval, DEFAULT_TICK_INTERVAL)
            val minDistance = ta
                    .getFloat(R.styleable.RangeBar_mrb_minThumbDistance, DEFAULT_MIN_DISTANCE)
            val tickCount = ((tickEnd - tickStart) / tickInterval).toInt() + 1
            if (isValidTickCount(tickCount)) {

                // Similar functions performed above in setTickCount; make sure
                // you know how they interact
                this.tickCount = tickCount
                mTickStart = tickStart
                mTickEnd = tickEnd
                mTickInterval = tickInterval
                leftIndex = 0
                rightIndex = this.tickCount - 1
                mDesiredMinDistance = minDistance
                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                            getPinValue(leftIndex),
                            getPinValue(rightIndex))
                }
            } else {
                Log.e(TAG, "tickCount less than 2; invalid tickCount. XML input ignored.")
            }
            mTickHeight = ta.getDimension(R.styleable.RangeBar_mrb_tickHeight,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TICK_HEIGHT_DP,
                            mDisplayMetrices)
            )
            mBarWeight = ta.getDimension(R.styleable.RangeBar_mrb_barWeight,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BAR_WEIGHT_DP,
                            mDisplayMetrices)
            )
            mThumbSize = ta.getDimension(R.styleable.RangeBar_mrb_thumbSize,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CIRCLE_SIZE_DP,
                            mDisplayMetrices)
            )
            mThumbBoundarySize = ta.getDimension(R.styleable.RangeBar_mrb_thumbBoundarySize,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CIRCLE_BOUNDARY_SIZE_DP,
                            mDisplayMetrices)
            )
            mConnectingLineWeight = ta.getDimension(R.styleable.RangeBar_mrb_connectingLineWeight,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CONNECTING_LINE_WEIGHT_DP,
                            mDisplayMetrices)
            )
            mExpandedPinRadius = ta.getDimension(R.styleable.RangeBar_mrb_pinRadius,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_EXPANDED_PIN_RADIUS_DP,
                            mDisplayMetrices)
            )
            mPinPadding = ta.getDimension(R.styleable.RangeBar_mrb_pinPadding,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PIN_PADDING_DP,
                            mDisplayMetrices)
            )
            mBarPaddingBottom = ta.getDimension(R.styleable.RangeBar_mrb_rangeBarPaddingBottom,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BAR_PADDING_BOTTOM_DP,
                            mDisplayMetrices)
            )
            mBarColor = ta.getColor(R.styleable.RangeBar_mrb_rangeBarColor, DEFAULT_BAR_COLOR)
            mTextColor = ta.getColor(R.styleable.RangeBar_mrb_pinTextColor, DEFAULT_TEXT_COLOR)
            mPinColor = ta.getColor(R.styleable.RangeBar_mrb_pinColor, DEFAULT_PIN_COLOR)
            mActiveBarColor = mBarColor
            mThumbColor = ta.getColor(R.styleable.RangeBar_mrb_thumbColor,
                    DEFAULT_CONNECTING_LINE_COLOR)
            mThumbColorLeft = ta.getColor(R.styleable.RangeBar_mrb_leftThumbColor,
                    mThumbColor)
            mThumbColorRight = ta.getColor(R.styleable.RangeBar_mrb_rightThumbColor,
                    mThumbColor)
            mThumbBoundaryColor = ta.getColor(R.styleable.RangeBar_mrb_thumbBoundaryColor,
                    DEFAULT_CONNECTING_LINE_COLOR)
            mActiveCircleColor = mThumbColor
            mActiveCircleColorLeft = mThumbColorLeft
            mActiveCircleColorRight = mThumbColorRight
            mActiveCircleBoundaryColor = mThumbBoundaryColor
            mTickDefaultColor = ta.getColor(R.styleable.RangeBar_mrb_tickDefaultColor, DEFAULT_TICK_COLOR)
            mActiveTickDefaultColor = mTickDefaultColor
            mTickColors = getColors(ta.getTextArray(R.styleable.RangeBar_mrb_tickColors), mTickDefaultColor)
            mActiveTickColors = ArrayList(mTickColors!!)
            mTickLabelColor = ta.getColor(R.styleable.RangeBar_mrb_tickLabelColor, DEFAULT_TICK_LABEL_COLOR)
            mActiveTickLabelColor = mTickLabelColor
            mTickLabelSelectedColor = ta.getColor(R.styleable.RangeBar_mrb_tickLabelSelectedColor, DEFAULT_TICK_LABEL_SELECTED_COLOR)
            mActiveTickLabelSelectedColor = mTickLabelSelectedColor
            mTickBottomLabels = ta.getTextArray(R.styleable.RangeBar_mrb_tickBottomLabels)
            mTickTopLabels = ta.getTextArray(R.styleable.RangeBar_mrb_tickTopLabels)
            mTickDefaultLabel = ta.getString(R.styleable.RangeBar_mrb_tickDefaultLabel)
            mTickDefaultLabel = if (mTickDefaultLabel != null) mTickDefaultLabel else DEFAULT_TICK_LABEL
            val mConnectingLineColor = ta.getColor(R.styleable.RangeBar_mrb_connectingLineColor,
                    DEFAULT_CONNECTING_LINE_COLOR)
            mActiveConnectingLineColor = mConnectingLineColor
            val colors = ta.getTextArray(R.styleable.RangeBar_mrb_connectingLineColors)
            if (colors != null && colors.size > 0) {
                for (colorHex in colors) {
                    var hexString = colorHex.toString()
                    if (hexString.length == 4) hexString += "000"
                    mConnectingLineColors!!.add(Color.parseColor(hexString))
                }
            } else {
                mConnectingLineColors!!.add(mConnectingLineColor)
            }
            mActiveConnectingLineColors = ArrayList(mConnectingLineColors!!)
            isRangeBar = ta.getBoolean(R.styleable.RangeBar_mrb_rangeBar, true)
            mArePinsTemporary = ta.getBoolean(R.styleable.RangeBar_mrb_temporaryPins, true)
            mIsBarRounded = ta.getBoolean(R.styleable.RangeBar_mrb_rangeBar_rounded, false)
            val density = mDisplayMetrices.density
            mMinPinFont = ta.getDimension(R.styleable.RangeBar_mrb_pinMinFont,
                    DEFAULT_MIN_PIN_FONT_SP * density)
            mMaxPinFont = ta.getDimension(R.styleable.RangeBar_mrb_pinMaxFont,
                    DEFAULT_MAX_PIN_FONT_SP * density)
            mTickLabelSize = ta.getDimension(R.styleable.RangeBar_mrb_tickLabelSize,
                    DEFAULT_TICK_LABEL_FONT_SP * density)
            isRangeBar = ta.getBoolean(R.styleable.RangeBar_mrb_rangeBar, true)
            mOnlyOnDrag = ta.getBoolean(R.styleable.RangeBar_mrb_onlyOnDrag, false)
        } finally {
            ta.recycle()
        }
    }

    /**
     * Creates a new mBar
     */
    private fun createBar() {
        mBar = Bar(context,
                marginLeft,
                yPos,
                barLength,
                tickCount,
                mTickHeight,
                mTickDefaultColor,
                mTickColors,
                mBarWeight,
                mBarColor,
                mIsBarRounded,
                mTickLabelColor,
                mTickLabelSelectedColor,
                mTickTopLabels,
                mTickBottomLabels,
                mTickDefaultLabel,
                mTickLabelSize)
        invalidate()
    }

    /**
     * Creates a new ConnectingLine.
     */
    private fun createConnectingLine() {
        mConnectingLine = ConnectingLine(yPos,
                mConnectingLineWeight,
                mConnectingLineColors!!)
        invalidate()
    }

    /**
     * Creates two new Pins.
     */
    private fun createPins() {
        val ctx = context
        val yPos = yPos
        var expandedPinRadius = 0.0f
        if (isEnabled) {
            expandedPinRadius = mExpandedPinRadius / mDisplayMetrices.density
        }
        if (isRangeBar && mLeftThumb == null) {
            mLeftThumb = PinView(ctx)
            mLeftThumb!!.init(ctx, yPos, expandedPinRadius, mPinColor, mTextColor, mThumbSize, mThumbColorLeft, mThumbBoundaryColor, mThumbBoundarySize,
                    mMinPinFont, mMaxPinFont, mArePinsTemporary)
        }
        if (mRightThumb == null) {
            mRightThumb = PinView(ctx)
            mRightThumb!!.init(ctx, yPos, expandedPinRadius, mPinColor, mTextColor, mThumbSize, mThumbColorRight, mThumbBoundaryColor, mThumbBoundarySize,
                    mMinPinFont, mMaxPinFont, mArePinsTemporary)

        }
        val marginLeft = marginLeft
        val barLength = barLength

        // Initialize thumbs to the desired indices
        if (isRangeBar) {
            mLeftThumb!!.x = marginLeft + leftIndex / (tickCount - 1).toFloat() * barLength
            mLeftThumb!!.setXValue(getPinValue(leftIndex))
        }
        mRightThumb!!.x = marginLeft + rightIndex / (tickCount - 1).toFloat() * barLength
        mRightThumb!!.setXValue(getPinValue(rightIndex))
        invalidate()
    }

    /**
     * Get marginLeft in each of the public attribute methods.
     *
     * @return float marginLeft
     */
    private val marginLeft: Float
        private get() = Math.max(mExpandedPinRadius, mThumbSize)

    /**
     * Get yPos in each of the public attribute methods.
     *
     * @return float yPos
     */
    private val yPos: Float
        private get() = height - mBarPaddingBottom

    /**
     * Get barLength in each of the public attribute methods.
     *
     * @return float barLength
     */
    private val barLength: Float
        private get() = width - 2 * marginLeft

    /**
     * Returns if either index is outside the range of the tickCount.
     *
     * @param leftThumbIndex  Integer specifying the left thumb index.
     * @param rightThumbIndex Integer specifying the right thumb index.
     * @return boolean If the index is out of range.
     */
    private fun indexOutOfRange(leftThumbIndex: Int, rightThumbIndex: Int): Boolean {
        return leftThumbIndex < 0 || leftThumbIndex >= tickCount || rightThumbIndex < 0 || rightThumbIndex >= tickCount
    }

    /**
     * Returns if either value is outside the range of the tickCount.
     *
     * @param leftThumbValue  Float specifying the left thumb value.
     * @param rightThumbValue Float specifying the right thumb value.
     * @return boolean If the index is out of range.
     */
    private fun valueOutOfRange(leftThumbValue: Float, rightThumbValue: Float): Boolean {
        return leftThumbValue < mTickStart || leftThumbValue > mTickEnd || rightThumbValue < mTickStart || rightThumbValue > mTickEnd
    }

    /**
     * If is invalid tickCount, rejects. TickCount must be greater than 1
     *
     * @param tickCount Integer
     * @return boolean: whether tickCount > 1
     */
    private fun isValidTickCount(tickCount: Int): Boolean {
        return tickCount > 1
    }

    /**
     * Gets the distance between x and the left pin. If the left and right pins are equal, this
     * returns 0 if x is < the pins' position. Also returns 0 if the bar is not a range bar.
     *
     * @param x the x-coordinate to be checked
     * @return the distance between x and the left pin, or 0 if the pins are equal and x is to the left.
     * Also returns 0 if the bar is not a range bar.
     */
    private fun getLeftThumbXDistance(x: Float): Float {
        return if (isRangeBar) {
            val leftThumbX = mLeftThumb!!.x
            if (leftThumbX == mRightThumb!!.x && x < leftThumbX) 0f else Math.abs(leftThumbX - x)
        } else {
            0f
        }
    }

    /**
     * Gets the distance between x and the right pin
     *
     * @param x the x-coordinate to be checked
     * @return the distance between x and the right pin
     */
    private fun getRightThumbXDistance(x: Float): Float {
        return abs(mRightThumb!!.x - x)
    }

    /**
     * Handles a [android.view.MotionEvent.ACTION_DOWN] event.
     *
     * @param x the x-coordinate of the down action
     * @param y the y-coordinate of the down action
     */
    private fun onActionDown(x: Float, y: Float) {
        if (isRangeBar) {
            if (!mRightThumb!!.isPressed && mLeftThumb!!.isInTargetZone(x, y)) {
                pressPin(mLeftThumb)
            } else if (!mLeftThumb!!.isPressed && mRightThumb!!.isInTargetZone(x, y)) {
                pressPin(mRightThumb)
            }
        } else {
            if (mRightThumb!!.isInTargetZone(x, y)) {
                pressPin(mRightThumb)
            }
        }
        mDragging = true
        if (mListener != null) mListener!!.onTouchStarted(this)
    }

    /**
     * Handles a [MotionEvent.ACTION_UP] or
     * [MotionEvent.ACTION_CANCEL] event.
     *
     * @param x the x-coordinate of the up action
     * @param y the y-coordinate of the up action
     */
    private fun onActionUp(x: Float, y: Float) {
        var x = x
        if (isRangeBar && mLeftThumb!!.isPressed) {
            releasePin(mLeftThumb!!)
        } else if (mRightThumb!!.isPressed) {
            releasePin(mRightThumb!!)
        } else if (!mOnlyOnDrag) {
            val leftThumbXDistance = getLeftThumbXDistance(x)
            val rightThumbXDistance = getRightThumbXDistance(x)
            val moveLeftThumb = leftThumbXDistance < rightThumbXDistance
            if (moveLeftThumb && x > mLeftBoundX) {
                x = mLeftBoundX
            } else if (!moveLeftThumb && x < mRightBoundX) {
                x = mRightBoundX
            }
            //move if is rangeBar and left index is lower of right one
            //if is not range bar leftThumbXDistance is always 0
            if (moveLeftThumb && isRangeBar) {
                mLeftThumb!!.x = x
                releasePin(mLeftThumb!!)
            } else {
                mRightThumb!!.x = x
                releasePin(mRightThumb!!)
            }

            // Get the updated nearest tick marks for each thumb.
            val newLeftIndex = if (isRangeBar) mBar!!.getNearestTickIndex(mLeftThumb!!) else 0
            val newRightIndex = mBar!!.getNearestTickIndex(mRightThumb!!)
            // If either of the indices have changed, update and call the listener.
            if (newLeftIndex != leftIndex || newRightIndex != rightIndex) {
                leftIndex = newLeftIndex
                rightIndex = newRightIndex
                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                            getPinValue(leftIndex),
                            getPinValue(rightIndex))
                }
            }
        }
        mDragging = false
        if (mListener != null) mListener!!.onTouchEnded(this)
    }

    /**
     * Handles a [MotionEvent.ACTION_MOVE] event.
     *
     * @param x the x-coordinate of the move event
     */
    private fun onActionMove(x: Float) {
        var x = x
        if (isRangeBar && mRightThumb!!.isPressed && x < mRightBoundX) {
            x = mRightBoundX
        } else if (isRangeBar && mLeftThumb!!.isPressed && x > mLeftBoundX) {
            x = mLeftBoundX
        }

        // Move the pressed thumb to the new x-position.
        if (isRangeBar && mLeftThumb!!.isPressed) {
            movePin(mLeftThumb, x)
        } else if (mRightThumb!!.isPressed) {
            movePin(mRightThumb, x)
        }

        // If the thumbs have switched order, fix the references.
        if (isRangeBar && mLeftThumb!!.x > mRightThumb!!.x) {
            val temp = mLeftThumb
            mLeftThumb = mRightThumb
            mRightThumb = temp
        }

        // Get the updated nearest tick marks for each thumb.
        var newLeftIndex = if (isRangeBar) mBar!!.getNearestTickIndex(mLeftThumb!!) else 0
        var newRightIndex = mBar!!.getNearestTickIndex(mRightThumb!!)
        val componentLeft = paddingLeft
        val componentRight = right - paddingRight - componentLeft
        if (x <= componentLeft) {
            newLeftIndex = 0
            movePin(mLeftThumb, mBar!!.leftX)
        } else if (x >= componentRight) {
            newRightIndex = tickCount - 1
            movePin(mRightThumb, mBar!!.rightX)
        }

        /// end added code
        // If either of the indices have changed, update and call the listener.
        if (newLeftIndex != leftIndex || newRightIndex != rightIndex) {
            leftIndex = newLeftIndex
            rightIndex = newRightIndex
            if (isRangeBar) {
                mLeftThumb!!.setXValue(getPinValue(leftIndex))
            }
            mRightThumb!!.setXValue(getPinValue(rightIndex))
            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex),
                        getPinValue(rightIndex))
            }
        }
    }

    /**
     * Set the thumb to be in the pressed state and calls invalidate() to redraw
     * the canvas to reflect the updated state.
     *
     * @param thumb the thumb to press
     */
    private fun pressPin(thumb: PinView?) {
        if (mFirstSetTickCount) {
            mFirstSetTickCount = false
        }
        if (mArePinsTemporary) {
            val animator = ValueAnimator.ofFloat(0f, mExpandedPinRadius)
            animator.addUpdateListener { animation ->
                mThumbRadiusDP = animation.animatedValue as Float
                thumb!!.setSize(mThumbRadiusDP, mPinPadding * animation.animatedFraction)
                invalidate()
            }
            animator.start()
        }
        thumb!!.press()
    }

    /**
     * Set the thumb to be in the normal/un-pressed state and calls invalidate()
     * to redraw the canvas to reflect the updated state.
     *
     * @param thumb the thumb to release
     */
    private fun releasePin(thumb: PinView) {
        val nearestTickX = mBar!!.getNearestTickCoordinate(thumb)
        thumb.x = nearestTickX
        val tickIndex = mBar!!.getNearestTickIndex(thumb)
        thumb.setXValue(getPinValue(tickIndex))
        if (mArePinsTemporary) {
            val animator = ValueAnimator.ofFloat(mExpandedPinRadius, 0f)
            animator.addUpdateListener { animation ->
                mThumbRadiusDP = animation.animatedValue as Float
                thumb.setSize(mThumbRadiusDP,
                        mPinPadding - mPinPadding * animation.animatedFraction)
                invalidate()
            }
            animator.start()
        } else {
            invalidate()
        }
        thumb.release()
    }

    /**
     * Set the value on the thumb pin, either from map or calculated from the tick intervals
     * Integer check to format decimals as whole numbers
     *
     * @param tickIndex the index to set the value for
     */
    private fun getPinValue(tickIndex: Int): String {
        if (mPinTextListener != null) {
            return mPinTextListener!!.getPinValue(this, tickIndex)
        }
        val tickValue = if (tickIndex == tickCount - 1) mTickEnd else tickIndex * mTickInterval + mTickStart
        var xValue = mTickMap!![tickValue]
        if (xValue == null) {
            xValue = if (tickValue.toDouble() == Math.ceil(tickValue.toDouble())) {
                (tickValue.toInt()).toString()
            } else {
                tickValue.toString()
            }
        }
        return mPinTextFormatter.getText(xValue)
    }

    /**
     * Loads list of colors and sets default
     *
     * @param colors
     * @return ArrayList<Integer>
    </Integer> */
    private fun getColors(colors: Array<CharSequence>?, defaultColor: Int): ArrayList<Int> {
        val colorList = ArrayList<Int>()
        if (colors != null && colors.size > 0) {
            for (colorHex in colors) {
                var hexString = colorHex.toString()
                if (hexString.length == 4) hexString += "000"
                colorList.add(Color.parseColor(hexString))
            }
        } else {
            colorList.add(defaultColor)
        }
        return colorList
    }

    /**
     * Moves the thumb to the given x-coordinate.
     *
     * @param thumb the thumb to move
     * @param x     the x-coordinate to move the thumb to
     */
    private fun movePin(thumb: PinView?, x: Float) {

        // If the user has moved their finger outside the range of the bar,
        // do not move the thumbs past the edge.
        if (x < mBar!!.leftX || x > mBar!!.rightX) {
            // Do nothing.
        } else if (thumb != null) {
            thumb.x = x
            invalidate()
        }
    }

    /**
     * Updates the Thumbs bounds based on the minimum distance, to their right and their left respectively.
     */
    private fun updateThumbBounds() {
        mMinIndexDistance = Math.ceil(mDesiredMinDistance / mTickInterval.toDouble()).toInt()
        if (mMinIndexDistance > tickCount - 1) {
            Log.e(TAG, "Desired thumb distance greater than total range.")
            mMinIndexDistance = tickCount - 1
        }
        val maxIndexLeft = rightIndex - mMinIndexDistance
        val minIndexRight = leftIndex + mMinIndexDistance
        mLeftBoundX = mBar!!.getTickX(Math.max(0, maxIndexLeft))
        mRightBoundX = mBar!!.getTickX(Math.min(tickCount - 1, minIndexRight))
    }

    /**
     * This flag is useful for tracking touch events that were meant as scroll events.
     * Copied from hidden method of [View] isInScrollingContainer.
     *
     * @return true if any of this View parents is a scrolling View.
     */
    private val isInScrollingContainer: Boolean
        private get() {
            var p = parent
            while (p is ViewGroup) {
                if (p.shouldDelayChildPressedState()) {
                    return true
                }
                p = p.getParent()
            }
            return false
        }

    /**
     * Sets the minimum desired distance between Thumbs.
     *
     * @param distance The desired minimum distance
     */
    fun setMinimumThumbDistance(distance: Float) {
        mDesiredMinDistance = distance
    }
    // Inner Classes ///////////////////////////////////////////////////////////
    /**
     * A callback that notifies clients when the RangeBar has changed. The
     * listener will only be called when either thumb's index has changed - not
     * for every movement of the thumb.
     */
    interface OnRangeBarChangeListener {
        fun onRangeChangeListener(rangeBar: RangeBar?, leftPinIndex: Int,
                                  rightPinIndex: Int, leftPinValue: String?, rightPinValue: String?)

        fun onTouchStarted(rangeBar: RangeBar?)
        fun onTouchEnded(rangeBar: RangeBar?)
    }

    interface PinTextFormatter {
        fun getText(value: String): String
    }

    /**
     * @author robmunro
     * A callback that allows getting pin text exernally
     */
    interface OnRangeBarTextListener {
        fun getPinValue(rangeBar: RangeBar?, tickIndex: Int): String
    }

    companion object {
        // Member Variables ////////////////////////////////////////////////////////
        private const val TAG = "RangeBar"

        // Default values for variables
        private const val DEFAULT_TICK_START = 0f
        private const val DEFAULT_TICK_END = 5f
        private const val DEFAULT_TICK_INTERVAL = 1f
        private const val DEFAULT_MIN_DISTANCE = -1f
        private const val DEFAULT_TICK_HEIGHT_DP = 1f
        private const val DEFAULT_PIN_PADDING_DP = 16f
        const val DEFAULT_MIN_PIN_FONT_SP = 8f
        const val DEFAULT_MAX_PIN_FONT_SP = 24f
        private const val DEFAULT_BAR_WEIGHT_DP = 2f
        private const val DEFAULT_CIRCLE_BOUNDARY_SIZE_DP = 0f
        private const val DEFAULT_BAR_COLOR = Color.LTGRAY
        private const val DEFAULT_TEXT_COLOR = Color.WHITE
        private const val DEFAULT_TICK_COLOR = Color.BLACK
        private const val DEFAULT_TICK_LABEL_COLOR = Color.LTGRAY
        private const val DEFAULT_TICK_LABEL_SELECTED_COLOR = Color.BLACK
        private const val DEFAULT_TICK_LABEL = ""
        const val DEFAULT_TICK_LABEL_FONT_SP = 4f

        // Corresponds to material indigo 500.
        private const val DEFAULT_PIN_COLOR = -0xc0ae4b
        private const val DEFAULT_CONNECTING_LINE_WEIGHT_DP = 4f

        // Corresponds to material indigo 500.
        private const val DEFAULT_CONNECTING_LINE_COLOR = -0xc0ae4b
        private const val DEFAULT_EXPANDED_PIN_RADIUS_DP = 12f
        private const val DEFAULT_CIRCLE_SIZE_DP = 5f
        private const val DEFAULT_BAR_PADDING_BOTTOM_DP = 24f
    }
}