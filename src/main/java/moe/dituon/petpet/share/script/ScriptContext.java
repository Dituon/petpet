package moe.dituon.petpet.share.script;

import moe.dituon.petpet.share.script.utils.FunctionBuildImage;
import moe.dituon.petpet.share.service.PetpetService;
import moe.dituon.petpet.share.template.PetpetTemplate;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public class ScriptContext {
    protected final Globals globals = JsePlatform.standardGlobals();
    protected final LuaFunction onLoad;
    protected final LuaFunction onSend;

    public ScriptContext(File scriptFile) {
        globals.loadfile(scriptFile.getAbsolutePath()).call();
        var onLoadRaw = globals.get("on_load");
        onLoad = onLoadRaw.isnil() ? null : onLoadRaw.checkfunction();
        var onSendRaw = globals.get("on_send");
        onSend = onSendRaw.isnil() ? null : onSendRaw.checkfunction();
        globals.set("build_image", new FunctionBuildImage(scriptFile.getParentFile().toPath()));
    }

    public void onLoad() {
        if (onLoad == null) return;
        onLoad.call();
    }
}
