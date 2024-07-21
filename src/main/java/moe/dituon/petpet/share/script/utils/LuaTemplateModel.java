package moe.dituon.petpet.share.script.utils;

import moe.dituon.petpet.share.script.LuaExtraData;
import moe.dituon.petpet.share.service.BackgroundResource;
import moe.dituon.petpet.share.template.PetpetTemplate;
import moe.dituon.petpet.share.template.TemplateBuilder;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.io.IOException;
import java.nio.file.Path;

public class LuaTemplateModel {
    public final String name;
    public final LuaTable alias;
    protected final TemplateBuilder builder;
    protected final Path basePath;

    public LuaTemplateModel(String name, TemplateBuilder builder, Path basePath) {
        this.builder = builder;
        this.name = name;
        this.alias = LuaTable.listOf(builder.templateData.getAlias()
                .stream().map(LuaValue::valueOf)
                .toArray(LuaValue[]::new)
        );
        this.basePath = basePath;
    }

    public LuaValue build(LuaValue extraData) {
        var extraDataInstance = LuaExtraData.fromLuaTable(extraData.checktable());
        var model = builder.build(extraDataInstance.toExtraData(basePath));
        try {
            var result = new LuaResultImage(model.getResult(), basePath);
            return CoerceJavaToLua.coerce(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
