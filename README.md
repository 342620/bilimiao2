<p align="center">
<img width="100px" src="https://10miaomiao.cn/icons/bilimiao_new.png"/>
</p>

<div align="center">

# bilimiao（哔哩喵~）
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/10miaomiao/bilimiao2)](https://github.com/10miaomiao/bilimiao2/releases)  ![GitHub All Releases](https://img.shields.io/github/downloads/10miaomiao/bilimiao2/total) ![GitHub stars](https://img.shields.io/github/stars/10miaomiao/bilimiao2?style=flat) ![GitHub forks](https://img.shields.io/github/forks/10miaomiao/bilimiao2)

</div>

<div align="center">

[更新日志](CHANGELOG.md)
&bull;
[QQ频道交流](https://pd.qq.com/s/hn9hmg)

</div>

### 无障碍（a11y）改造说明

本仓库是 [bilimiao](https://github.com/10miaomiao/bilimiao2) 的无障碍改造版，目标是让这个 App 能被读屏软件（TalkBack）完整操作，视障用户也能正常使用；改动都沿用作者原有的结构和组件，不改动功能本身。

当前改造基于作者的最新稳定版 **2.4.8.1**。上游 **2.5.0-alpha** 目前仍在重构中，暂未适配。

已处理的主要无障碍问题：

* **焦点合并**：一行里的“文字 + 开关”合并成一个焦点，历史记录的日期、列表项的附属按钮等零散焦点也做了合并，状态只播报一次。
* **缺少名称的焦点**：列表右侧“更多”、弹窗关闭按钮等补上名称；抽屉周围那层只用于“点空白关闭”的遮罩不再被读成一个没有名字的按钮。
* **弹窗与菜单没有标题**：原生 PopupMenu 没有公开的标题接口，改为弹出后播报一次标题；Compose 的下拉菜单直接在菜单节点上挂标题（排序、清晰度、倍速、字幕、画质、区号等）。
* **选中态重复**：开关、单选按钮、分段按钮去掉多余的“已选中 / 未选中”，选中状态只由一条语义表达。
* **播放器**：画面区域补上点击动作，读屏双击可展开 / 收起播控栏；全屏按钮按当前方向播报“全屏 / 竖屏”。
* **图片与表情**：图片组按“含一张图片 / 共 N 张图片”播报，图片详情逐张播报“图片第 X 张，共 Y 张”；表情面板读出表情自身的文本（如 [doge]），不再是没名字的按钮。
* **页面标题**：进入设置等页面时不再读到兜底的应用名，进入瞬间即可读出页面标题。
* **卡片文案**：视频卡片固定按“标题、播放、弹幕、时长、UP 主”的顺序播报，并去掉“播放量 / 弹幕数”里的冗余字样。
* **剪贴板链接**：复制 B 站视频 / UP 主链接后自动识别，用底部面板以现有卡片形式展示解析结果。

这些改动主要围绕 `contentDescription`、`stateDescription`、`clearAndSetSemantics` 三者的取舍：能用可见文本表达含义时不再重复设置描述，需要覆盖子树语义时用 `clearAndSetSemantics` 清干净再补 `role`、`toggleableState`，纯装饰的元素则直接对读屏隐藏。

希望在重构新版本时，这些改动能给作者以及其它第三方客户端的开发者一些参考。新版本的框架可能与 2.4.8.1 不同，但上面这类无障碍问题大体是共通的，可以对照着做改造。

本仓库 Release 里就是基于 2.4.8.1 的无障碍版 APK。

### 关于本项目
bilimiao原为哔哩哔哩时光机，原始功能主要为查看各个分区在每个时间段的热门视频列表，具体用法详见 [bilimiao时光机使用方法](doc/时光机.md)，后经不断改进，功能不断完善，形成了一款安卓上的[哔哩哔哩](https://www.bilibili.com/)的第三方APP。

本项目使用安卓原生技术开发，后续UI将逐步改用Jetpack Compose。

### 下载及使用
#### 下载
1. 从[10miaomiao.cn](https://10miaomiao.cn/project/1)下载
2. 从[GithubRelease](https://github.com/10miaomiao/bilimiao2/releases)下载
3. 从[GiteeRelease](https://gitee.com/10miaomiao/bilimiao2/releases)下载

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on-zh-hans.png"
    alt="下载应用请到 F-Droid"
    height="80">](https://f-droid.org/packages/com.a10miaomiao.bilimiao)

#### 使用说明
1. [手表使用说明](doc/手表使用说明.md)
2. [时光机](doc/时光机.md)
3. [列表屏蔽关键字及up主屏蔽](doc/列表屏蔽关键字及up主屏蔽.md)
4. [区域限制-代理服务器设置](doc/区域限制-代理服务器设置.md)

### 感谢以下开源项目
* [bilibili-API-collect](https://github.com/SocialSisterYi/bilibili-API-collect)
* [BiliRoaming](https://github.com/yujincheng08/BiliRoaming)
* [Kodein-DI](https://github.com/Kodein-Framework/Kodein-DI)
* [Splitties](https://github.com/LouisCAD/Splitties)
* [okhttp](https://github.com/square/okhttp)
* [glide](https://github.com/bumptech/glide)
* [BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)
* [ModernAndroidPreferences](https://github.com/Maxr1998/ModernAndroidPreferences)
* [NumberPickerView](https://github.com/Carbs0126/NumberPickerView)
* [ShadowLayout](https://github.com/lihangleo2/ShadowLayout)
* [GSYVideoPlayer](https://github.com/CarGuo/GSYVideoPlayer)
* [DanmakuFlameMaster](https://github.com/bilibili/DanmakuFlameMaster)
* [mojito](https://github.com/mikaelzero/mojito)
* [DialogX](https://github.com/kongzue/DialogX)
* [scale](https://github.com/jvziyaoyao/scale)


### 关于我
* 个人网站 [10喵喵](https://10miaomiao.cn/)
* B站 [10喵喵](https://space.bilibili.com/6789810/)
