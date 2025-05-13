package api

import data.BACKUP_ENDPOINT
import data.CONTRACT_ENDPOINT
import data.CURRENT_CLIENT_VERSION
import data.ContractData
import data.MISSION_ENDPOINT
import data.MissionData
import ei.Ei.AuthenticatedMessage
import ei.Ei.Backup
import ei.Ei.BasicRequestInfo
import ei.Ei.ContractCoopStatusRequest
import ei.Ei.ContractCoopStatusResponse
import ei.Ei.EggIncFirstContactRequest
import ei.Ei.EggIncFirstContactResponse
import ei.Ei.GetActiveMissionsResponse
import ei.Ei.MissionInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import tools.buildSecureAuthMessage
import java.util.concurrent.TimeUnit

suspend fun fetchActiveMissions(basicRequestInfo: BasicRequestInfo): List<MissionInfo> {
    val url = MISSION_ENDPOINT

    val authMessage = try {
        buildSecureAuthMessage(data = basicRequestInfo)
    } catch (e: Exception) {
        throw e
    }

    val encodedRequest = authMessage.toByteArray().encodeBase64()
    val client = createHttpClient()
    val response = makeRequest(client, url, encodedRequest)

    when (response.status.value) {
        in 200..299 -> {
            try {
                val authMessageResponse =
                    AuthenticatedMessage.parseFrom(
                        response.bodyAsText().decodeBase64Bytes()
                    ).message
                val activeMissionsResponse =
                    GetActiveMissionsResponse.parseFrom(authMessageResponse)
                return activeMissionsResponse.activeMissionsList
            } catch (e: Exception) {
                throw e
            }
        }

        else -> throw Exception("Error retrieving data")
    }
}

suspend fun fetchContractStatus(
    basicRequestInfo: BasicRequestInfo,
    contractId: String,
    coopId: String
): ContractCoopStatusResponse {
    val url = CONTRACT_ENDPOINT

    val contractRequest = ContractCoopStatusRequest.newBuilder()
        .setRinfo(basicRequestInfo)
        .setUserId(basicRequestInfo.eiUserId)
        .setContractIdentifier(contractId)
        .setCoopIdentifier(coopId)
        .build()

    val encodedRequest = contractRequest.toByteArray().encodeBase64()
    val client = createHttpClient()
    val response = makeRequest(client, url, encodedRequest)

    when (response.status.value) {
        in 200..299 -> {
            try {
                val authMessageResponse =
                    AuthenticatedMessage.parseFrom(
                        response.bodyAsText().decodeBase64Bytes()
                    ).message
                val contractResponse =
                    ContractCoopStatusResponse.parseFrom(authMessageResponse)
                return contractResponse
            } catch (e: Exception) {
                throw e
            }
        }

        else -> throw Exception("Error retrieving data")
    }
}

suspend fun fetchBackup(basicRequestInfo: BasicRequestInfo): Backup {
    val url = BACKUP_ENDPOINT

    val firstContactRequest = EggIncFirstContactRequest.newBuilder()
        .setRinfo(basicRequestInfo)
        .setEiUserId(basicRequestInfo.eiUserId)
        .build()

    val encodedRequest = firstContactRequest.toByteArray().encodeBase64()
    val client = createHttpClient()
    val response = makeRequest(client, url, encodedRequest)

    when (response.status.value) {
        in 200..299 -> {
            try {
                val firstContactResponse =
                    EggIncFirstContactResponse.parseFrom(response.bodyAsText().decodeBase64Bytes())
                if (!firstContactResponse.hasBackup()) throw Exception("No backup found")
                return firstContactResponse.backup
            } catch (e: Exception) {
                throw e
            }
        }

        else -> throw Exception("Error retrieving data")
    }
}

suspend fun fetchMissionData(eid: String): MissionData {
    val basicRequestInfo = getBasicRequestInfo(eid)
    var activeMissions = fetchActiveMissions(basicRequestInfo)
    val backup = fetchBackup(basicRequestInfo)

    val fuelingMission = backup.artifactsDb.fuelingMission

    fuelingMission?.let {
        if (fuelingMission.capacity > 0) {
            activeMissions = activeMissions + fuelingMission
        }
    }

    return MissionData(activeMissions, backup.artifacts)
}

suspend fun fetchContractData(eid: String): ContractData {
    val basicRequestInfo = getBasicRequestInfo(eid)
    val backup = fetchBackup(basicRequestInfo)
    val statuses = backup.contracts.contractsList.map { contract ->
        fetchContractStatus(
            basicRequestInfo,
            contract.contract.identifier,
            contract.coopIdentifier
        )
    }
    return ContractData(backup.contracts.contractsList, statuses)
}

fun getBasicRequestInfo(eid: String): BasicRequestInfo {
    return BasicRequestInfo.newBuilder()
        .setEiUserId(eid)
        .setClientVersion(CURRENT_CLIENT_VERSION)
        .setPlatform("DROID")
        .build()
}

private fun createHttpClient(): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(20, TimeUnit.SECONDS)
                readTimeout(20, TimeUnit.SECONDS)
                writeTimeout(20, TimeUnit.SECONDS)
            }
        }
    }
}

private suspend fun makeRequest(
    client: HttpClient,
    url: String,
    encodedRequest: String
): HttpResponse {
    return try {
        client.post(urlString = url) {
            parameter("data", encodedRequest)
            contentType(ContentType.Application.FormUrlEncoded)
        }
    } catch (e: Exception) {
        throw e
    } finally {
        client.close()
    }
}