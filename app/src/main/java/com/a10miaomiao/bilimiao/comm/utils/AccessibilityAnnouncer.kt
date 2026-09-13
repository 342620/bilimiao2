package com.a10miaomiao.bilimiao.comm.utils

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

/**
 * 无障碍播报工具
 * 用于让 TalkBack 立即朗读指定文本（如气泡通知、操作反馈等）
 */
object AccessibilityAnnouncer {

    private var context: Context? = null

    /**
     * 初始化，需要在 Application.onCreate 中调用
     */
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    /**
     * 发送无障碍播报事件
     */
    fun announce(message: String) {
        try {
            val ctx = context ?: return
            val accessibilityManager = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                ?: return
            if (!accessibilityManager.isEnabled) return
            val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
            event.text.add(message)
            event.className = "Toast"
            event.packageName = ctx.packageName
            accessibilityManager.sendAccessibilityEvent(event)
        } catch (e: Exception) {
            // 忽略异常，不影响正常功能
        }
    }
}
