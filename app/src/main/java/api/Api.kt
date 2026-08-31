package api

import com.widgetegg.widgeteggapp.BuildConfig
import data.constants.BACKUP_ENDPOINT
import data.constants.CONTRACTS_ARCHIVE_ENDPOINT
import data.constants.CONTRACTS_INFO_ENDPOINT
import data.constants.CONTRACT_ENDPOINT
import data.constants.CONTRACT_PLAYER_INFO_ENDPOINT
import data.constants.CURRENT_CLIENT_VERSION
import data.ContractData
import data.constants.MISSION_ENDPOINT
import data.MissionData
import data.constants.PERIODICALS_ENDPOINT
import data.PeriodicalsData
import ei.Ei.Backup
import ei.Ei.BasicRequestInfo
import ei.Ei.ContractsArchive
import ei.Ei.ContractsInfoRequest
import ei.Ei.ContractsInfoResponse
import ei.Ei.ContractPlayerInfo
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
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import tools.buildSecureAuthMessage
import tools.encodeRequest
import tools.handleAuthMessageResponse
import tools.handleBackupResponse
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

suspend fun fetchContractData(backup: Backup): ContractData {
    val eid = BuildConfig.DEV_ACCOUNT
    val basicRequestInfo = getBasicRequestInfo(eid)
    val validContracts = backup.contracts.contractsList.mapNotNull { contract ->
        try {
            val status = fetchContractStatus(
                basicRequestInfo,
                contract.contractIdentifier,
                contract.coopIdentifier
            )
            contract to status
        } catch (e: Exception) {
            if (e is AppError.EopError) {
                null
            } else {
                throw e
            }
        }
    }

    val contractsInfo = fetchContractsInfo(
        basicRequestInfo,
        backup.contracts.contractsList.mapNotNull { it.contractIdentifier }
    )

    val contracts = validContracts.map { it.first }
    val statuses = validContracts.map { it.second }

    return ContractData(contracts, statuses, contractsInfo)
}

suspend fun fetchPeriodicalsData(eid: String): PeriodicalsData {
    val periodicals = fetchPeriodicals(eid)
    val contractPlayerInfo = fetchPlayerContractInfo(getBasicRequestInfo(eid))

    return PeriodicalsData(
        periodicals.contracts.contractsList,
        contractPlayerInfo,
        periodicals.contracts.customEggsList,
        periodicals.contracts.currentSeason,
        periodicals.events.eventsList
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
    } catch (_: Exception) {
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
                val authMessageResponse = handleAuthMessageResponse(response)
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

private suspend fun fetchContractsInfo(
    basicRequestInfo: BasicRequestInfo,
    contractIds: List<String>
): ContractsInfoResponse {
    val url = CONTRACTS_INFO_ENDPOINT

    val getContractsInfoRequest = ContractsInfoRequest.newBuilder()
        .setRinfo(basicRequestInfo)
        .addAllContractIdentifiers(contractIds)
        .build()

    val authMessage = try {
        buildSecureAuthMessage(data = getContractsInfoRequest)
    } catch (e: Exception) {
        throw e
    }

    val encodedRequest = encodeRequest(authMessage.toByteArray())
    val response = makeRequest(url, encodedRequest)

    when (response.status.value) {
        in 200..299 -> {
            try {
                val authMessageResponse = handleAuthMessageResponse(response)
                val contractsInfoResponse =
                    ContractsInfoResponse.parseFrom(authMessageResponse)
                return contractsInfoResponse
            } catch (e: Exception) {
                throw e
            }
        }

        else -> throw Exception("Error retrieving data")
    }
}

private suspend fun fetchPlayerContractInfo(
    basicRequestInfo: BasicRequestInfo
): ContractPlayerInfo {
    val url = CONTRACT_PLAYER_INFO_ENDPOINT

    val authMessage = try {
        buildSecureAuthMessage(data = basicRequestInfo)
    } catch (e: Exception) {
        throw e
    }

    val encodedRequest = encodeRequest(authMessage.toByteArray())
    val response = makeRequest(url, encodedRequest)

    when (response.status.value) {
        in 200..299 -> {
            try {
                val authMessageResponse = handleAuthMessageResponse(response)
                val contractPlayerInfoResponse =
                    ContractPlayerInfo.parseFrom(authMessageResponse)
                return contractPlayerInfoResponse
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
                val authMessageResponse = handleAuthMessageResponse(response)
                val contractResponse =
                    ContractCoopStatusResponse.parseFrom(authMessageResponse)
                return contractResponse
            } catch (e: Exception) {
                throw e
            }
        }

        500 -> {
            try {
                val errorText = response.bodyAsText()
                if (errorText == "eop") {
                    throw AppError.EopError()
                } else {
                    throw Exception("Error retrieving data")
                }
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
                val authMessageResponse = handleAuthMessageResponse(response)
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
                val authMessageResponse = handleAuthMessageResponse(response)
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
                val backupResponse = handleBackupResponse(response)
                val firstContactResponse =
                    EggIncFirstContactResponse.parseFrom(backupResponse)
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
            connectTimeout(20, TimeUnit.SECONDS)
            readTimeout(20, TimeUnit.SECONDS)
            writeTimeout(20, TimeUnit.SECONDS)
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