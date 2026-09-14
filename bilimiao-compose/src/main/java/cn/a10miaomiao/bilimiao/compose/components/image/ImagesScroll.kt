package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerController
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import cn.a10miaomiao.bilimiao.compose.components.image.provider.localImagePreviewerController
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.PreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.TransformItemView
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.VerticalDragType
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberPreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberTransformItemState
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlin.math.min

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ImagesScrollItem(
    modifier: Modifier = Modifier,
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
            // 无障碍：每张图各自一个焦点，读“图片第X张，共Y张”，双击打开大图
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
                model = model.previewUrl,
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImagesScroll(
    imageModels: List<PreviewImageModel>,
) {
    val count = imageModels.size
    val previewerController = localImagePreviewerController()
    val previewerState = rememberPreviewerState(
        verticalDragType = VerticalDragType.Down,
        pageCount = { count },
        getKey = { imageModels[it].originalUrl },
    )
    var pagerState = rememberPagerState { count }
    HorizontalPager(
        modifier = Modifier.fillMaxWidth()
            .height(200.dp),
        state = pagerState,
    ) { page ->
        val item = imageModels
        ImagesScrollItem(
            modifier = Modifier.fillMaxSize(),
            index = page,
            imageModels = imageModels,
            previewerController = previewerController,
            previewerState = previewerState,
        )
    }
}