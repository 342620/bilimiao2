package cn.a10miaomiao.bilimiao.compose.components.community

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import cn.a10miaomiao.bilimiao.compose.common.foundation.AnnotatedTextNode
import cn.a10miaomiao.bilimiao.compose.components.image.imageCountText

/**
 * 评论项无障碍合并语义的纯函数
 * 拼接规则：昵称（视频作者加“UP主”前缀）, 评论内容, 图片, 点赞/已点赞, X条回复, 时间发布于IP
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
        isUpper: Boolean = false,
        cardLabels: List<String> = emptyList(),
    ): String {
        val parts = mutableListOf<String>()
        // 置顶这类接口给的卡片标放在最前面；UP主已经由 isUpper 表达，这里跳过避免重复
        cardLabels.filter { it.isNotBlank() && it != "UP主" }
            .takeIf { it.isNotEmpty() }
            ?.let { parts.add(it.joinToString("、")) }
        // 视频作者在自己视频下评论时，昵称带“UP主”前缀；昵称与内容之间用“说:”引出
        val namePart = if (isUpper) "UP主$uname" else uname
        if (content.isNotBlank()) {
            // 名字和内容作为同一段，避免中间再插逗号
            parts.add("$namePart说: $content")
        } else {
            parts.add(namePart)
        }
        val imageText = imageCountText(pictureCount)
        if (imageText.isNotEmpty()) {
            parts.add(imageText)
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
        isUpper: Boolean = false,
        cardLabels: List<String> = emptyList(),
    ): AnnotatedString {
        return buildAnnotatedString {
            // 置顶这类接口给的卡片标放在最前面；UP主已经由 isUpper 表达，这里跳过避免重复
            cardLabels.filter { it.isNotBlank() && it != "UP主" }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    append(it.joinToString("、"))
                    append(", ")
                }
            // 视频作者在自己视频下评论时，昵称带“UP主”前缀；昵称与内容之间用“说:”引出
            append(if (isUpper) "UP主$uname" else uname)
            // 评论内容（保留链接）
            if (!contentNodes.isNullOrEmpty()) {
                append("说: ")
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
            if (pictureCount > 0) {
                append(", ")
                append(imageCountText(pictureCount))
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
