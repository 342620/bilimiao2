# bilimiao2 无障碍修改记录

## 仓库信息
- 远程地址：https://github.com/10miaomiao/bilimiao2.git
- 本地路径：/home/user/Doubao/chats/2951121666/bilimiao2
- 基线 commit：7171bd4a（2026-09-07）
- 包名修改：com.a10miaomiao.bilimiao → com.a10miaomiao.bilimiao.a11y（debug 自动加 .dev）
- 当前版本：2.5.0 alpha (versionCode 118)
- 最新构建：v42（2026-09-13）
- 打包命名规则：bilimiao-a11y-v版本号.apk（如 bilimiao-a11y-v30.apk）

## 构建环境
- JDK 17：/home/user/android-build/jdk-17.0.12+7
- Android SDK：/home/user/android-build/android-sdk
- 构建命令：
```bash
cd /home/user/Doubao/chats/2951121666/bilimiao2
export JAVA_HOME=/home/user/android-build/jdk-17.0.12+7
export ANDROID_HOME=/home/user/android-build/android-sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
./gradlew assembleFossDebug --no-parallel
```
- 输出路径：app/build/outputs/apk/foss/debug/app-foss-debug.apk
- 签名：debug 版用 debug 签名；release 版用 app/bilimiao-a11y.keystore（alias=bilimiao，密码见 app/signing.properties）
- 系统内存 3.9GB 无 swap，构建需严格控制内存
- 构建前如遇锁文件冲突：`pkill -9 -f java` + 删除 .lock 文件

## 构建配置修改（非无障碍，低内存环境优化）

### 1. app/build.gradle.kts
- applicationId 改为 com.a10miaomiao.bilimiao.a11y
- ABI 过滤器缩减为仅 arm64-v8a（降低构建内存占用）
- 新增依赖：androidx.media:media:1.7.0（MediaStyle 通知 + MediaSessionCompat 桥接）

### 2. gradle.properties
- org.gradle.jvmargs=-Xmx2560m -XX:+UseSerialGC -XX:MaxMetaspaceSize=384m -Xss512k
- org.gradle.daemon=false
- kotlin.compiler.execution.strategy=in-process
- android.dexingInProcess=true

## 启动崩溃修复

### MainComposeHost.kt
- 修复：将 navigator.attach(bottomBar, pageNavigation) 从 LaunchedEffect 移到组合过程中
- 原因：HomePageViewModel 构造时通过 Kodein 获取 PageNavigation，此时 attach 尚未执行导致 pageNavigationImpl 为 null
- 异常：IllegalStateException: PageNavigation not attached

## 当前有效的无障碍修改

### 1. 纯图标按钮 contentDescription 修复（10+ 处）

| 文件 | 修改内容 |
|---|---|
| StartUserCard.kt | 消息按钮 "message" → "消息" |
| StartSearchCard.kt | 扫码按钮 null → "扫码" |
| ReplyDetailContent.kt | 评论详情关闭按钮 null → "关闭" |
| BangumiEpisodeItem.kt | 番剧更多按钮 null → "更多" |
| DownloadDetailItem.kt | 播放/暂停/更多按钮 null → "播放"/"暂停"/"更多" |
| UserFavouriteDetailContent.kt | 折叠/展开按钮 null → "折叠菜单"/"展开菜单" |
| UserMedialistDetailContent.kt | 折叠/展开按钮 null → "折叠菜单"/"展开菜单" |
| UserSeasonDetailContent.kt | 折叠/展开按钮 null → "折叠菜单"/"展开菜单" |
| SearchOverlay.kt | 搜索全屏遮罩 null → "关闭搜索" |
| PlayerGestureHost.kt | 视频播控栏切换空按钮 null → "展开播控栏"/"收起播控栏" |

排查说明：全项目 74 处 contentDescription=null，大部分是装饰性图片（封面、头像）或伴随文字的图标，上述为纯图标按钮且无伴随文字，已修复。

### 2. 顶部菜单项读两遍 + 选中态修复

**文件：** MenuItem.kt + AppBarNavigationItem.kt

- 问题：Icon 的 contentDescription=data.title 和 Text 的 text=data.title 重复，clickable 自动 mergeDescendants 后合并成"动态 动态"读两遍
- 修复：Icon contentDescription 改 null
- 问题：appBarMenuItemSemantics 只设 contentDescription 没设 selected，不读选中态
- 修复：增加 checked 参数，只有 checked=true 时才设 selected=true（避免非选中按钮读"未选中"）

### 3. 选中态语义补充

| 文件 | 修改内容 |
|---|---|
| ThemeColorButton.kt | 主题颜色按钮 Surface 加 semantics { selected = isSelected } |
| ListStylePreference.kt | 列表式/卡片式切换两个 Surface 分别加 selected |

### 4. 全局页面 paneTitle

**文件：** MainComposeHost.kt

- MyNavHost 外层包 Box，自动读取 LocalPageConfigState.currentConfig.title 设 paneTitle，60+ 页面统一生效
- MyBottomSheet 弹窗内容也包一层设 paneTitle
- 标题为空时默认"bilimiao"/"弹窗"

### 5. 对话框 paneTitle（29 处，20+ 文件）

