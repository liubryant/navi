# Navi 项目说明文档

## 1. 项目概览

`navi` 是一个单模块 Android 应用，包名为 `cn.navibeidou.beidou`，当前配置版本为 `3.6.0`，`versionCode=37`。从代码和资源命名看，这是一个以“卫星导航地图”为主题的地图导航类应用，核心能力包括：

- 高德地图定位、地图展示、路线规划与导航
- 百度全景/街景展示
- POI 搜索、周边搜索、天气查询、地铁网页入口
- 开屏广告、Banner 广告、插屏/全屏视频广告
- 语音播报导航

项目根目录实际代码位于子目录 [navi](/D:/github/navi/navi) 下，Gradle 仅包含一个 `:app` 模块，见 [settings.gradle](/D:/github/navi/navi/settings.gradle:1)。

## 2. 技术栈与依赖

### 2.1 构建环境

- Gradle Plugin: `com.android.tools.build:gradle:7.0.3`
- `compileSdkVersion 33`
- `targetSdkVersion 33`
- `minSdkVersion 29`
- 模块类型: Android Application

相关配置见 [build.gradle](/D:/github/navi/navi/build.gradle:1) 和 [app/build.gradle](/D:/github/navi/navi/app/build.gradle:1)。

### 2.2 主要第三方 SDK

- 高德地图/导航/搜索/定位
    - `AMap3DMap_9.8.3_AMapNavi_9.8.3_AMapSearch_9.7.0_AMapLocation_6.4.2_20231215.jar`
- 百度地图全景
    - `BaiduLBS_Android.jar`
    - `IndoorscapeAlbumPlugin.jar`
- 穿山甲广告
    - `open_ad_sdk.aar`
- 友盟
    - `com.umeng.umsdk:common`
    - `com.umeng.umsdk:asms`
    - `com.umeng.umsdk:apm`
    - `com.umeng.umsdk:abtest`
- 微信开放平台
    - `com.tencent.mm.opensdk:wechat-sdk-android-without-mta:+`
- 网络与图片
    - `okhttp-3.4.1.jar`
    - `okio-1.9.0.jar`
    - `glide-3.6.0.jar`
- 语音
    - `Msc.jar`
    - `libmsc.so`

### 2.3 当前依赖层面的明显问题

- 项目同时使用了旧版 `com.android.support:*` 依赖和 `androidx.*` 包，属于过渡态工程。
- `appcompat-v7:33.0.2`、`design:33.0.2` 这类版本号并不符合传统 support library 发布方式，后续如果重新同步依赖，存在构建失败风险。
- 仓库中仍使用 `jcenter()` 和 `dl.bintray.com`，这些源已不稳定。
- 大量依赖以本地 `jar/aar` 形式存在，升级与替换成本较高。

## 3. 模块结构

项目是典型的“单 app 模块 + 平铺 Java 包”结构，没有按 feature 做现代拆分。

### 3.1 顶层目录

- `app/`: Android 应用主体
- `gradle/`: Gradle Wrapper
- `app/libs/`: 本地 SDK、SO、AAR、JAR
- `app/signfile/`: 签名文件
- `app/release/`: 已产出的 APK

### 3.2 代码包结构

主代码目录为 [app/src/main/java/cn/navibeidou/beidou](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou)。

主要子包如下：

- `cn.navibeidou.beidou`
    - 主 Activity、Fragment、Application、页面入口
- `cn.navibeidou.beidou.navi`
    - 高德导航相关页面、基类、TTS
- `cn.navibeidou.beidou.overlay`
    - 地图覆盖物辅助类
- `cn.navibeidou.beidou.indoor`
    - 百度室内全景相册相关逻辑
- `cn.navibeidou.beidou.okhttp`
    - 自带的 OkHttp 封装
- `cn.navibeidou.beidou.toutiao`
    - 穿山甲广告相关封装与 UI
- `cn.navibeidou.beidou.Util`
    - 常量、权限、Toast、SP、加解密、工具类
- `cn.navibeidou.beidou.widget`
    - 自定义 View 与对话框

### 3.3 规模概览

- Java 文件数: `114`
- `navi` 子目录导航相关 Java 文件数: `13`
- `layout` XML 数量: `57`

## 4. 启动流程

