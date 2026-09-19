package com.a10miaomiao.bilimiao.comm.entity.live

import kotlinx.serialization.Serializable

@Serializable
data class RoomInfo(
    val uid: String,
    val room_id: String,
    val short_id: String,
    val background: String,
    val title: String,
    val user_cover: String,
    val keyframe: String,
    // 直播状态：0 未开播、1 直播中、2 轮播。缺省给 0，避免旧响应解析失败。
    val live_status: Int = 0,
)