- AnyPopDialog.kt：新增可选 title 参数，内容层设 paneTitle
- MessageDialog.kt / SingleChoiceDialog.kt：加 paneTitle
- 全项目 AlertDialog 批量加 paneTitle：提示、确认删除、搜索筛选、设置分组、编辑收藏夹、检查更新、代理服务器、屏蔽标签/关键字、倍速设置等

### 6. 抽屉菜单 paneTitle

**文件：** ComposeScaffold.kt

- 给 drawerContent 内容包一层 Box 设 paneTitle = "菜单"
- **注意：必须加在内容层，不能加在做位移动画的外层 Box**，否则会导致全局焦点丢失

### 7. 视频评论区标题修复

**文件：** VideoReplyContent.kt

- 评论区页面标题从 "AV号/BV号" 改为视频标题 arcData.title

### 8. 视频卡片语义简化

**文件：** VideoItemBox.kt（卡片式）+ MiniVideoItemBox.kt（列表式）

- "视频时长" → "时长"
- "播放量" → "播放"
- "弹幕数" → "弹幕"
- 多指标朗读格式：不同指标之间用英文逗号加空格隔开（每个指标 contentDescription 前面加", "），标签和值之间不加空格直接连在一起
- 效果：`标题,up主哈里大王,播放473.5万,弹幕797,时长10:27`

### 9. 顶部操作按钮去掉多余选中态

**文件：** MenuItem.kt

- 问题：视频详情页下载、收藏、更多等操作按钮 TalkBack 读"未选中"
- 修复：只有 checked=true 时才设 selected=true，checked=false 时不设 selected 语义

### 10. 评论区按钮语义修复

**文件：** ReplyItemBox.kt

- 点赞按钮："like" → "点赞"，加 selected 语义
- 回复按钮："reply" → "回复"
- 删除按钮：错误的 "reply" → "删除"

### 11. 底部 AppBar 遍历顺序修复

**文件：** AppBar.kt + 5 个页面文件

- 问题：垂直 AppBar 位于屏幕底部，菜单项用 LazyRow + reverseLayout=true 渲染，导致视觉顺序和 TalkBack 遍历顺序相反
- 修复：去掉 LazyRow 的 reverseLayout=true，把 menus 列表反转后再传进去
- 注意：index 仍用原始列表的 index，确保子菜单 anchor 不错乱
- traversalIndex 方案已验证不生效，不要用

### 12. 下载列表 FilterChip 重复语义修复

**文件：** DownloadListPage.kt

- 问题：Material3 FilterChip 同时设置 selected 语义和 role=Checkbox+checked 语义，导致重复朗读"已选中，未选中复选框"
- 修复：给三个 FilterChip（全部/下载中/下载完成）加 clearAndSetSemantics，只保留 contentDescription + selected + onClick
- UI 完全不变

### 13. 多选对话框复选框重复语义修复

**文件：** PreferenceComponents.kt

- 问题：第三方库 me.zhanghai.compose.preference 的列表项 Row 用了 toggleable(..., Role.Checkbox, ...)，本身就是复选框角色，里面又放了一个 Checkbox 组件，两套语义被 mergeDescendants 合并后重复朗读
- 修复：在项目封装函数 MultiSelectListPreference 里传自定义 item，给 Checkbox 加 clearAndSetSemantics {} 清掉它的语义，只保留 Row 的 toggleable 语义
- 影响：播放器自动控制、播放器播放顺序、底部进度条显示控制等所有多选对话框
- UI 完全不变
- 注意：第三方库支持 item 参数，不支持 itemContent 参数

### 14. 纯图标按钮 contentDescription 修复（第二批，v21）

| 文件 | 修改内容 |
|------|----------|
| VideoPagesBox.kt | 分P展开按钮 "More" → "展开分P列表" |
| MainComposeHost.kt | 底部弹窗关闭按钮 "close" → "关闭"（影响所有底部弹窗） |
| BiliLoadingBox.kt | 加载动画 "loading" → "加载中" |

### 15. UP主头像跳转 bug 修复（v21）

**文件：** UserSpacePage.kt

- 问题：点击任意 UP 主头像都进入自己的个人中心
- 原因：UserSpaceViewModel 缺 key，ViewModel 缓存导致返回第一个创建的（自己的）
- 修复：UserSpaceViewModel 加 key = "space$id"

### 16. 视频详情页 paneTitle 修正（v21）

**文件：** VideoDetailContent.kt

- 问题：视频详情页 paneTitle 是 "AV号 / BV号"，打开时 TalkBack 读一串数字
- 修复：改成 arcData.title（视频标题），和评论区页面保持一致

### 17. 气泡通知无障碍朗读（v21）

**文件：** MainComposeHost.kt

- 问题：GlobalToaster 弹出的气泡通知（点赞成功、收藏成功等）TalkBack 不会自动朗读
- 原因：用的是 com.dokar.sonner 库的自定义 Toaster，不是系统 Toast
- 修复：给 Toaster 包一层 Box，加 semantics { liveRegion = LiveRegionMode.Assertive }
- 效果：toast 出现时 TalkBack 立即朗读，打断当前内容
- UI 完全不变

### 18. 音频焦点修复 + TalkBack 音频闪避（v21）

**涉及文件：**
- commonMain/PlayerPlatform.kt：加 expect fun setPlayerAudioFocus(player, enabled)
- androidMain/PlayerPlatform.kt：实现 exoPlayer.setAudioAttributes(attributes, enabled)
- desktopMain/PlayerPlatform.kt：no-op
- PlayerDelegateImpl.kt：监听 PlayerAudioFocus 设置变化，动态更新

