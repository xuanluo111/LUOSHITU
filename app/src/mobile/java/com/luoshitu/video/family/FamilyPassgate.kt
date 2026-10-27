package com.luoshitu.video.family

import android.content.Context
import android.util.Base64
import com.github.catvod.utils.Prefers
import java.security.SecureRandom
import java.security.Security
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * 家属模式一次性口令（提示词 1.4 铁律：口令家属自设，手机号后 4 位默认；本地哈希存储，不存明文；
 * 不设倒计时、不设图形验证）。入口对老人结构上不可达。
 */
object FamilyPassgate {

    private const val PREF_KEY = "luoshitu_family_pass_digest"
    private const val SALT_LEN = 16
    private const val ITERATIONS = 100_000

    fun digest(phone: String): String {
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val key = pbkdf2(phone, salt, ITERATIONS)
        return Base64.encodeToString(key, Base64.NO_WRAP) + "@" + Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    /** 校验手机号后 4 位是否等于已存储口令的指纹（不可逆） */
    fun verify(phone: String): Boolean {
        val expected = Prefers.getString(PREF_KEY)
        if (expected.isNullOrBlank()) return false
        val salt = Base64.decode(expected.split('@')[1], Base64.NO_WRAP)
        return digest(phone).removePrefix("@").equals(Base64.encodeToString(salt, Base64.NO_WRAP))
    }

    fun enter(phone: String): Boolean {
        if (phone.isBlank()) return false
        Prefers.put(PREF_KEY, digest(phone))
        return true
    }

    fun clear() { Prefers.remove(PREF_KEY) }

    fun phoneFriendlyText(phone: String): String =
        if (phone.isBlank()) "未设置" else "第 ${phone.lastOrNull()} 位：$phone.substring(phone.length - 4)}"

    private fun pbkdf2(phone: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(phone.toCharArray(), salt, iterations, 256)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec)
        spec.clearPassword()
        return key.getEncoded()
    }
}
