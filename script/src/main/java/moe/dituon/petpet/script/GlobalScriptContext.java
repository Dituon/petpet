package moe.dituon.petpet.script;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;

public class GlobalScriptContext {
    static {
        System.setProperty("nashorn.args", "--language=es6 --no-java");
    }

    private static class GlobalScriptContextInstance {
        private static final GlobalScriptContext INSTANCE = new GlobalScriptContext();
    }

    public static GlobalScriptContext getInstance() {
        return GlobalScriptContextInstance.INSTANCE;
    }

    public final ScriptEngine engine;
    public final Invocable invocable;

    protected GlobalScriptContext() {
        ScriptEngine tempEngine;
        try {
            tempEngine = new NashornScriptEngineFactory().getScriptEngine();
        } catch (NoClassDefFoundError ex) {
            throw new RuntimeException("No script engine found");
            // TODO: support for other engines
//            ScriptEngineManager manager = new ScriptEngineManager();
//            tempEngine = manager.getEngineByName("javascript");
        }

//        if (tempEngine == null) {
//            throw new RuntimeException("No script engine found");
//        }

        this.engine = tempEngine;
        this.invocable = (Invocable) tempEngine;
    }
}