- 问题：设置里的"占用音频焦点"选项是个摆设，没有任何代码读取它的值
- 问题：TalkBack 朗读时视频音量不会自动降低（音频闪避无效）
- 修复：
  1. 用 exoPlayer.setAudioAttributes(AudioAttributes, handleAudioFocus) 同时设置音频属性和是否占用焦点
  2. AudioAttributes 设置为 USAGE_MEDIA + CONTENT_TYPE_MOVIE，确保系统正确识别为媒体播放
  3. PlayerDelegateImpl 里启动协程监听设置变化，动态调用 setPlayerAudioFocus
  4. closePlayer 时取消协程，避免泄漏
- 效果：
  - 开启"占用音频焦点"（默认）：TalkBack 朗读时视频音量自动降低，读完恢复
  - 关闭"占用音频焦点"：不请求音频焦点，可与其它应用同时播放
  - 设置变化时动态生效，不用重启播放器

### 19. 音频焦点修复第二版（v22）

**涉及文件：** androidMain/PlayerPlatform.kt

- 问题：v21 的设置在异步协程里执行，播放器创建后可能立即开始播放，设置还没执行到，导致第一首视频闪避不生效
- 修复：在 createMediampPlayer() 里创建 ExoPlayer 后立即同步调用 exoPlayer.setAudioAttributes(USAGE_MEDIA + CONTENT_TYPE_MOVIE, true)
- 效果：播放器一创建就持有音频焦点，闪避立即生效

### 20. 耳机线控 + 完整媒体通知（v23-v28）

**核心文件：** app/src/main/java/com/a10miaomiao/bilimiao/service/PlaybackService.kt

**问题根因：** PlaybackService 从来没被启动过，MediaSession 根本不存在。代码里只有 Service 定义和 import，没有任何地方调用 startService（旧版本 PlayerDelegate2.startPlaybackService() 在重构时丢失）。

**修复过程：**
- v23：PlaybackService 加 startService/stopService 静态方法；MainActivity 监听 playbackState，播放时启动 Service 并设置 ExoPlayer 给 MediaSession；BiliExoPlayerMediampPlayer 把 exoPlayer 从 internal 改成 public
- v23 崩溃：ForegroundServiceDidNotStartInTimeException（startForegroundService 后 10 秒内未调用 startForeground）
- v24：在 PlaybackService.onCreate() 里创建完 MediaSession 后立即调用 startForeground()，显示一个简单通知
- v24 新问题：双击暂停后再次点击播放不了，一返回就崩
- v25：setPlayer 里加判断，同一个 player 不重复 release；onDestroy 里不再释放 player（player 由 PlayerDelegateImpl 管理）
- v26：尝试完整媒体通知失败，media3 的 API 和旧版差异大
- v27：完整媒体通知实现
  - 加依赖 androidx.media:media:1.7.0
  - 用 MediaSessionCompat.Token.fromToken(it.platformToken) 桥接 media3 到旧版 MediaStyle
  - 三个按钮（上一个用 replay、播放/暂停、下一个）走 PendingIntent.getService + onStartCommand
  - 标题和 UP 主从 playerDelegate.sourceState 读取
  - Player.Listener 监听 onIsPlayingChanged/onMediaItemTransition/onPlaybackStateChanged 自动刷新通知
- v28：
  - 播放/暂停状态不同步：改用 exoPlayer?.isPlaying 直接读 ExoPlayer 状态（之前用 playerDelegate.isPlaying() 读 _playbackState 有延迟）
  - 封面图：用 Glide 异步加载 currentSource.coverUrl，设为通知大图标（setLargeIcon），加载完成后刷新通知

**上一个/下一个逻辑：**
- 上一个：调 playerDelegate.replay() = seekTo(0) + resume()，回到视频开头重新播
- 下一个：调 playerDelegate.playNext()，从当前 source 的 next() 拿下一个分 P。如果视频没有分 P，next() 返回 null，就不切。播放列表维度的"下一个"目前没对接

**关键代码位置：**
- onCreate：第68行，initializeSessionAndPlayer() 后立即 startForeground
- buildNotification：第85行，MediaStyle + platformToken 桥接 + 三按钮 + 标题UP主 + 封面
- setPlayer：第205行，已加同一个 player 不重复 release 判断
- onDestroy：第270行附近，不再释放 player
- onStartCommand：第247行附近，处理三个按钮事件
- loadCover：第92行附近，Glide 异步加载封面

### 21. 表情面板无障碍修复（v29）

**文件：** EmojiGridBox.kt

- 问题1：表情面板里的所有 Emoji 表情都读不出来，图片表情 contentDescription=null，文字表情也没有语义
- 修复1：给两种类型的表情外层 Box 都加 semantics { contentDescription = item.text }，item.text 是表情的文字描述（如 "[doge]"）
- 问题2：表情面板顶上的分类 tab 有重复的"已选中/未选中"语义（和下载列表 FilterChip 同样的问题）
- 修复2：给 FilterChip 加 clearAndSetSemantics，只保留 contentDescription + selected + onClick
- UI 完全不变

### 22. Toast 气泡通知无障碍播报修复（v30）

