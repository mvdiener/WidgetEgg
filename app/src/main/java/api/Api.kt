package api

import com.widgetegg.widgeteggapp.BuildConfig
import data.BACKUP_ENDPOINT
import data.CONTRACTS_ARCHIVE_ENDPOINT
import data.CONTRACT_ENDPOINT
import data.CURRENT_CLIENT_VERSION
import data.ContractData
import data.MISSION_ENDPOINT
import data.MissionData
import data.PERIODICALS_ENDPOINT
import data.PeriodicalsData
import ei.Ei.AuthenticatedMessage
import ei.Ei.Backup
import ei.Ei.BasicRequestInfo
import ei.Ei.ContractsArchive
import ei.Ei.ContractCoopStatusRequest
import ei.Ei.ContractCoopStatusResponse
import ei.Ei.EggIncFirstContactRequest
import ei.Ei.EggIncFirstContactResponse
import ei.Ei.GetActiveMissionsRequest
import ei.Ei.GetActiveMissionsResponse
import ei.Ei.GetPeriodicalsRequest
import ei.Ei.LocalContract
import ei.Ei.MissionInfo
import ei.Ei.PeriodicalsResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import tools.buildSecureAuthMessage
import tools.decodeRequest
import tools.encodeRequest
import java.util.concurrent.TimeUnit

suspend fun fetchBackupData(eid: String): Backup {
    val basicRequestInfo = getBasicRequestInfo(eid)
    val backup = fetchBackup(basicRequestInfo)

    return backup
}

suspend fun fetchMissionData(eid: String, resetIndex: Int): MissionData {
    val basicRequestInfo = getBasicRequestInfo(eid)
    val activeMissions = fetchActiveMissions(basicRequestInfo, resetIndex)

    val (virtueMissions, normalMissions) = activeMissions.partition { mission -> mission.type == MissionInfo.MissionType.VIRTUE }

    return MissionData(normalMissions, virtueMissions)
}

suspend fun fetchContractData(eid: String, backup: Backup): ContractData {
//    val eid = BuildConfig.DEV_ACCOUNT
    val basicRequestInfo = getBasicRequestInfo(eid)
    val statuses = backup.contracts.contractsList.map { contract ->
        fetchContractStatus(
            basicRequestInfo,
            contract.contract.identifier,
            contract.coopIdentifier
        )
    }
    return ContractData(backup.contracts.contractsList, statuses)
}

suspend fun fetchPeriodicalsData(eid: String): PeriodicalsData {
    val periodicals = fetchPeriodicals(eid)
    return PeriodicalsData(
        periodicals.contracts.contractsList,
        periodicals.contracts.customEggsList,
        periodicals.contracts.currentSeason
    )
}

suspend fun fetchContractsArchive(eid: String): List<LocalContract> {
    val basicRequestInfo = getBasicRequestInfo(eid)
    val contractsArchive = fetchContractsArchive(basicRequestInfo)
    return contractsArchive.archiveList
}

fun getBasicRequestInfo(eid: String): BasicRequestInfo {
    return BasicRequestInfo.newBuilder()
        .setEiUserId(eid)
        .setClientVersion(CURRENT_CLIENT_VERSION)
        .setPlatform("DROID")
        .build()
}

// Used to get colleggtible assets
suspend fun downloadImageBytes(url: String): ByteArray? {
    return try {
        val response = sharedClient.get(url)
        if (response.status.value == 200) {
            response.readRawBytes()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private suspend fun fetchActiveMissions(
    basicRequestInfo: BasicRequestInfo,
    resetIndex: Int
): List<MissionInfo> {
    val url = MISSION_ENDPOINT

    val getActiveMissionsRequest = GetActiveMissionsRequest.newBuilder()
        .setRinfo(basicRequestInfo)
        .setResetIndex(resetIndex)
        .build()

    val authMessage = try {
        buildSecureAuthMessage(data = getActiveMissionsRequest)
    } catch (e: Exception) {
        throw e
    }

    val encodedRequest = encodeRequest(authMessage.toByteArray())
    val response = makeRequest(url, encodedRequest)

    when (response.status.value) {
        in 200..299 -> {
            try {
                val authMessageResponse =
                    AuthenticatedMessage.parseFrom(
                        decodeRequest(response.bodyAsText())
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

private suspend fun fetchContractStatus(
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

    val encodedRequest = encodeRequest(contractRequest.toByteArray())
    val response = makeRequest(url, encodedRequest)

    when (response.status.value) {
        in 200..299 -> {
            try {
                val authMessageResponse =
                    AuthenticatedMessage.parseFrom(
                        decodeRequest(response.bodyAsText())
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

private suspend fun fetchPeriodicals(eid: String): PeriodicalsResponse {
    val url = PERIODICALS_ENDPOINT

    val getPeriodicalsRequest = GetPeriodicalsRequest.newBuilder()
        .setUserId(eid)
        .setCurrentClientVersion(CURRENT_CLIENT_VERSION)
        .build()

    val encodedRequest = encodeRequest(getPeriodicalsRequest.toByteArray())
    val response = makeRequest(url, encodedRequest)

    when (response.status.value) {
        in 200..299 -> {
            try {
                val authMessageResponse =
                    AuthenticatedMessage.parseFrom(
                        decodeRequest(response.bodyAsText())
                    ).message
                val periodicalsResponse =
                    PeriodicalsResponse.parseFrom(authMessageResponse)
                return periodicalsResponse
            } catch (e: Exception) {
                throw e
            }
        }

        else -> throw Exception("Error retrieving data")
    }
}

private suspend fun fetchContractsArchive(basicRequestInfo: BasicRequestInfo): ContractsArchive {
    val url = CONTRACTS_ARCHIVE_ENDPOINT

    val encodedRequest = encodeRequest(basicRequestInfo.toByteArray())
    val response = makeRequest(url, encodedRequest)

    when (response.status.value) {
        in 200..299 -> {
            try {
                val authMessageResponse =
                    AuthenticatedMessage.parseFrom(
                        decodeRequest(response.bodyAsText())
                    ).message
                val contractsArchiveResponse =
                    ContractsArchive.parseFrom(authMessageResponse)
                return contractsArchiveResponse
            } catch (e: Exception) {
                throw e
            }
        }

        else -> throw Exception("Error retrieving data")
    }
}

private suspend fun fetchBackup(basicRequestInfo: BasicRequestInfo): Backup {
    val url = BACKUP_ENDPOINT

    val firstContactRequest = EggIncFirstContactRequest.newBuilder()
        .setRinfo(basicRequestInfo)
        .setEiUserId(basicRequestInfo.eiUserId)
        .build()

    val encodedRequest = encodeRequest(firstContactRequest.toByteArray())
    val response = makeRequest(url, encodedRequest)

    when (response.status.value) {
        in 200..299 -> {
            try {
                val firstContactResponse =
                    EggIncFirstContactResponse.parseFrom(decodeRequest(response.bodyAsText()))
                if (!firstContactResponse.hasBackup()) throw Exception("No backup found")
                return firstContactResponse.backup
            } catch (e: Exception) {
                throw e
            }
        }

        else -> throw Exception("Error retrieving data")
    }
}

private val sharedClient = HttpClient(OkHttp) {
    engine {
        config {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
        }
    }
}

private suspend fun makeRequest(
    url: String,
    encodedRequest: String
): HttpResponse {
    return sharedClient.post(urlString = url) {
        parameter("data", encodedRequest)
        contentType(ContentType.Application.FormUrlEncoded)
    }
}