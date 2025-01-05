package compose.wvhs.wvhsapp.Utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking

// Calls the Soap service
suspend fun callSoapService(requestData: String, districtDomain: String, client: HttpClient): String {
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
fun removeXMLSequences(data: String): String {
    val startIndex = data.indexOf("<ProcessWebServiceRequestResult>") + 32
    val endIndex = data.indexOf("</ProcessWebServiceRequestResult>")
    return data.substring(startIndex, endIndex)
        .replace(Regex("&amp;amp;amp;|&amp;amp;|&amp;"), "&") // Replace ampersand with &
        .replace("&gt;", ">") // Greater than
        .replace("&lt;", "<") // Less than
        .replace(Regex("""(\r\n)|\n|"""), "") // Remove newlines
}