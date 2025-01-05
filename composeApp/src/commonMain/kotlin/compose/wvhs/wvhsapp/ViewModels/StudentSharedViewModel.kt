package compose.wvhs.wvhsapp.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.liftric.kvault.KVault
import compose.wvhs.wvhsapp.DataClasses.Attendance
import compose.wvhs.wvhsapp.DataClasses.Classes
import compose.wvhs.wvhsapp.DataClasses.Gradebook
import compose.wvhs.wvhsapp.StudentVUE
import compose.wvhs.wvhsapp.ViewModels.ScheduleViewModel.Period

// Create shared view model
class StudentSharedViewModel : ViewModel() {
    var gradebook: Gradebook? = null
    var classes: Classes? = null
    var store: KVault? = null
    var gradingPeriods: List<String>? by mutableStateOf(null)
    var selectedGradingPeriod: Int? by mutableStateOf(null)
    var student: StudentVUE? by mutableStateOf(null)
    var currentBellScheduleType: String? by mutableStateOf(null)
    var attendance: Attendance? by mutableStateOf(null)
    var todaysBellSchedule: List<Period>? by mutableStateOf(null)

    fun changeBellSchedule(newSchedule: List<Period>?) {
        todaysBellSchedule = newSchedule
    }
    fun changeGradebook(newGradebook: Gradebook) {
        gradebook = newGradebook
    }
    fun setClassList(newClasses: Classes) {
        classes = newClasses
    }
    fun setViewmodelStore(newStore: KVault) {
        store = newStore
    }
    fun changeGradingPeriods(newGradingPeriods: List<String>) {
        gradingPeriods = newGradingPeriods
    }
    fun changeSelectedGradingPeriod(newGradingPeriod: Int) {
        selectedGradingPeriod = newGradingPeriod
    }
    fun changeStudent(newStudent: StudentVUE) {
        student = newStudent
    }
    fun changeBellScheduleType(newBellSchedule: String?) {
        currentBellScheduleType = newBellSchedule
    }
    fun changeAttendance(newAttendance: Attendance) {
        attendance = newAttendance
    }
    fun resetAllData() {
        gradebook = null
        classes = null
        store = null
        gradingPeriods = null
        selectedGradingPeriod = null
        student = null
        attendance = null
        todaysBellSchedule = null
        currentBellScheduleType = null
    }
}