**涉及文件：**
- commonMain/GlobalToaster.kt：加 expect fun announceForAccessibility(message: String)，在 show/showLong/showWithAction 里调用
- androidMain/GlobalToasterAndroid.kt：actual 实现，用 AccessibilityManager 发送 TYPE_ANNOUNCEMENT 事件
- desktopMain/GlobalToasterDesktop.kt：actual 空实现

- 问题：v21 给 Toaster 外层 Box 加的 liveRegion 语义没生效，sonner 库的内部结构和动画导致 liveRegion 检测不到内容变化
- 修复：改成在 GlobalToaster.show() 里主动发送 AccessibilityEvent.TYPE_ANNOUNCEMENT，用 AccessibilityManager.sendAccessibilityEvent() 直接让 TalkBack 播报，不依赖 UI 层的 liveRegion 检测
- 效果：点赞成功、收藏成功、下载开始等所有气泡通知都能被 TalkBack 立即朗读

### 23. 图片预览加载动画修复 + 预览页无障碍标签（v32）

**文件：** MyImagePreviewer.kt

- 问题：点开图片预览后一直显示加载动画，图片实际已加载但动画不消失
- 原因：原来只在 painter.state is Success 时才赋值 painterState.value，加载失败或状态异常时卡住
- 修复：无论 Success/Loading/Error 都把 painter 传出去
- 新增：图片预览页加 contentDescription = "图片预览，第X张，共Y张"

### 24. 评论区焦点合并（v32-v33，TDD 由红转绿）

**核心文件：**
- ReplyItemSemantics.kt（新增）：纯字符串拼接函数 buildContentDescription() + 带链接的 buildAnnotatedText()
- ReplyItemSemanticsTest.kt（新增）：7个单元测试用例
- ReplyItemBox.kt：外层 Row 加 clearAndSetSemantics，text=buildAnnotatedText()，添加 CustomAccessibilityAction

**拼接规则（用户明确确认）：**
- 排序：昵称, 评论内容, 图片（有图才加）, 点赞/已点赞, X条回复, 时间+IP
- 间隔：英文逗号加空格，标签与值无空格
- 图片：1张→"含一张图片"，>1张→"共X张图片"
- 点赞：未点赞→"点赞XX"，已点赞→"已点赞XX"
- 回复："X条回复"
- 时间IP：有IP→"3小时前发布于浙江"，无IP→单独"3小时前"
- 不需要 UP 主标签

**CustomAccessibilityAction 列表：**
- 查看用户主页
- 点赞/取消点赞（根据状态切换文案）
- 查看回复/回复评论（一级评论 count>0 时"查看回复"否则"回复"）
- 查看图片（有图时）
- 删除评论（自己的评论）

**技术要点：**
- 用 text(AnnotatedString带LinkAnnotation) 而非 contentDescription，保留评论内容里的链接可被 TalkBack 链接菜单识别
- clearAndSetSemantics 清除内部所有子节点语义，外层 Row 唯一焦点
- build.gradle.kts 加 testImplementation("junit:junit:4.13.2")（bilimiao-compose 模块原本无测试依赖）

### 25. 评论区点赞无障碍反馈（v33）

- 点赞/取消点赞后调用 announceForAccessibility() 播报"已点赞"/"已取消点赞"
- 不显示 Toast，只发无障碍公告
- announceForAccessibility 是 expect/actual 函数，Android 端发 AccessibilityEvent.TYPE_ANNOUNCEMENT

### 26. 三个 bug 修复（v34）

| bug | 文件 | 修复 |
|-----|------|------|
| UP主合集切换，点第二个合集进不去 | UserMedialistPage.kt | diViewModel key 从 mid 改成 mid + "_" + bizId，不同合集创建独立 ViewModel |
| 用户资料卡头像无无障碍焦点 | UserSpaceHeader.kt | clearAndSetSemantics 清除内部 AsyncImage 语义，外层 Box 唯一焦点，contentDescription="用户头像，点击查看大图" |
| 自动连播开关文字和开关被拆成两个焦点 | VideoUgcSeasonBox.kt | 包一层 Row + clickable（整行可点切换）+ clearAndSetSemantics 合并语义，stateDescription="已开启"/"已关闭" |

### 27. 视频放完无法从头播放修复（v35）

**根因（反编译 mediamp-exoplayer 确认）：**
- MediampPlayer.seekTo()：playbackState.compareTo(READY) < 0 或 openResource == null 时直接 return，不执行 exoPlayer.seekTo()
- MediampPlayer.resumeImpl()：switch 只处理 PAUSED/BUFFERING 状态，FINISHED 状态直接 return
- stopPlaybackImpl()：设置 playbackState = FINISHED，调用 exoPlayer.stop() + clearMediaItems()
- 播放完成后状态变成 FINISHED，replay() 里的 seekTo(0) + resume() 完全无效

**修复：**
- PlayerPlatform.kt（commonMain）：加 expect fun replayFromStart(player: MediampPlayer)
- PlayerPlatform.kt（androidMain）：actual 实现，直接获取 BiliExoPlayerMediampPlayer.exoPlayer 调用 seekTo(0) + setPlayWhenReady(true)
- PlayerPlatform.kt（desktopMain）：用原有逻辑
- PlayerDelegateImpl.kt：replay() 方法改为调用 replayFromStart

### 28. 通知栏播放器修复（v36-v41，多轮迭代）

