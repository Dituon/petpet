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

一个生成摸摸头GIF的 Mirai 插件，灵感/数据来自 [nonebot-plugin-petpet](https://github.com/noneplugin/nonebot-plugin-petpet)。

java 编写，**未使用任何第三方库** ：轻量，高效。

## 使用方法

1. 下载 [最新版本](https://github.com/Dituon/petpet/releases/)

2. 将插件放入 `Mirai/plugins/`

3. 下载 [图片素材](https://github.com/Dituon/petpet/tree/main/data/xmmt.dituon.petpet)

4. 将图片素材放入 `Mirai/data/xmmt.dituon.petpet`

5. 使用 **戳一戳** 有 `30%` 的概率触发; 或发送 `pet @xxx`

> `pet @xxx` 后跟 `key` 可以返回指定图片 例如 `pet @xxx kiss`
>> 启用 `keyCommand` 后 上述指令可简写为 `kiss @xxx`

> 启用 `respondImage` 后 可通过发送的图片生成Petpet `pet [图片] kiss`

## 配置文件

首次运行 Petpet 插件时，会生成 `Mirai/config/xmmt.dituon.petpet/Petpet.yml` 文件

```
content: 
  version: 3.0 #配置文件版本
  command: pet #触发 petpet 的指令
  probability: 30 #使用 戳一戳 的触发概率
  antialias: true #抗锯齿
  disabled: [] #禁用列表
  keyCommand: false #以 key 作为指令头
  commandMustAt: true #必须有At对象
  respondImage: false #使用发送的图片生成 petpet
  respondSelfNudge: false #响应机器人发出的戳一戳
  headless: false #使用headless模式
```

修改后重启 Mirai 以重新加载

## 权限管理

> 群主或管理员使用 `pet on` `pet off` 以 启用/禁用 插件

> 可在配置文件中禁用指定key, 被禁用的key不会随机触发, 但仍可以通过指令使用

## 图片预览

**图片按key排序(见`data/xmmt.dituon.petpet/`)**

<details>
<summary>展开/收起</summary>

| key | 预览 |
| --- | --- |
| kiss | ![image](img/0.gif) |
| rub | ![image](img/1.gif) |
| throw | ![image](img/2.gif) |
| petpet | ![image](img/3.gif) |
| play | ![image](img/4.gif) |
| roll | ![image](img/5.gif) |
| bite | ![image](img/6.gif) |
| twist | ![image](img/7.gif) |
| pound | ![image](img/8.gif) |
| thump | ![image](img/9.gif) |
| knock | ![image](img/10.gif) |
| suck | ![image](img/11.gif) |
| hammer | ![image](img/12.gif) |
| tightly | ![image](img/13.gif) |

</details>

## 自定义

**[在线编辑器](https://dituon.github.io/petpet/editor)**

### data.json

`./data/xmmt.dituon.petpet/` 下的目录名为 `key` ，插件启动时会遍历 `./data/xmmt.dituon.petpet/$key/data.json`

`data.json` 标准如下 (以 `thump/data.json` 为例)

```
{
  "type": "GIF", // 图片类型(enum)
  "avatar": [{ //头像(objArr), 参考下文
      "type": "TO",
      "pos": [
        [65, 128, 77, 72], [67, 128, 73, 72], [54, 139, 94, 61], [57, 135, 86, 65]
      ],
      "round": true,
      "avatarOnTop": false
    }],
  "text": [] //文字(objArr), 参考下文
}
```

##### 图片类型枚举

**`type`**

- `GIF`  动图
- `IMG`  静态图片

#### 坐标

坐标的基本组成单位是 4长度 `int[]` 数组

其中，前两项为 **左上角顶点坐标**， 后两项为 **宽度和高度**

例: 
`[65, 128, 77, 72]` 即 头像的左上角顶点坐标是 `(65,128)`, 宽度为 `77`, 高度为 `72`

如果是 `GIF` 类型，坐标应为二维数组，`GIF` 的每一帧视为单个图像文件
```
"pos": [ // pos的元素对应GIF的4帧
    [65, 128, 77, 72], [67, 128, 73, 72], [54, 139, 94, 61], [57, 135, 86, 65]
  ],
```

如果是`IMG`类型, 可以使用一维数组
```
  "pos": [0, 0, 200, 200]
```

###### 仿射变换/图像变形

**坐标格式枚举`posType`**

- `ZOOM`  缩放(见上文)
- `DEFORM`  变形

`DEFORM` 坐标格式为 `[[x1,y1],[x2,y2],[x3,y3],[x4,y4]]`; 
分别对应图片的`[[左下角],[左上角],[右上角],[右下角]]`

目前仿射变换仅支持单帧

#### 头像

`3.0`版本后 提供了更灵活的头像构造方法, 与之前的版本有很大差别

```
"avatar": [
    {
      "type": "FROM", //头像类型枚举(enum), 非空
      "pos": [[92, 64, 40, 40], [135, 40, 40, 40], [84, 105, 40, 40]], // 坐标
      "round": true, // 值为true时, 头像裁切为圆形, 默认为false
      "avatarOnTop": true // 值为true时, 头像图层在背景之上, 默认为true
      "angle": 90, // 初始角度
    },
    {
      "type": "TO", 
      "pos": [[65, 128],[60,210],[110,210],[110, 120]],
      "posType": "DEFORM", //图像变形 坐标格式, 默认为ZOOM
      "antialias": true, // 抗锯齿, 对头像单独使用抗锯齿算法, 默认为false
      "rotate": false // 值为true时, GIF类型的头像会旋转, 默认为false
    }
  ]
```

##### 头像类型枚举

**`type`**

- `FROM`  发送者头像
- `TO`  接收者头像, 或构造的图片
- `GROUP`  群头像
- `BOT`  机器人头像

#### 文字

如果你想在图片上添加文字，可以编辑 `text`

```
"text": [ // 这是一个数组, 可以添加很多文字
  {
    "text": "Petpet!", // 文字内容
    "color": "#66ccff", // 颜色, 默认为#191919
    "pos": [100, 100], // 坐标, 默认为 [2,14]
    "size": 24 // 字号, 默认为12
  },
  {
    "text": "发送者: $from, 接收者: $to", // 支持变量
    "color": [0,0,0,255], // 颜色可以使用RGB或RGBA的格式
    "pos": [20, 150], // 坐标
    "font": "宋体" // 字体, 默认为黑体
  },
  {
    "text": "$txt1[我]超市$txt2[你]!", // 支持关键词变量
    "pos": [0,200],
    "font":  "./data/xmmt.dituon.petpet/key/微软雅黑.ttf" // 支持路径
  }
  ]
```

**`变量`**

- `$from` : 发送者, 会被替换为发送者群名片，如果没有群名片就替换为昵称
- `$to` : 接收者, 被戳或At的对象, 发送图片构造时为"你"
- `$group` : 群名称
- `$txt(i)[(xxx)]` : 文本变量, 可用于生成meme图, i为关键词索引, xxx为默认值; 例: `$txt1[我]超市$txt2[你]` 指令为 `pet [key] 我 你`

**需要更多变量请提交 Issue**

## 常见问题

> 戳一戳无法触发?
>> 检查 Mirai 登录协议, 仅 `ANDORID_PHONE` 可以收到 戳一戳 消息

> `NoClassDefFoundError`?
>> `Mirai 2.11.0` 提供了新的 `JavaAutoSaveConfig` 方法, 请更新Mirai版本至 `2.11.0` (不是`2.11.0-M1`), 或使用本插件 `2.0` 及以下版本

> `Exception in coroutine <unnamed>`?
>> 图片素材应位于 `Mirai/data/xmmt.dituon.petpet` 目录下, 请检查路径

> 文字构造乱码?
>> `Linux` 系统 可能缺少中文字体, 使用 `fc-list` 列出已安装的字体; `Windows` 系统 可能是文件编码问题, 更改 `data.json` 编码 或加入`-Dfile.encoding=utf-8` 启动项

> `Could not initialize class java.awt.Toolkit`?
>> 对于无输入输出设备的服务器 需要启用`headless`

## 分享你的作品

如果你想分享自定义的 Petpet, **欢迎Pr**

## 依赖share包二次开发

- 方式1. 在本项目内二次开发（非mirai插件形式）：见`xmmt.dituon.example.SimpleUsage`
- 方式2. 在别的项目二次开发：[mirai-simplepetpet-plugin](https://github.com/hundun000/mirai-simplepetpet-plugin)

## 后话

如果此插件和您预期的一样正常工作，请给我一个 `star`

欢迎提交任何请求

交流群: `534814022`
