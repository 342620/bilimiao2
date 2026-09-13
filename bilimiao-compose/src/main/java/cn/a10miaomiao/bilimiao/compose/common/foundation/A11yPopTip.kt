package cn.a10miaomiao.bilimiao.compose.common.foundation

import com.a10miaomiao.bilimiao.comm.utils.AccessibilityAnnouncer
import com.kongzue.dialogx.dialogs.PopTip

/**
 * 无障碍友好的 PopTip 包装
 * 显示气泡通知的同时，发送 AccessibilityEvent 让 TalkBack 朗读
 */
object A11yPopTip {

    fun show(message: String) {
        PopTip.show(message)
        AccessibilityAnnouncer.announce(message)
    }

    fun show(message: String, icon: Int) {
        PopTip.show(message, icon)
        AccessibilityAnnouncer.announce(message)
    }
}
