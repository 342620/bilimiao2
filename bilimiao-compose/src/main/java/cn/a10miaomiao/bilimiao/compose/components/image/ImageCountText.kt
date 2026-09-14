package cn.a10miaomiao.bilimiao.compose.components.image

/**
 * 图片数量的无障碍文案。
 *
 * 评论区、动态卡片、大图预览共用同一套说法，避免同一个含义在不同页面朗读不一致：
 * 1 张时读"含一张图片"，多张时读"共 N 张图片"。
 */
fun imageCountText(count: Int): String = when {
    count == 1 -> "含一张图片"
    count > 1 -> "共${count}张图片"
    else -> ""
}
