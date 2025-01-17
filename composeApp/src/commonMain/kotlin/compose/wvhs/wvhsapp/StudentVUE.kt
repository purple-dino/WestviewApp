package compose.wvhs.wvhsapp

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import compose.wvhs.wvhsapp.DataClasses.Absence
import compose.wvhs.wvhsapp.DataClasses.Assignment
import compose.wvhs.wvhsapp.DataClasses.Attendance
import compose.wvhs.wvhsapp.DataClasses.ClassInfo
import compose.wvhs.wvhsapp.DataClasses.Classes
import compose.wvhs.wvhsapp.DataClasses.DecodedEvent
import compose.wvhs.wvhsapp.DataClasses.GradeBreakdown
import compose.wvhs.wvhsapp.DataClasses.Gradebook
import compose.wvhs.wvhsapp.Utils.callSoapService
import compose.wvhs.wvhsapp.Utils.parseCalendar
import compose.wvhs.wvhsapp.Utils.removeXMLSequences
import compose.wvhs.wvhsapp.ViewModels.ScheduleViewModel
import compose.wvhs.wvhsapp.ViewModels.StudentSharedViewModel
import io.ktor.client.*
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
        studentSharedViewModel.changeBellScheduleType(currentBellSchedule)

        if (currentBellSchedule != "MonFri Bell" && currentBellSchedule != "Tues-Thurs Bell" && currentBellSchedule != "Wed Bell") {
            studentSharedViewModel.changeBellSchedule(createBellScheduleFromGoogleCalendar(newClassList.values.toList(), studentSharedViewModel = studentSharedViewModel))
        } else {
            studentSharedViewModel.changeBellSchedule(getSchedule(classList=newClassList.values.toList(), dayOfWeek = listOf("MonFri Bell", "Tues-Thurs Bell", "Wed Bell").indexOf(currentBellSchedule), studentSharedViewModel = studentSharedViewModel))
        }

        println(currentBellSchedule)
        println(getSchedule(classList=newClassList.values.toList(), dayOfWeek = listOf("MonFri Bell", "Tues-Thurs Bell", "Wed Bell").indexOf(currentBellSchedule), studentSharedViewModel = studentSharedViewModel))
        return Classes(newClassList.values.toList())
    }

    private suspend fun createBellScheduleFromGoogleCalendar(classlist: List<String>, studentSharedViewModel: StudentSharedViewModel): List<ScheduleViewModel.Period>? {
        val client = HttpClient()
        val dateToGet = LocalDate.fromEpochDays(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays()).toString() + "T00:00:00-07:00"
        val bellSchedule = (parseCalendar(withContext(Dispatchers.IO) {client.get(urlString = "https://www.googleapis.com/calendar/v3/calendars/5c71a8e801b662f3862e4283a18c0a5a052121b5fcbff169539c1aaa8b5a8684@group.calendar.google.com/events?key=AIzaSyDeFW5b_wnH-uDLG-RjPsTX6P2iOZHwGBo&timeMin=${dateToGet}&maxResults=1&singleEvents=True&orderBy=startTime").bodyAsText() }))
        if (bellSchedule.events != emptyList<DecodedEvent>()) {
            val schedule = bellSchedule.events[0].description
            if (schedule != null) {
                val finalSchedule: MutableList<ScheduleViewModel.Period> = mutableListOf()
                val newSchedule = Ksoup.parse(schedule).select("tr").toList().drop(1)
                for (period in newSchedule) {
                    val parsedPeriod = period.select("td").toList()
                    val startTime = LocalTime(parsedPeriod[1].text().split(":")[0].toInt(), parsedPeriod[1].text().split(":")[1].toInt())
                    val endTime = LocalTime(parsedPeriod[2].text().split(":")[0].toInt(), parsedPeriod[2].text().split(":")[1].toInt())
                    if (parsedPeriod[0].text() == "1" || parsedPeriod[0].text() == "2" || parsedPeriod[0].text() == "3" || parsedPeriod[0].text() == "4") {
                        finalSchedule.add(ScheduleViewModel.Period(name = try{classlist[parsedPeriod[0].text().toInt()-1]}catch (e:Exception){"Period 1"}, start = startTime, end = endTime))
                    } else {
                        finalSchedule.add(ScheduleViewModel.Period(name = parsedPeriod[0].text(), start = startTime, end = endTime))
                    }
                }
                studentSharedViewModel.changeBellScheduleType(bellSchedule.events[0].summary)
                return finalSchedule
            } else {
                return null
            }
        } else {
            return null
        }
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
            removeXMLSequences(callSoapService(requestData, districtDomain, client))
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

        val response = callSoapService(requestData, districtDomain, client)

        return if (response.contains("The user name or password is incorrect.") || response.contains("Invalid user id or password")) {
            ("Login Failed")
        } else if (response.contains("TodayScheduleInfoData")) {
            ("Login Successful!")
        } else {
            ("Something went wrong.")
        }
    }
}
