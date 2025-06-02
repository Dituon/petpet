# Petpet HTTP Server

## 部署

1. 下载 [最新版本 `petpet-httpserver.jar`](https://github.com/Dituon/petpet/releases)
2. [模板素材](https://github.com/Dituon/petpet-templates) 并放入 `./data/xmmt.dituon.petpet/` 目录
3. 启动 `petpet-httpserver.jar` (`java -jar petpet-httpserver.jar`)
4. 编辑配置文件 / 发起请求

## 配置项

- **port**: `2333`

  > HTTP 服务器端口, 默认为 `2333`

- **template_path**

  > 模板数据路径

- **font_path**

  > 字体路径

- **default_font_family**: `"MiSans"`

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
  <br/>
  
- **update**

  > 从远程仓库自动更新模板与字体
  >
  > 详见 [Updater](../service/README.md#updater) 配置项文档

- **headless**: `true`

  > 是否使用 headless 模式 , 默认为 `true`


## 接口文档

### **`/`**

获取模板列表

**请求方法**: `GET`

**请求示例**:

`http://localhost:2333/`

**返回示例**:

```json5
{
  "version": "1.0.0",
  "api_version": 100,
  "graphics_api": "awt",
  "templates": [
    {
      "id": "play",
      "metadata": {
        "apiVersion": 100,
        "templateVersion": 0,
        "alias": [
          "玩",
          "顶"
        ],
        // ...
      }
    },
    // ...
  ]
}
```

### **`/generate`**

**请求方法**: `POST`

**请求参数**:

| 参数名       | 类型                 | 描述        |
|-----------|--------------------|-----------|
| **id**    | `string`           | 模板 ID 或别名 |
| **image** | `{string: URL}`    | 输入的图像数据   |
| **text**  | `{string: string}` | 输入的文本数据   |

**请求示例**:

`http://localhost:2333/generate`

纯文本/json 请求:

```json5
{
  "id": "petpet",
  // 模板 ID 或别名
  "image": {
    // 输入的图像数据
    "to": "https://avatars.githubusercontent.com/u/68615161?v=4"
  },
  "text": {
    // 输入的文本数据
    "to": "Petpet!"
  }
}
```

`multipart/form-data` 请求:

```text
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary12345

------WebKitFormBoundary12345
Content-Disposition: form-data; name="id"

petpet

------WebKitFormBoundary12345
Content-Disposition: form-data; name="to"

Petpet!

------WebKitFormBoundary12345
Content-Disposition: form-data; name="to"; filename="image.png"
Content-Type: image/png

<图像内容>
------WebKitFormBoundary12345--
```

**请求方法**: `GET`

**请求参数**:

| 参数名            | 类型       | 描述        |
|----------------|----------|-----------|
| **id**         | `string` | 模板 ID 或别名 |
| **image_$key** | `URL`    | 输入的图像数据   |
| **text_$key**  | `string` | 输入的文本数据   |

**请求示例**:

```text
http://localhost:2333/generate
      ?id=petpet
      &image_to=https%3A%2F%2Favatars.githubusercontent.com%2Fu%2F68615161%3Fv%3D4
      &text_to=Petpet!
```

### **`/generate/$id`**

**请求方法**: `POST`

**请求参数**:

与 [`/generate`](#generate) 一致, 可省略 `id` 参数

**请求示例**:

`http://localhost:2333/generate/petpet`

纯文本/json 请求:

```json5
{
  // 省略模板 ID
  "image": {
    // 输入的图像数据
    "to": "https://avatars.githubusercontent.com/u/68615161?v=4"
  },
  "text": {
    // 输入的文本数据
    "to": "Petpet!"
  }
}
```

`multipart/form-data` 请求示例详见上文

**请求方法**: `GET`

**请求参数**:

与 [`/generate`](#generate) 一致, 可省略 `id` 参数

**请求示例**:

```text
http://localhost:2333/generate/petpet
      ?image_to=https%3A%2F%2Favatars.githubusercontent.com%2Fu%2F68615161%3Fv%3D4
      &text_to=Petpet!
```
