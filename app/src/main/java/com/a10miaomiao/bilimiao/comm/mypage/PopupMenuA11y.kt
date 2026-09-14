package com.a10miaomiao.bilimiao.comm.mypage

import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu

/**
 * 给系统 PopupMenu 加一个标题。
 *
 * 原生 PopupMenu 本身没有标题，读屏弹出时读不到"这是什么菜单"；
 * MenuBuilder 的 header 会画在菜单顶部并被读屏念出来，用它当标题。
 */
fun PopupMenu.setA11yTitle(title: String) {
    if (title.isBlank()) return
    (menu as? MenuBuilder)?.setHeaderTitle(title)
}