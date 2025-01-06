package moe.dituon.petpet.script;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.script.event.EventManager;
import moe.dituon.petpet.script.event.ScriptSendEvent;
import moe.dituon.petpet.script.functions.EventListenerRegisterer;
import moe.dituon.petpet.script.functions.GenerateImageFunction;
import moe.dituon.petpet.script.functions.IsFileExistsFunction;
import moe.dituon.petpet.script.functions.TemplateRegisterer;
import moe.dituon.petpet.template.Metadata;
import org.jetbrains.annotations.Nullable;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public class PetpetJsScriptModel implements PetpetScriptModel {
    protected final Bindings bindings;
    protected final CompiledScript script;
    @Nullable
    @Getter
    protected final Metadata metadata;
    @Getter
    public final EventManager eventManager = new EventManager();
    @Getter
    public final File basePath;

    public PetpetJsScriptModel(File jsFile) throws IOException, ScriptException {
        this(Files.readString(jsFile.toPath()), jsFile.getParentFile());
    }

    public PetpetJsScriptModel(String scriptStr, File basePath) throws ScriptException {
        var engine = GlobalScriptContext.getInstance().engine;
        bindings = engine.createBindings();
        this.basePath = basePath;
        var templateRegisterer = new TemplateRegisterer(basePath.getName());
        bindings.put("register", templateRegisterer);
        script = ((Compilable) engine).compile(scriptStr);
        var eventRegisterer = new EventListenerRegisterer(eventManager);
        bindings.put("on", eventRegisterer);
        bindings.put("generate", new GenerateImageFunction(basePath, null));
        bindings.put("isFileExists", new IsFileExistsFunction(basePath));
        bindings.put("log", new ScriptLogger());

        script.eval(bindings);
        metadata = templateRegisterer.getMetadata();
    }

    @Override
    public EncodedImage draw() {
        return draw(RequestContext.newEmpty());
    }

    @Override
    public @Nullable EncodedImage draw(RequestContext requestContext) {
        var eventContext = new ScriptSendEvent(requestContext, basePath);
        eventManager.trigger("send", eventContext);
        return eventContext.getResult();
    }

    public static PetpetJsScriptModel fromJsFile(File jsFile) {
        try {
            return new PetpetJsScriptModel(jsFile);
        } catch (ScriptException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable File getPreviewImage() {
        if (metadata == null) return null;
        if (metadata.getPreview() != null) {
            return basePath.toPath().resolve(metadata.getPreview()).toFile();
        }
        return null;
    }

    public class ScriptLogger {
        public final String name = basePath.getName();

        public void info(Object msg) {
            log.info(name + ": " + msg);
        }

        public void warn(Object msg) {
            log.warn(name + ": " + msg);
        }

        public void error(Object msg) {
            log.error(name + ": " + msg);
        }

        public void debug(Object msg) {
            log.debug(name + ": " + msg);
        }
    }
}
