package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

abstract class BasePlayerSource() {
    abstract val id: String // cid
    abstract val title: String
    abstract val coverUrl: String
    abstract val ownerId: String
    abstract val ownerName: String
    abstract suspend fun getPlayerUrl(quality: Int, fnval: Int): PlayerSourceInfo
    abstract fun getSourceIds(): PlayerSourceIds

    open suspend fun getSubtitles(): List<SubtitleSourceInfo> = emptyList()

    /** 直播源标记：进度条、时间与拖动快进按直播处理。 */
    open val isLive: Boolean get() = false

    open suspend fun getDanmakuParser(): BaseDanmakuParser? = null
    open suspend fun historyReport(progress: Long) {}

    open fun next(): BasePlayerSource? = null

    var defaultPlayerSource = PlayerSourceInfo()
    var proxyServer: ProxyServerInfo? = null
    var uposHost: String = ""
    var isLoop: Boolean = false // 循环播放
}