package eu.hithredin.easingdate

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import java.lang.Exception

/**
 * Singleton that aggregate all infos that can be found about the device.
 * Helps optimizing screen, memory performance according to what's available
 */
class DeviceDateRange (context: Context) {
    val context = context
    var sdk: Int
    val deviceWidth: Int
    val deviceHeight: Int
    val scaleDensity: Float

    init {
        val windowManager = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        deviceWidth = dm.widthPixels
        deviceHeight = dm.heightPixels
        scaleDensity = dm.scaledDensity

        //Sdk version
        try {
            sdk = android.os.Build.VERSION.SDK_INT
        } catch (e: Exception) {
            sdk = 14
            e.printStackTrace()
        }
    }

    /**
     * @param dipValue
     * *
     * @return the pixel value for the provided dip dimension
     */
    fun dipToPixels(dipValue: Float): Int {
        return (dipValue * scaleDensity).toInt()
    }

    val isDpiXXHigh: Boolean
        get() {
            if (scaleDensity > 2) {
                return true
            }
            return false
        }

    val isDpiXHigh: Boolean
        get() {
            if (scaleDensity <= 2 && scaleDensity > 1.5) {
                return true
            }
            return false
        }

    val isDpiHigh: Boolean
        get() {
            if (scaleDensity <= 1.5 && scaleDensity > 1) {
                return true
            }
            return false
        }

    val isDpiMedium: Boolean
        get() {
            if (scaleDensity <= 1 && scaleDensity > 0.75) {
                return true
            }
            return false
        }

    val isDpiLow: Boolean
        get() {
            if (scaleDensity <= 0.75) {
                return true
            }
            return false
        }
}