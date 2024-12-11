package compose.wvhs.wvhsapp

import kotlinx.coroutines.delay
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle


actual suspend fun vibrate(length: String) {
    if (length == "short") {
        val feedbackGenerator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleRigid)
        feedbackGenerator.prepare()
        feedbackGenerator.impactOccurred()
    }
    else {
        // iOS doesn't have a direct API for vibration, but we can use the AudioServicesPlaySystemSound function
        // to play a system sound that vibrates the device
        platform.AudioToolbox.AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
        delay(400)
        platform.AudioToolbox.AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
        delay(400)
        platform.AudioToolbox.AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
    }
}
