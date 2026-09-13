package cn.a10miaomiao.bilimiao.compose.components.community

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import cn.a10miaomiao.bilimiao.compose.common.foundation.AnnotatedTextNode

/**
 * 评论项无障碍合并语义的纯函数
 * 拼接规则：昵称, 评论内容, 图片, 点赞/已点赞, X条回复, 时间发布于IP
 * 间隔：英文逗号加空格
 */
object ReplyItemSemantics {

    /**
     * 构建评论项合并后的无障碍文本（纯字符串部分，用于单元测试）
     */
    fun buildContentDescription(
        uname: String,
        content: String,
        pictureCount: Int,
        like: Long,
        isLike: Boolean,
        replyCount: Long,
        time: String,
        location: String,
    ): String {
        val parts = mutableListOf<String>()
        parts.add(uname)
        if (content.isNotBlank()) {
            parts.add(content)
        }
        when {
            pictureCount == 1 -> parts.add("含一张图片")
            pictureCount > 1 -> parts.add("共${pictureCount}张图片")
        }
        parts.add(if (isLike) "已点赞$like" else "点赞$like")
        parts.add("${replyCount}条回复")
        parts.add(if (location.isNotBlank()) "${time}发布于${location}" else time)
        return parts.joinToString(", ")
    }

    /**
     * 构建评论项合并后的无障碍文本（带链接，用于 semantics.text）
     * 评论内容里的链接保留 LinkAnnotation，TalkBack 可通过链接菜单打开
     * Emote 表情直接读文字（如 [doge]）
     */
    fun buildAnnotatedText(
        uname: String,
        contentNodes: List<AnnotatedTextNode>?,
        pictureCount: Int,
        like: Long,
        isLike: Boolean,
        replyCount: Long,
        time: String,
        location: String,
    ): AnnotatedString {
        return buildAnnotatedString {
            append(uname)
            // 评论内容（保留链接）
            if (!contentNodes.isNullOrEmpty()) {
                append(", ")
                contentNodes.forEach { node ->
                    when (node) {
                        is AnnotatedTextNode.Text -> append(node.text)
                        is AnnotatedTextNode.Emote -> append(node.text)
                        is AnnotatedTextNode.Link -> {
                            if (node.withLineBreak) {
                                append("\n")
                            }
                            withLink(LinkAnnotation.Url(url = node.url)) {
                                append(node.text)
                            }
                        }
                    }
                }
            }
            // 图片
            when {
                pictureCount == 1 -> {
                    append(", ")
                    append("含一张图片")
                }
                pictureCount > 1 -> {
                    append(", ")
                    append("共${pictureCount}张图片")
                }
            }
            // 点赞
            append(", ")
            append(if (isLike) "已点赞$like" else "点赞$like")
            // 回复
            append(", ")
            append("${replyCount}条回复")
            // 时间+IP
            append(", ")
            append(if (location.isNotBlank()) "${time}发布于${location}" else time)
        }
    }
}
