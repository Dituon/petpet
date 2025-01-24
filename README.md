# Petpet

![GitHub Repo stars](https://img.shields.io/github/stars/dituon/petpet)
![Mirai version](https://img.shields.io/badge/Mirai-2.16.0-ff69b4)
![GitHub](https://img.shields.io/github/license/dituon/petpet)
![GitHub all releases](https://img.shields.io/github/downloads/dituon/petpet/total)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/dituon/petpet)
![GitHub issues](https://img.shields.io/github/issues/dituon/petpet)
![GitHub closed issues](https://img.shields.io/github/issues-closed/dituon/petpet)
![GitHub closed pull requests](https://img.shields.io/github/issues-pr-closed/dituon/petpet)
[![](https://jitpack.io/v/Dituon/petpet.svg)](https://jitpack.io/#Dituon/petpet)

根据模板生成图像。

- [在线体验](https://petpet.d2n.moe)

## 迁移

旧版仓库已迁移至 [6.2-latest](https://github.com/Dituon/petpet/tree/6.2-latest)

数据目录已迁移至 [petpet-templates](https://github.com/Dituon/petpet-templates)

新版模板配置文件为 `template.json`, 并完全兼容旧版 `data.json`; 如遇不兼容情况, 请提交 [issue](https://github.com/Dituon/petpet/issues)

机器人迁移指南请参阅 [bot/ 从旧版迁移](bot/README.md#从旧版迁移)

## 文档索引

- **开发文档** - [core/](core/README.md)
- **模板标准** - [docs/](docs/index.md)
- **部署 Onebot / Mirai 插件** - [bot/](bot/README.md)
- **部署 HTTP 服务器** - [httpserver/](httpserver/README.md)

## 项目结构

- `bot/`: 机器人模块
  - `mirai/`: Mirai 插件
  - `onebot/`: Onebot 客户端
  - `shared/`: 通用代码
- `core/`: 渲染核心模块
- `docs/`: 标准文档
- `httpserver/`: HTTP 服务器模块
- `script/`: 脚本与动态模板模块
- `service/`: 通用服务模块

## 自定义模板

#### `template.json`

模板标准文档请查看 [docs/template](docs/template/index.md), 新版在线编辑器正在更新中...

对文档中的描述有疑问? 可参考 [已实现的新版模板](https://github.com/search?q=repo%3ADituon%2Fpetpet-templates+path%3Atemplate.json)

#### `data.json`

旧版模板请参考 [docs/old_template](docs/old_tmplate/index.md) 或使用[在线编辑器](https://d2n.moe/petpet-js/editor)

#### `main.js`

动态脚本请参考 [docs/script](docs/script/index.md) 或 [types.d.ts](https://github.com/dituon/petpet/service/src/test/resources/test-templates/script-test/types.d.ts)

## 相关链接

- [petpet-js](https://github.com/Dituon/petpet-js) - Petpet 前端实现，支持旧版模板。
- [petpet-rs](https://github.com/Dituon/petpet-rs) - 使用 Rust Skia 后端实现，支持旧版模板。
- [QQ 交流群](https://qm.qq.com/q/ikRbuuGWRi) - 群号 `961494251`

## TODO List

以下功能尚未实现。如需使用这些功能，可选择 [6.2-latest](https://github.com/Dituon/petpet/tree/6.2-latest) 版本。

**Bot**

- [ ] Message Hook
- [ ] 消息事件同步锁

**Core**

- [ ] 重采样缩放
- [ ] 合成时比例缩小图像
- [ ] 可选 js 引擎
- [ ] 缩小打包体积
- [ ] 补全文档

## 鸣谢

- [Contributors](https://github.com/Dituon/petpet/graphs/contributors) - 本项目的贡献者们
- [nonebot-plugin-petpet](https://github.com/noneplugin/nonebot-plugin-petpet) - 灵感 & 部分数据来源
- [Mirai](https://github.com/mamoe/mirai) - 机器人开发框架
- [Overflow](https://github.com/MrXiaoM/Overflow) & [onebot-client](https://github.com/cnlimiter/onebot-client) - Onebot 协议开发框架
- [Jetbrains](https://www.jetbrains.com/) - 提供开源 IDE 许可证

## 后话

如果你喜欢这个项目，请点击 Star，关注此项目更新请点击 Watch。

反馈 / 建议 / 贡献请提交 [Issue](https://github.com/Dituon/petpet/issues)。

欢迎提交自定义模板至 [petpet-templates](https://github.com/Dituon/petpet-templates)。

QQ 交流群号: [`961494251`](https://qm.qq.com/q/ikRbuuGWRi)
