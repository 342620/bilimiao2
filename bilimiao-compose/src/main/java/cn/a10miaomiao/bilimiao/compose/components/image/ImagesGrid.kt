package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerController
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import cn.a10miaomiao.bilimiao.compose.components.image.provider.localImagePreviewerController
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.PreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.TransformItemView
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.VerticalDragType
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberPreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberTransformItemState
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlin.math.min

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ImagesGridItem(
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    index: Int,
    imageModels: List<PreviewImageModel>,
    previewerController: ImagePreviewerController,
    previewerState: PreviewerState,
) {
    val model = imageModels[index]
    val itemState = rememberTransformItemState(
        intrinsicSize = Size(model.width, model.height),
    )
//    ScaleGrid(
//        modifier = modifier,
//        detectGesture = DetectScaleGridGesture(
//            onPress = {
//                previewerController.enterTransform(
//                    state = previewerState,
//                    models = imageModels,
//                    index = index
//                )
//            }
//        )
//    ) {
    Box(
        modifier = modifier
            // 无障碍：每张图单独一个焦点，读“图片第X张，共Y张”，双击可打开大图。
            // 动态列表里的卡片会用 clearAndSetSemantics 整张合并、评论区也会整条合并，
            // 这里的语义在那些地方会被清掉，只在动态详情这类没有合并的页面生效。
            .semantics {
                role = Role.Image
                contentDescription = imagePositionText(index, imageModels.size)
            }
            .clickable(onClickLabel = "查看图片") {
                previewerController.enterTransform(
                    state = previewerState,
                    models = imageModels,
                    index = index
                )
            }
    ) {
        TransformItemView(
            key = model.originalUrl,
            itemState = itemState,
            transformState = previewerState,
        ) {
            GlideImage(
                modifier = Modifier.fillMaxSize(),
                contentDescription = null,
                model = if (previewerController.isImageLoaded(model.originalUrl)) {
                    model.originalUrl
                } else { model.previewUrl },
                contentScale = contentScale,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImagesGrid(
    imageModels: List<PreviewImageModel>,
) {
    val count = imageModels.size
    val previewerController = localImagePreviewerController()
    val previewerState = rememberPreviewerState(
        verticalDragType = VerticalDragType.Down,
        pageCount = { count },
        getKey = { imageModels[it].originalUrl },
    )
    // 无障碍：整组图片不单独合成焦点，每张图各自一个焦点（见 ImagesGridItem）
    if (count == 1) {
        ImagesGridItem(
            modifier = Modifier.sizeIn(
                maxWidth = 300.dp,
                maxHeight = 300.dp,
            ),
            index = 0,
            imageModels = imageModels,
            previewerController = previewerController,
            previewerState = previewerState,
        )
    } else if (count <= 4) {
        BoxWithConstraints {
            val width = min(maxWidth.value, 300f)
            FlowRow(
                modifier = Modifier.width(width.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (index in 0 until count) {
                    ImagesGridItem(
                        modifier = Modifier.size((width / 2 - 4).dp),
                        index = index,
                        imageModels = imageModels,
                        previewerController = previewerController,
                        previewerState = previewerState,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    } else {
        BoxWithConstraints {
            val width = min(maxWidth.value, 330f)
            FlowRow(
                modifier = Modifier.width(width.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (index in 0 until count) {
                    ImagesGridItem(
                        modifier = Modifier.size((width / 3 - 3).dp),
                        index = index,
                        imageModels = imageModels,
                        previewerController = previewerController,
                        previewerState = previewerState,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}