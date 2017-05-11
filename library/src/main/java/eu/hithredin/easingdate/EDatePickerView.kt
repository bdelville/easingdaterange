package eu.hithredin.easingdate

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Easing of the date selector
 */
enum class TimeMode {
    /** No preference */
    LINEAR,
    /** Medium precision in the middle of the range */
    QUADRATIC,
    /** High precision in the middle of the range */
    CUBIC,
    /** High precision in the extremum of the range */
    ATAN
}

val MIN: Long = 1000 * 60
val HOUR: Long = MIN * 60
val DAY: Long = HOUR * 24
val MONTH: Long = DAY * 30

interface DateRangeChangeListener {
    fun onDateChanged(lowerDate: Long, upperDate: Long)
}

/**
 * INSERT DOC
 */
class EDatePickerView : RelativeLayout, RangeSliderListener {
    lateinit var textMinDate: TextView
    lateinit var textMaxDate: TextView
    lateinit var rangeSlider: MaterialRangeSlider

    var minDate: Long = 0.toLong()
        set(date) {
            field = date
            if (pivotDate == 0L) setDefaultPivot()
            onLowerChanged(rangeSlider.selectedMin)
        }
    var maxDate: Long = 0.toLong()
        set(date) {
            field = date
            if (pivotDate == 0L) setDefaultPivot()
            onUpperChanged(rangeSlider.selectedMax)
        }
    var pivotDate: Long = 0.toLong()
    var lowerDate: Long = minDate; private set
    var upperDate: Long = maxDate; private set
    var mode: TimeMode = TimeMode.LINEAR

    private val dateYearFormat: DateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
    private val dateFormat: DateFormat = SimpleDateFormat("EEE, MMMM dd", Locale.getDefault())
    private val timeFormat: DateFormat = SimpleDateFormat("dd/MM, HH:mm:ss", Locale.getDefault())

    var dateChanged: DateRangeChangeListener? = null
    var dateChangeSet: DateRangeChangeListener? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    fun setDefaultPivot() {
        if(maxDate > 0L && minDate > 0L){
            pivotDate = minDate + (maxDate - minDate) / 2
        } else {
            pivotDate = if(minDate == 0L) maxDate else minDate
        }
    }

    fun init(attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.date_picker, this, true)
        textMinDate = findViewById(R.id.text_date_min) as TextView
        textMaxDate = findViewById(R.id.text_date_max) as TextView
        rangeSlider = findViewById(R.id.range_slider) as MaterialRangeSlider
        rangeSlider.rangeSliderListener = this

        // TODO Improve Text Style using Material design
    }

    override fun onUpperChanged(newValue: Int) {
        upperDate = toDate(newValue)
        textMaxDate.text = toDateString(upperDate)
        dateChanged?.onDateChanged(lowerDate, upperDate)
    }

    override fun onLowerChanged(newValue: Int) {
        lowerDate = toDate(newValue)
        textMinDate.text = toDateString(lowerDate)
        dateChanged?.onDateChanged(lowerDate, upperDate)
    }

    /**
     * Display date format according to delta with Now
     */
    fun toDateString(date: Long): String {
        val deltaNow = Math.abs(System.currentTimeMillis() - date)
        if (deltaNow < DAY) {
            return timeFormat.format(Date(date))
        }
        if (deltaNow < MONTH) {
            return dateFormat.format(Date(date))
        }
        return dateYearFormat.format(Date(date))
    }

    override fun onEndAction(lower: Int, upper: Int) {
        dateChangeSet?.onDateChanged(lowerDate, upperDate)
    }

    /**
     * Convert the slider value [0-DEFAULT_MAX] to a Date
     * We assume the maxDate is always respected (not the min Date)
     * TODO optimize function parameters calculations
     * TODO Allow a dynamic range, the slider itself expands in a nice animation when reaching borders
     */
    fun toDate(slider: Int): Long {
        val xPivot  = (pivotDate - minDate) * rangeSlider.range / (maxDate - minDate)
        val x: Double = (slider - xPivot).toDouble()
        val D: Double = (pivotDate - minDate).toDouble() // Delta X to apply

        when (mode) {
            TimeMode.LINEAR -> {
                val rangeDate = (maxDate - minDate) * slider
                return minDate + (rangeDate / rangeSlider.range)
            }
            TimeMode.CUBIC -> {
                val alpha: Double = (maxDate - pivotDate) / Math.pow(rangeSlider.max.toDouble() - xPivot, 3.0)
                return (pivotDate + Math.pow(x, 3.0) * alpha).toLong()
            }
            TimeMode.QUADRATIC -> {
                val alpha: Double = (maxDate - pivotDate) / Math.pow(rangeSlider.max.toDouble() - xPivot, 2.0)
                return (pivotDate + Math.pow(Math.abs(x), 2.0) * alpha * Math.signum(x)).toLong()
            }
            TimeMode.ATAN -> {
                val alpha: Double = (maxDate - pivotDate) / Math.atan(rangeSlider.max.toDouble() - xPivot)
                return (pivotDate + Math.atan(x) * alpha).toLong()
            }
        }
    }
}
