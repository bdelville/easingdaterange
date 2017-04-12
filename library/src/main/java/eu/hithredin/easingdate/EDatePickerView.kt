package eu.hithredin.easingdate

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

enum class TimeMode {
    LINEAR, QUADRATIC
}

val MIN = 1000 * 60
val HOUR = MIN * 60
val DAY = HOUR * 24

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
    var maxDate: Long = 0.toLong()
    var lowerDate: Long = minDate
    var upperDate: Long = maxDate
    var mode: TimeMode = TimeMode.LINEAR

    private val date:DateFormat = SimpleDateFormat.getDateInstance()
    private val time:DateFormat = SimpleDateFormat.getTimeInstance()
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

    fun init(attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.date_picker, this, true)
        textMinDate = findViewById(R.id.text_date_min) as TextView
        textMaxDate = findViewById(R.id.text_date_max) as TextView
        rangeSlider = findViewById(R.id.range_slider) as MaterialRangeSlider
        rangeSlider.rangeSliderListener = this
    }

    override fun onUpperChanged(newValue: Int) {
        upperDate = toDate(newValue)
        textMaxDate.text = date.format(Date(upperDate))
        dateChanged?.onDateChanged(lowerDate, upperDate)
    }

    override fun onLowerChanged(newValue: Int){
        lowerDate = toDate(newValue)
        textMinDate.text = date.format(Date(lowerDate))
        dateChanged?.onDateChanged(lowerDate, upperDate)
    }

    override fun onEndAction(lower: Int, upper: Int) {
        dateChangeSet?.onDateChanged(lowerDate, upperDate)
    }

    // TODO Display meaningful date (date / date range / date hours according to precision)
    fun toDate(slider: Int): Long {
        when (mode) {
            TimeMode.LINEAR -> return minDate + ((maxDate - minDate) * slider) / rangeSlider.DEFAULT_MAX
            TimeMode.QUADRATIC -> return 0
        }
    }

    // TODO Make it great:
    // - Parametrable: adapt display Date according to difference between them and Now

    // TODO Make range configurable, or even better dynamic (if range small the slider itself expands in a nice animation
    // TODO the width of the bar could represent the precision
}
