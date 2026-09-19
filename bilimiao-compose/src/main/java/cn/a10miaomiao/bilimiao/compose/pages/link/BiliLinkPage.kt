package cn.a10miaomiao.bilimiao.compose.pages.link

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.view.v1.ViewGRPC
import bilibili.app.view.v1.ViewReq
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.status.BiliFailBox
import cn.a10miaomiao.bilimiao.compose.components.status.BiliLoadingBox
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.search.components.AuthorItemBox
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import com.a10miaomiao.bilimiao.comm.apis.LiveApi
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.LivePlayerSource
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.live.RoomInfo
import com.a10miaomiao.bilimiao.comm.entity.user.SpaceInfo
import com.a10miaomiao.bilimiao.comm.link.BiliLink
import com.a10miaomiao.bilimiao.comm.link.BiliLinkParser
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

/**
 * 外部链接解析面板。
 *
 * 剪贴板里认到 B 站链接时弹出，内容直接复用搜索页那两张卡片：
 * 视频用视频卡片（点卡片进播放页），UP 主用 UP 主卡片（点卡片进主页），
 * 不额外加按钮。
 */
@Serializable
class BiliLinkPage(
    private val kind: String,
    private val id: String,
) : ComposePage() {

    companion object {
        const val KIND_VIDEO = "video"
        const val KIND_SPACE = "space"
        const val KIND_SHORT = "short"
        const val KIND_LIVE = "live"

        /** 解析结果转面板参数：视频是 BV 号或 av 数字，UP 主是 mid，短链是原始链接，直播间是房间号 */
        fun of(link: BiliLink): BiliLinkPage = when (link) {
            is BiliLink.Video -> BiliLinkPage(KIND_VIDEO, link.bvid)
            is BiliLink.VideoAv -> BiliLinkPage(KIND_VIDEO, link.aid.toString())
            is BiliLink.Space -> BiliLinkPage(KIND_SPACE, link.mid)
            is BiliLink.Short -> BiliLinkPage(KIND_SHORT, link.url)
            is BiliLink.Live -> BiliLinkPage(KIND_LIVE, link.roomId)
        }
    }

    @Composable
    override fun Content() {
        val viewModel: BiliLinkViewModel = diViewModel(key = "biliLinkPage") {
            BiliLinkViewModel(it, kind, id)
        }
        BiliLinkContent(viewModel)
    }
}

