package tools

import com.google.protobuf.MessageLite
import com.widgetegg.widgeteggapp.BuildConfig
import ei.Ei.AuthenticatedMessage
import java.security.MessageDigest

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