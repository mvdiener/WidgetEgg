package api

import data.BACKUP_ENDPOINT
import ei.Ei.Backup
import ei.Ei.BasicRequestInfo
import ei.Ei.EggIncFirstContactRequest
import ei.Ei.EggIncFirstContactResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.Base64

suspend fun fetchBackup(basicRequestInfo: BasicRequestInfo): Backup {
    val url = BACKUP_ENDPOINT

    val firstContactRequest = EggIncFirstContactRequest.newBuilder()
        .setRinfo(basicRequestInfo)
        .setEiUserId(basicRequestInfo.eiUserId)
        .build()

    val encodedRequest = encodeData(firstContactRequest.toByteArray())

    val client = createHttpClient()

    val response = try {
        client.post(urlString = url) {
            parameter("data", encodedRequest)
            contentType(ContentType.Application.FormUrlEncoded)
        }
    } catch (e: Exception) {
        throw e
    }

    client.close()

    when (response.status.value) {
        in 200..299 -> {
            try {
                val decoded = Base64.getDecoder().decode(response.bodyAsText())
                val firstContactResponse = EggIncFirstContactResponse.parseFrom(decoded)
                if (!firstContactResponse.hasBackup()) throw Exception("No backup found")
                return firstContactResponse.backup
            } catch (e: Exception) {
                throw e
            }
        }

        else -> throw Exception("Error retrieving data")
    }
}

suspend fun fetchData(eid: String): Backup {
    val basicRequestInfo = getBasicRequestInfo(eid)

    return fetchBackup(basicRequestInfo)
}

fun getBasicRequestInfo(eid: String): BasicRequestInfo {
    return BasicRequestInfo.newBuilder()
        .setEiUserId(eid)
        .setClientVersion(127)
        .setPlatform("DROID")
        .build()
}

private fun encodeData(input: ByteArray): String {
    return Base64.getEncoder().encodeToString(input)
}

private fun createHttpClient(): HttpClient {
    return HttpClient(CIO)
}