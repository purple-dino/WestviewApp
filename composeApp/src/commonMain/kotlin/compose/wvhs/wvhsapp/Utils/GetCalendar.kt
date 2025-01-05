package compose.wvhs.wvhsapp.Utils

import compose.wvhs.wvhsapp.DataClasses.DecodedEvent
import compose.wvhs.wvhsapp.DataClasses.FinalCalendar
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

suspend fun getAthleticsCalendar(): FinalCalendar {
    val client = HttpClient()
    val dateToGet = LocalDate.fromEpochDays(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays()).toString() + "T00:00:00-07:00"
    return parseCalendar(withContext(Dispatchers.IO) {client.get(urlString = "https://www.googleapis.com/calendar/v3/calendars/c_bdlim8dben51vvr6p2omiguh1k@group.calendar.google.com/events?key=AIzaSyDeFW5b_wnH-uDLG-RjPsTX6P2iOZHwGBo&timeMin=${dateToGet}&singleEvents=True&orderBy=startTime").bodyAsText() })
}

suspend fun getSchoolCalendar(): FinalCalendar {
    val client = HttpClient()
    val dateToGet = LocalDate.fromEpochDays(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays()).toString() + "T00:00:00-07:00"
    return parseCalendar(withContext(Dispatchers.IO) {client.get(urlString = "https://www.googleapis.com/calendar/v3/calendars/westviewwolverines@gmail.com/events?key=AIzaSyDeFW5b_wnH-uDLG-RjPsTX6P2iOZHwGBo&timeMin=${dateToGet}&singleEvents=True&orderBy=startTime&maxResults=100").bodyAsText() })

}

fun parseCalendar(calendar: String): FinalCalendar {
    val json = Json { ignoreUnknownKeys = true }
    val newCalendar = json.parseToJsonElement(calendar).jsonObject["items"]
    val returnCalendar = mutableListOf<DecodedEvent>()

    newCalendar?.jsonArray?.forEach { eventJson ->
        val tempEvent = DecodedEvent(
            kind = eventJson.jsonObject["kind"]?.jsonPrimitive?.content,
            etag = eventJson.jsonObject["etag"]?.jsonPrimitive?.content,
            id = eventJson.jsonObject["id"]?.jsonPrimitive?.content,
            status = eventJson.jsonObject["status"]?.jsonPrimitive?.content,
            htmlLink = eventJson.jsonObject["htmlLink"]?.jsonPrimitive?.content,
            created = eventJson.jsonObject["created"]?.jsonPrimitive?.content,
            updated = eventJson.jsonObject["updated"]?.jsonPrimitive?.content,
            summary = eventJson.jsonObject["summary"]?.jsonPrimitive?.content?.trim(),
            description = eventJson.jsonObject["description"]?.jsonPrimitive?.content?.trim(),
            location = eventJson.jsonObject["location"]?.jsonPrimitive?.content,
            start = eventJson.jsonObject["start"]?.jsonObject?.get("dateTime")?.jsonPrimitive?.content?.let { startIsoTime ->
                Instant.parse(startIsoTime).toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
            },
            startDate = eventJson.jsonObject["start"]?.jsonObject?.get("date")?.jsonPrimitive?.content?.let { startIsoTime ->
                LocalDate.parse(startIsoTime)
            },
            end = eventJson.jsonObject["end"]?.jsonObject?.get("dateTime")?.jsonPrimitive?.content?.let { endIsoTime ->
                Instant.parse(endIsoTime).toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
            },
            endDate = eventJson.jsonObject["end"]?.jsonObject?.get("date")?.jsonPrimitive?.content?.let { endIsoTime ->
                LocalDate.parse(endIsoTime)
            },
            iCalUID = eventJson.jsonObject["iCalUID"]?.jsonPrimitive?.content,
            sequence = eventJson.jsonObject["sequence"]?.jsonPrimitive?.int,
            eventType = eventJson.jsonObject["eventType"]?.jsonPrimitive?.content
        )
        returnCalendar.add(tempEvent)
    }
    return FinalCalendar(returnCalendar.toList())
}