**根因：** 作者从 PlayerDelegate2 重构为 PlayerDelegateImpl 后，把整个通知栏启动逻辑丢了。2.4.8.1 正式版用 PlayerDelegate2.onResume() + MediaController.Builder + SessionToken 绑定服务，最新 master 里 PlaybackService 只有定义和 import，没有任何地方启动它。

**迭代过程：**
- v36：删除自定义通知，恢复 MediaSessionService 自动管理 → ForegroundServiceDidNotStartInTimeException 崩溃（空播放器时 MediaSessionService 不立即调 startForeground）
- v37：onCreate 手动 startForeground + setPlayerDelegate 重新绑定 player → 10次8次播放加载不出来（重新绑定 player 干扰了正常播放流程）
- v38：完全恢复作者原本代码 → 仍然崩溃
- v39：v38 + onCreate startForeground → 通知栏只显示占位，第二个视频放不了
- v40：发现 setPlayer 里 exoPlayer?.release() 会释放正在使用的播放器；加 isPlayerAttached 保护只设置一次；MainActivity onCreate 启动服务 → 通知栏显示仍不对
- v41（最终方案）：参考 2.4.8.1 正式版实现
  - MainActivity.onResume() 用 MediaController.Builder(context, SessionToken(context, ComponentName(context, PlaybackService::class.java))).buildAsync() 绑定服务
  - 绑定成功后调用 setPlayer（有 isPlayerAttached 保护）和 setPlayerDelegate
  - 删除 MainActivity.onCreate 里的服务启动逻辑
  - PlaybackService 保留：isPlayerAttached 保护、MyForwardingPlayer.getMediaMetadata()、NetworkBitmapLoader、onCreate startForeground 防崩溃

**上一个/下一个逻辑：**
- 上一个：playerDelegate.replay() = seekTo(0) + resume()
- 下一个：playerDelegate.playNext()，只处理分P视频，播放列表维度的"下一个"未对接

### 29. 通知栏封面图修复（v42）

**文件：** PlaybackService.kt（MyForwardingPlayer.getMediaMetadata()）

- 根因：B站返回的 coverUrl 是 //i0.hdslb.com/bfs/archive/xxx.jpg 相对路径，直接 Uri.parse() 生成无 scheme 的 URI，NetworkBitmapLoader 用 OkHttp 请求失败
- 修复：UrlUtil.autoHttps(coverUrl) + "@300w_300h_1c_" 缩略图参数，和 2.4.8.1 正式版处理方式一致

### 30. 视频详情页统计文字焦点合并（v42）

**文件：** VideoInfoBox.kt

- 播放量、弹幕数、发布时间那一排 Row 加 clearAndSetSemantics
- contentDescription = "播放XX, 弹幕XX, 发布时间"
- 两个图标的 contentDescription 改为 null，避免重复朗读
- 视频标题保持独立焦点不变

### 31. Tab 栏 TalkBack 误触发滚动修复（v42）

**文件：** VideoDetailPage.kt + ScreenReader.kt（common/android/desktop）

- 问题：TalkBack 滑动浏览焦点时，遍历到视频分类/Tab 区域有概率触发横向滚动，把 Tab 从推荐切到评论区
- 根因：HorizontalPager 默认 userScrollEnabled=true，手势检测器和 TalkBack 浏览手势冲突
- 修复：HorizontalPager 加 userScrollEnabled = !isScreenReaderEnabled()
- isScreenReaderEnabled() 用 AccessibilityManager.isEnabled + isTouchExplorationEnabled 检测
- TalkBack 启用时禁用滑动翻页，只能点击 Tab 切换；明眼用户不受影响

### 32. 播控栏无障碍焦点保持（v43 完成，Android View 层方案）

**文件：** PlayerControlsVisibilityPolicy.kt + PlayerA11yFocusContainer.kt（common/android/desktop）+ A11yFocusInterceptorFrameLayout.kt + VideoScaffold.kt + PlayerGestureHost.kt

- 目标：TalkBack 焦点在播放器区域内时不自动收起播控栏，焦点离开后按正常 3 秒逻辑收起
- v42 尝试 Compose 层 semantics.onAccessibilityEvent，编译不过，已撤掉
- v43 方案：在 Android View 层重写 requestSendAccessibilityEvent + sendAccessibilityEventUnchecked，拦截 TYPE_VIEW_ACCESSIBILITY_FOCUSED/CLEARED 事件
- 焦点移动的事件流：先 CLEARED 再 FOCUSED，用 100ms 延迟检查判断焦点是否真的离开（播放器内移动时 CLEARED 后很快有新 FOCUSED，不判定为离开）
- PlayerControlsVisibilityPolicy 纯函数策略：只有"触摸浏览开着 且 焦点在播放器内"才不自动收起
- 单元测试 4 个用例全部通过（红→绿）
- VideoScaffold 用 PlayerA11yFocusContainer 包裹播放器区域，维护 a11yFocusInside 状态，通过 CompositionLocal 传给 PlayerGestureHost
- PlayerGestureHost 自动收起 LaunchedEffect 里加入策略判断：shouldAutoHide 返回 false 时不执行 delay+hide
- 桌面端为空实现（直接 Box 包裹）
- 不改变 UI，只改语义层和自动收起逻辑

## 已回滚的修改（不要重新加）

