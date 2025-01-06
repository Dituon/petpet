# Petpet Core

## 导入

`build.gradle`
```groovy
dependencies {
    implementation 'com.github.dituon:petpet:$version'
}
repositories {
    maven { url 'https://jitpack.io' }
    mavenCentral()
}
```

`build.gradle.kts`
```kotlin
dependencies {
    implementation("com.github.dituon:petpet:$version")
}
repositories {
    maven { setUrl("https://jitpack.io") }
    mavenCentral()
}
```

### 读取现有模板 & 生成图像

```java
// 读取模板文件
File templateFile = new File("template.json");
PetpetTemplate template = PetpetTemplate.fromJsonFile(templateFile);
// 构造模型
PetpetModel model = new PetpetTemplateModel(template);
EncodedImage resultImage = model.draw(new RequestContext(
        // 传入图像数据
        Map.of("to", "https://avatars.githubusercontent.com/u/68615161?v=4",
               "from", "./input.png"),
        // 传入文本数据
        Map.of("to", "To User Name",
               "from", "From User Name")
));
// 保存图像
resultImage.save("./output.png");
// 或获取图像信息
System.out.printf(
        "output size: %s; format: %s",
        resultImage.bytes.length,
        resultImage.format
); // output size: 524288; format: png
```

### 以编程方式创建新模板

```java
PetpetTemplate template = PetpetTemplate.builder()
        .type(TemplateType.IMAGE)
        .elements(List.of(
                AvatarTemplate.builder()
                        .src("./input.png")
                        .coords(new AvatarXYWHCoords(
                                0, 0, 200, 200
                        ))
                        .angle(45f)
                        .build(),
                TextTemplate.builder()
                        .text("Text 01")
                        .coords(new TextXYWCoords(
                                0, 0, 200
                        ))
                        .size(16)
                        .color(Color.WHITE)
                        .build()
        ))
        .canvas(
                TemplateCanvas.builder()
                        .width(200)
                        .height(200)
                        .build()
        )
        .build();
// 设置模板基础路径, 用于相对路径读取图像
template.setBasePath(new File("./"));
```

等价于

```json
{
  "type": "image",
  "elements": [
    {
      "type": "image",
      "src": "./input.png",
      "coords": [0, 0, 200, 200],
      "angle": 45
    },
    {
      "type": "text",
      "text": "Text 01",
      "coords": [0, 0, 200],
      "size": 16,
      "color": "#ffffff"
    }
  ],
  "canvas": {
    "width": 200,
    "height": 200
  }
}
```

