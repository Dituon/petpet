### 文字

```jsonc
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

```jsonc
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