### 4.1 启动入口

清单中启动页为 [InitActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/InitActivity.java:1)，定义见 [AndroidManifest.xml](/D:/github/navi/navi/app/src/main/AndroidManifest.xml:43)。

启动链路如下：

1. `InitActivity`
    - 读取本地 `launch_ad` 状态
    - 调接口 `http://cjym123.cn/api/info`
    - 根据服务端开关和设备品牌决定是否关闭广告
    - 首次运行时可能弹出 `CommonStartDialog`
    - 初始化友盟与穿山甲
2. `SplashActivity`
    - 请求开屏广告
    - 超时、跳过或广告结束后进入主页面
3. `MapActivity`
    - 作为当前实际主页面
    - 初始化地图、定位、抽屉、Banner、插屏等

### 4.2 Application 初始化

[ApplicationShared.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/ApplicationShared.java:1) 是全局 `Application`：

- 提供单例入口
- 保存 `BMapManager`
- 暴露百度地图鉴权初始化逻辑

需要注意：

- `mContext` 定义了但没有赋值，`getContext()` 目前没有实际意义。
- `getInstance()` 在空时直接 `new ApplicationShared()`，这不是标准 Android Application 单例写法，只是恰好大多数路径下会先走系统创建的 `onCreate()`。

## 5. 核心业务模块

### 5.1 地图主页面

[MapActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/MapActivity.java:1) 是当前最重要的业务页面，职责很重，集成了：

- 高德地图 `MapView`
- 定位
- 地图模式切换
- 抽屉侧栏
- 周边、天气、地铁、全景入口
- 广告 Banner/插屏逻辑
- 隐私协议入口与反馈入口

其主要 UI 行为包括：

- 普通图、卫星图、公交图切换
- 路况开关
- 地图语言中英文切换
- 打开全景页 `QuanJingActivity`
- 跳转搜索/导航页 `IndexActivity`
- 跳转天气页、反馈页、协议页、游戏页

### 5.2 Fragment 版本地图页

[MapFragment.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/MapFragment.java:1) 与 `MapActivity` 存在大量职责重叠：

- 同样负责地图展示与定位
- 提供 POI 搜索、周边搜索、天气入口
- 生命周期中维护 `MapView`

这说明项目历史上存在两套主界面路线：

- 一套是 `MainHomeActivity + ViewPager + Fragment`
- 一套是当前更重的 `MapActivity`

从实际启动链看，生产路径已经偏向 `MapActivity`，而 `MainHomeActivity` 更像保留代码。

### 5.3 导航页

[IndexActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/IndexActivity.java:1) 主要承担路线计算与导航入口职责：

- 驾车
- 步行
- 骑行
- 货车
- 调起高德 `AmapNaviPage`
- 提供“附近街景”入口

此外 `navi` 包中的页面提供更细分的导航演示/路线页面，例如：

- `DriverListActivity`
- `WalkRouteCalculateActivity`
- `RideRouteCalculateActivity`
- `TruckRouteCalculateActivity`
- `RestRouteShowActivity`

[BaseActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/navi/BaseActivity.java:1) 是导航页面基类，统一管理：

- `AMapNavi`
- `AMapNaviView`
- 模拟导航速度
- 导航回调

### 5.4 语音播报

[TTSController.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/navi/TTSController.java:1) 封装了两种 TTS：

- 系统 TTS
- 讯飞 TTS

实现方式是一个简单的语音队列：

- 导航文本进入 `LinkedList`
- 通过 `Handler` 顺序播报
- 可在 `IFLYTTS` 和 `SYSTEMTTS` 间切换

### 5.5 百度全景

[QuanJingActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/QuanJingActivity.java:1) 负责百度全景：

- 将高德/GCJ02 坐标转换给百度全景
- 展示街景/室内全景
- 支持 marker、箭头、自定义相册、全景参数调整

该页面演示性质较强，保留了大量测试常量、测试按钮和样例全景 ID。

### 5.6 搜索与辅助页面

主要页面如下：

- `PoiKeywordSearchActivity`: 关键词搜索
- `PoiAroundSearchActivity`: 周边搜索
- `WeatherSearchActivity`: 天气查询
- `JsActivity`: 地铁/网页桥接入口
- `WebActivity`: 反馈页/静态网页页
- `ProtocolActivity`: 用户协议/隐私政策
- `GameActivity`: 游戏页或广告跳转页

