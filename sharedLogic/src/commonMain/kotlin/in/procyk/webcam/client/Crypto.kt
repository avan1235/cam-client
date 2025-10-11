package `in`.procyk.webcam.client

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.AES.IvAuthenticatedCipher
import io.ktor.util.*
import kotlin.jvm.JvmInline

@JvmInline
value class Key private constructor(val value: ByteArray) {
    companion object {
        suspend fun generate(): Key = withAesGcm {
            val keyGenerator = keyGenerator(keySize = AES.Key.Size.B256)
            val key = keyGenerator.generateKey()
            key.encodeToByteArray(AES.Key.Format.RAW).let(::Key)
        }

        fun fromBase64(value: String): Key = Key(value.decodeBase64Bytes())
    }

    fun encodeBase64(): String = value.encodeBase64()
}

suspend fun encryptor(key: Key): suspend (ByteArray) -> ByteArray =
    withDecodedKeyCipher(key) { { encrypt(plaintext = it) } }

suspend fun decryptor(key: Key): suspend (ByteArray) -> ByteArray =
    withDecodedKeyCipher(key) { { decrypt(ciphertext = it) } }

private suspend inline fun <T> withDecodedKeyCipher(key: Key, f: suspend IvAuthenticatedCipher.() -> T): T =
    withAesGcm {
        val keyDecoder = keyDecoder()
        val decodedKey = keyDecoder.decodeFromByteArray(AES.Key.Format.RAW, key.value)
        val cipher = decodedKey.cipher()
        f(cipher)
    }

private inline fun <T> withAesGcm(f: AES.GCM.() -> T): T {
    val provider = CryptographyProvider.Default
    val aesGcm = provider.get(AES.GCM)
    return f(aesGcm)
}