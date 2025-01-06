# Petpet 模板

本文是关于 Petpet 新版本 `template.json` 的模板标准, 旧版本模板 `data.json` 请参考 [旧版模板](../old_tmplate/index.md)。

## 结构

```text
data/
├── my-template/
│   ├── template.json      # 模板数据
```

## `template.json`

```jsonc
{
    "type": "gif",    // gif 或 image
    "metadata": {       // 元数据
        "api_version": 100,          // API 版本
        "template_version": 1,       // 模板版本
        "alias": [ "别名1", "别名2" ],
        "desc": "...",
        // tags, author...
    },
    "elements": [       // 元素
        // ...
    ],
    "canvas": {         // 画布
        "width": "500px",
        "height": "500px"
    },
    "delay": 50         // 帧间延时, 单位为ms
}
```

| **属性**       | **类型**                  | **描述**                 | **默认值** |
|--------------|-------------------------|------------------------|---------|
| **type**     | string                  | 模板类型, `gif` 或 `image`  | 必须      |
| **metadata** | [Metadata](metadata.md) | 模板元数据                  | 无       |
| **elements** | [Element](#元素)[]        | 元素列表                   | 无       |
| **canvas**   | [Canvas](#画布)           | 画布配置                   | 无       |
| **delay**    | int                     | 帧间延时, 单位为 ms           | `50`    |
| **fps**      | float                   | 帧速率, 对 **delay** 字段的补充 | 无       |

`fps` 字段为 `delay` 的补充, 当 `delay` 有值时, `fps` 字段会被忽略。

`fps` 字段会被换算为 `delay: 1000 / fps`, 例如 `fps: 60` 等价于 `delay: 17`。

## 元素

- [image](./image.md): 图像元素
- [text](./text.md): 文本元素
- [background](./background.md): 背景元素

> **background** 元素最多存在一个, 会覆盖画布配置。

## 画布

| **属性**      | **类型**                    | **描述** | **默认值**   |
|-------------|---------------------------|--------|-----------|
| **width**   | [Length](length.md)       | 画布宽度   | 空         |
| **height**  | [Length](length.md)       | 画布高度   | 空         |
| **length**  | [Length](length.md)       | 帧长度    | 空         |
| **color**   | [Color](./types.md#color) | 背景色    | `#ffffff` |
| **reverse** | boolean                   | 是否倒序播放 | false     |

