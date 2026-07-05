package cn.a10miaomiao.bilimiao.compose.common.navigation

import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import java.net.URLEncoder

object BilibiliNavigation {

    private fun isNumeric(str: String): Boolean {
        return str.all { it.isDigit() }
    }

    private data class SimpleUri(
        val scheme: String?,
        val host: String?,
        val path: String?,
        val queryParams: Map<String, String>,
    ) {
        val queryParameterNames: Set<String> get() = queryParams.keys
        fun getQueryParameter(key: String): String? = queryParams[key]
    }

    private fun parseUrl(url: String): SimpleUri {
        val schemeEnd = url.indexOf("://")
        if (schemeEnd == -1) return SimpleUri(null, null, url, emptyMap())
        val scheme = url.substring(0, schemeEnd)
        val rest = url.substring(schemeEnd + 3)
        val pathStart = rest.indexOf('/')
        val queryStart = rest.indexOf('?')
        val host = when {
            pathStart != -1 -> rest.substring(0, pathStart)
            queryStart != -1 -> rest.substring(0, queryStart)
            else -> rest
        }
        val path = if (pathStart != -1) {
            val end = if (queryStart != -1) queryStart else rest.length
            rest.substring(pathStart, end)
        } else null
        val query = if (queryStart != -1) rest.substring(queryStart + 1) else ""
        val queryParams = if (query.isNotEmpty()) {
            query.split("&").associate {
                val eq = it.indexOf('=')
                if (eq != -1) it.substring(0, eq) to it.substring(eq + 1)
                else it to ""
            }
        } else emptyMap()
        return SimpleUri(scheme, host, path, queryParams)
    }

    fun navigationTo(
        pageNavigation: PageNavigator,
        url: String,
    ): Boolean {
        miaoLogger() debug url
        val uri = parseUrl(url)
        if (uri.scheme == "http" || uri.scheme == "https") {
            // BV号 → 视频
            Regex("BV([a-zA-Z0-9]{5,})").find(url)?.let {
                pageNavigation.navigateToVideoInfo("BV${it.groupValues[1]}")
                return true
            }
            // ss号 → 番剧
            Regex("ss(\\d+)").find(url)?.let {
                pageNavigation.navigateByUri("bilimiao://bangumi/${it.groupValues[1]}")
                return true
            }
            // ep号 → 番剧
            Regex("ep(\\d+)").find(url)?.let {
                pageNavigation.navigateByUri("bilimiao://bangumi/?epId=${it.groupValues[1]}")
                return true
            }
            // md号 → 番剧
            Regex("md(\\d+)").find(url)?.let {
                pageNavigation.navigateByUri("bilimiao://bangumi/?mediaId=${it.groupValues[1]}")
                return true
            }
        }
        // 空间 → 用户主页
        if (uri.host == "space.bilibili.com") {
            val path = uri.path?.replace("/", "") ?: ""
            val mid = if (isNumeric(path)) path else ""
            pageNavigation.navigateByUri("bilibili://space/$mid")
            return true
        }
        // avid → 视频
        if (uri.queryParameterNames.contains("avid")) {
            val aid = uri.getQueryParameter("avid")!!
            pageNavigation.navigateToVideoInfo(aid)
            return true
        }

        // bilibili://www.bilibili.com/... 这类 URI 实际是 B 站网页链接，
        // 交给 navigationToWeb 转成 https:// 后用 WebView 打开。
        // 但 bilibili://video、bilibili://space、bilibili://author 等 app scheme
        // 已在上游注册了 deep link，需先尝试 navigateByUri 让 NavHost 匹配。
        val navResult = pageNavigation.navigateByUri(url)
        if (navResult) return true

        if (uri.scheme == "bilibili") {
            val host = uri.host ?: ""
            // 仅当 host 是 bilibili.com / bilibili.tv / b23.tv 等域名时，
            // 才视作网页链接转 https:// 打开；其它 bilibili://xxx 走不支持提示。
            if ("bilibili.com" in host
                || "bilibili.tv" in host
                || "b23.tv" in host
                || "b23.snm0516.aisee.tv" in host) {
                navigationToWeb(pageNavigation, url)
                return true
            }
        }

        return false
    }

    fun navigationToWeb(
        pageNavigation: PageNavigator,
        url: String,
    ) {
        // bilibili://www.bilibili.com/xxx → https://www.bilibili.com/xxx
        // 仅当 host 是 B 站域名时才转换，其它 bilibili:// scheme（如 bilibili://game）
        // 无法安全转成 https://，直接提示不支持。
        val fullUrl = when {
            url.startsWith("bilibili://") -> {
                val rest = url.substringAfter("bilibili://")
                val host = rest.substringBefore('/')
                if ("bilibili.com" in host
                    || "bilibili.tv" in host
                    || "b23.tv" in host
                    || "b23.snm0516.aisee.tv" in host) {
                    "https://" + rest
                } else {
                    GlobalToaster.show("不支持的链接：$url")
                    return
                }
            }
            "://" in url -> url
            else -> "http://$url"
        }
        val uri = parseUrl(fullUrl)
        if (uri.scheme != "http" && uri.scheme != "https") {
            GlobalToaster.show("不支持的链接：$url")
            return
        }
        val host = uri.host ?: ""
        if ("bilibili.com" in host
            || "bilibili.tv" in host
            || "b23.tv" in host
            || "b23.snm0516.aisee.tv" in host) {
            // URL 编码：B 站专题页 URL 常带 ? 和 &，不编码的话 deepLink 会把 &...
            // 当成 bilimiao://web 的额外查询参数，导致 WebPage.url 被截断、页面打不开。
            // 接收端 navDeepLink 会自动 URL 解码。
            val encodedUrl = URLEncoder.encode(fullUrl, "UTF-8")
            pageNavigation.navigateByUri("bilimiao://web?url=$encodedUrl")
        } else {
            pageNavigation.launchWebBrowser(fullUrl)
        }
    }

}
