package cn.a10miaomiao.bilimiao.compose.pages.message.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
internal fun MessageItemBox(
    avatar: String,
    nickname: String,
    actionText: String,
    title: String,
    sourceContent: String,
    time: Long,
    onUserClick: () -> Unit,
    onDetailClick: () -> Unit,
    onMessageClick: (() -> Unit),
    /** 右侧那栏（被评论的内容 / 视频标题）对应的操作名称 */
    detailActionLabel: String = "查看详情",
    /** 评论内容对应的操作名称，不需要时传 null */
    messageActionLabel: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            // 整条消息合成一个无障碍焦点：头像、昵称、动作、评论内容、右侧被评论内容、时间
            // 一起读；原来的点头像/点内容/点右侧改成读屏的操作菜单
            .clearAndSetSemantics {
                contentDescription = buildMessageDescription(
                    nickname = nickname,
                    actionText = actionText,
                    sourceContent = sourceContent,
                    title = title,
                    time = time,
                )
                customActions = buildList {
                    add(CustomAccessibilityAction("查看用户主页") {
                        onUserClick()
                        true
                    })
                    if (sourceContent.isNotBlank() && messageActionLabel != null) {
                        add(CustomAccessibilityAction(messageActionLabel) {
                            onMessageClick()
                            true
                        })
                    }
                    add(CustomAccessibilityAction(detailActionLabel) {
                        onDetailClick()
                        true
                    })
                }
            },
    ) {
        GlideImage(
            model = UrlUtil.autoHttps(avatar) + "@200w_200h",
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onUserClick)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(nickname)
                        }
                        append(actionText)
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
            }
            if (sourceContent.isNotBlank()) {
                Text(
                    modifier = Modifier.clickable(onClick = onMessageClick)
                        .padding(bottom = 5.dp),
                    text = sourceContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row {
                Text(
                    text = NumberUtil.converCTime(time),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Text(
            modifier = Modifier.width(60.dp)
                .clickable(onClick = onDetailClick),
            text = title,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * 消息条目的无障碍文案，顺序：昵称加动作、评论内容、右侧被评论的内容、时间。
 */
private fun buildMessageDescription(
    nickname: String,
    actionText: String,
    sourceContent: String?,
    title: String?,
    time: Long,
): String {
    val parts = mutableListOf<String>()
    (nickname + actionText).trim().takeIf { it.isNotEmpty() }?.let { parts.add(it) }
    sourceContent?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    title?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    NumberUtil.converCTime(time).takeIf { it.isNotBlank() }?.let { parts.add(it) }
    return parts.joinToString(", ")
}