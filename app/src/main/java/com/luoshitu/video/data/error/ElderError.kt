package com.luoshitu.video.data.error

import com.github.catvod.utils.RuntimeModule

/**
 * 分级错误模型（提示词 3.10）：禁止笼统"加载失败"。
 * 三层文案分层：tech 仅供家属模式诊断；family 给家属的口语化说明；elder 是老人界面唯一可见文案。
 * 老人端错误结构一律「怎么了 + 怎么办 + 按钮」，且绝不出现代码/英文/术语。
 */
sealed class ElderError(
    val kind: Kind,
    val tech: String,
    val family: String,
    val elder: String,
    val action: Action,
) {

    enum class Kind {
        CONFIG_UNREACHABLE, DNS_FAIL, NOT_CONFIG_FORMAT, NO_SITE,
        JAR_DOWNLOAD, JAR_INVALID, JAR_CLASS_MISSING, RESOURCE_MISSING,
        EXT_ENCRYPTED, TIMEOUT, MODULE_MISSING, UNKNOWN,
    }

    enum class Action { RETRY, SWITCH_SOURCE, GO_LIVE, REOPEN, CALL_FAMILY }

    class ConfigUnreachable(detail: String) : ElderError(
        Kind.CONFIG_UNREACHABLE, "地址不可达: $detail",
        "这个内容地址现在打不开，可能是地址失效了，或者家里网络不通",
        "节目单打不开，等一会儿再试试",
        Action.RETRY,
    )

    class DnsFail(detail: String) : ElderError(
        Kind.DNS_FAIL, "DNS 失败: $detail",
        "家里网络没有连上网址，先检查路由器和 Wi-Fi",
        "网络不通，检查一下路由器，或者等一会儿再试",
        Action.RETRY,
    )

    class NotConfigFormat(detail: String) : ElderError(
        Kind.NOT_CONFIG_FORMAT, "返回不是合法 JSON 或 TXT: $detail",
        "这个地址给的内容不对，可能不是节目单，或者内容提供方改了格式",
        "节目单出了点问题，家人正在处理",
        Action.CALL_FAMILY,
    )

    class NoAvailableSite(detail: String) : ElderError(
        Kind.NO_SITE, "无可用站点: $detail",
        "节目单里没有能用的内容，需要换一个内容地址",
        "现在没有能看的节目，家人正在准备",
        Action.CALL_FAMILY,
    )

    class JarDownloadFail(detail: String) : ElderError(
        Kind.JAR_DOWNLOAD, "jar 下载失败: $detail",
        "节目引擎下载失败。内容地址那边的服务器很慢或不稳定，请在家属模式里重试，或改用文件导入",
        "还在准备节目，请稍等；也可以先看直播",
        Action.GO_LIVE,
    )

    class JarInvalid(detail: String) : ElderError(
        Kind.JAR_INVALID, "jar 校验不通过（非 ZIP 或不完整）: $detail",
        "下载下来的节目引擎不完整，已拒绝使用（半截引擎会导致所有内容空白）",
        "还在准备节目，请稍等；也可以先看直播",
        Action.GO_LIVE,
    )

    class JarClassMissing(detail: String, val missing: List<String>) : ElderError(
        Kind.JAR_CLASS_MISSING, "jar 缺类: $detail (${missing.size} 个)",
        "节目引擎里缺少 ${missing.size} 个内容模块，可能是引擎版本与节目单不匹配",
        "这个节目暂时看不了，换个节目试试",
        Action.SWITCH_SOURCE,
    )

    class ResourceMissing(detail: String) : ElderError(
        Kind.RESOURCE_MISSING, "资源不存在（catch-all 回落）: $detail",
        "这个内容地址里没有它（文件不存在）。服务器对任何地址都返回同一个内容，属于内容源的问题",
        "这个节目暂时看不了，换个节目试试",
        Action.SWITCH_SOURCE,
    )

    class ExtEncrypted(detail: String) : ElderError(
        Kind.EXT_ENCRYPTED, "ext 无法解密: $detail",
        "该源依赖 jar 自带解密，未就绪。不是本机问题，需要在真机（arm64）上使用",
        "这个节目暂时看不了，换个节目试试",
        Action.SWITCH_SOURCE,
    )

    class Timeout(detail: String) : ElderError(
        Kind.TIMEOUT, "超时: $detail",
        "等太久没有响应，内容服务器可能太慢",
        "这个有点慢，换一个看吧",
        Action.SWITCH_SOURCE,
    )

    class ModuleMissing(detail: String) : ElderError(
        Kind.MODULE_MISSING, "运行时模块缺失: $detail",
        "此内容需要的运行组件没有装好，请在家属模式里重新检查",
        "这个节目暂时看不了，换个节目试试",
        Action.SWITCH_SOURCE,
    )

    class Unknown(detail: String) : ElderError(
        Kind.UNKNOWN, "未知: $detail",
        "出了一个问题。请在家属模式里复制诊断信息，发给家里人看看",
        "出了点小问题，点这里换个节目，或者找家人",
        Action.CALL_FAMILY,
    )

    companion object {
        fun unknown(detail: String): Unknown { return Unknown(detail) }

        fun from(t: Throwable): ElderError = when (t) {
            is ElderError -> t
            is java.net.UnknownHostException -> DnsFail(t.message ?: "")
            is java.net.SocketTimeoutException, is java.net.ConnectException -> Timeout(t.message ?: "")
            is java.io.FileNotFoundException -> ResourceMissing(t.message ?: "")
            is java.util.zip.ZipException -> JarInvalid(t.message ?: "")
            is org.json.JSONException -> NotConfigFormat(t.message ?: "")
            is RuntimeModule.MissingException -> ModuleMissing(t.message ?: "")
            is javax.net.ssl.SSLException -> ConfigUnreachable(t.message ?: "")
            is java.io.IOException -> ConfigUnreachable(t.message ?: "")
            else -> Unknown("${t.javaClass.simpleName}: ${t.message}")
        }
    }
}
