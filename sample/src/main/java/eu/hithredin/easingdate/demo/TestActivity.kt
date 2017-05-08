package eu.hithredin.easingdate.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import eu.hithredin.easingdate.*

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        initPicker(findViewById(R.id.date_picker) as EDatePickerView, 35 * DAY, TimeMode.LINEAR)
        initPicker(findViewById(R.id.date_picker2) as EDatePickerView, 35 * DAY, TimeMode.CUBIC)
        initPicker(findViewById(R.id.date_picker3) as EDatePickerView, 35 * DAY, TimeMode.QUADRATIC)
        initPicker(findViewById(R.id.date_picker4) as EDatePickerView, 5 * HOUR, TimeMode.ATAN)
    }

    fun initPicker(picker: EDatePickerView, delta: Long, mode: TimeMode){
        val now = System.currentTimeMillis()
        picker.minDate = now - delta
        picker.maxDate = now + delta
        picker.mode = mode

        picker.dateChangeSet = object : DateRangeChangeListener {
            override fun onDateChanged(lowerDate: Long, upperDate: Long) {
                Toast.makeText(this@TestActivity, "Date set", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
