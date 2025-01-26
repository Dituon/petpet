# Petpet Bot

## 部署

#### Onebot

1. 部署支持 [`Onebot v11`](https://github.com/botuniverse/onebot-11) 标准的服务端, 例如:
    - [NapCat](https://napneko.github.io/guide/start-install) - 基于无头 NTQQ 的实现
    - [LLOneBot](https://llonebot.github.io/zh-CN/guide/getting-started) - 基于 LiteLoaderQQNT 的实现
    - [AstralGocq](https://github.com/ProtocolScience/AstralGocq) - 基于 Mirai & AndroidNT 协议的实现

2. 下载 [最新版本 `petpet-onebot.jar`](https://github.com/Dituon/petpet/releases)

3. 下载 [模板素材](https://github.com/Dituon/petpet-templates) 并放入 `./data/xmmt.dituon.petpet/` 目录

4. 启动 `petpet-onebot.jar` (`java -jar petpet-onebot.jar`)

5. 修改 `onebot.yml` 配置文件:
    - `url`: 正向 Onebot WS 实现端地址 (正向模式)
    - `reversedPort`: 反向 Onebot WS 端口 (反向模式)
    - `token`: 连接 Onebot 实现端的令牌 (默认为空)

6. 编辑其它配置项, 重启以加载新配置

> 如果需要缓存由其它客户端发出的消息, 请在协议端开启 **报告自身消息**

#### Mirai

1. 部署 [Mirai](https://github.com/mamoe/mirai) 或 [Overflow](https://mirai.mrxiaom.top/) 框架

2. 下载 [最新版本 `petpet.mirai2.jar`](https://github.com/Dituon/petpet/releases) 并将插件放入 `Mirai/plugins/` 目录

3. 下载 [模板素材](https://github.com/Dituon/petpet-templates) 并放入 `Mirai/data/xmmt.dituon.petpet/` 目录

4. 启动 Mirai, 可自行更改配置文件 `Petpet.yml`, 重启后生效

> 如果需要缓存由其它 客户端 / 插件 发出的消息, 请在协议端开启 **报告自身消息**

## 从旧版迁移

程序的数据完全兼容旧版本, 但可下载改进旧版模板效果的补丁文件:

- **应用模板补丁** (可选)

1. 下载 [`data-pached.zip`](https://github.com/Dituon/petpet-templates/releases/tag/1.0.0-beta1) 或 [`data-pached.tar.gz`](https://github.com/Dituon/petpet-templates/releases/tag/1.0.0-beta1)
2. 解压并覆盖至旧版数据目录 `./data/`
3. 重启程序以重载模板

- **从 `overflow` 迁移至 Onebot**

> 建议在使用 `overflow` 适配到 Mirai 框架的用户部署 [Onebot 版本](#onebot)

1. 下载 [最新版本 `petpet-onebot.jar`](https://github.com/Dituon/petpet/releases) 并放入 `overflow/` 目录
2. 启动 `petpet-onebot.jar` (`java -jar petpet-onebot.jar`)
3. 编辑配置项, 重启以加载新配置

- **更新 Mirai 插件**

1. 下载 [最新版本 `petpet.mirai2.jar`](https://github.com/Dituon/petpet/releases) 并将插件放入 `Mirai/plugins/` 目录
2. 删除旧版插件

- **更新 Onebot 客户端**

1. 下载 [最新版本 `petpet-onebot.jar`](https://github.com/Dituon/petpet/releases)
2. 替换旧版程序
3. 启动 `petpet-onebot.jar` (`java -jar petpet-onebot.jar`)
4. 编辑配置项, 重启以加载新配置

## 默认权限与指令

1. 使用 `pet` 获取已加载的模板表列

2. 使用模板 id 或别名触发指定模板, 例如:
   > `pr` `舔屏` `hammer` `锤`

3. At 用户以替换模板为用户头像, 例如:
   > `hammer @user` `@user 舔屏`

4. 发送或回复图像替换模板为指定图像, 例如:
   > `锤 [图片]` `[回复 [图片]] kiss`

5. 部分模板需要额外文本, 例如:
   > `osu hso!` `喜报 好消息!好消息!`

6. 可同时指定图像与额外文本, 例如:
   > `anyasuki [图片] 这张图片!` `ask @user 群友 不知道哦` (多个文本使用空格分割)

7. 群主或管理员使用 `pet on/off` 启用或禁用功能, 例如:
   > `pet on`(启用所有功能) `pet off img`(禁用自定义图像) 详见 [权限管理](#权限管理)

## 配置项

- **command**: `pet`

    > 触发指令, 默认为`pet`
    >
    > 例: `pet @xxx` `pet kiss @xxx`
    >
    > 仅发送 `pet` 时会返回模板索引或默认模板

- **nudgeProbability**: `0.3`

    > 戳一戳 触发概率, `0 - 1`, 默认为 `0.3`
    >
    > Mirai 插件考虑到兼容旧版数据, 此字段为 `0 - 100`, 默认为 `30.0`

- **resampling**: `true`

    > 重采样缩放, 启用后头像质量更高, 可对模板单独配置
    >
    > **暂时无效**

- **disabledTemplates**: `[]`

    > 禁用表列, 默认为空, 在此表列中的模板会被排除
    >
    > **暂时无效**

- **keyCommandHead**: `''`

    > 关键词前缀, 默认为空
    >
    > 例 (配置项为`'#'`时): `#kiss @user` `#osu hso!`
    >
    > 此配置不影响 `pet` 触发指令, 例如 `pet kiss @user`

- **imageCachePoolSize**: `2048`

    > 图片链接缓存池大小, 默认为 `2048`
    > 
    > 用于获取回复图像

- **respondSelfNudge**: `false`

    > 某些情况下, 机器人会主动戳其他成员, 响应机器人自己发出的戳一戳, 默认为`false`

- **defaultReplyType**: `TEMPLATE`

    > 发送 `pet` 时默认响应格式, 默认为 `TEMPLATE`
    > 
    > 枚举:
    > - `RANDOM` (回复随机模板)
    > - `TEXT` (文本消息回复模板表列)
    > - `FORWARD_TEXT` (转发消息发送模板表列)
    > - `TEMPLATE` (回复默认模板)
    > 
    > 当没有有效默认模板时, 会使用 `FORWARD_TEXT` 方案

- **defaultTemplate**: `null`

    > 指定默认模板, 留空则允许模板自行注册
    >
    > **仅在 `defaultReplyType = TEMPLATE` 时有效**

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
    > 
    > **暂时无效**

- **defaultFontFamily**: `MiSans`

    > 默认字体, 当模板未指定字体时, 使用此字体
    >
    > 指定字体未注册时, 会在系统环境中寻找以下字体:
    > - `Microsoft YaHei` (微软雅黑)
    > - `PingFang SC` (苹方)
    > - `Noto Sans CJK SC` (思源黑体)
    > 
    > 找不到以上字体时, 会尝试使用任意支持 `CJK` 字符集的字体
    > 
    > 以上尝试都失败且模板没有指定有效字体时可能导致程序无法绘制 CJK 文字

- **gifQuality**: `5`

    > Gif编码质量(`1 - 49`), 默认为`5`
    >
    > 数字越小, 速度越慢, 质量越好 (大于 `20` 时, 速度不会有明显提升)

- **headless**: `true`

    > 启用`headless`模式, 默认为`true`

- **autoUpdate**: `true`

    > 自动更新模板, 每次启动程序时都会检查并自动下载, 默认为 `true`
    >
    > **暂时无效**

- **repositoryUrl**

    > 仓库地址, 用于自动更新, 默认为本仓库地址
    >
    > **暂时无效**

- **userCooldownTime**: `1000`

    > 成功触发指令后对该用户的冷却时间(单位为毫秒), 默认为 `1000`
    >
    > 设置为 `-1` 可禁用冷却

- **groupCooldownTime**: `-1`

    > 成功触发指令后对该群聊的冷却时间, 默认为 `-1`

- **inCoolDownMessage**: `技能冷却中...`

    > 在冷却时间中触发命令的回复消息
    >
    > 配置项为`[nudge]`时, 会以戳一戳形式回复

- **commandPermissionName**

    > 指令权限节点名称别名, 详见 [权限管理](#权限管理)

- **commandOperationName**

    > 指令权限操作别名, 详见 [权限管理](#权限管理)

- **timeUnitName**

    > 时间单位别名, 用于设置冷却时间, 详见 [权限管理](#权限管理)

- **defaultGroupCommandPermission**

    > 默认群聊指令权限节点, 详见 [权限管理](#权限管理)

- **defaultGroupEditPermission**: `"command_permission nudge_probability disable_template"`

    > 默认管理权限ID, 详见 [权限管理](#权限管理)
    > 
    > 例:
    > - 当本配置项为 `command_permission` 时, 群聊管理员只能编辑指令权限节点, 不能修改戳一戳触发概率或其它权限
    > - 当本配置项为 `nudge_probability cooldown_time` 时, 群聊管理员可以编辑戳一戳触发概率与冷却时间

Onebot 客户端的更多配置项请参考 [bot/onebot/](./onebot/README.md)

## 权限管理

权限操作:

- `pet on/off <权限节点>`: 启用或禁用功能

  > 权限ID: `command_permission`
  >
  > 默认别名: `启用` `禁用`

  权限节点:

    - `command`: 使用 **指令 + 关键词** 生成模板
      > 默认别名: `cmd` `指令`
      >
      > 例: 禁用此节点后以下指令失效
      >
      > `pet` `pet kiss` `pet 锤`

    - `at`: **At 用户**以生成模板
      > 默认别名: `提及`
      >
      > 例: 禁用此节点后以下指令失效
      >
      > `kiss @user` `@user 锤`

    - `image`: **发送 / 回复图像**以生成模板
      > 默认别名: `img` `回复` `图像`
      >
      > 例: 禁用此节点后以下指令失效
      >
      > `pr [图片]` `[回复 [图片]] 舔屏`

    - `command_head`: **直接使用关键词**生成模板
      > 默认别名: `id` `key` `指令头`
      >
      > 例: 禁用此节点后以下指令失效
      >
      > `pr` `锤 [图片]` `舔屏 @user`

    - `all`: 默认值, 表示所有权限节点

  示例:

    - `pet on command`:  启用 `command` 节点
    - `pet 禁用 img at`: 禁用 `image` 与 `at` 节点
    - `pet off`: 禁用所有权限节点

- `pet nudge_probability <概率>`

  > 权限ID: `nudge_probability`
  >
  > 默认别名: `概率` `戳一戳概率`

  示例:

    - `pet nudge_probability 50%`: 戳一戳时 **50%** 概率触发随机模板
    - `pet 概率 33`: 戳一戳时 **33%** 概率触发随机模板
    - `pet 概率 0`: 禁用戳一戳随机模板

- `pet cooldown_time <时间>`

  > 权限ID: `cooldown_time`
  >
  > 默认别名: `冷却` `冷却时间`

  时间单位:

    - `ms` & `毫秒`: **毫秒**
    - `s` & `sec` & `秒`: **秒**
    - `m` & `min` & `分`: **分钟**
    - `h` & `hr` & `hour` & `小时`: **小时**
    - `d` & `day` & `天`: **天**

  示例:

    - `pet cooldown_time 1m`: 群聊冷却时间为 **1 分钟**
    - `pet 冷却 2h`: 群聊冷却时间为 **2 小时**
    - `pet 冷却 3600秒`: 群聊冷却时间为 **3600 秒**

- `pet disable_template <关键词>`

  **暂时无效**

  > 权限ID: `disable_template`
  >
  >  默认别名: `禁用` `禁用模板`

