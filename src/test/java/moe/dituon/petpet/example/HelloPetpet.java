package moe.dituon.petpet.example;

import kotlin.Pair;
import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonArray;
import moe.dituon.petpet.share.*;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public class HelloPetpet {
    //(｡･∀･)ﾉﾞ嗨 欢迎来到互动式实例, 本实例可以让你快速了解到Petpet基础API的使用
    static BasePetService service = new BasePetService(); //在开始之前, 需要先实例化PetService
    //Service是什么呢?
    //在Petpet中, Service是程序的核心, 可以把Service理解成工厂
    //工人(线程)在流水线上通过设计图(模板)将不同图案的零件(图片/素材)拼装为最终产品
    //
    //1. 实例化 new BasePetService() —— 计划建造一个工厂, 买下一处厂房
    //
    //2. 读取配置 service.readBaseServiceConfig() —— 装修工厂, 并制定全局规则
    //    —— 例如 所有的产品都要经过抗锯齿处理(antialias=true), 最多有10个工人(线程)同时工作(threadPoolSize=10)...
    //
    //3. service.putKeyData() —— 添加设计图(模板)并编号, 这些零件(素材)应该这样拼装...
    //
    //4. service.generateImage() —— 根据设计图, 给定零件 生产指定的产品

    @Test
    public void test() {
        //实例化后, 需要加载配置参数
        BaseServiceConfig config = new BaseServiceConfig(); //你可以直接使用默认值, 也可以自定义一些配置
        //config.setAntialias(false); //举个例子, 禁用抗锯齿
        service.readBaseServiceConfig(config); //读取刚刚实例化的配置对象

        //给定配置后, 就可以添加模板了

        /*
        //直接读取本地文件 加载PetData(KeyData)模板
        service.readData(new File("./data/xmmt.dituon.petpet").listFiles());
         */

        //当然, 也可以在程序中创建并添加
        KeyData exampleTemplate = new KeyData(Type.IMG); //实例化一个模板对象, 指定模板Type

        AvatarData avatarData = new AvatarData(AvatarType.TO); //手动实例化头像模板, 并指定参数
        //你可以更改一些参数的默认值, 例如:
        avatarData.setPos(jsonArrayFromString("[0, 0, 100, 100]")); //设置坐标
        avatarData.setOpacity(0.5F); //设置透明度

        /*
        //直接解析字符串JSON格式的模板, 例如
        KeyData exampleTemplate = KeyData.getData(
                "{\"type\": \"IMG\", \"avatar\": [], \"text\": []}" //详见文档#自定义
        );
         */

        exampleTemplate.getAvatar().add(avatarData); //向模板中添加刚刚定义的头像
        service.putKeyData("example", exampleTemplate, new BufferedImage(500, 500, Image.SCALE_DEFAULT)); //添加模板, 可通过Key"example"调用模板

        //到目前为止, 你已经了解到如何添加模板, 接下来  让我们通过模板生成图片吧!

        //合成方法必须的数据为AvatarExtraData和TextExtraData, 将数据代入模板合成图片

        //可通过工厂方法 使用URL快速创建AvatarExtraData (头像数据)
        GifAvatarExtraDataProvider avatarExtraData = BaseConfigFactory.getGifAvatarExtraDataFromUrls( //要合成的图片素材
                "https://q1.qlogo.cn/g?b=qq&nk=2544193782&s=640", //指定FromAvatar为网络图片
                "file:./example-data/input/to.gif", //指定ToAvatar为本地文件(支持GIF)
                null, //不指定GroupAvatar(null)
                null, //不指定BotAvatar
                List.of("url1", "url2", "url3") //指定RandomAvatarList(可空)
        );

        /*
        //当然, 也可以直接使用内存中的BufferedImage进行构建
        //ImageSynthesis和ImageSynthesisCore封装了JDK底层画图API, 提供了多个使用方法, 可快速调用
        //ImageSynthesis基于本程序的Model模型, 有依赖关系; ImageSynthesisCore为抽象静态实用类, 无依赖关系

        BufferedImage exampleBufferedImage = new BufferedImage(500, 500, Image.SCALE_DEFAULT);
        GifAvatarExtraDataProvider avatarExtraData = BaseConfigFactory.toGifAvatarExtraDataProvider(
                new AvatarExtraDataProvider( //为了便于演示, 这里使用了弃用的AvatarExtraDataProvider方法, 只能加载静态图像
                        () -> exampleBufferedImage, //为保证按需加载和多线程执行, 本质为返回BufferedImage的lambda表达式
                        () -> ImageSynthesisCore.mirrorImage(exampleBufferedImage), //表达式中的代码会在多线程环境下执行, 保证执行效率
                        null,
                        null,
                        null
                )
        ); //使用GifAvatarExtraDataProvider以保证对GIF格式的解析
         */

        Pair<InputStream, String> result = service.generateImage( //合成图片, 返回合成后的图片及其格式
                "example", //通过Key调用刚刚添加的模板
                avatarExtraData, //头像数据
                new TextExtraData( //替换文本 (文本数据)
                        "我",
                        "恋恋",
                        "你群",
                        List.of("txt1", "txt2", "txt3")
                ),
                null //附加文本 (可空)
        );

        String savePath = AbstractTest.OUTPUT_ROOT + "example." + result.getSecond(); //保存路径
        AbstractTest.copyInputStreamToFile(
                result.getFirst(), //图片输入流
                new File(savePath)
        );
        System.out.println("保存目录: " + savePath);
    }

    /**
     * 调用Kotlin方法, 将字符串解析为JsonArray类型
     */
    static JsonArray jsonArrayFromString(String str) {
        return Json.Default.decodeFromString(JsonArray.Companion.serializer(), str);
    }
}