### 1. heading 语义（全局焦点丢失，已确认回滚）

**涉及文件：** PreferenceComponents.kt、MiaoTitleBar.kt、HistoryPage.kt、ThemeSettingPage.kt

- 第一次尝试（v6 前后）：给设置页面分组标题加 semantics { heading() }
- 结果：导致整个应用所有页面都获取不到无障碍焦点，TalkBack 完全用不了
- 回滚后确认：heading 语义独立导致全局焦点丢失
- v23 重新尝试：在抽屉内容层 paneTitle 方案稳定后，重新给设置页面分组标题加了 heading 语义
- 结果：再次导致全局焦点丢失，已回滚
- **最终结论：heading 语义在当前 Compose 版本下独立导致全局焦点丢失，不要加**

### 2. 抽屉菜单动画外层 paneTitle（全局焦点丢失）

**涉及文件：** ComposeScaffold.kt

- 尝试：给做位移动画的外层 Box 加 paneTitle
- 结果：全局焦点丢失
- 原因：抽屉关闭时外层 Box 仍存在于语义树中（alpha=0 但未设不可访问），加 paneTitle 后变成"幽灵窗格"干扰全局
- 正确做法：paneTitle 加在抽屉内容层的 Box 上（当前有效修改第 6 条）

### 3. traversalIndex（不生效）

**涉及文件：** MenuItemPropInfo.kt、MenuItemData.kt、AppBarTreeMenuItem.kt 等 6 个文件

- 尝试：给 LazyRow 子项加 Modifier.traversalIndex(index) 改变遍历顺序
- 结果：在 LazyRow + reverseLayout 情况下完全不生效
- 替代方案：去掉 reverseLayout + 反转数据列表（当前有效修改第 11 条）

### 4. onAccessibilityEvent（API 编译不过，v42 再次确认）

**涉及文件：** A11yFocus.kt（common/android/desktop）、VideoScaffold.kt

- 尝试：监听无障碍焦点事件，实现播控栏在无障碍焦点期间不自动收起
- 结果：当前 Compose 版本（1.11.1，BOM 2026.04.01）的 onAccessibilityEvent API 签名不稳定，编译报错 Unresolved reference
- v42 再次尝试：仍然编译不过，Android 端暂时改为空实现（返回 this）
- 当前状态：A11yFocus expect/actual 文件保留，VideoScaffold 里的焦点计数器保留，但功能未生效
- 后续方案：需在 Android View 层用 AccessibilityManager 实现，或给每个可交互元素单独加监听

## 焦点丢失问题排查结论（重要）

经过 v6-v14 多轮构建验证，确认以下两个修改独立均会导致全局焦点丢失：
1. **heading 语义**（任何页面加了 heading() 都会导致全局焦点丢失）
2. **抽屉菜单 paneTitle 加在动画外层 Box**

以下修改**不会**导致焦点丢失：
- 全局页面 paneTitle（MyNavHost 外层 Box）
- 顶部按钮选中态语义
- traversalIndex（虽然不生效但不导致焦点丢失）
- 视频播放页修改
- 抽屉菜单 paneTitle 加在内容层 Box

## 后续合并作者更新的流程

1. 备份当前修改：`git diff > a11y-patches.patch`
2. 拉取最新代码：`git pull origin master`（或 main）
3. 尝试应用 patch：`git apply a11y-patches.patch`
4. 如果冲突，手动解决冲突，重点关注上述修改文件
5. 重新构建：按上面的构建命令
6. 测试无障碍修复是否仍然有效
7. 特别注意：不要重新加 heading 语义和抽屉动画外层 paneTitle

## 待修复的无障碍问题（已知）

- 评论区焦点合并后链接点击验证：用 clearAndSetSemantics + text(AnnotatedString带LinkAnnotation) 的方式，TalkBack 能否识别链接并通过链接菜单打开，需实测验证
- 楼中楼（ReplyDetailContent）里的回复项也走同一个 ReplyItemBox，需确认 onReplyClick 回调在楼中楼里是打开回复对话框而非展开楼中楼
- ~~视频播控栏无障碍焦点期间不自动收起~~（v43 已修复，Android View 层方案）
- 评论区底部"按热度"/"发表评论"按钮多余"未选中"语义（暂时搁置）
- 播放列表维度的"下一个"未对接，目前 playNext() 只处理分P视频
- release 版构建因系统内存不足 OOM，未成功（做了内存优化后可能可以，未再尝试）
- ABI 过滤器临时缩减为仅 arm64-v8a，是否恢复全 ABI 未确认
- 抽屉菜单打开时 TalkBack 不知道抽屉打开了（paneTitle 加在内容层但可能不够明显），需研究更好的方案
- 搜索遮罩点击关闭的无障碍标签已加，需验证是否生效

## v44: 从 2.4.8.1 稳定版覆盖无障碍优化点（2026-09-15）

对比 2.4.8.1 稳定版分支上的无障碍优化，找出 2.5 Alpha 上缺失的优化点并补充。

### 已覆盖的优化点

1. **首页 Tab 重复语义修复**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/home/HomePage.kt`
   - 修改：给 Tab 组件加 `clearAndSetSemantics { contentDescription = tab.name; selected = ... }`
   - 原因：Material3 Tab 组件默认会同时读文字和选中态，导致重复语义

2. **主题设置页 SegmentedButton 重复语义修复**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/setting/ThemeSettingPage.kt`
   - 修改：给深色模式和应用操作栏风格的两个 SegmentedButton 加 `clearAndSetSemantics`
   - 原因：SegmentedButton 默认会同时读文字和选中态，导致重复语义