private class BiliLinkViewModel(
    override val di: DI,
    kind: String,
    id: String,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    private var kind = kind
    private var id = id

    data class VideoCard(
        val bvid: String,
        val title: String,
        val pic: String,
        val upperName: String,
        val playNum: Long,
        val danmakuNum: Long,
        val duration: String,
    )

    data class AuthorCard(
        val mid: String,
        val name: String,
        val face: String,
        val sign: String,
        val fans: Int,
        val archives: Int,
        val level: Int,
    )

    data class LiveCard(
        val roomId: String,
        val title: String,
        val cover: String,
        val liveStatus: Int,
        val ownerId: String,
    )

    val loading = MutableStateFlow(true)
    val fail = MutableStateFlow<Any?>(null)
    val videoCard = MutableStateFlow<VideoCard?>(null)
    val authorCard = MutableStateFlow<AuthorCard?>(null)
    val liveCard = MutableStateFlow<LiveCard?>(null)

    init {
        load()
    }

    private fun load() = viewModelScope.launch(Dispatchers.IO) {
        try {
            loading.value = true
            fail.value = null
            if (kind == BiliLinkPage.KIND_SHORT) {
                // b23.tv 短链先跟随跳转，再按真实地址判断是视频还是 UP 主
                val realUrl = BiliLinkParser.resolveShort(id)
                    ?: throw IllegalStateException("短链接解析失败，请检查网络")
                when (val parsed = BiliLinkParser.parseUrl(realUrl)) {
                    is BiliLink.Video -> {
                        kind = BiliLinkPage.KIND_VIDEO
                        id = parsed.bvid
                    }

                    is BiliLink.VideoAv -> {
                        kind = BiliLinkPage.KIND_VIDEO
                        id = parsed.aid.toString()
                    }

                    is BiliLink.Space -> {
                        kind = BiliLinkPage.KIND_SPACE
                        id = parsed.mid
                    }

                    is BiliLink.Live -> {
                        kind = BiliLinkPage.KIND_LIVE
                        id = parsed.roomId
                    }

                    else -> throw IllegalStateException("链接里没有找到视频、UP主或直播间")
                }
            }
            when (kind) {
                BiliLinkPage.KIND_VIDEO -> loadVideo(id)
                BiliLinkPage.KIND_SPACE -> loadAuthor(id)
                BiliLinkPage.KIND_LIVE -> loadLive(id)
                else -> throw IllegalStateException("链接里没有找到视频、UP主或直播间")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fail.value = e
        } finally {
            loading.value = false
        }
    }

    private suspend fun loadVideo(videoId: String) {
        val req = if (videoId.startsWith("BV")) {
            ViewReq(bvid = videoId)
        } else {
            ViewReq(aid = videoId.toLongOrNull() ?: 0L)
        }
        val res = BiliGRPCHttp.request {
            ViewGRPC.view(req)
        }.awaitCall()
        val arc = res.arc ?: throw IllegalStateException("没有拿到视频信息")
        videoCard.value = VideoCard(
            bvid = res.bvid.ifBlank { videoId },
            title = arc.title,
            pic = arc.pic,
            upperName = arc.author?.name.orEmpty(),
            playNum = arc.stat?.view?.toLong() ?: 0L,
            danmakuNum = arc.stat?.danmaku?.toLong() ?: 0L,
            duration = NumberUtil.converDuration(arc.duration),
        )
    }

    private suspend fun loadAuthor(mid: String) {
        val res = BiliApiService.userApi
            .space(mid)
            .awaitCall()
            .json<ResponseData<SpaceInfo>>()
        val space = res.requireData()
        val card = space.card
        authorCard.value = AuthorCard(
            mid = card.mid,
            name = card.name,
            face = card.face,
            sign = card.sign,
            fans = card.fans,
            archives = space.archive.count,
            level = card.level_info.current_level,
        )
    }

    private suspend fun loadLive(roomId: String) {
        val res = LiveApi().info(roomId).awaitCall().json<ResponseData<RoomInfo>>()
        val room = res.requireData()
        liveCard.value = LiveCard(
            roomId = room.room_id.ifBlank { roomId },
            title = room.title,
            cover = room.user_cover,
            liveStatus = room.live_status,
            ownerId = room.uid,
        )
    }

    /** 进直播间：交给应用内同一个播放器播放，沿用原有播放控件与无障碍标签。 */
    fun openLive() {
        val card = liveCard.value ?: return
        basePlayerDelegate.openPlayer(
            LivePlayerSource(
                title = card.title,
                coverUrl = card.cover,
                id = card.roomId,
                ownerId = card.ownerId,
            )
        )
    }

    fun openVideo() {
        val card = videoCard.value ?: return
        pageNavigation.navigate(VideoDetailPage(id = card.bvid))
    }

    fun openAuthor() {
        val card = authorCard.value ?: return
        pageNavigation.navigate(UserSpacePage(id = card.mid))
    }
}

@Composable
private fun BiliLinkContent(
    viewModel: BiliLinkViewModel,
) {
    PageConfig(
        title = "链接解析"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val loading by viewModel.loading.collectAsState()
    val fail by viewModel.fail.collectAsState()
    val videoCard by viewModel.videoCard.collectAsState()
    val authorCard by viewModel.authorCard.collectAsState()
    val liveCard by viewModel.liveCard.collectAsState()
    // 委派属性不能智能转换，先取出来
    val failMessage = fail

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(windowInsets.toPaddingValues()),
    ) {
        when {
            loading -> BiliLoadingBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            )

            failMessage != null -> BiliFailBox(
                e = failMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            )

            videoCard != null -> {
                val card = videoCard!!
                VideoItemBox(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    title = card.title,
                    pic = card.pic,
                    upperName = card.upperName,
                    playNum = NumberUtil.converString(card.playNum),
                    damukuNum = NumberUtil.converString(card.danmakuNum),
                    duration = card.duration,
                    onClick = { viewModel.openVideo() },
                )
            }

            liveCard != null -> {
                val card = liveCard!!
                // 卡片播报只念标题、观看、弹幕、时长、UP主，remark 不参与播报，
                // 所以直播状态直接并进标题，保证读屏能听到"直播中/未开播"。
                VideoItemBox(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    title = "${if (card.liveStatus == 1) "直播中" else "未开播"}，${card.title}",
                    pic = card.cover,
                    onClick = { viewModel.openLive() },
                )
            }

            authorCard != null -> {
                val card = authorCard!!
                AuthorItemBox(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    name = card.name,
                    face = card.face,
                    sign = card.sign,
                    fans = card.fans,
                    archives = card.archives,
                    level = card.level,
                    onClick = { viewModel.openAuthor() },
                )
            }
        }
    }
}