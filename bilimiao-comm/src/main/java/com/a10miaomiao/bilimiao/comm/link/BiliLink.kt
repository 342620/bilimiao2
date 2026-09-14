package com.a10miaomiao.bilimiao.comm.link

import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

/**
 * 从文本（例如剪贴板）里解析出来的 B 站链接。
 */
sealed interface BiliLink {

    /** 视频：BV 号 */
    data class Video(val bvid: String) : BiliLink

    /** 视频：av 号 */
    data class VideoAv(val aid: Long) : BiliLink

    /** UP 主主页 */
    data class Space(val mid: String) : BiliLink

    /** b23.tv 短链：需要先跟随跳转拿到真实地址 */
    data class Short(val url: String) : BiliLink
}

/**
 * B 站链接解析。
 *
 * 支持整段文本（分享文案里前后带别的字也能认出来）：
 * bilibili.com/video/BV... 、bilibili.com/video/av... 、space.bilibili.com/数字、
 * 以及 b23.tv 短链（短链要配合 [resolveShort] 先拿到真实地址）。
 */
object BiliLinkParser {

    private val urlRegex = Regex("""https?://[^\s，。、！？；：“”（）【】《》"'<>]+""")

    private val bvRegex = Regex("""BV[0-9A-Za-z]{10}""")

    private val avRegex = Regex("""/av(\d+)""", RegexOption.IGNORE_CASE)

    private val spaceRegex = Regex("""space\.bilibili\.com/(\d+)""", RegexOption.IGNORE_CASE)

    /** 从任意文本里找第一个 B 站链接，找不到返回 null */
    fun parse(text: String): BiliLink? {
        if (text.isBlank()) return null
        return urlRegex.findAll(text)
            .mapNotNull { parseUrl(it.value) }
            .firstOrNull()
    }

    /** 解析单个链接；不是 B 站链接返回 null */
    fun parseUrl(url: String): BiliLink? {
        val trimmed = url.trim()
        val lower = trimmed.lowercase()
        if ("b23.tv" in lower) {
            return BiliLink.Short(trimmed)
        }
        spaceRegex.find(trimmed)?.let {
            return BiliLink.Space(it.groupValues[1])
        }
        bvRegex.find(trimmed)?.let {
            return BiliLink.Video(it.value)
        }
        avRegex.find(lower)?.let {
            it.groupValues[1].toLongOrNull()?.let { aid -> return BiliLink.VideoAv(aid) }
        }
        return null
    }

    /** b23.tv 短链跟随跳转，返回最终地址；失败返回 null */
    suspend fun resolveShort(url: String): String? = try {
        MiaoHttp.request { this.url = url }
            .awaitCall()
            .request.url.toString()
    } catch (e: Exception) {
        null
    }
}