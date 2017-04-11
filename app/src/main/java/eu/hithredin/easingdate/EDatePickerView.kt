package eu.hithredin.easingdate

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import eu.hithredin.easingdate.MaterialRangeSlider.RangeSliderListener
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

enum class TimeMode {
    LINEAR, ELLIPTIC
}

val MIN = 1000 * 60
val HOUR = MIN * 60
val DAY = HOUR * 24

/**
 * INSERT DOC
 */
class EDatePickerView : RelativeLayout, RangeSliderListener {
    lateinit var textMinDate: TextView
    lateinit var textMaxDate: TextView
    lateinit var rangeSlider: MaterialRangeSlider

    var minDate: Long = 0.toLong()
    var maxDate: Long = 0.toLong()
    var mode: TimeMode = TimeMode.LINEAR

    val date:DateFormat = SimpleDateFormat.getDateInstance()
    val time:DateFormat = SimpleDateFormat.getTimeInstance()

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

        val now = System.currentTimeMillis()
        minDate = now - (5 * DAY)
        maxDate = now + (5 * DAY)
    }

    override fun onMaxChanged(newValue: Int) {
        textMaxDate.setText(date.format(toDate(newValue)))
    }

    override fun onMinChanged (newValue: Int){
        textMinDate.setText(date.format(toDate(newValue)))
    }

    // TODO Display meaningful date (date / date range / date hours according to precision)
    fun toDate(slider: Int): Date {
        when (mode) {
            TimeMode.LINEAR -> return Date(minDate + ((maxDate - minDate) * slider) / rangeSlider.DEFAULT_MAX)
            TimeMode.ELLIPTIC -> return Date(0)
        }
    }

    // TODO Make it great:
    // - Text style, paddings
    // - Parametrable: set range limits and adapt display Date according to difference between them and Now

    // TODO Make range configurable, or even better dynamic (if range small the slider itself expands in a nice animation
    // TODO the width of the bar could represent the precision
}