## 6. 广告系统

广告逻辑散落在多个页面中，主要基于穿山甲 SDK。

### 6.1 开屏广告

[SplashActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/SplashActivity.java:1)

- 通过 `TTAdManagerHolder` 创建 `TTAdNative`
- 请求 `TTSplashAd`
- 3 秒超时
- 广告关闭后进入 `MapActivity`

### 6.2 Banner/信息流广告

`MapActivity` 和 `IndexActivity` 中均有模板广告加载逻辑：

- `loadBannerExpressAd`
- `loadNativeExpressAd`
- 自定义 dislike 对话框

### 6.3 全屏视频广告

`MapActivity` 中通过定时器循环加载全屏视频广告，并在特定时机调用展示。

### 6.4 服务端广告开关

[InitActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/InitActivity.java:1) 会请求：

- `UrlUtil.URLPERMISSIONADOPEN = http://cjym123.cn/api/info`

根据返回：

- `isPermissionReceiveAd`
- `versionCode`
- `adCloseType`

决定是否关闭广告。

### 6.5 广告常量

[Constants.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/Util/Constants.java:1) 中维护广告位：

- `APPID`
- `OPEN_ID`
- `INTERACTION_ID`
- `BANNER_ID`
- `STREAM_ID`

## 7. 网络与配置

### 7.1 网络实现

项目自带一套 OkHttp 封装，核心入口在：

- [OkHttpUtils.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/okhttp/OkHttpUtils.java:1)

常见调用方式如：

- `OkHttpUtils.get().url(...).build().execute(...)`

### 7.2 远端地址

[UrlUtil.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/Util/UrlUtil.java:1) 当前只看到少量硬编码接口：

- `http://cjym123.cn/api/info`
- 若干广告位 ID

这说明：

- 业务接口高度硬编码
- 没有明显环境切换机制
- 线上地址与广告位配置大概率直接写死在代码里

## 8. 权限与隐私

### 8.1 Manifest 权限

[AndroidManifest.xml](/D:/github/navi/navi/app/src/main/AndroidManifest.xml:1) 中主要声明了：

- `ACCESS_COARSE_LOCATION`
- `ACCESS_FINE_LOCATION`
- `ACCESS_NETWORK_STATE`
- `ACCESS_WIFI_STATE`
- `CHANGE_WIFI_STATE`
- `INTERNET`
- `WAKE_LOCK`
- `RECEIVE_BOOT_COMPLETED`

### 8.2 运行时权限

[CheckPermissionsActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/Util/CheckPermissionsActivity.java:1) 目前只做了定位权限申请：

- 粗定位
- 精确定位

特点：

- 有“权限用途说明”对话框
- 被拒绝后不会强制退出
- 可引导去系统设置页

### 8.3 隐私协议

`strings.xml` 中内置了较长的用户协议与隐私政策文本，`ProtocolActivity` 负责展示。

注意：

- 当前资源文件在终端查看时存在明显乱码，说明项目文件编码可能不是 UTF-8，或历史上经过不同编码工具编辑。

## 9. 构建、签名与发布

### 9.1 签名

签名配置写在 [app/build.gradle](/D:/github/navi/navi/app/build.gradle:1)：

- keystore: `app/signfile/findcar.jks`
- alias: `liuzheng`

当前仓库中直接保存了：

- 签名文件
- 明文签名密码

这是高风险配置，不应继续保留在公开或多人共享仓库中。

### 9.2 ABI

当前 `defaultConfig.ndk.abiFilters` 仅启用了：

- `arm64-v8a`

但 `libs/` 中仍保留多架构 `so`，说明仓库资源多于实际打包目标。

### 9.3 输出 APK 规则

APK 文件名通过 `applicationVariants` 自定义，格式如下：

```text
app_<applicationId>_<buildType>_v<versionName>-<yyyyMMddHH>.apk
```

仓库中已存在发布产物：

- [app_cn.navibeidou.beidou_release_v3.6.0-2026040115.apk](/D:/github/navi/navi/app/release/app_cn.navibeidou.beidou_release_v3.6.0-2026040115.apk)

## 10. 资源与页面

