# petpet

一个生成摸摸头GIF的 Mirai 插件，灵感/数据来自 [nonebot-plugin-petpet](https://github.com/noneplugin/nonebot-plugin-petpet)。

java 编写，**未使用任何第三方库** ：轻量，高效。

## 使用方法

1. 下载 [最新版本](https://github.com/Dituon/petpet/releases/tag/dev)

2. 将插件放入 `Mirai/plugins/`

3. 下载 [图片素材](https://github.com/Dituon/petpet/tree/main/res/petpet)

4. 将图片素材放入 `Mirai/res/petpet`

5. 使用 **戳一戳** 有 `30%` 的概率触发; 或发送 `pet @xxx`

## 配置文件

首次运行 Petpet 插件时，会生成 `Mirai/plugins/petpet.json` 文件

```
{
  "command": "pet", // 触发 petpet 的指令
  "probability": 30, // 使用 戳一戳 的触发概率
  "antialias": false // 抗锯齿
}
```

修改后重启 Mirai 以重新加载

## 图片预览

**图片按index排序(见`Petpet.java`)**

<details>
<summary>展开/收起</summary>

![image](img/0.gif)

![image](img/1.gif)

![image](img/2.gif)

![image](img/3.gif)

![image](img/4.gif)

![image](img/5.gif)

![image](img/6.gif)

![image](img/7.gif)

![image](img/8.gif)

![image](img/9.gif)

![image](img/10.gif)

![image](img/11.gif)

![image](img/12.gif)

![image](img/13.gif)

</details>

## 二次开发

快速进行二次开发

### `ImageSynthesis.java`

通过坐标和图片路径生成GIF并发送:

**单个头像生成GIF**

`sendImage(Member m, String path, int[][] pos, boolean isAvatarOnTop, boolean isRotate)`

- m: Member对象，用于获取头像和上传图片
- path: 图片路径，用于遍历图片
- pos: 头像坐标，用于生成图片
- isAvatarOnTop: 头像在贴图之上
- isRotate: 头像旋转，自动旋转360度
返回 Mirai Image 对象。

**两个头像生成GIF**

`sendImage(Member m1, Member m2, String path, int[][] pos1, int[][] pos2)`

- m1&m2: Member对象，**m2在m1图层之上**
- path: 图片路径，用于遍历图片
- pos1&pos2: 头像坐标，对应m1&m2
返回 Mirai Image 对象。

### `GifMaker.java`

构造GifMaker对象并生成GIF

使用例:

```
GifMaker gifMaker = new GifMaker(ImageIO.read(new File("0.png")).getType(), 60, true);
// 构造对象，参数为: 图片标签, 帧间距(ms), 循环
BufferedImage output = new BufferedImage(...);
gifMaker.writeToSequence(output);
// 输入 BufferedImage 对象
gifMaker.close();
// 停止输入并生成GIF
gifMaker.getOutput();
// 获取GIF (InputStream)
// ExternalResource resource = ExternalResource.create(gifMaker.getOutput());
// Image image = xxx.uploadImage(resource);
```

## 常见问题

> 为什么戳一戳无法触发?
>> 检查 Mirai 登录协议，仅 `ANDORID_PHONE` 可以收到 戳一戳 消息

> 为什么不用 Mirai 的原生配置文件方法?
>> 因为 **JAutoSavePluginData 根本无法使用**, 如果 Mirai 的开发者看到了麻烦测试一下

## 后话

造轮子是非常痛苦的，我这辈子再也不想碰 ImageWriter 和 IIOMetadata 这些东西了...

对此插件进行二次开发比你想象的简单很多，我认为这是初学者入门 Mirai 开发的不二选择。

交流群: `534814022`
