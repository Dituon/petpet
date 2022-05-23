# petpet

一个生成摸摸头GIF的 Mirai 插件，灵感/数据来自 [nonebot-plugin-petpet](https://github.com/noneplugin/nonebot-plugin-petpet)。

java 编写，**未使用任何第三方库** ：轻量，高效。

## 使用方法

使用 **戳一戳** 或 **指令`pet @xxx`** 有 `30%` 的概率触发。

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

## 后话

造轮子是非常痛苦的，我这辈子再也不想碰 ImageWriter 和 IIOMetadata 这些东西了...

对此插件进行二次开发比你想象的简单很多，我认为这是初学者入门 Mirai 开发的不二选择。

交流群: `534814022`