3. **抽屉菜单 paneTitle**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/StartViewContent.kt`
   - 修改：给抽屉菜单的 LazyColumn 加 `.semantics { paneTitle = "菜单" }`
   - 原因：视障用户打开抽屉菜单时，TalkBack 不知道这是菜单页面

4. **视频详情页底部操作按钮选中态语义**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/video/components/VideoStatBox.kt`
   - 修改：给 VideoStatButton 加 `selected` 参数，Icon 的 contentDescription 动态变为"已点赞"/"已投币"/"已收藏"
   - 原因：已点赞/投币/收藏的按钮没有状态反馈，视障用户不知道是否操作成功

### 2.5 Alpha 已覆盖或实现更好的优化点

- 视频卡片语义简化（2.5 Alpha 用英文逗号+空格分隔，更简洁）
- 评论区焦点合并 + CustomAccessibilityAction（2.5 Alpha 实现完整）
- 下载页 FilterChip 重复语义修复
- 主题颜色按钮选中态
- 卡片式/列表式切换选中态
- 自动连播开关焦点合并
- 全局页面 paneTitle
- 对话框 paneTitle
- 图片预览
- 气泡通知无障碍播报（2.5 Alpha 用 GlobalToaster + AccessibilityEvent）
- UP 主头像标签
- 搜索遮罩标签
- 视频详情页标题修复（不读 AV/BV 号）
- 视频详情页统计文字焦点合并（2.5 Alpha 合并为一个焦点，更好）
- Tab 栏 TalkBack 误触发滚动修复
- 播控栏无障碍焦点保持（2.5 Alpha 用 PlayerA11yFocusContainer + PlayerControlsVisibilityPolicy）
- 音频焦点 + TalkBack 音频闪避
- 耳机线控 + 完整媒体通知
- 表情面板无障碍
- 分P按钮标签（2.5 Alpha 用中文"展开分P列表"，比 2.4.8.1 的"More"更好）
- 播放/暂停按钮动态标签
- 视频画面区域动态标签（展开/收起播控栏）
- 全屏按钮动态标签

### 注意事项

- 2.4.8.1 用旧版 GSYVideoPlayer（View 体系），2.5 Alpha 用新版 Compose 播放器，播放器相关无障碍优化需分别适配，不能直接复制代码
- 2.4.8.1 用 A11yPopTip 包装 PopTip，2.5 Alpha 用 GlobalToaster expect/actual + AccessibilityEvent，实现方式不同但功能等价
- 2.4.8.1 用单源集 src/main/java，2.5 Alpha 用 KMP src/commonMain/kotlin + src/androidMain/kotlin，文件路径不同

## v45: 从 fork/a11y-2.4.8.1 合入消息中心无障碍优化（2026-09-15）

从用户 GitHub 仓库的 `fork/a11y-2.4.8.1` 分支合入消息中心页面的无障碍优化。

### 优化点

1. **消息页整条消息合并为一个焦点并补操作菜单**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/message/components/MessageItemBox.kt`
   - 修改：给 Row 加 `clearAndSetSemantics`，整条消息（头像、昵称、动作、评论内容、右侧被评论内容、时间）合并为一个焦点
   - 新增参数：`detailActionLabel`（右侧内容操作名称）、`messageActionLabel`（评论内容操作名称）、`spokenActionText`（播报时用的动作词）
   - 自定义操作菜单：查看用户主页、查看原评论/内容来源、查看详情/展开楼层
   - 播报顺序：昵称、对方内容、动作、被回复内容、时间（动作放在两段内容中间，区分哪段是对方说的、哪段是自己被回复的）

2. **回复消息播报优化**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/message/content/ReplyMessageContent.kt`
   - 修改：传入 `detailActionLabel = "展开楼层"`、`messageActionLabel = "查看被回复的评论"`、`spokenActionText = "回复"`
   - 原因：动作词只说"回复"，后面直接接自己那条评论的内容，更简洁

3. **@我的消息操作菜单**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/message/content/AtMessageContent.kt`
   - 修改：传入 `detailActionLabel = "查看详情"`、`messageActionLabel = "查看内容来源"`

4. **点赞消息操作菜单**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/message/content/LikeMessageContent.kt`
   - 修改：传入 `detailActionLabel = "查看详情"`、`messageActionLabel = null`（点赞消息没有可点开的评论内容）

5. **消息中心顶部 Tab 重复语义修复**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/message/MessagePage.kt`
   - 修改：给 Tab 组件加 `clearAndSetSemantics { contentDescription = ...; selected = ... }`
   - contentDescription 包含未读消息数，如"回复我的，3 条未读"
   - 原因：Material3 Tab 组件默认会同时读文字和选中态，导致重复语义

### 来源提交

- `5adb38fd fix(a11y): 消息页整条消息合并为一个焦点并补操作菜单`
- `18675efc fix(a11y): 回复消息播报改为"昵称、对方内容、回复、自己内容、时间"，动作词只说回复`

---

## v46：从 2.4.8.1 稳定版合入无障碍优化点（第二轮）

### 优化内容

1. **追番更多按钮补无障碍标签**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/components/bangumi/BangumiItemBox.kt`
   - 修改：更多按钮 contentDescription 从 null 改为"更多"，DropdownMenu 加 paneTitle="更多"

