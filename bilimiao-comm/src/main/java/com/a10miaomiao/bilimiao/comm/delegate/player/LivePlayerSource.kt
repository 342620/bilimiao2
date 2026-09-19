package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.apis.LiveApi
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo

/**
 * 直播间播放源：取到直播流地址后交给同一个播放器，沿用原有播放控件与无障碍标签。
 *
 * 直播没有时长、没有分 P，也不接弹幕，所以只提供单条“原画”清晰度，
 * 不实现 getDanmakuParser、historyReport 与 next。
 */
class LivePlayerSource(
    override val title: String,
    override val coverUrl: String,
    override val id: String, // 房间号
    override val ownerId: String = "",
    override val ownerName: String = "",
) : BasePlayerSource() {

    override suspend fun getPlayerUrl(quality: Int, fnval: Int): PlayerSourceInfo {
        val url = LiveApi().roomPlayUrl(id)
            ?: throw Exception("没有取到直播流地址，可能未开播或房间受限")
        return defaultPlayerSource.also {
            it.url = url
            it.quality = LIVE_QUALITY
            it.acceptList = listOf(PlayerSourceInfo.AcceptInfo(LIVE_QUALITY, "原画"))
            // 取流带上 Referer 与 UA。实测清单不带也能取到，但分片请求在部分节点会校验，统一带上更稳。
            it.header = mapOf(
                "Referer" to LiveApi.LIVE_REFERER,
                "User-Agent" to LiveApi.LIVE_USER_AGENT,
            )
        }
    }

    override fun getSourceIds(): PlayerSourceIds = PlayerSourceIds()

    /** 直播没有可定位的进度，播放器据此隐藏进度条与时间、禁掉拖动快进。 */
    override val isLive: Boolean get() = true

    companion object {
        /** 直播只有原画一档，沿用接口里的最高清晰度取值。 */
        const val LIVE_QUALITY = 10000
    }
}