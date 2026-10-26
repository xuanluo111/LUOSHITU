package com.luoshitu.video

import com.luoshitu.video.data.config.ContentGuard
import com.luoshitu.video.data.error.ElderError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** 3.7 反静默失败校验（S1 DoD 单测项，覆盖 A6/A45 的纯逻辑部分） */
class ContentGuardTest {

    private fun realJar(): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry("classes.dex")); zip.write(byteArrayOf(1, 2, 3)); zip.closeEntry()
            zip.putNextEntry(ZipEntry("assets/ftyguard_v8.so")); zip.write(ByteArray(1024)); zip.closeEntry()
        }
        return out.toByteArray()
    }

    private fun md5(bytes: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(bytes).joinToString("") { "%02x".format(it) }

    @Test fun `valid jar passes`() {
        val verdict = ContentGuard.check(ContentGuard.Expected.JAR, realJar())
        assertTrue(verdict is ContentGuard.Verdict.Ok)
    }

    @Test fun `json body rejected as jar (catch-all server)`() {
        val json = """{"sites":[]}""".toByteArray()
        val verdict = ContentGuard.check(ContentGuard.Expected.JAR, json)
        assertTrue(verdict is ContentGuard.Verdict.Reject)
        assertTrue((verdict as ContentGuard.Verdict.Reject).error is ElderError.JarInvalid)
    }

    @Test fun `half-downloaded jar rejected by content-length`() {
        val jar = realJar()
        val verdict = ContentGuard.check(ContentGuard.Expected.JAR, jar.copyOf(jar.size / 2), contentLength = jar.size.toLong())
        assertTrue(verdict is ContentGuard.Verdict.Reject)
        assertTrue((verdict as ContentGuard.Verdict.Reject).error is ElderError.JarInvalid)
    }

    @Test fun `truncated zip without central directory rejected`() {
        val jar = realJar()
        // 魔数在，但尾部（central directory）缺失：模拟"首 dex 完整而 .guard 缺失"的半截 jar
        val verdict = ContentGuard.check(ContentGuard.Expected.JAR, jar.copyOf(jar.size - 64), contentLength = jar.size.toLong())
        assertTrue(verdict is ContentGuard.Verdict.Reject)
    }

    @Test fun `response identical to config body is resource-missing (A6)`() {
        val config = """{"sites":[],"lives":[]}""".toByteArray()
        val verdict = ContentGuard.check(ContentGuard.Expected.JS, config, configMd5 = md5(config))
        assertTrue(verdict is ContentGuard.Verdict.Reject)
        assertTrue((verdict as ContentGuard.Verdict.Reject).error is ElderError.ResourceMissing)
    }

    @Test fun `js file starting with json brace rejected`() {
        val verdict = ContentGuard.check(ContentGuard.Expected.JS, "{\"error\":\"not found\"}".toByteArray())
        assertTrue(verdict is ContentGuard.Verdict.Reject)
        assertTrue((verdict as ContentGuard.Verdict.Reject).error is ElderError.ResourceMissing)
    }

    @Test fun `html page rejected as js`() {
        val verdict = ContentGuard.check(ContentGuard.Expected.JS, "<html><body>404</body></html>".toByteArray())
        assertTrue(verdict is ContentGuard.Verdict.Reject)
    }

    @Test fun `real js source passes`() {
        val verdict = ContentGuard.check(ContentGuard.Expected.JS, "// drpy2 spider\nfunction init(){}\n".toByteArray())
        assertTrue(verdict is ContentGuard.Verdict.Ok)
    }

    @Test fun `json config passes`() {
        val verdict = ContentGuard.check(ContentGuard.Expected.CONFIG, "{\"sites\":[]}".toByteArray())
        assertTrue(verdict is ContentGuard.Verdict.Ok)
    }

    @Test fun `txt multi-depot config passes`() {
        val verdict = ContentGuard.check(ContentGuard.Expected.CONFIG, "【*接口*】\r\nhttp://a.com/b.json".toByteArray())
        assertTrue(verdict is ContentGuard.Verdict.Ok)
    }

    @Test fun `garbage config rejected`() {
        val verdict = ContentGuard.check(ContentGuard.Expected.CONFIG, "<html>gateway error</html>".toByteArray())
        assertTrue((verdict as ContentGuard.Verdict.Reject).error is ElderError.NotConfigFormat)
    }

    @Test fun `record captures expected type content type and size`() {
        val bytes = realJar()
        val verdict = ContentGuard.check(ContentGuard.Expected.JAR, bytes, contentType = "application/octet-stream")
        val record = (verdict as ContentGuard.Verdict.Ok).record
        assertEquals(ContentGuard.Expected.JAR, record.expected)
        assertEquals("application/octet-stream", record.contentType)
        assertEquals(bytes.size.toLong(), record.bytes)
    }
}
