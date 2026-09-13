package cn.a10miaomiao.bilimiao.compose.common.foundation

import com.a10miaomiao.bilimiao.comm.utils.AccessibilityAnnouncer
import com.kongzue.dialogx.dialogs.PopTip

/**
 * 无障碍友好的 PopTip 包装。
 *
 * 在显示气泡通知的同时发送 AccessibilityEvent，让 TalkBack 朗读内容；
 * 返回值与 PopTip 保持一致，便于链式调用 showTop() 等扩展函数。
 */
object A11yPopTip {

    fun show(message: String): PopTip {
        val tip = PopTip.show(message)
        AccessibilityAnnouncer.announce(message)
        return tip
    }

    fun show(message: String, buttonText: String): PopTip {
        val tip = PopTip.show(message, buttonText)
        AccessibilityAnnouncer.announce(message)
        return tip
    }
}