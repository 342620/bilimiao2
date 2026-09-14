package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.localPageNavigation
import cn.a10miaomiao.bilimiao.compose.components.image.provider.localImagePreviewerController
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.VerticalDragType
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberPreviewerState
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage

@Composable
fun DynamicItemCard(
    modifier: Modifier = Modifier,
    item: bilibili.app.dynamic.v2.DynamicItem,
    isJumpToUser: Boolean = true,
    onClick: () -> Unit,
    onLikeClick: (() -> Unit)? = null,
    onCommentClick: (() -> Unit)? = null,
) {
    val a11yInfo = remember(item) { item.toA11yInfo() }
    val pageNavigation = localPageNavigation()
    val previewerController = localImagePreviewerController()
    val previewerState = rememberPreviewerState(
        verticalDragType = VerticalDragType.Down,
        pageCount = { a11yInfo.imageModels.size },
        getKey = { a11yInfo.imageModels[it].originalUrl },
    )
    // 语义挂在卡片外层：clearAndSetSemantics 会清掉整棵子树，
    // MiaoCard 自己那层点的语义（点击、合并）就不会再叠出来，
    // 整张卡片只保留一条播报文案和一组读屏操作
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                text = AnnotatedString(a11yInfo.description)
                customActions = buildList {
                    if (isJumpToUser) {
                        a11yInfo.authorMid?.let { mid ->
                            add(CustomAccessibilityAction("查看用户主页") {
                                pageNavigation.navigate(UserSpacePage(id = mid))
                                true
                            })
                        }
                    }
                    if (a11yInfo.imageModels.isNotEmpty()) {
                        add(CustomAccessibilityAction("查看图片") {
                            previewerController.enterTransform(
                                state = previewerState,
                                models = a11yInfo.imageModels,
                                index = 0,
                            )
                            true
                        })
                    }
                    onLikeClick?.let { like ->
                        add(CustomAccessibilityAction(if (a11yInfo.isLiked) "取消点赞" else "点赞") {
                            like()
                            true
                        })
                    }
                    onCommentClick?.let { comment ->
                        add(CustomAccessibilityAction("评论") {
                            comment()
                            true
                        })
                    }
                }
                // 点卡片本身是打开动态详情，清掉语义后要补回点击动作，双击才有效
                onClick(label = null) {
                    onClick()
                    true
                }
            },
    ) {
        MiaoCard(
            modifier = Modifier.padding(horizontal = 10.dp),
            onClick = onClick,
        ) {
            for (module in item.modules) {
                DynamicModuleBox(
                    module = module,
                    isJumpToUser = isJumpToUser,
                )
            }
        }
    }
}