2. **追番剧集更多按钮下拉菜单加窗口标题**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/bangumi/components/BangumiEpisodeItem.kt`
   - 修改：DropdownMenu 加 paneTitle="更多"（按钮本身已有"更多"标签）

3. **自动连播开关合并为一个焦点（3处）**
   - 文件：
     - `bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/mine/WatchLaterPage.kt`
     - `bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/user/content/UserFavouriteDetailContent.kt`
     - `bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/user/content/UserMedialistDetailContent.kt`
   - 修改：把"自动连播"文字和 Switch 包在一个 Row 里，加 clearAndSetSemantics
   - contentDescription = "自动连播"，role = Role.Switch，toggleableState 表达开关状态
   - Switch 的 onCheckedChange 设为 null，点击行为由整个 Row 的 clickable 处理
   - 原因：原来文字和开关被拆成两个无障碍焦点，体验割裂

4. **历史日历日期合并为一个焦点**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/pages/mine/HistoryPage.kt`
   - 修改：CalendarRowView 里整个日期 Column 加 clearAndSetSemantics
   - contentDescription = "X月X日 周X"（今天会带上"，今天"）
   - 去掉了内部日期数字 Box 的 clickable，避免两个可点击焦点
   - 原因：原来星期和日号被拆成两个焦点

5. **11处 Compose 下拉菜单补窗口标题（paneTitle）**
   - 文件及标题：
     - `BangumiItemBox.kt` - "更多"
     - `BangumiEpisodeItem.kt` - "更多"
     - `ReplyDetailContent.kt` - "列表排序"
     - `DownloadBangumiCreatePage.kt` - "选择画质"
     - `DownloadDetailItem.kt` - "更多"
     - `TagFollowContent.kt` - "关注操作"
     - `SelectProxyServerPage.kt` - "选择upos视频服务器"
     - `CustomTime.kt` - "选择时间"
     - `VideoDownloadDialog.kt` - "选择画质"
     - `VideoReplyTitleBar.kt` - "列表排序"
     - `InternationalDialingPrefixSelect.kt` - "选择国家区号"
   - 修改：给 DropdownMenu 加 `modifier = Modifier.semantics { paneTitle = "..." }`
   - 原因：下拉菜单弹出时 TalkBack 不知道这是一个新的窗口/面板

### 已确认 2.5 Alpha 已有、无需重复修改的点

- 评论详情页左上角关闭按钮：已有 contentDescription = "关闭"
- 顶栏返回/菜单按钮：NavigationIcon 组件已有 contentDescription（"返回"/"菜单"）
- 表情面板：已有 contentDescription = item.text
- 搜索卡片 kotlin.Unit：with(StringBuilder()) 最后有 .toString()
- 画中画播放/暂停：由 MediaSession 接管

### 来源提交（fork/a11y-2.4.8.1 分支）

- `1bb56341 fix(a11y): 追番更多按钮补标签、连播行合并焦点、历史日期合并`
- `364735c5 fix(a11y): 给剩余 11 处 Compose 下拉菜单补窗口标题`
- `e3461d48 fix(a11y): 顶栏左上角返回/菜单按钮补无障碍名称`
- `fc98e1b8 fix(a11y): 评论详情页左上角关闭按钮补无障碍名称`

---

## v47：从 2.4.8.1 合入动态卡片无障碍优化

### 优化内容

1. **动态卡片整张合并为一个焦点**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/components/dyanmic/DyanmicItemCard.kt`
   - 修改：在 MiaoCard 外面包一层 Box，加 clearAndSetSemantics
   - 整张卡片只保留一条播报文案和一组读屏操作
   - 播报顺序：昵称、发布时间、正文、（含 N 张图片 / 视频标题）、点赞、评论、转发

2. **动态卡片自定义操作菜单**
   - 查看用户主页（如果有作者 mid）
   - 查看图片（如果有图片）
   - 点赞 / 取消点赞（如果传入了 onLikeClick）
   - 评论（如果传入了 onCommentClick）
   - 双击卡片打开动态详情

3. **新增 DynamicItemSemantics.kt**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/components/dyanmic/DynamicItemSemantics.kt`
   - 定义 DynamicItemA11yInfo 数据类和 DynamicItem.toA11yInfo() 扩展函数
   - 从动态的各个 module 里抽取无障碍描述与操作需要的数据
   - 支持转发动态（递归抽取被转发内容的图片/视频标题）

4. **新增 ImageCountText.kt**
   - 文件：`bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/components/image/ImageCountText.kt`
   - imageCountText(count)：1 张读"含一张图片"，多张读"共 N 张图片"
   - imagePositionText(index, count)："图片第 N 张，共 M 张"
   - 评论区、动态卡片、大图预览共用同一套说法

### 来源提交（fork/a11y-2.4.8.1 分支）

- `6effed65 fix(a11y): 动态卡片多图读"共N张图片"，单图读"含一张图片"`
- `ecdb2909 fix(a11y): 动态卡片图片文案改回"含一张图片/含N张图片"`
- `8ae02c6a fix(a11y): 修正一批无障碍问题并补齐语义`（动态卡片整体合并焦点）
