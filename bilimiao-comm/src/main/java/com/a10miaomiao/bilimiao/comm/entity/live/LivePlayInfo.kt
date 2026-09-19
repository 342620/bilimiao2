package com.a10miaomiao.bilimiao.comm.entity.live

import kotlinx.serialization.Serializable

/**
 * 直播拉流信息（xlive/web-room/v2/index/getRoomPlayInfo 的 data 段）。
 *
 * 接口里协议、格式、编码都是数组且顺序不保证（同一个房间可能同时给 http_stream 与 http_hls），
 * 所以这里一律按名字挑，不按数组下标取，避免源站调整顺序后取到播不了的流。
 */
@Serializable
data class LivePlayInfo(
    val playurl_info: PlayurlInfo? = null,
) {
    @Serializable
    data class PlayurlInfo(
        val playurl: Playurl? = null,
    )

    @Serializable
    data class Playurl(
        val stream: List<Stream> = emptyList(),
    )

    @Serializable
    data class Stream(
        val protocol_name: String = "",
        val format: List<Format> = emptyList(),
    )

    @Serializable
    data class Format(
        val format_name: String = "",
        val codec: List<Codec> = emptyList(),
    )

    @Serializable
    data class Codec(
        val codec_name: String = "",
        val base_url: String = "",
        val url_info: List<UrlInfo> = emptyList(),
    )

    @Serializable
    data class UrlInfo(
        val host: String = "",
        val extra: String = "",
    )

    /**
     * 挑一个可播地址：优先 HLS（fmp4、ts），其次 FLV。
     * 未开播、房间被加密或源站没有下发可用流时返回 null。
     */
    fun pickPlayUrl(): String? =
        findUrl("http_hls", listOf("fmp4", "ts")) ?: findUrl("http_stream", listOf("flv"))

    private fun findUrl(protocolName: String, formatNames: List<String>): String? {
        val streams = playurl_info?.playurl?.stream ?: return null
        val stream = streams.firstOrNull { it.protocol_name == protocolName } ?: return null
        // 按调用方给的优先级挑格式（fmp4 先于 ts），不按响应里的数组顺序挑
        for (name in formatNames) {
            val format = stream.format.firstOrNull { it.format_name == name } ?: continue
            val codec = format.codec.firstOrNull() ?: continue
            val urlInfo = codec.url_info.firstOrNull() ?: continue
            if (codec.base_url.isBlank() || urlInfo.host.isBlank()) continue
            return urlInfo.host + codec.base_url + urlInfo.extra
        }
        return null
    }
}
