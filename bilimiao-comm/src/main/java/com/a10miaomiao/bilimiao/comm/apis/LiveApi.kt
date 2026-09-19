package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.live.LivePlayInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger

class LiveApi {

    /**
     * 直播間信息
     */
    fun info(roomId: String) = MiaoHttp.request {
        url = "https://api.live.bilibili.com/room/v1/Room/get_info?room_id=${roomId}"
    }

    /**
     * 直播间拉流地址：优先 HLS，其次 FLV。
     *
     * 请求要带 Referer；拿到手的流地址在播放时也要继续带 Referer 与 UA，取流 CDN 会校验，
     * 这一步由播放源负责。未开播、房间被加密或没有可用流时返回 null。
     */
    suspend fun roomPlayUrl(roomId: String, qn: Int = 10000): String? {
        val res = MiaoHttp.request {
            url = "https://api.live.bilibili.com/xlive/web-room/v2/index/getRoomPlayInfo" +
                "?room_id=$roomId&protocol=0,1&format=0,1,2&codec=0,1&qn=$qn&platform=web&ptype=8"
            headers["Referer"] = LIVE_REFERER
        }.awaitCall().json<ResponseData<LivePlayInfo>>()
        val url = res.data?.pickPlayUrl()
        // 落一条日志：真机排查时能直接看出选中的是 HLS 还是 FLV、以及取流主机
        miaoLogger() debug "live_playurl room=$roomId picked=${url?.substringBefore('?') ?: "none"}"
        return url
    }

    companion object {
        /** 直播间请求与取流都必须带的 Referer。 */
        const val LIVE_REFERER = "https://live.bilibili.com/"

        /**
         * 取流用的 UA。拉流接口是按 platform=web 要的地址，所以取流也配网页 UA；
         * 若个别 ROM 或机型上出现 403，把这里换成应用 UA 再验证。
         */
        const val LIVE_USER_AGENT = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36"
    }

}