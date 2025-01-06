package compose.wvhs.wvhsapp.DataClasses

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Creator(
    val email: String
)

@Serializable
data class Organizer(
    val email: String,
    val displayName: String,
    val self: Boolean
)

@Serializable
data class DecodedEvent(
    val kind: String? = null,
    val etag: String? = null,
    val id: String? = null,
    val status: String? = null,
    val htmlLink: String? = null,
    val created: String? = null,
    val updated: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val location: String? = null,
    val creator: Creator?  = null,
    val organizer: Organizer? = null,
    val start: LocalDateTime? = null,
    val startDate: LocalDate? = null,
    val end: LocalDateTime? = null,
    val endDate: LocalDate? = null,
    val iCalUID: String? = null,
    val sequence: Int? = null,
    val eventType: String? = null
)

@Serializable
data class FinalCalendar(
    val events: List<DecodedEvent>
)