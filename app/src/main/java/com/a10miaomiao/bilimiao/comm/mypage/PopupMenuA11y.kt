package com.a10miaomiao.bilimiao.comm.mypage

import android.view.View

/**
 * 原生 PopupMenu 的标题播报。
 *
 * 系统自带的 PopupMenu 没有公开的标题接口，弹出时读屏不会说这是什么菜单；
 * 这里在弹出后直接播报一次标题，效果上等同于给菜单加上标题。
 */
fun View.announcePopupMenuTitle(title: String?) {
    val text = title?.takeIf { it.isNotBlank() } ?: return
    post { announceForAccessibility(text) }
}