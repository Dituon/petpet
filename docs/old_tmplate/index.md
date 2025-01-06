# Petpet 旧版模板

本文是关于 Petpet 旧版本 `data.json` 的模板标准, 旧版本标准不再更新, 请使用新版本模板。

## 结构

```text
data/
├── my-template/
│   ├── 0.png          # 模板背景图像
|   ├── 1.png
│   ├── 2.png
|   ├── 3.png
│   ├── data.json      # 模板数据
```

## `data.json`

```jsonc
{
  "type": "GIF", // GIF 或 IMG
  "avatar": [],
  "text": [],
  "delay": 50,
  "alias": [ "别名1", "别名2" ]
}
```

| **属性**         | **类型**      | **注释**                   | **默认值** |
| ---------------- | ------------- | -------------------------- | ---------- |
| **type**         | 模板类型枚举  | 图片类型枚举, `IMG`或`GIF` | 必须       |
| **avatar**       | `Avatar` 数组 | 头像配置数组               | 必须       |
| **text**         | `Text` 数组   | 文本配置数组               | 必须       |
| **inRandomList** | 布尔值        | 是否在随机列表中           | `false`    |
| **reverse**      | 布尔值        | GIF是否倒放                | `false`    |
| **delay**        | 整数          | 帧间延时 (毫秒)            | `65`       |
| **background**   | `Background`  | 背景配置, 见下文           | `null`     |
| **alias**        | 字符串数组    | 别名数组                   | `[]`       |
| **hidden**       | 布尔值        | 是否隐藏                   | `false`    |

##### 模板类型枚举

- `GIF`  动图, 程序会读取目录下所有 `.png` 格式的图像
- `IMG`  静态图片, 程序会读取目录下随机 `.png` 格式的图像

