package moe.dituon.petpet.script.functions;

import lombok.Getter;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.script.ScriptObjectTransformerKt;
import moe.dituon.petpet.template.Metadata;
import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.openjdk.nashorn.internal.runtime.Undefined;

import java.util.List;

public class TemplateRegisterer extends AbstractJSObject {
    @Getter
    @Nullable
    protected Metadata metadata;
    protected final String id;

    public TemplateRegisterer(String id) {
        this.id = id;
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    /**
     * <pre>
     * register((info: RuntimeInfo) => Metadata): string (templateId)
     * </pre>
     */
    @Override
    public Object call(Object thiz, Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("register() requires exactly 1 argument");
        }
        var handler = (ScriptObjectMirror) args[0];
        if (!handler.isFunction()) {
            throw new IllegalArgumentException("handler must be a function");
        }
        var metadataRaw = handler.call(thiz, RuntimeInfo.getInstance());
        if (metadataRaw == null || metadataRaw instanceof Undefined) {
            this.metadata = null;
            return null;
        }
        this.metadata = Metadata.fromJsonElement(
                ScriptObjectTransformerKt.scriptObjectToJsonElement((ScriptObjectMirror) metadataRaw)
        );
        return id;
    }

    public static class RuntimeInfo {
        @SuppressWarnings("unused")
        public final int version = GlobalContext.API_VERSION;
        @SuppressWarnings("unused")
        public final int scriptApiVersion = 100;
        @SuppressWarnings("unused")
        public final String platform = "jvm";
        @SuppressWarnings("unused")
        public final String jsEngine = "nashorn";
        @SuppressWarnings("unused")
        public final String drawingApi = "awt";
        @SuppressWarnings("unused")
        public final List<String> features = GlobalContext.getInstance().features;

        private static class RuntimeInfoInstance {
            private static final RuntimeInfo INSTANCE = new RuntimeInfo();
        }
        public static RuntimeInfo getInstance() {
            return RuntimeInfoInstance.INSTANCE;
        }
    }
}
