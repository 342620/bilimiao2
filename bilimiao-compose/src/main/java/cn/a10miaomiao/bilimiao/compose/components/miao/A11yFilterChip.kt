package cn.a10miaomiao.bilimiao.compose.components.miao

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.stateDescription

/**
 * 带无障碍状态的 FilterChip。
 *
 * FilterChip 自己会把 selected 读成"已选中"和控件状态两条，导致一个状态念两遍；
 * 这里在外面套一层 clearAndSetSemantics 覆盖整棵子树，只保留一条文字和一条状态描述，
 * 外观与点击行为不变。
 *
 * @param text 读屏播报的文字（不传 label 时也用它当可见文字）
 * @param selected 是否选中
 * @param label 可见内容，默认就是 text
 */
@Composable
fun A11yFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    label: @Composable () -> Unit = { Text(text) },
) {
    Box(
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = text
            stateDescription = if (selected) "已选中" else "未选中"
            onClick(label = null) {
                onClick()
                true
            }
        },
    ) {
        FilterChip(
            modifier = modifier,
            selected = selected,
            enabled = enabled,
            onClick = onClick,
            leadingIcon = leadingIcon,
            label = label,
        )
    }
}