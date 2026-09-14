package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import bilibili.app.dynamic.v2.DynamicItem
import bilibili.app.dynamic.v2.Module
import bilibili.app.dynamic.v2.ModuleDynamic
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import kotlin.math.min

/**
 * 动态卡片的无障碍信息。
 *
 * @param description 整张卡片合并成的一句话，顺序：昵称、发布时间、正文、
 *                    （含 N 张图片 / 视频标题）、点赞、评论
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
                isLiked = item.value.likeInfo?.isLike == true
            }

            else -> Unit
        }
    }

    if (content.isNotBlank()) {
        parts.add(content)
    }
    when {
        images.size == 1 -> parts.add("含一张图片")
        images.size > 1 -> parts.add("含${images.size}张图片")
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

    return DynamicItemA11yInfo(
        description = parts.joinToString(", "),
        authorMid = authorMid,
        imageModels = images,
        isLiked = isLiked,
        likeCount = like,
        commentCount = reply,
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