# Petpet

![Mirai version](https://img.shields.io/badge/Mirai-2.11.0-ff69b4)
![GitHub](https://img.shields.io/github/license/dituon/petpet)
![GitHub all releases](https://img.shields.io/github/downloads/dituon/petpet/total)
![GitHub Repo stars](https://img.shields.io/github/stars/dituon/petpet)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/dituon/petpet)
![GitHub issues](https://img.shields.io/github/issues/dituon/petpet)
![GitHub closed issues](https://img.shields.io/github/issues-closed/dituon/petpet)
![GitHub closed pull requests](https://img.shields.io/github/issues-pr-closed/dituon/petpet)
[![](https://jitpack.io/v/Dituon/petpet.svg)](https://jitpack.io/#Dituon/petpet)

自定义合成图片的 Mirai 插件 / 独立程序 / gocq-http插件, 灵感/部分数据来自 [nonebot-plugin-petpet](https://github.com/noneplugin/nonebot-plugin-petpet)。

原生 java 编写, kotlin仅用于数据序列化, **使用底层API**, **多线程优化**: 轻量, 高性能, 易拓展

- **[在线编辑器](https://dituon.github.io/petpet-js/editor)**

- **[JS 前端版本](https://github.com/Dituon/petpet-js)**

- **[在线体验](https://dituon.github.io/petpet-js)**

## 使用方法

### 单独运行

1. 下载 [最新版本](https://github.com/Dituon/petpet/releases/) `petpet.jar` 或 `petpet-no-ws.jar`

2. 下载 [图片素材](https://github.com/Dituon/petpet/tree/main/data/xmmt.dituon.petpet)

3. 将图片素材放入 `./data/xmmt.dituon.petpet/` 目录

4. 运行 `start.bat` 或 `start.sh`, 可自行更改配置文件 `config.json`, 重启后生效

5. 参考[`WebServer`](#WebServer)一节 发起网络请求 / 或使用[`WebUI`](#WebUI)

### [Mirai](https://github.com/mamoe/mirai)插件

0. 部署 [Mirai](https://github.com/mamoe/mirai) 机器人框架

1. 下载 [最新版本](https://github.com/Dituon/petpet/releases/)

2. 将插件放入 `Mirai/plugins/`

3. 下载 [图片素材](https://github.com/Dituon/petpet/tree/main/data/xmmt.dituon.petpet)

4. 将图片素材放入 `Mirai/data/xmmt.dituon.petpet/`

5. 启动 `Mirai`, 可自行更改配置文件 `Petpet.yml`, 重启后生效 (参考 [配置项说明](#配置项说明))

- 使用 **戳一戳** 有 `30%` 的概率触发; 或发送 `pet @xxx`

> `pet key @xxx` 或 `key @xxx` 可返回指定图片 例如 `pet kiss @xxx` `kiss @xxx`

> 可通过发送的图片生成Petpet `kiss [图片]`, **支持GIF**
>> 可通过回复构造图片, 例如 `[图片]` -> `[回复[图片]] 对称`

> 可使用 `pet`指令 获取 `keyList`

### [gocq-http](https://github.com/Mrs4s/go-cqhttp)插件

> **Warning**
>
> 此功能处于测试阶段, 目前仅能通过`key`生成图片, 请期待后续开发!

0. 部署 [gocq-http](https://github.com/Mrs4s/go-cqhttp) 机器人框架, 设置**正向 WebSocket** 监听 (默认端口为`8080`)

1. 更改 `gocq-http` 配置项 `message.post-format` 为 `array`

2. 下载 [最新版本](https://github.com/Dituon/petpet/releases/) `petpet.jar`

3. 下载 [图片素材](https://github.com/Dituon/petpet/tree/main/data/xmmt.dituon.petpet)

4. 将图片素材放入 `./data/xmmt.dituon.petpet/` 目录

5. `cd ./` `java -jar petpet.jar -gocq`, 可自行更改配置文件 `gocq-config.json`, 重启后生效

## 配置文件
#### 配置项说明

<details>

<summary>展开/收起</summary>
<br/>

- **command**: `pet`

> 触发petpet指令, 默认为`pet`
> 
> 例: `pet @xxx` `pet kiss @xxx`
> 
> 仅发送`pet`时会返回`keyList`
<br/>

- **probability**: `30`

> 戳一戳 触发概率, `0-100`整数, 默认为 `30%`
<br/>

- **antialias**: `true`

> 画布抗锯齿, 默认为`true`
<br/>

- **resampling**: `true`

> 重采样缩放, 启用后头像质量更高, 可对模板单独配置
<br/>

- **disabled**: `[]`

> 禁用表列, 默认为空, 在此数组中的`key`不会被随机触发 (会覆盖`data.json`中的配置)
<br/>

- **keyCommandHead**: `''`

> `key`作为指令头时的前缀, 默认为空
> 
> 例 (配置项为`'#'`时): `#kiss @xxx` `osu hso!`
<br/>

- **respondReply**: `true`

> 响应回复的消息, 默认为`true`
> 
> 可通过回复消息 定位到之前发送的图片并构造petpet
> 
> 启用后 会缓存接收到的图片(见`cachePoolSize`)
> 
> 例 : `[回复[图片]]kiss`(等价于 `kiss [图片]`)
<br/>

- **cachePoolSize**: `10000`

> `respondReply=true`时, 图片消息缓存池大小, 默认为`10000`
> 
> 本质为`HashMap<imageId(long), imageUrl(String)>`, 超过此限制会清空Map
<br/>

- **respondSelfNudge**: `false`

> 某些情况下, 机器人会主动戳其他成员, 响应机器人自己发出的戳一戳, 默认为`false`
<br/>

- **keyListFormat**: `FORWARD`

> 发送`pet`时 `keyList`响应格式, 默认为`FORWARD`
>
> 枚举: `MESSAGE`(发送普通消息)  `FORWARD`(发送转发消息)  `IMAGE`(发送图片)
<br/>

- **disablePolicy**: `FULL`

> 发送`pet on/off`时 禁用哪些功能, 默认为`FULL`
> 
> 枚举: `NONE`(无效)  `NUDGE`(只禁用戳一戳)  `MESSAGE`(只禁用指令)  `FULL`(同时禁用戳一戳和指令)

- **fuzzy**: `false`

> 模糊匹配用户名, 默认为`false`
> 
> 例 : (配置项为`true`时): `kiss @田所浩二`(响应) `kiss 浩二`(响应)
<br/>

- **strictCommand**: `true`

> 严格匹配指令, 默认为`true`
>
> ~~人话: 可以省略key后的空格~~
>
> 例 : (配置项为`false`时): `kiss 田所`(响应) `kiss田所`(响应)
<br/>

- **synchronized**: `false`

> 消息事件同步锁, 会锁住相同的消息事件, 默认为`false`
> 
> ~~人话: 多机器人对于同一条指令只有一个会响应~~
<br/>
 
- **gifEncoder**: `ANIMATED_LIB`

> GIF编码器, 默认为`ANIMATED_LIB`
> 
> 枚举: 
> **`BUFFERED_STREAM`**:
> 基于缓存的`STREAM`流, 在编码过程中对Gif进行压缩;
> 
> - 编码速度较慢, 所需堆内存小, 生成Gif体积小
>
> **`ANIMATED_LIB`**:
> 基于`byte[]`序列, 使用多线程分析像素;
> 
> - 编码速度极快, 所需堆内存较多, 生成Gif体积较小

<br/>

- **gifMaxSize**: `[]`
> GIF缩放阈值/尺寸, 默认为空 (不限制)
> 
> `[width, height, frameLength]`:
> 
> 当Gif长度超过`frameLength`时, 会对Gif进行等比例缩放
> 
> 注: 缩放在图片合成时进行, 不会影响性能
> 
> 例: (配置项为`[200, 200, 32]`时) 
> - 当Gif长度超过`32`帧时, 检查Gif尺寸
> - 当Gif尺寸大于`200*200`时, 对Gif进行等比例缩放
> - Gif缩放后 最长边不会超过设定值
> (当Gif中包含`40`帧, 尺寸为`300*500`时)
> - 输出的Gif长度不变, 尺寸为`120*200`

- **gifQuality**: `5`

> Gif编码质量(`1`-`49`), 默认为`5`
> 
> 数字越小, 速度越慢, 质量越好 (大于`20`时, 速度不会有明显提升)
> 
> 仅适用于`ANIMATED_LIB`编码器

- **headless**: `true`

> 启用`headless`模式, 默认为`true`
> 
> ~~人话: 有些服务器没有输入输出设备, 画图库无法正常运行, 启用这个配置项可以修复, 因为总是有人不看常见问题, 干脆默认启用了(~~
<br/>

- **autoUpdate**: `true`

> 自动更新`PetData`, 每次启动时都会检查并自动下载船新pet, 默认为`true`
> 
> 注: 仅更新`PetData`, 不会更新插件版本, 请放心食用
> 
> ~~人话: 每次启动都会自动下载新的超赞梗图, 墙裂推荐~~
<br/>

- **repositoryUrl**: `'https://dituon.github.io/petpet'`

> 仓库地址, 用于自动更新, 默认为此仓库的`github page`

- **devMode**: `false`

> 开发模式, 启用后**任何人都能使用`pet reload`指令热重载`PetData`**, 默认为`false`
<br/>

- **messageHook**: `false`

> 消息注入, 参考[MessageHook](#MessageHook), 默认为`false`
<br/>

- **coolDown**: `1000`

> 成功触发指令后对该用户的冷却时间(单位为毫秒), 默认为 `1000`
>
> 设置为 `-1` 可禁用冷却
<br/>

- **groupCoolDown**: `-1`

> 成功触发指令后对该群聊的冷却时间, 默认为 `-1`
<br/>

- **inCoolDownMessage**: `技能冷却中...`

> 在冷却时间中触发命令的回复消息
> 
> 配置项为`[nudge]`时, 会以戳一戳形式回复
<br/>

</details>

修改后重启 Mirai 以重新加载

## 权限管理

> 群主或管理员使用 `pet on` `pet off` 以 启用/禁用 戳一戳

> **`pet on/off`指令控制的事件可在配置文件中更改**

> 可在配置文件中禁用指定key, 被禁用的key不会随机触发, 但仍可以通过指令使用

## 图片预览

**[在线尝试](https://dituon.github.io/petpet-js)**

## 自定义

**[在线编辑器](https://dituon.github.io/petpet-js/editor)**

### data.json

`./data/xmmt.dituon.petpet/` 下的目录名为 `key` ，插件启动时会遍历 `./data/xmmt.dituon.petpet/$key/data.json`

`data.json` 是模板配置文件, 程序解析此文件以生成图像

```json
{
  "type": "GIF",
  "avatar": [],
  "text": [],
  "delay": 50,
  "alias": [ "别名1", "别名2" ]
}
```

| **属性**           | **类型**       | **注释**              | **默认值** |
|------------------|--------------|---------------------|---------|
| **type**         | 模板类型枚举       | 图片类型枚举, `IMG`或`GIF` | 必须      |
| **avatar**       | `Avatar` 数组  | 头像配置数组, 见下文         | 必须      |
| **text**         | `Text` 数组    | 文本配置数组, 见下文         | 必须      |
| **inRandomList** | 布尔值          | 是否在随机列表中            | `false` |
| **reverse**      | 布尔值          | GIF是否倒放             | `false` |
| **delay**        | 整数           | 帧间延时 (毫秒)           | `65`    |
| **background**   | `Background` | 背景配置, 见下文           | `null`  |
| **alias**        | 字符串数组        | 别名数组                | `[]`    |
| **hidden**       | 布尔值          | 是否隐藏                | `false` |

##### 模板类型枚举

- `GIF`  动图, 程序会读取目录下所有`.png`格式的图像
- `IMG`  静态图片, 程序会读取目录下随机`.png`格式的图像

### 头像

程序支持复杂的图像处理, 包括裁切, 旋转, 透明度, 滤镜等

```json
{
  "avatar": [
    {
      "type": "FROM",
      "pos": [[92, 64, 40, 40], [135, 40, 40, 40], [84, 105, 40, 40]],
      "round": true,
      "rotate": false,
      "avatarOnTop": true,
      "angle": 90
    },
    {
      "type": "TO",
      "pos": [[5, 8], [60, 90], [50, 90], [50, 0], [60, 120]],
      "posType": "DEFORM",
      "opacity": 0.5
    }
  ]
}
```

| **属性**          | **类型**  | **注释**                      | **默认值**   |
|-----------------|---------|-----------------------------|-----------|
| **type**        | 头像类型枚举  | 见下文, 例如`FROM`或`TO`          | 必须        |
| **pos**         | 坐标数组    | 头像的坐标信息                     | 必须        |
| **posType**     | 坐标格式枚举  | 坐标格式枚举, `ZOOM`或`DEFORM`     | `ZOOM`    |
| **round**       | 布尔值     | 头像是否裁切为圆形                   | `false`   |
| **avatarOnTop** | 布尔值     | 头像图层是否在背景之上                 | `true`    |
| **angle**       | 整数      | 头像的初始角度                     | `0`       |
| **origin**      | 旋转原点枚举  | 头像的旋转原点                     | `DEFAULT` |
| **opacity**     | 浮点数     | 头像的不透明度                     | `1.0`     |
| **rotate**      | 布尔值     | GIF类型的头像是否旋转                | `false`   |
| **fit**         | 填充模式枚举  | 填充模式枚举, 可以是`CONTAIN`或`FILL` | `FILL`    |
| **crop**        | 裁切坐标数组  | 头像裁切坐标信息                    | `null`    |
| **cropType**    | 裁切格式枚举  | 见下文                         | `NONE`    |
| **style**       | 风格化枚举数组 | 风格化枚举数组, 见下文                | `[]`      |
| **filter**      | 滤镜对象数组  | 滤镜数组, 见下文                   | `[]`      |
| **antialias**   | 布尔值     | 是否使用抗锯齿算法, 默认跟随全局配置         | `null`    |
| **resampling**  | 布尔值     | 是否使用重采样缩放, 默认跟随全局配置         | `null`    |


**头像类型枚举 `type`**

- `FROM`  发送者头像
- `TO`  接收者头像, 或构造的图片
- `GROUP`  群头像
- `BOT`  机器人头像
- `RANDOM`  随机头像 (随机从群聊成员中选择, 不会重复)

#### 坐标

**坐标格式枚举`posType`**

- `ZOOM`  缩放, 通过 x, y, width, height 表示图像
- `DEFORM`  变形, 通过四点坐标来表示图像

###### ZOOM

`ZOOM` 缩放坐标的基本组成单位是 4长度 `int[]` 数组

其中，前两项为 **左上角顶点坐标**， 后两项为 **宽度和高度**

例:
`[65, 128, 77, 72]` 即 头像的左上角顶点坐标是 `(65,128)`, 宽度为 `77`, 高度为 `72`

如果是 `GIF` 类型，坐标应为二维数组，`GIF` 的每一帧视为单个图像文件

```json lines
 {
  // pos的元素对应GIF的4帧
  "pos": [[65, 128, 77, 72], [67, 128, 73, 72], [54, 139, 94, 61], [57, 135, 86, 65]]
}
```

如果是`IMG`类型, 可以使用一维数组

```json lines
{
  "pos": [0, 0, 200, 200]
}
```

坐标支持变量运算, 例如 `[100,100,"width/2","height*1.5^2"]`

**坐标变量**

- `width`  原图宽度
- `height`  原图高度

###### DEFORM

`DEFORM` 仿射变换坐标格式为 `[[x1,y1],[x2,y2],[x3,y3],[x4,y4],[x_anchor,y_anchor]]`;
分别对应图片的`[[左上角],[左下角],[右下角],[右上角],[锚点]]`，四角坐标用相对于锚点的偏移量表示

**旋转原点枚举 `origin`**

- `DEFAULT` 左上角
- `CENTER` 中心

#### 裁切

图片裁切坐标 `[x1, y1, x2, y2]`, `[0, 0, x2, y2]` 可简写为 `[x2, y2]`

**裁切格式枚举 `cropType`**

- `NONE`  不裁切
- `PIXEL`  按像素裁切
- `PERCENT`  按百分比裁切

**填充模式 `fit`**

- `CONTAIN` 缩小以适应画布, 不改变原比例
- `COVER` 裁切以适应画布, 不改变原比例
- `FILL` 拉伸, 改变原比例

**风格化枚举 `style`**

- `MIRROR`  水平镜像
- `FLIP`  上下翻转
- `GRAY`  灰度化
- `BINARIZATION`  二值化

#### 滤镜 `filter`

通过滤镜实现头像特效, Java 与 JavaScript 版本实现有偏差, 并非完全相同

```json
{
  "filter": [
    {
      "type": "SWIRL",
      "radius": 200,
      "angle": 5.0
    }
  ]
}
```

##### 滤镜类型 `type`

- `SWIRL` 对应 `AvatarSwirlFilter`
- `BULGE` 对应 `AvatarBulgeFilter`
- `BLUR` 对应 `AvatarBlurFilter`
- `CONTRAST` 对应 `AvatarContrastFilter`
- `HSB` 对应 `AvatarHSBFilter`
- `HALFTONE` 对应 `AvatarHalftoneFilter`
- `DOT_SCREEN` 对应 `AvatarDotScreenFilter`
- `NOISE` 对应 `AvatarNoiseFilter`
- `DENOISE` 对应 `AvatarDenoiseFilter`

##### 滤镜对象

- `AvatarSwirlFilter` 对象, 漩涡滤镜

| **属性**     | **类型** | **注释**           | **默认值** |
|------------|--------|------------------|---------|
| **radius** | 浮点数    | 涡旋半径, 值为0时表示图片半径 | `0.0`   |
| **angle**  | 浮点数    | 涡旋角度             | `3.0`   |
| **x**      | 浮点数    | 中心点X坐标百分比        | `0.5`   |
| **y**      | 浮点数    | 中心点Y坐标百分比        | `0.5`   |

- `AvatarBulgeFilter` 对象, 膨胀收缩滤镜

| **属性**       | **类型** | **注释**                    | **默认值** |
|--------------|--------|---------------------------|---------|
| **radius**   | 浮点数    | 膨胀半径, 值为0时表示图片半径          | `0.0`   |
| **strength** | 浮点数    | 膨胀强度 `[-1, 1]`, 负数时产生收缩效果 | `0.5`   |
| **x**        | 浮点数    | 中心点X坐标百分比                 | `0.5`   |
| **y**        | 浮点数    | 中心点Y坐标百分比                 | `0.5`   |

- `AvatarBlurFilter` 对象, 模糊滤镜

| **属性**     | **类型** | **注释** | **默认值** |
|------------|--------|--------|---------|
| **radius** | 浮点数    | 模糊半径   | `10.0`  |

- `AvatarContrastFilter` 对象, 亮度对比度滤镜

| **属性**         | **类型** | **注释** | **默认值** |
|----------------|--------|--------|---------|
| **brightness** | 浮点数    | 亮度     | `0.0`   |
| **contrast**   | 浮点数    | 对比度    | `0.0`   |

- `AvatarHSBFilter` 对象, 相对HSB (色相, 饱和度, 亮度) 滤镜

| **属性**         | **类型** | **注释** | **默认值** |
|----------------|--------|--------|---------|
| **hue**        | 浮点数    | 色相     | `0.0`   |
| **saturation** | 浮点数    | 饱和度    | `0.0`   |
| **brightness** | 浮点数    | 亮度     | `0.0`   |

- `AvatarHalftoneFilter` 对象, 半色调滤镜 (模仿彩色印刷的CMYK色彩)

| **属性**     | **类型** | **注释**    | **默认值** |
|------------|--------|-----------|---------|
| **angle**  | 浮点数    | 角度        | `0.0`   |
| **radius** | 浮点数    | 半径        | `4.0`   |
| **x**      | 浮点数    | 中心点X坐标百分比 | `0.5`   |
| **y**      | 浮点数    | 中心点Y坐标百分比 | `0.5`   |

- `AvatarDotScreenFilter` 对象, 单色点阵滤镜 (模仿黑白印刷品)

| **属性**     | **类型** | **注释**    | **默认值** |
|------------|--------|-----------|---------|
| **angle**  | 浮点数    | 角度        | `0.0`   |
| **radius** | 浮点数    | 半径        | `4.0`   |
| **x**      | 浮点数    | 中心点X坐标百分比 | `0.5`   |
| **y**      | 浮点数    | 中心点Y坐标百分比 | `0.5`   |

- `AvatarNoiseFilter` 对象, 噪声滤镜

| **属性**     | **类型** | **注释** | **默认值** |
|------------|--------|--------|---------|
| **amount** | 浮点数    | 噪声强度   | `0.25`  |

- `AvatarDenoiseFilter` 对象, 降噪滤镜

| **属性**       | **类型** | **注释** | **默认值** |
|--------------|--------|--------|---------|
| **exponent** | 短整数    | 指数     | `20`    |

### 文字

如果你想在图片上添加文字，可以编辑 `text`

```json lines
{
  "text": [
    {
      "text": "Petpet!", // 文字内容
      "color": "#66ccff", // 颜色, 默认为#191919
      "pos": [100, 100], // 坐标
      "size": 24 // 字号, 默认为12
    },
    {
      "text": "发送者: $from, 接收者: $to", // 支持变量
      "pos": [20, 150], // 坐标
      "position": ["CENTER", "BOTTOM"], //坐标计算基准([x, y])
      "font": "宋体", // 字体, 默认为黑体
      "strokeColor": "#ffffff", // 描边颜色
      "strokeSize": 2 // 描边宽度
    },
    {
      "text": "$txt1[我]超市$txt2[你]!", // 支持关键词变量
      "pos": [0,200,300], // 第三个值为文本最大宽度
      "align": "CENTER", // 对齐方式, 默认为LEFT
      "wrap": "ZOOM", // 显示设置, 默认为NONE
      "style": "BOLD" // 字体样式, 默认为PLAIN
    }
  ]
}
```

| **属性**          | **类型** | **注释**       | **默认值**         |
|-----------------|--------|--------------|-----------------|
| **text**        | 字符串    | 文本内容         | 必须              |
| **pos**         | 数组     | 文本的坐标信息      | 必须              |
| **color**       | 字符串    | 文本颜色         | `#191919`       |
| **size**        | 整数     | 文本字号         | `12`            |
| **angle**       | 整数     | 头像的初始角度      | `0`             |
| **origin**      | 旋转原点枚举 | 文字的旋转原点      | `DEFAULT`       |
| **position**    | 数组     | 文本坐标计算基准     | [`LEFT`, `TOP`] |
| **font**        | 字符串    | 字体           | `黑体`            |
| **strokeColor** | 字符串    | 文本描边颜色       | `null`          |
| **strokeSize**  | 整数     | 文本描边宽度       | `0`             |
| **align**       | 字符串    | 文本对齐方式       | `LEFT`          |
| **wrap**        | 字符串    | 文本显示设置       | `NONE`          |
| **style**       | 字符串    | 字体样式         | `PLAIN`         |
| **greedy**      | 布尔值    | 是否贪婪匹配多余的关键词 | `false`         |

**`变量`**

- `$from` : 发送者, 会被替换为发送者昵称
- `$to` : 接收者, 被戳或At的对象
- `$group` : 群名称
- `$txt(i)[(xxx)]` : 文本变量, 可用于生成meme图, i为关键词索引, xxx为默认值; 例: `$txt1[我]超市$txt2[你]` 指令为 `pet [key] 我 你`

**`font`**

在`data/fonts`目录下的字体文件会注册到环境中

**`align`**

- `LEFT`: 左对齐, 文本基线是标准的字母基线
- `RIGHT`: 右对齐, 文本基线是标准的字母基线
- `CENTER`: 居中对齐, 文本基线在文本块的中间

**`wrap`**

- `NONE`: 不换行
- `BREAK`: 自动换行, 超过最大宽度的文本会显示在下一行
- `ZOOM`: 自动缩放, 缩放字体大小以填充最大宽度

使用`BREAK`或`ZOOM`时, `maxWidth` 默认为 `200`

**`style`**

- `PLAIN`: 默认
- `BOLD`: 粗体
- `ITALIC`: 斜体
- `BOLD_ITALIC`: 粗体与斜体

**`position`**

- `LEFT`: 左定位(默认)
- `RIGHT`: 右定位
- `TOP`: 上定位(默认)
- `BOTTOM`: 下定位
- `CENTER`: 居中定位


#### `background`

程序支持动态创建画布

```json lines
{
  "background": {
    "size": ["avatar0Width*2","avatar0Height"], //支持变量运算
    "color": "#f0f0f0"
  }
}
```

**坐标变量**

- `avatar(i)Width`  `i`号头像(`i`为定义头像时的顺序, 从`0`开始)处理后的宽度
- `avatar(i)Height`  `i`号头像处理后的高度
- `text(i)Width`  `i`号文本渲染后的宽度
- `text(i)Height`  `i`号文本渲染后的高度

### MessageHook

本特性仅适用于 **Mirai 插件**, 消息注入, 插件会检查将要发送的消息 解析后注入图片, 可配合各类消息回复插件使用

`<pet></pet>` 标签中的`JSON`会被解析, 请求格式参考 [`WebServer.POST`](#post)

用例: 
```text
这段文字之后的标签会变成一张图片发送<pet>{
  "key": "petpet",
  "to": {
    "qq": 2544193782 
  },
  "textList": [
    "text1"
  ]
}</pet>消息的顺序会被正确处理, 支持多张图片
```

不同于 `POST` 请求格式, 你可以用 `"qq"` 令程序自动获取头像和昵称, 也可以自定义`"name"` `"avatar"`
(更推荐自定义的做法, 程序可能在某些情况下无法推断出正确的`"name"`)

> 被`"hidden": true`隐藏的模板会正常调用
> 
> 此功能默认禁用, 需在配置文件中启用`messageHook: true`

# WebServer
  
程序可作为**http服务器 / API**单独运行, 被其它项目/语言使用

> `java -jar petpet.jar`

启动时会生成 `config.json`:
```json lines
{
    "port": 2333, // 监听端口
    "webServerThreadPoolSize": 10, // HTTP服务器线程池容量
    "dataPath": "data/xmmt.dituon.petpet", // PetData路径
    "preview": false, // 启用动态预览 (启动时生成所有模板预览)
    "antialias": true, // 启用抗锯齿, 详见上文
    "resampling": true, // 启用重采样, 详见上文
    "gifMaxSize": [200, 200, 32], // GIF缩放阈值, 详见上文
    "gifEncoder": "ANIMATED_LIB", // GIF编码器, 详见上文
    "gifQuality": 5, // GIF质量, 详见上文
    "threadPoolSize": 0, // GIF编码器线程池容量, 详见上文
    "headless": true // 使用headless模式
}
```

**程序使用`com.sun.net.httpserver`实现`http服务器`**

### `PetServer API`

访问 `127.0.0.1:2333/petpet` 以获取 `PetDataList`

### `GET`

使用 `GET` 传递参数, 例如 `127.0.0.1:2333/petpet?key=petpet&toAvatar=$avatarUrl`
`127.0.0.1:2333/petpet?key=osu&textList=hso!`

**结构**
<details>
<summary>展开/收起</summary>

- `key` (str): 对应`PetData`,例如`kiss` `rub`
- `fromAvatar` `toAvatar` `groupAvatar` `botAvatar` (url): 头像URL地址, `encodeURIComponent(rawUrl)`
- `randomAvatarList` (url[]): 随机头像列表, 使用`,`分割多个url
- `fromName` `toName` `groupName` (str): 昵称, 有默认值
- `textList` (str): 根据空格分割此字符串, 作为额外数据
</details>

### `POST`

使用 `POST` 传递参数, 例如 `127.0.0.1:2333/petpet`
```json
{
    "key": "petpet",
    "to": {
        "name":"d2n",
        "avatar":"https://q1.qlogo.cn/g?b=qq&nk=2544193782&s=640"
    },
    "randomAvatarList": [
        "url"
    ],
    "textList": [
        "text"
    ]
}
```
其中, `key`为必须项, 其它可以省略

#### `form-data`

可直接将图片二进制文件上传至服务器进行处理

类似于 **`GET`数据结构**, 使用 `multipart/form-data`

> 可参考[`example-script`](./example-script/)中的代码实现请求

| 语言           | 示例                                                                                              |
|--------------|-------------------------------------------------------------------------------------------------|
| `javascript` | [`post.js`](./example-script/javascript/post.js) [`get.js`](./example-script/javascript/get.js) |
| `python`     | [`example.py`](./example-script/python/example.py)                                              |
| `php`        | [`example.php`](./example-script/php/example.php)                                               |

# WebUI

启动`WebServer`后即可使用`WebUI`

启用`preview`配置项以加载`WebUI`模板预览 (可选, 默认关闭)

- 修改 `server-config.json` `preview: true`

## 常见问题

- 戳一戳无法触发?
  > 检查 Mirai 登录协议, 仅 `ANDORID_PHONE` 可以收到 戳一戳 消息

- 没有生成配置文件?
  > `Mirai 2.11.0` 提供了新的 `JavaAutoSaveConfig` 方法, 请更新Mirai版本至 `2.11.0` (不是`2.11.0-M1`), 旧版本不支持自定义配置项

- `Could not initialize class java.awt.Toolkit`?
  > 对于无输入输出设备的服务器 需要启用`headless`

- 自动更新下载速度慢 / 无法连接远程资源?
  > 修改`Petpet.yml`中`repositoryUrl`的值为`'https://ghproxy.com/https://raw.githubusercontent.com/Dituon/petpet/main'`(高速镜像)

- 自动更新后 读取`data.json`出错?
  > 自动更新时网络出错导致, 删除出错的文件 重新获取即可

- 其它错误? 问题?
  > 若此文档无法解决您的问题, 欢迎提交`issue`

## 性能 & 兼容性

程序使用底层`java.awt`类合成图片, 渲染时使用多线程, 静态图片渲染时间一般不会超过`1ms`

对GIF编码器的分析, 转换, 映射部分进行多线程优化, 速度极快

**Android JVM**没有实现`java.awt`, 推荐使用`JDK 11+`版本

## 分享你的作品 (模板)

如果你想分享自定义的 Petpet, **欢迎Pr**

## 二次开发

程序提供超多实用API  拓展性极强, 附有互动式开发实例, 欢迎初学者学习!

- 互动式开发实例 参见[`test.moe.dituon.petpet.example.HelloPetpet`](https://github.com/Dituon/petpet/blob/main/src/test/java/moe/dituon/petpet/example/HelloPetpet.java)

- 在别的项目二次开发: [mirai-simplepetpet-plugin](https://github.com/hundun000/mirai-simplepetpet-plugin)

## 后话

如果此插件和您预期的一样正常工作，请给我一个 `star`

欢迎提交任何请求

交流群: `828350277`
