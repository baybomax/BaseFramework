package com.android.db.library.http

import com.migcomponents.migbase64.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES
 *
 * Created by DengBo on 27/02/2018.
 */

class AESCipher(secretKey: String) {

    private val iv  = IvParameterSpec(secretKey.toByteArray(Charsets.UTF_8))
    private val key = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")

    /**
     * encode
     *
     * @param raw
     * @return String
     */
    fun encrypt(raw: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        return base64Encode(cipher.doFinal(raw.toByteArray(Charsets.UTF_8)))
    }

    /**
     * decode
     *
     * @param encrypted
     * @return String
     */
    fun decipher(encrypted: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        return cipher.doFinal(base64Decode(encrypted)).toString(Charsets.UTF_8)
    }

    /**
     *
     */
    private fun base64Encode(raw: ByteArray): String {
        return Base64.encodeToString(raw, false)
//        return String(Base64.encodeBase64(raw))
//        return BASE64Encoder().encode(raw).toString()
//        return Base64.getEncoder().encode(raw).toString(Charsets.UTF_8)
    }

    /**
     *
     */
    private fun base64Decode(raw: String): ByteArray {
        return Base64.decode(raw)
//        return Base64.decodeBase64(raw)
//        return BASE64Decoder().decodeBuffer(raw)
//        return Base64.getDecoder().decode(raw.toByteArray(Charsets.UTF_8))
    }
}
