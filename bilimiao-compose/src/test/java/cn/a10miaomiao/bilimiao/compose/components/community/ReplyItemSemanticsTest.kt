package cn.a10miaomiao.bilimiao.compose.components.community

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 评论项无障碍合并语义的单元测试
 * 验证拼接规则：置顶等卡片标, 昵称（视频作者加"UP主"前缀）, 评论内容, 图片, 点赞/已点赞, X条回复, 时间发布于IP
 * 昵称与内容之间用"说:"引出；间隔：英文逗号加空格
 */
class ReplyItemSemanticsTest {

    @Test
    fun 正常评论_未点赞_有IP() {
        val result = ReplyItemSemantics.buildContentDescription(
            uname = "张三",
            content = "这视频真好看",
            pictureCount = 0,
            like = 128,
            isLike = false,
            replyCount = 5,
            time = "3小时前",
            location = "浙江",
        )
        assertEquals(
            "张三说: 这视频真好看, 点赞128, 5条回复, 3小时前发布于浙江",
            result
        )
    }

    @Test
    fun 已点赞_语义变化() {
        val result = ReplyItemSemantics.buildContentDescription(
            uname = "张三",
            content = "这视频真好看",
            pictureCount = 0,
            like = 129,
            isLike = true,
            replyCount = 5,
            time = "3小时前",
            location = "浙江",
        )
        assertEquals(
            "张三说: 这视频真好看, 已点赞129, 5条回复, 3小时前发布于浙江",
            result
        )
    }

    @Test
    fun 一张图片_读含一张图片() {
        val result = ReplyItemSemantics.buildContentDescription(
            uname = "张三",
            content = "看这个",
            pictureCount = 1,
            like = 10,
            isLike = false,
            replyCount = 2,
            time = "10分钟前",
            location = "广东",
        )
        assertEquals(
            "张三说: 看这个, 含一张图片, 点赞10, 2条回复, 10分钟前发布于广东",
            result
        )
    }

    @Test
    fun 多张图片_读共X张图片() {
        val result = ReplyItemSemantics.buildContentDescription(
            uname = "张三",
            content = "看图",
            pictureCount = 3,
            like = 10,
            isLike = false,
            replyCount = 2,
            time = "10分钟前",
            location = "广东",
        )
        assertEquals(
            "张三说: 看图, 共3张图片, 点赞10, 2条回复, 10分钟前发布于广东",
            result
        )
    }

    @Test
    fun 没有IP_只读时间() {
        val result = ReplyItemSemantics.buildContentDescription(
            uname = "张三",
            content = "内容",
            pictureCount = 0,
            like = 1,
            isLike = false,
            replyCount = 0,
            time = "刚刚",
            location = "",
        )
        assertEquals(
            "张三说: 内容, 点赞1, 0条回复, 刚刚",
            result
        )
    }

    @Test
    fun 回复数为0_读0条回复() {
        val result = ReplyItemSemantics.buildContentDescription(
            uname = "李四",
            content = "沙发",
            pictureCount = 0,
            like = 0,
            isLike = false,
            replyCount = 0,
            time = "1小时前",
            location = "北京",
        )
        assertEquals(
            "李四说: 沙发, 点赞0, 0条回复, 1小时前发布于北京",
            result
        )
    }

    @Test
    fun 空内容_仍然拼接() {
        val result = ReplyItemSemantics.buildContentDescription(
            uname = "王五",
            content = "",
            pictureCount = 0,
            like = 5,
            isLike = false,
            replyCount = 1,
            time = "2天前",
            location = "上海",
        )
        assertEquals(
            "王五, 点赞5, 1条回复, 2天前发布于上海",
            result
        )
    }

    @Test
    fun 视频作者评论_昵称带UP主前缀() {
        val result = ReplyItemSemantics.buildContentDescription(
            uname = "张三",
            content = "感谢大家支持",
            pictureCount = 0,
            like = 66,
            isLike = true,
            replyCount = 3,
            time = "1天前",
            location = "浙江",
            isUpper = true,
        )
        assertEquals(
            "UP主张三说: 感谢大家支持, 已点赞66, 3条回复, 1天前发布于浙江",
            result
        )
    }

    @Test
    fun 置顶标签_放在最前面() {
        val result = ReplyItemSemantics.buildContentDescription(
            uname = "张三",
            content = "置顶说明",
            pictureCount = 0,
            like = 10,
            isLike = false,
            replyCount = 1,
            time = "5小时前",
            location = "广东",
            cardLabels = listOf("置顶"),
        )
        assertEquals(
            "置顶, 张三说: 置顶说明, 点赞10, 1条回复, 5小时前发布于广东",
            result
        )
    }

    @Test
    fun cardLabels含UP主_不重复播报() {
        val result = ReplyItemSemantics.buildContentDescription(
            uname = "张三",
            content = "作者本人评论",
            pictureCount = 0,
            like = 10,
            isLike = false,
            replyCount = 1,
            time = "5小时前",
            location = "广东",
            isUpper = true,
            cardLabels = listOf("置顶", "UP主"),
        )
        assertEquals(
            "置顶, UP主张三说: 作者本人评论, 点赞10, 1条回复, 5小时前发布于广东",
            result
        )
    }
}
