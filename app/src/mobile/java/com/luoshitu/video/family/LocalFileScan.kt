package com.luoshitu.video.family

import android.app.Activity
import java.io.File

/**
 * 家属侧"把配置文件放进 Download/ 目录"的扫描（F1 配置注入 ③）：
 * 优先扫描 App 内部 Download/ 目录（全版本可用）；外置目录在 Android 13+ 需要存储权限，
 * 读取失败时静默退回内部目录。只扫描约定目录下的 .json/.txt/.jar/.js。
 * 扫描结果只给家属看，不进老人界面。
 */
object LocalFileScan {

    const val DOWNLOAD_SUBDIR = "Download"
    private val SUPPORTED_SUFFIXES: Set<String> = setOf("json", "txt", "jar", "js")

    fun scan(activity: Activity): List<String> {
        val results = LinkedHashMap<String, MutableList<String>>()
        listFiles(File(activity.filesDir, DOWNLOAD_SUBDIR), 0, results)
        // 兼容 Android 9 传统外置存储：外置目录读不到时静默退回（内部目录已覆盖基本场景）
        runCatching {
            val ext = activity.getExternalFilesDir(null)
            if (ext != null && ext.isDirectory) listFiles(File(ext, DOWNLOAD_SUBDIR), 0, results)
        }
        return results.values.flatMap { it }.toList()
    }

    private fun listFiles(dir: File, depth: Int, out: LinkedHashMap<String, MutableList<String>>) {
        if (depth > 4) return
        val list = dir.listFiles() ?: return
        for (child in list.sortedBy { it.name.lowercase() }) {
            if (child.isDirectory) {
                listFiles(child, depth + 1, out)
                continue
            }
            if (SUPPORTED_SUFFIXES.contains(child.name.substringAfterLast('.'))) {
                val files = out[dir.absolutePath] ?: mutableListOf()
                files.add(dir.absolutePath + "/" + child.name)
                out[dir.absolutePath] = files
            }
        }
    }
}
