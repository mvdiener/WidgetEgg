package tools

import android.util.Base64
import com.google.protobuf.ByteString
import com.google.protobuf.MessageLite
import com.widgetegg.widgeteggapp.BuildConfig
import ei.Ei.AuthenticatedMessage
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import okio.Buffer
import okio.Inflater
import okio.InflaterSource
import java.security.MessageDigest

fun encodeRequest(input: ByteArray): String {
    return Base64.encodeToString(input, Base64.DEFAULT)
}

suspend fun handleAuthMessageResponse(response: HttpResponse): ByteString {
    val decoded = decodeRequest(response.bodyAsText())
    val authMessage = AuthenticatedMessage.parseFrom(decoded)
    return decompress(authMessage)
}

suspend fun handleBackupResponse(response: HttpResponse): ByteArray {
    val decoded = decodeRequest(response.bodyAsText())
    return try {
        val buffer = Buffer().write(decoded)
        InflaterSource(buffer, Inflater()).use { source ->
            Buffer().apply { writeAll(source) }.readByteArray()
        }
    } catch (_: Exception) {
        decoded
    }

}

fun <T : MessageLite> buildSecureAuthMessage(data: T): AuthenticatedMessage {
    val secretKey = BuildConfig.SECRET_KEY
    val secretHash = sha256(secretKey)
    val dataByteArray = data.toByteArray()

    val copy = dataByteArray.copyOf()
    copy[0x3b9af419 % dataByteArray.size] = 0x1B.toByte()
    val hash = sha256(copy + secretHash.toByteArray())

    return AuthenticatedMessage.newBuilder()
        .setMessage(data.toByteString())
        .setCode(hash)
        .build()
}

private fun sha256(input: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

private fun sha256(input: ByteArray): String {
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(input)
    return bytes.joinToString("") { "%02x".format(it) }
}

private fun decodeRequest(input: String): ByteArray {
    return Base64.decode(input, Base64.DEFAULT)
}

private fun decompress(authenticatedMessage: AuthenticatedMessage): ByteString {
    if (!authenticatedMessage.compressed) return authenticatedMessage.message

    return try {
        val buffer = Buffer().write(authenticatedMessage.message.toByteArray())

        val inflated = InflaterSource(buffer, Inflater()).use { source ->
            Buffer().apply { writeAll(source) }.readByteArray()
        }

        ByteString.copyFrom(inflated)
    } catch (_: Exception) {
        authenticatedMessage.message
    }
}