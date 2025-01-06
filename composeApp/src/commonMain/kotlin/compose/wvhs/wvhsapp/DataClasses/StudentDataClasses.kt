package compose.wvhs.wvhsapp.DataClasses

import kotlinx.datetime.LocalDate

// Create all of the base data classes
data class Assignment(
    val title: String?,
    val date: String?,
    val type: String?,
    val score: String?,
    val scoreType: String?,
    val points: String?,
    val notes: String?
)

data class GradeBreakdown(
    val type: String?,
    val weight: String?,
    val points: String?,
    val pointsPossible: String?,
    val weightedPercentage: String?
)

data class ClassInfo(
    val period: String?,
    val room: String?,
    val teacher: String?,
    val teacherEmail: String?,
    val overallLetterGrade: String?,
    val overallPercentageGrade: String?,
    val assignments: List<Assignment>,
    val gradeBreakdown: List<GradeBreakdown>
)

data class Gradebook(
    val classes: Map<String?, ClassInfo>
)

data class Classes(
    val classes: List<String>
)

data class Absence(
    val periodsAffected: List<String>?,
    val type: List<String>?,
    val date: LocalDate?
)

data class Attendance(
    val absences: List<Absence>
)