从资源规模看，这个项目是典型的“功能堆叠型旧 Android 应用”：

- 页面资源较多
- 命名不统一
- 既有正式业务页，也有 demo/样例页

页面大致可分为：

- 启动与广告
    - `activity_init.xml`
    - `activity_splash.xml`
- 地图与导航
    - `fragment_map.xml`
    - `activity_index.xml`
    - `activity_navi.xml`
    - `activity_basic_navi.xml`
- 搜索与天气
    - `poikeywordsearch_activity.xml`
    - `poiaroundsearch_activity.xml`
    - `weather_activity.xml`
- 协议与 Web
    - `activity_protocol.xml`
    - `activity_web.xml`
- 百度全景
    - `panodemo_main.xml`
    - `baidupano_*`

## 11. 当前代码状态判断

从整体代码特征判断，这个项目具备以下特点：

- 历史较久，持续叠加式维护
- 地图、导航、全景、广告、协议、网页入口都放在同一应用中
- `MapActivity` 和 `MapFragment` 存在重复实现
- `IndexActivity`、`MapActivity`、`SplashActivity` 都混入较多广告逻辑
- 页面与 SDK 演示代码混合，业务边界不清晰

它更像“以导航为主的商业化地图壳应用”，而不是职责清晰的现代 Android 项目。

## 12. 风险清单

### 12.1 安全风险

- 仓库包含签名文件与明文密码
- Manifest 中直接写入百度与高德 Key
- 广告位、接口地址硬编码

### 12.2 工程风险

- Support Library 与 AndroidX 混用
- 依赖仓库老旧
- 本地 jar/aar 较多，升级困难
- 大 Activity 过于臃肿，维护成本高

### 12.3 兼容性风险

- 目标 SDK 已到 33，但代码风格明显来自旧工程
- 权限模型、WebView、文件访问、广告 SDK、地图 SDK 在高版本系统上可能存在额外适配点
- 仅配置 `arm64-v8a`，但项目残留多 ABI 资源，容易让维护者误判实际构建行为

### 12.4 编码与可维护性风险

- 中文资源和注释存在乱码迹象
- 命名风格不统一
- 大量测试/demo 代码未隔离
- 注释与业务行为不总是同步

## 13. 建议的维护优先级

### 第一优先级

- 移除仓库中的签名文件和明文密码
- 抽离地图 Key、广告位 ID、接口地址到安全配置
- 明确 `MapActivity` 是否为唯一主流程，冻结或删除重复页面

### 第二优先级

- 梳理广告逻辑，集中到独立模块或管理类
- 梳理高德地图与百度全景的边界，减少页面耦合
- 修复资源/源码编码问题，统一为 UTF-8

### 第三优先级

- 逐步迁移到纯 AndroidX
- 替换过时仓库与本地依赖
- 按功能拆包，减少 `Activity` 体积

## 14. 建议阅读顺序

如果要快速接手这个项目，建议按下面顺序看代码：

1. [AndroidManifest.xml](/D:/github/navi/navi/app/src/main/AndroidManifest.xml:1)
2. [app/build.gradle](/D:/github/navi/navi/app/build.gradle:1)
3. [ApplicationShared.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/ApplicationShared.java:1)
4. [InitActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/InitActivity.java:1)
5. [SplashActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/SplashActivity.java:1)
6. [MapActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/MapActivity.java:1)
7. [IndexActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/IndexActivity.java:1)
8. [MapFragment.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/MapFragment.java:1)
9. [QuanJingActivity.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/QuanJingActivity.java:1)
10. [TTSController.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/navi/TTSController.java:1)
11. [UrlUtil.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/Util/UrlUtil.java:1) 与 [Constants.java](/D:/github/navi/navi/app/src/main/java/cn/navibeidou/beidou/Util/Constants.java:1)

## 15. 文档结论

这个项目已经具备可运行的地图导航商业应用形态，但工程层面偏老、耦合偏重、配置外露明显。对于后续维护，最重要的不是继续往现有大页面里加功能，而是先做最基本的工程收敛：

- 收口配置
- 去除敏感信息
- 明确主流程
- 分离广告与地图业务

否则每次改动都会同时碰到地图、广告、定位、权限和页面逻辑，回归成本会越来越高。
