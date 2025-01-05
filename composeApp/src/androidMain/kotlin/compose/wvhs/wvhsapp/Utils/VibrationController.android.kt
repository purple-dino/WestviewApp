package compose.wvhs.wvhsapp.Utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build

object ContextHolder {
    lateinit var contextProvider: ContextProvider
}

class AndroidContextProvider(private val application: Context) : ContextProvider {
    override fun getApplicationContext(): Any {
        return application.applicationContext
    }
}



actual suspend fun vibrate(length: String) {
    if (length == "short") {
        val applicationContext = ContextHolder.contextProvider.getApplicationContext() as Context
        val vibrator = (applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(50)
        }
    } else if (length == "long") {
        val applicationContext = ContextHolder.contextProvider.getApplicationContext() as Context
        val vibrator = (applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }
}