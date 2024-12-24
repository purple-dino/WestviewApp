package compose.wvhs.wvhsapp

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern


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

// Create the studentvue class
class StudentVUE(
    private val username: String,
    private val password: String,
    private val districtDomain: String
) {

    // Create a client to access the api
    private val client = HttpClient {
        install(HttpCookies)
    }

    suspend fun requestAttendance(gradingPeriod: Int? = null, studentSharedViewModel: StudentSharedViewModel) {
        val totalAttendanceInArrayList = ArrayList<Absence>()
        val attendanceList = withContext(Dispatchers.IO) { requestStudentVUEData("Attendance", gradingPeriod) }

        // Parse the data
        val mainData = Ksoup.parse(html = attendanceList, parser = Parser.xmlParser()).select("Attendance").select("Absences").select("Absence").toList()

        // Iterate through and create absences
        for (absence in mainData) {
            // Parse the data some more
            val classList = Regex("Course=\"(.*?)\"").findAll(absence.toString())
                .map { it.groupValues[1] }
                .toList()
            val typeOfAbsence = Regex(" Name=\"(.*?)\"").findAll(absence.toString())
                .map { it.groupValues[1] }
                .toList()

            // All of the date stuff
            val date = Regex("AbsenceDate=\"(.*?)\"").find(absence.toString())?.groupValues?.get(1)
            var formattedDate: LocalDate? = null
            date?.let {
                formattedDate = LocalDate(date.substring(6,10).toInt(), date.substring(0,2).toInt(), date.substring(3,5).toInt())
            }

            val trimmedClassList = ArrayList<String>()
            val trimmedTypeOfAbsenceList = ArrayList<String>()

            typeOfAbsence.forEachIndexed { index, reason ->
                if (reason != "") {
                    trimmedClassList.add(classList[index])
                    trimmedTypeOfAbsenceList.add(reason)
                }
            }

            // Create the absence
            val currentAbsence = Absence(periodsAffected = trimmedClassList, type = trimmedTypeOfAbsenceList, date = formattedDate)

            // Add to the list
            totalAttendanceInArrayList.add(currentAbsence)
        }
        val allAttendance = Attendance(totalAttendanceInArrayList)

        studentSharedViewModel.changeAttendance(allAttendance)
    }

    suspend fun requestClassListAndBellSchedule(studentSharedViewModel: StudentSharedViewModel): Classes {
        val classes = requestStudentVUEData("StudentClassList")
        val classList = Regex("CourseTitle=\"(.*?)\"").findAll(classes)
            .map { it.groupValues[1] }
            .toList()
        val periodList = Regex("ClassListing Period=\"(.*?)\"").findAll(classes)
            .map { it.groupValues[1] }
            .toList()

        val newClassList: MutableMap<Int, String> = mutableMapOf()

        for (period in periodList) {
            if (period == "1" || period == "2" || period == "3" || period == "4") {
                newClassList[period.toInt()] = classList[periodList.indexOf(period)]
            }
        }

        val currentBellSchedule = Regex("BellSchedName=\"(.*?)\"").find(classes)?.groups?.get(1)?.value
        studentSharedViewModel.changeBellSchedule(currentBellSchedule)

//        if (currentBellSchedule != "MonFriBell" && currentBellSchedule != "Tues-Thurs Bell" && currentBellSchedule != "Wed Bell") {
////            createBellSchedule(classes)
//        }
//        createBellSchedule(classes)


        return Classes(newClassList.values.toList())
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    private fun createBellSchedule(classes: String) {
        println(classes)
        val endDates = Regex("EndDate=\"(.*?)\"").findAll(classes)
            .map { it.groupValues[1] }
            .toList()
        val startDates = Regex("StartDate=\"(.*?)\"").findAll(classes)
            .map { it.groupValues[1] }
            .toList()

        println(endDates)
        for (endDate in endDates) {
            var newEndDate: LocalDateTime? = null
            if (endDate.length == 21) {
                newEndDate = if (endDate.substring(20, 21) == "AM") {
                    LocalDateTime.parse(endDate.dropLast(3), format = LocalDateTime.Format { byUnicodePattern("MM/dd/yyyy H:mm:ss") })
                } else {
                    LocalDateTime.parse(endDate.dropLast(3), format = LocalDateTime.Format { byUnicodePattern("MM/dd/yyyy H:mm:ss") })
                }
            } else if (endDate.length == 22){
                newEndDate = LocalDateTime.parse(endDate.dropLast(3), format = LocalDateTime.Format { byUnicodePattern("MM/dd/yyyy HH:mm:ss") })
            }
            println(newEndDate)
        }
        println(startDates)
    }

    suspend fun requestGradingPeriods(studentSharedViewModel: StudentSharedViewModel) {
        withContext(Dispatchers.IO) {
            val gradeBook = requestStudentVUEData("Gradebook") // Request the gradebook
            var gradingPeriods = Regex("GradePeriod=\"(.*?)\"").findAll(gradeBook)
                .map { it.groupValues[1] }
                .toList()

            if (gradingPeriods.size-1 >=0) {
                gradingPeriods = gradingPeriods.subList(
                    0,
                    gradingPeriods.size-1
                )

                gradingPeriods = gradingPeriods.map { it.replace("\\s{2,}".toRegex(), " ") }
                studentSharedViewModel.changeSelectedGradingPeriod(
                    gradingPeriods.size-1
                )
            } else {
                gradingPeriods = emptyList()
            }


            studentSharedViewModel.changeGradingPeriods(gradingPeriods)
        }

    }


    // Get the entire grade book, return a MutableMap<Class Name, Map<String, Any>> (all nullable)
    // For example, return {French 1, {Period = 1}, Assignments = [
    suspend fun requestOrUpdateGradebook(gradingPeriod: Int? = null, studentSharedViewModel: StudentSharedViewModel) {
        withContext(Dispatchers.IO) {
            val classes =
                mutableMapOf<String?, ClassInfo>() // Create an empty mutable map of the gradebook
            val gradeBook =
                requestStudentVUEData("Gradebook", gradingPeriod) // Request the gradebook

            // Get all of the information about each individual course and store to a list of courses
            val allCourseInfo = Regex("<Course (.*?)</Course>").findAll(gradeBook)
                .map { it.groupValues[1] }
                .toList()

            // Iterate through the courses
            for (periodClass in allCourseInfo) {
                val period = Regex("""Period="(.*?)"""").findAll(periodClass)
                    .map { it.groupValues[1] }
                    .toList()[0] // Get the period number
                var className =
                    Regex("""Title="(.*?)"""").find(periodClass)?.groups?.get(1)?.value // Get the class name
                className = className?.replace(""" \(\d{6}\)""".toRegex(), "") ?: ""
                val room =
                    Regex("""Room="(.*?)"""").find(periodClass)?.groups?.get(1)?.value // Get the room number
                val teacher =
                    Regex("""Staff="(.*?)"""").find(periodClass)?.groups?.get(1)?.value // get the teacher name
                val teacherEmail =
                    Regex("""StaffEMail="(.*?)"""").find(periodClass)?.groups?.get(1)?.value // get the teacher email
                val overallLetterGrade =
                    Regex("""CalculatedScoreString="(.*?)"""").find(periodClass)?.groups?.get(1)?.value // Get the overall letter grade (A+!)
                val overallPercentageGrade =
                    Regex("""CalculatedScoreRaw="(.*?)"""").find(periodClass)?.groups?.get(1)?.value // Get the overall percentage grade

                val assignments =
                    Regex("""<Assignment (.*?)>""").findAll(periodClass).map { it.groupValues[1] }
                        .toList() // Get the list of assignments

                val allAssignments =
                    mutableListOf<Assignment>() // Create an empty list of assignments
                // Iterate through each assignment
                for (assignment in assignments) {
                    // Get all assignment data
                    val assignmentTitle =
                        Regex("""Measure="(.*?)"""").find(assignment)?.groups?.get(1)?.value // Get the title of the assignment
                    val assignmentDate =
                        Regex("""Date="(.*?)/\d{4}"""").find(assignment)?.groups?.get(1)?.value // Get the date of the assignment
                    val assignmentType =
                        Regex("""Type="(.*?)"""").find(assignment)?.groups?.get(1)?.value // Get the type of the assignment
                    val assignmentScore =
                        Regex("""Score="(.*?)"""").find(assignment)?.groups?.get(1)?.value // Get the score of the assignment
                    val assignmentScoreType =
                        Regex("""ScoreType="(.*?)"""").find(assignment)?.groups?.get(1)?.value // Get the score type (raw, etc) of the assignment
                    val assignmentPoints =
                        Regex("""Points="(.*?)"""").find(assignment)?.groups?.get(1)?.value // Get the points (5/5) of the assignment
                    val assignmentNotes =
                        Regex("""Notes="(.*?)"""").find(assignment)?.groups?.get(1)?.value // Get any notes of the assignment

                    // Add the assignment data to the allAssignments list
                    allAssignments.add(
                        Assignment(
                            title = assignmentTitle,
                            date = assignmentDate,
                            type = assignmentType,
                            score = assignmentScore,
                            scoreType = assignmentScoreType,
                            points = assignmentPoints,
                            notes = assignmentNotes
                        )
                    )
                }

                val allGradeBreakdown = mutableListOf<GradeBreakdown>()

                val breakdowns = Regex("""<AssignmentGradeCalc (.*?) />""").findAll(periodClass)
                    .map { it.groupValues[1] }.toList()
                for (breakdown in breakdowns) {
                    val breakdownType =
                        Regex("""Type="(.*?)"""").find(breakdown)?.groups?.get(1)?.value
                    val breakdownWeight =
                        Regex("""Weight="(.*?)"""").find(breakdown)?.groups?.get(1)?.value
                    val breakdownPoints =
                        Regex("""Points="(.*?)"""").find(breakdown)?.groups?.get(1)?.value
                    val breakdownPointsPossible =
                        Regex("""PointsPossible="(.*?)"""").find(breakdown)?.groups?.get(1)?.value
                    val breakdownWeightedPercentage =
                        Regex("""WeightedPct="(.*?)"""").find(breakdown)?.groups?.get(1)?.value
                    allGradeBreakdown.add(
                        GradeBreakdown(
                            type = breakdownType,
                            weight = breakdownWeight,
                            points = breakdownPoints,
                            pointsPossible = breakdownPointsPossible,
                            weightedPercentage = breakdownWeightedPercentage
                        )
                    )
                }

                // Add the class to the dictionary of classes
                classes[className] = ClassInfo(
                    period = period,
                    room = room,
                    teacher = teacher,
                    teacherEmail = teacherEmail,
                    overallLetterGrade = overallLetterGrade,
                    overallPercentageGrade = overallPercentageGrade,
                    assignments = allAssignments,
                    gradeBreakdown = allGradeBreakdown
                )
            }

            studentSharedViewModel.changeGradebook(Gradebook(classes))
        }
    }


    // Request the gradebook, returns a string
    private suspend fun requestStudentVUEData(methodName: String, reportingPeriod: Int? = null): String {
        val requestData = """
            <?xml version='1.0' encoding='utf-8'?>
            <soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>
                <soap:Body>
                    <ProcessWebServiceRequest xmlns='http://edupoint.com/webservices/'>
                        <userID>$username</userID>
                        <password>$password</password>
                        <skipLoginLog>1</skipLoginLog>
                        <parent>0</parent>
                        <webServiceHandleName>PXPWebServices</webServiceHandleName>
                        <methodName>$methodName</methodName>
                        <paramStr>&lt;Parms&gt;&lt;ChildIntID&gt;0&lt;/ChildIntID&gt;&lt;ReportPeriod&gt;$reportingPeriod&lt;/ReportPeriod&gt;&lt;/Parms&gt;</paramStr>
                    </ProcessWebServiceRequest>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
        val response: String = try{
            removeXMLSequences(callSoapService(requestData))
        } catch (e: Exception){
            ""
        }
        return response
    }

    // Logs into Synergy. Doesn't really do anything, just checks for username and password.
    suspend fun login(): String {
        val requestData = """
            <?xml version='1.0' encoding='utf-8'?>
            <soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>
                <soap:Body>
                    <ProcessWebServiceRequest xmlns='http://edupoint.com/webservices/'>
                        <userID>$username</userID>
                        <password>$password</password>
                        <skipLoginLog>1</skipLoginLog>
                        <parent>0</parent>
                        <webServiceHandleName>PXPWebServices</webServiceHandleName>
                        <methodName>StudentClassList</methodName>
                        <paramStr>&lt;Parms&gt;&lt;ChildIntID&gt;0&lt;/ChildIntID&gt;&lt;/Parms&gt;</paramStr>
                    </ProcessWebServiceRequest>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        val response = callSoapService(requestData)

        return if (response.contains("The user name or password is incorrect.") || response.contains("Invalid user id or password")) {
            ("Login Failed")
        } else if (response.contains("TodayScheduleInfoData")) {
            ("Login Successful!")
        } else {
            ("Something went wrong.")
        }
    }

    // Calls the Soap service
    private suspend fun callSoapService(requestData: String): String {
        val reqUrl = "https://$districtDomain/Service/PXPCommunication.asmx?WSDL"

        return runBlocking {
            try {
                val response: HttpResponse = client.post(reqUrl) {
                    contentType(ContentType.Text.Xml)
                    setBody(requestData)
                }
                response.body<String>()
            } catch (e: Exception) {
                ""
            }
        }
    }

    // Removes xml sequences
    private fun removeXMLSequences(data: String): String {
        val startIndex = data.indexOf("<ProcessWebServiceRequestResult>") + 32
        val endIndex = data.indexOf("</ProcessWebServiceRequestResult>")
        return data.substring(startIndex, endIndex)
            .replace(Regex("&amp;amp;amp;|&amp;amp;|&amp;"), "&") // Replace ampersand with &
            .replace("&gt;", ">") // Greater than
            .replace("&lt;", "<") // Less than
            .replace(Regex("""(\r\n)|\n|"""), "") // Remove newlines
    }
}
