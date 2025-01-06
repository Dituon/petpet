# Length

长度的基本单位:

- `px`: pixel (像素)
- `cw`: content width (内容宽度)
- `ch`: content height (内容高度)
- `vw`: viewport width (视口宽度)
- `vh`: viewport height (视口高度)

示例:

- `100`: 100 像素
- `50vw`: 视口宽度的 50%

```jsonc
{
    "type": "image",
    "elements": [{
        "type": "image",
        "src": "./bg.png",
        // 示例: 填满整个画布的图像:
        "coords": [0, 0, "100vw", "100vh"],
        // 示例: 使用图像的原始尺寸:
        "coords": [0, 0, "100cw", "100ch"]
    }],
    "canvas": {
        "width": "1200px",
        "height": "1200px"
    }
}
```

## Percentage Length

额外支持百分比单位:

- `%`: percent (百分比)

常用于 [Offset](#offset) 类型。

## Dynamic Length

动态计算长度:

- `"calc(50% + 20px)"`
- `"50vw + 15px"` (省略计算函数)

### 变量与引用

Petpet 支持引用尺寸变量

- `<name>_width`: 元素宽度
- `<name>_height`: 元素高度
- `<name>_length`: 元素长度

以下为合法的 `<name>`:

- `element_n`: 第 n 个元素
- `<id>`: 在元素中定义的 `id` 字段
- `<key>`: 传入数据的 `<key>` 字段

> 可在任何 [Length](./length.md) 类型中使用变量。
> 为确保正确构建变量依赖关系, 请检查没有循环引用的变量。

例:

```jsonc
{
    "type": "image",
    "elements": [
        {
            "type": "image",                            // 元素 element_0
            "key": "to",
            "coords": [0, 0, "100vw", "100vh"]          // 占满整个画布
        }, {
            "type": "image",                            // 元素 element_1
            "id": "cover",                              // 定义元素名为 cover
            "src": "...",
            "coords": [0, 0, "to_width", "to_height"],  // 引用 to 数据尺寸
            "opacity": 0.5
        }
    ],
    "canvas": {
        "width": "cover_width",                         // 引用 cover 元素的尺寸
        "height": "cover_height",
        "length": "to_length"                           // 引用 to 元素的帧长度
    }
}
```

变量支持动态运算，例如:

- `to_width + 20px`
- `element_0_width * 2`

# Offset

表示相对于原点的偏移量

`"x-offset y-offset"`

允许使用关键词与 [Percentage Length](#percentage-length)

## 关键词

- `center`: 居中, 同 `50%`
- `left`: 左侧, 同 `0%`
- `right`: 右侧, 同 `100%`
- `top`: 顶部, 同 `0%`
- `bottom`: 底部, 同 `100%`

示例:

- `"left top"`: 左侧顶部
- `"20px 80%"`: 20px 水平偏移和 80% 垂直偏移

简写:

- `"center"`: `"center center"`
- `"left"`: `"left center"`
- `"top"`: `"center top"`
- `"80%"`: `"80% 80%"`

更多示例与图示参见:

- [image.fit](./image.md#fit)
- [image.origin](./image.md#origin)
