package eu.hithredin.easingdate

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout

/**
 * INSERT DOC
 */
class EDatePickerView : RelativeLayout {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun init(attrs: AttributeSet) {
        LayoutInflater.from(context).inflate(R.layout.date_picker, this, true)
    }

    // TODO Extract Range slider picker from
    // TODO https://github.com/twotoasters/MaterialRangeSlider/blob/master/app/src/main/java/com/ticketmaster/mobilestudio/materialrangeslider/MaterialRangeSlider.java

    // TODO Display meaningful date (date / date range / date hours according to precision)

    // TODO Make range configurable, or even better dynamic (if range small the slider itself expands in a nice animation
    // TODO the width of the bar could represent the precision

    //TODO Then just unit tests and ease of use integration
}
