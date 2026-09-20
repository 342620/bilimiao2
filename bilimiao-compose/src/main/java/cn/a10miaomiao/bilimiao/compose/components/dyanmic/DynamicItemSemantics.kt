package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import bilibili.app.dynamic.v2.DynamicItem
import bilibili.app.dynamic.v2.Module
import bilibili.app.dynamic.v2.ModuleDynamic
import cn.a10miaomiao.bilimiao.compose.components.image.imageCountText
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import kotlin.math.min

/**
 * 动态卡片的无障碍信息。
 *
 * @param description 整张卡片合并成的一句话，顺序：昵称、发布时间、正文、
 *                    （含 N 张图片 / 视频标题）、点赞、评论、转发
 * @param authorMid 作者 mid，用于“查看用户主页”
 * @param imageModels 图片列表，用于“查看图片”
 * @param isLiked 当前是否已点赞，用于播报“已点赞 …”
 */
data class DynamicItemA11yInfo(
    val description: String,
    val authorMid: String? = null,
    val imageModels: List<PreviewImageModel> = emptyList(),
    val isLiked: Boolean = false,
    val likeCount: Long = 0L,
    val commentCount: Long = 0L,
    val forwardCount: Long = 0L,
)
/** 从动态的各个 module 里抽取无障碍描述与操作需要的数据 */
fun DynamicItem.toA11yInfo(): DynamicItemA11yInfo {
    val parts = mutableListOf<String>()
    var authorMid: String? = null
    var content = ""
    var images: List<PreviewImageModel> = emptyList()
    var videoTitle: String? = null
    var like = 0L
    var reply = 0L
    var repost = 0L
    var isLiked = false

    for (module in modules) {
        when (val item = module.moduleItem) {
            is Module.ModuleItem.ModuleAuthor -> {
                item.value.author?.let {
                    parts.add(it.name)
                    authorMid = it.mid.toString()
                }
                item.value.ptimeLabelText
                    .takeIf { it.isNotBlank() }
                    ?.let { parts.add(it) }
            }

            is Module.ModuleItem.ModuleDesc -> {
                content = item.value.desc.joinToString("") { it.text }
            }

            is Module.ModuleItem.ModuleDynamic -> {
                when (val dynamicItem = item.value.moduleItem) {
                    is ModuleDynamic.ModuleItem.DynDraw -> {
                        images = dynamicItem.value.toImageModels()
                    }

                    is ModuleDynamic.ModuleItem.DynArchive -> {
                        videoTitle = dynamicItem.value.title
                    }

                    is ModuleDynamic.ModuleItem.DynCommonLive -> {
                        val liveStateText = when (dynamicItem.value.liveState.number) {
                            1 -> "直播中"
                            2 -> "轮播中"
                            else -> "未开播"
                        }
                        videoTitle = "$liveStateText，${dynamicItem.value.title}"
                    }

                    is ModuleDynamic.ModuleItem.DynForward -> {
                        // 转发的内容：取被转发那条的图片 / 视频标题
                        val forwarded = dynamicItem.value.item?.modules ?: emptyList()
                        for (forwardModule in forwarded) {
                            when (val forwardItem = forwardModule.moduleItem) {
                                is Module.ModuleItem.ModuleDynamic -> {
                                    when (val inner = forwardItem.value.moduleItem) {
                                        is ModuleDynamic.ModuleItem.DynDraw -> {
                                            if (images.isEmpty()) images = inner.value.toImageModels()
                                        }

                                        is ModuleDynamic.ModuleItem.DynArchive -> {
                                            if (videoTitle == null) videoTitle = inner.value.title
                                        }

                                        else -> Unit
                                    }
                                }

                                else -> Unit
                            }
                        }
                    }

                    else -> Unit
                }
            }

            is Module.ModuleItem.ModuleStat -> {
                like = item.value.like
                reply = item.value.reply
                repost = item.value.repost
                isLiked = item.value.likeInfo?.isLike == true
            }

            // 新版文字动态：动态摘要（标题 + 正文）
            is Module.ModuleItem.ModuleOpusSummary -> {
                val titleText = item.value.title?.let { extractParagraphText(it) }.orEmpty()
                val summaryText = item.value.summary?.let { extractParagraphText(it) }.orEmpty()
                val opusText = listOf(titleText, summaryText)
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
                if (opusText.isNotBlank()) {
                    content = if (content.isBlank()) opusText else content + "\n" + opusText
                }
                // 摘要附带的封面图
                if (images.isEmpty()) {
                    val covers = item.value.covers
                    if (covers.isNotEmpty()) {
                        images = covers.map {
                            val w = min(600, it.width)
                            val h = w * it.width / it.height
                            val url = UrlUtil.autoHttps(it.src)
                            PreviewImageModel(
                                previewUrl = url + "@${w}w_${h}h",
                                originalUrl = url,
                                height = it.height.toFloat(),
                                width = it.width.toFloat(),
                            )
                        }
                    }
                }
            }

            // 新版文字动态：段落（正文段落 / 图片段落）
            is Module.ModuleItem.ModuleParagraph -> {
                val para = item.value.paragraph
                if (para != null) {
                    when (val contentItem = para.content) {
                        is bilibili.app.dynamic.v2.Paragraph.Content.Text -> {
                            val text = extractTextFromNodes(contentItem.value.nodes)
                            if (text.isNotBlank()) {
                                content = if (content.isBlank()) text else content + "\n" + text
                            }
                        }
                        is bilibili.app.dynamic.v2.Paragraph.Content.Pic -> {
                            val pics = contentItem.value.pics?.items
                            if (images.isEmpty() && pics != null && pics.isNotEmpty()) {
                                images = pics.map {
                                    val w = min(600, it.width)
                                    val h = w * it.width / it.height
                                    val url = UrlUtil.autoHttps(it.src)
                                    PreviewImageModel(
                                        previewUrl = url + "@${w}w_${h}h",
                                        originalUrl = url,
                                        height = it.height.toFloat(),
                                        width = it.width.toFloat(),
                                    )
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }

            else -> Unit
        }
    }

    if (content.isNotBlank()) {
        parts.add(content)
    }
    val imageText = imageCountText(images.size)
    when {
        imageText.isNotEmpty() -> parts.add(imageText)
        videoTitle != null -> parts.add("视频：$videoTitle")
    }
    parts.add(
        if (isLiked) {
            "已点赞${NumberUtil.converString(like)}"
        } else {
            "点赞${NumberUtil.converString(like)}"
        }
    )
    parts.add("评论${NumberUtil.converString(reply)}")
    parts.add("转发${NumberUtil.converString(repost)}")
    return DynamicItemA11yInfo(
        description = parts.joinToString(", "),
        authorMid = authorMid,
        imageModels = images,
        isLiked = isLiked,
        likeCount = like,
        commentCount = reply,
        forwardCount = repost,
    )
}

private fun bilibili.app.dynamic.v2.MdlDynDraw.toImageModels(): List<PreviewImageModel> {
    return items.map {
        val w = min(600, it.width)
        val h = w * it.width / it.height
        val url = UrlUtil.autoHttps(it.src)
        PreviewImageModel(
            previewUrl = url + "@${w}w_${h}h",
            originalUrl = url,
            height = it.height.toFloat(),
            width = it.width.toFloat(),
        )
    }
}

/** 从 Paragraph 提取纯文本（含表情的替代文字、链接的显示文字） */
private fun extractParagraphText(paragraph: bilibili.app.dynamic.v2.Paragraph): String {
    val content = paragraph.content ?: return ""
    return when (content) {
        is bilibili.app.dynamic.v2.Paragraph.Content.Text -> {
            extractTextFromNodes(content.value.nodes)
        }
        else -> ""
    }
}

/** 从 TextNode 列表提取纯文本：文字取 raw、表情取 rawText、链接取显示文字 */
private fun extractTextFromNodes(nodes: List<bilibili.app.dynamic.v2.TextNode>): String {
    return nodes.mapNotNull { node ->
        when (val text = node.text) {
            is bilibili.app.dynamic.v2.TextNode.Text.Word -> text.value.words
            is bilibili.app.dynamic.v2.TextNode.Text.Emote -> text.value.rawText?.words
            is bilibili.app.dynamic.v2.TextNode.Text.Link -> {
                // Link 的 showText 带控制字符，提取可见文字部分
                val showText = text.value.showText
                if (showText.isNotEmpty()) {
                    var start = 0
                    if (showText[0].code == 0x0a || showText[0].code == 0x0c) start = 1
                    if (start < showText.length && showText.getOrElse(1) { ' ' }.code == 0x0c) start = 2
                    if (start > 0) {
                        val end = showText.indexOf(0x11.toChar())
                        if (end > start) showText.substring(start, end) else showText.substring(start)
                    } else showText
                } else ""
            }
            null -> null
        }
    }.joinToString("")
}