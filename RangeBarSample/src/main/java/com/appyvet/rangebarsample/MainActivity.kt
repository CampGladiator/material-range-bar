package com.appyvet.rangebarsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.Window
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.appyvet.materialrangebar.RangeBar
import com.appyvet.materialrangebar.RangeBar.OnRangeBarChangeListener
import com.appyvet.rangebarsample.colorpicker.ColorPickerDialog
import com.appyvet.rangebarsample.colorpicker.ColorPickerDialog.OnColorSelectedListener
import com.appyvet.rangebarsample.colorpicker.Utils

class MainActivity : FragmentActivity(), OnColorSelectedListener {
    // Sets variables to save the colors of each attribute
    private var mBarColor = 0
    private var mConnectingLineColor = 0
    private var mPinColor = 0
    private var mTextColor = 0
    private var mTickColor = 0

    // Initializes the RangeBar in the application
    private var rangebar: RangeBar? = null
    private var mThumbColor = 0
    private var mThumbBoundaryColor = 0
    private var mTickLabelColor = 0
    private var mTickLabelSelectedColor = 0

    // Saves the state upon rotating the screen/restarting the activity
    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putInt("BAR_COLOR", mBarColor)
        bundle.putInt("CONNECTING_LINE_COLOR", mConnectingLineColor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Removes title bar and sets content view
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        // Sets fonts for all
//        Typeface font = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
////        ViewGroup root = (ViewGroup) findViewById(R.id.mylayout);
////        setFont(root, font);

        // Gets the buttons references for the buttons
        val barColor = findViewById<TextView>(R.id.barColor)
        val thumbBoundaryColor = findViewById<TextView>(R.id.thumbBoundaryColor)
        val connectingLineColor = findViewById<TextView>(R.id.connectingLineColor)
        val pinColor = findViewById<TextView>(R.id.pinColor)
        val pinTextColor = findViewById<TextView>(R.id.textColor)
        val tickColor = findViewById<TextView>(R.id.tickColor)
        val thumbColor = findViewById<TextView>(R.id.thumbColor)
        val rangeButton = findViewById<TextView>(R.id.enableRange)
        val disabledButton = findViewById<TextView>(R.id.disable)
        val tickBottomLabelsButton = findViewById<TextView>(R.id.toggleTickBottomLabels)
        val tickTopLabelsButton = findViewById<TextView>(R.id.toggleTickTopLabels)
        val tickLabelColor = findViewById<TextView>(R.id.tickLabelColor)
        val tickLabelSelectedColor = findViewById<TextView>(R.id.tickLabelSelectColor)
        val tvLeftIndex = findViewById<TextView>(R.id.tvLeftIndex)
        val tvRightIndex = findViewById<TextView>(R.id.tvRightIndex)
        val tvLeftValue = findViewById<TextView>(R.id.tvLeftValue)
        val tvRightValue = findViewById<TextView>(R.id.tvRightValue)


        // Gets the RangeBar
        rangebar = findViewById(R.id.rangebar1)
        rangeButton.setOnClickListener { rangebar!!.setRangeBarEnabled(!rangebar!!.isRangeBar) }
        disabledButton.setOnClickListener { rangebar!!.isEnabled = !rangebar!!.isEnabled }

        // Sets the display values of the indices
        rangebar!!.setOnRangeBarChangeListener(object : OnRangeBarChangeListener {
            override fun onRangeChangeListener(rangeBar: RangeBar?, leftPinIndex: Int,
                                               rightPinIndex: Int, leftPinValue: String?, rightPinValue: String?) {
                tvLeftIndex.text = String.format("Left Index %d", leftPinIndex)
                tvRightIndex.text = String.format("Right Index %d", rightPinIndex)
                tvLeftValue.text = String.format("Left Value %s", leftPinValue)
                tvRightValue.text = String.format("Right Value %s", rightPinValue)
            }

            override fun onTouchEnded(rangeBar: RangeBar?) {
                Log.d("RangeBar", "Touch ended")
            }

            override fun onTouchStarted(rangeBar: RangeBar?) {
                Log.d("RangeBar", "Touch started")
            }
        })

        // Setting Number Attributes -------------------------------

        // Sets tickStart
        val tickStart = findViewById<TextView>(R.id.tickStart)
        val tickStartSeek = findViewById<SeekBar>(R.id.tickStartSeek)
        tickStartSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(tickCountSeek: SeekBar, progress: Int, fromUser: Boolean) {
                try {
                    rangebar!!.tickStart = progress.toFloat()
                } catch (e: IllegalArgumentException) {
                }
                tickStart.text = "tickStart = $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Sets tickEnd
        val tickEnd = findViewById<TextView>(R.id.tickEnd)
        val tickEndSeek = findViewById<SeekBar>(R.id.tickEndSeek)
        tickEndSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(tickCountSeek: SeekBar, progress: Int, fromUser: Boolean) {
                try {
                    rangebar?.tickEnd = progress.toFloat()
                } catch (e: IllegalArgumentException) {
                }
                tickEnd.text = "tickEnd = $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Sets tickInterval
        val tickInterval = findViewById<TextView>(R.id.tickInterval)
        val tickIntervalSeek = findViewById<SeekBar>(R.id.tickIntervalSeek)
        tickIntervalSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(tickCountSeek: SeekBar, progress: Int, fromUser: Boolean) {
                try {
                    rangebar?.setTickInterval(progress.toFloat())
                } catch (e: IllegalArgumentException) {
                }
                tickInterval.text = "tickInterval = $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Sets Minimum Thumb Distance
        val minThumbDistance = findViewById<TextView>(R.id.minThumbDistance)
        val minThumbDistanceSeek = findViewById<SeekBar>(R.id.minThumbDistanceSeek)
        minThumbDistanceSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(tickCountSeek: SeekBar, progress: Int, fromUser: Boolean) {
                // We want minimum value to be -1 instead of 0 (For ignoring the thumb distance) So that is why we subtract -1
                val newProgress = progress - 1
                try {
                    rangebar?.setMinimumThumbDistance(newProgress.toFloat())
                } catch (e: IllegalArgumentException) {
                }
                minThumbDistance.text = "Min Thumb Distance = $newProgress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Sets barWeight
        val barWeight = findViewById<TextView>(R.id.barWeight)
        val barWeightSeek = findViewById<SeekBar>(R.id.barWeightSeek)
        barWeightSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(barWeightSeek: SeekBar, progress: Int, fromUser: Boolean) {
                rangebar?.setBarWeight(convertDpToPixel(progress, this@MainActivity).toFloat())
                barWeight.text = String.format("barWeight = %ddp", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Sets connectingLineWeight
        val connectingLineWeight = findViewById<TextView>(R.id.connectingLineWeight)
        val connectingLineWeightSeek = findViewById<SeekBar>(R.id.connectingLineWeightSeek)
        connectingLineWeightSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(connectingLineWeightSeek: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                rangebar?.setConnectingLineWeight(convertDpToPixel(progress, this@MainActivity).toFloat())
                connectingLineWeight.text = String.format("connectingLineWeight = %ddp", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Sets Pin radius
        val pinRadius = findViewById<TextView>(R.id.pinSize)
        val pinRadiusSeek = findViewById<SeekBar>(R.id.pinSizeSeek)
        pinRadiusSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(thumbRadiusSeek: SeekBar, progress: Int, fromUser: Boolean) {
                rangebar?.setPinRadius(convertDpToPixel(progress, this@MainActivity).toFloat())
                pinRadius.text = String.format("Pin Size = %ddp", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Sets Thumb boundary Radius
        val thumbBoundarySize = findViewById<TextView>(R.id.thumbBoundarySize)
        val thumbBoundarySeek = findViewById<SeekBar>(R.id.thumbBoundarySeek)
        thumbBoundarySeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(thumbRadiusSeek: SeekBar, progress: Int, fromUser: Boolean) {
                rangebar?.setThumbBoundarySize(convertDpToPixel(progress, this@MainActivity))
                thumbBoundarySize.text = String.format("Thumb Boundary Size = %ddp", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Setting Color Attributes---------------------------------

        // Sets barColor
        barColor.setOnClickListener { initColorPicker(Component.BAR_COLOR, mBarColor, mBarColor) }

        // Set tickLabelColor
        tickLabelColor.setOnClickListener { initColorPicker(Component.TICK_LABEL_COLOR, mTickLabelColor, mTickLabelColor) }

        // Set tickLabelSelectedColor
        tickLabelSelectedColor.setOnClickListener { initColorPicker(Component.TICK_LABEL_SELECTED_COLOR, mTickLabelSelectedColor, mTickLabelSelectedColor) }

        // Sets connectingLineColor
        connectingLineColor.setOnClickListener {
            initColorPicker(Component.CONNECTING_LINE_COLOR, mConnectingLineColor,
                    mConnectingLineColor)
        }

        // Sets pinColor
        pinColor.setOnClickListener { initColorPicker(Component.PIN_COLOR, mPinColor, mPinColor) }
        // Sets pinTextColor
        pinTextColor.setOnClickListener { initColorPicker(Component.TEXT_COLOR, mTextColor, mTextColor) }
        // Sets tickColor
        tickColor.setOnClickListener { initColorPicker(Component.TICK_COLOR, mTickColor, mTickColor) }
        // Sets thumbColor
        thumbColor.setOnClickListener { initColorPicker(Component.THUMB_COLOR, mThumbColor, mThumbColor) }
        thumbBoundaryColor.setOnClickListener { initColorPicker(Component.THUMB_BOUNDARY_COLOR, mThumbBoundaryColor, mThumbBoundaryColor) }
        val cbRoundedBar = findViewById<CheckBox>(R.id.cbRoundedBar)
        cbRoundedBar.isChecked = rangebar?.isBarRounded!!
        cbRoundedBar.setOnCheckedChangeListener { buttonView, isChecked -> rangebar?.isBarRounded = isChecked }
        tickTopLabelsButton.setOnClickListener(object : View.OnClickListener {
            private var mLabels: Array<CharSequence> = arrayOf()
            override fun onClick(v: View) {
                val labels = rangebar?.tickTopLabels
                if (labels != null) {
                    mLabels = labels
                    rangebar?.tickTopLabels = null
                } else {
                    rangebar?.tickTopLabels = mLabels
                }
            }
        })
        tickBottomLabelsButton.setOnClickListener(object : View.OnClickListener {
            private var mLabels: Array<CharSequence> = arrayOf()
            override fun onClick(v: View) {
                val labels = rangebar?.tickBottomLabels
                if (labels != null) {
                    mLabels = labels
                    rangebar?.tickBottomLabels = null
                } else {
                    rangebar?.tickBottomLabels = mLabels
                }
            }
        })
        findViewById<View>(R.id.thumbRecyclerView).setOnClickListener { startActivity(Intent(this@MainActivity, RecyclerViewActivity::class.java)) }
    }

    /**
     * Sets the changed color using the ColorPickerDialog.
     *
     * @param component Component specifying which input is being used
     * @param newColor  Integer specifying the new color to be selected.
     */
    override fun onColorSelected(newColor: Int, component: Component?) {
        Log.d("Color selected", " new color = $newColor,compoment = $component")
        val hexColor = String.format("#%06X", 0xFFFFFF and newColor)
        when (component) {
            Component.BAR_COLOR -> {
                mBarColor = newColor
                rangebar!!.setBarColor(newColor)
                val barColorText = findViewById<TextView>(R.id.barColor)
                barColorText.text = "barColor = $hexColor"
                barColorText.setTextColor(newColor)
            }
            Component.TEXT_COLOR -> {
                mTextColor = newColor
                rangebar!!.setPinTextColor(newColor)
                val textColorText = findViewById<TextView>(R.id.textColor)
                textColorText.text = "textColor = $hexColor"
                textColorText.setTextColor(newColor)
            }
            Component.CONNECTING_LINE_COLOR -> {
                mConnectingLineColor = newColor
                rangebar!!.setConnectingLineColor(newColor)
                val connectingLineColorText = findViewById<TextView>(
                        R.id.connectingLineColor)
                connectingLineColorText.text = "connectingLineColor = $hexColor"
                connectingLineColorText.setTextColor(newColor)
            }
            Component.PIN_COLOR -> {
                mPinColor = newColor
                rangebar!!.setPinColor(newColor)
                val pinColorText = findViewById<TextView>(R.id.pinColor)
                pinColorText.text = "pinColor = $hexColor"
                pinColorText.setTextColor(newColor)
            }
            Component.TICK_COLOR -> {
                mTickColor = newColor
                rangebar!!.setTickDefaultColor(newColor)
                val tickColorText = findViewById<TextView>(R.id.tickColor)
                tickColorText.text = "tickColor = $hexColor"
                tickColorText.setTextColor(newColor)
            }
            Component.THUMB_COLOR -> {
                mThumbColor = newColor
                rangebar!!.setThumbColor(newColor)
                val thumbColorText = findViewById<TextView>(R.id.thumbColor)
                thumbColorText.text = "Thumb Color = $hexColor"
                thumbColorText.setTextColor(newColor)
            }
            Component.THUMB_BOUNDARY_COLOR -> {
                mThumbBoundaryColor = newColor
                rangebar!!.setThumbBoundaryColor(newColor)
                val thumbBoundaryColorText = findViewById<TextView>(R.id.thumbBoundaryColor)
                thumbBoundaryColorText.text = "Thumb Boundary Color = $hexColor"
                thumbBoundaryColorText.setTextColor(newColor)
            }
            Component.TICK_LABEL_COLOR -> {
                mTickLabelColor = newColor
                rangebar!!.setTickLabelColor(newColor)
            }
            Component.TICK_LABEL_SELECTED_COLOR -> {
                mTickLabelSelectedColor = newColor
                rangebar!!.setTickLabelSelectedColor(newColor)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /**
     * Initiates the colorPicker from within a button function.
     *
     * @param component    Component specifying which input is being used
     * @param initialColor Integer specifying the initial color choice. *
     * @param defaultColor Integer specifying the default color choice.
     */
    private fun initColorPicker(component: Component, initialColor: Int, defaultColor: Int) {
        val colorPicker: ColorPickerDialog = ColorPickerDialog.Companion.newInstance(R.string.colorPickerTitle, Utils.ColorUtils.colorChoice(this),
                initialColor, 4, ColorPickerDialog.Companion.SIZE_SMALL, component)
        colorPicker.setOnColorSelectedListener(this)
        colorPicker.show(supportFragmentManager, "color")
    }

    companion object {
        // Sets the initial values such that the image will be drawn
        private const val INDIGO_500 = -0xc0ae4b

        /**
         * This method converts dp unit to equivalent pixels, depending on device density.
         *
         * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent px equivalent to dp depending on device density
         */
        fun convertDpToPixel(dp: Int, context: Context): Int {
            return (dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        }

        /**
         * This method converts device specific pixels to density independent pixels.
         *
         * @param px      A value in px (pixels) unit. Which we need to convert into db
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent dp equivalent to px value
         */
        fun convertPixelsToDp(px: Int, context: Context): Int {
            return (px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        }
    }
}