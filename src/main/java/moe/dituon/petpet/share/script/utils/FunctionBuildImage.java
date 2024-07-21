package moe.dituon.petpet.share.script.utils;

import moe.dituon.petpet.share.script.LuaExtraData;
import moe.dituon.petpet.share.service.BackgroundResource;
import moe.dituon.petpet.share.template.PetpetTemplate;
import moe.dituon.petpet.share.template.TemplateBuilder;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.io.IOException;
import java.nio.file.Path;

public class FunctionBuildImage extends TwoArgFunction {
    private final Path basePath;

    public FunctionBuildImage(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public LuaValue call(LuaValue template, LuaValue extraData) {
        var templateInstance = PetpetTemplate.fromLuaTable(template.checktable());
        var extraDataInstance = LuaExtraData.fromLuaTable(extraData.checktable());
//        var service = PetpetService.getInstance();
        var builder = new TemplateBuilder(templateInstance, new BackgroundResource(basePath.toFile()));
        var model = builder.build(extraDataInstance.toExtraData(basePath));
        try {
            var result = new LuaResultImage(model.getResult(), basePath);
            return CoerceJavaToLua.coerce(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
