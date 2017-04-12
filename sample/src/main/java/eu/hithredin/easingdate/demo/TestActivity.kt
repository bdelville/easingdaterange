package eu.hithredin.easingdate.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import eu.hithredin.easingdate.DAY
import eu.hithredin.easingdate.DateRangeChangeListener
import eu.hithredin.easingdate.EDatePickerView

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val picker = findViewById(R.id.date_picker) as EDatePickerView

        val now = System.currentTimeMillis()
        picker.minDate = now - (5 * DAY)
        picker.maxDate = now + (5 * DAY)

        picker.dateChangeSet = object : DateRangeChangeListener {
            override fun onDateChanged(lowerDate: Long, upperDate: Long) {
                Toast.makeText(this@TestActivity, "Date set", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
