package moe.dituon.petpet.script.functions;

import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.PetpetTemplateModel;
import moe.dituon.petpet.core.imgres.ImageResourceMap;
import moe.dituon.petpet.script.ScriptObjectTransformerKt;
import moe.dituon.petpet.template.PetpetTemplate;
import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GenerateImageFunction extends FunctionObject {
    @Nullable
    public final File basePath;
    @Nullable
    public final RequestContext requestContext;

    public GenerateImageFunction(
            @Nullable File basePath,
            @Nullable RequestContext requestContext
    ) {
        this.requestContext = requestContext;
        this.basePath = basePath;
    }

    /**
     * <pre>
     * generate(template, requestData?)
     * </pre>
     */
    @Override
    public Object call(Object thiz, Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("generate() requires at least 1 argument");
        }
        if (!(args[0] instanceof ScriptObjectMirror)) {
            return null;
        }
        var template = PetpetTemplate.fromJsonElement(
                ScriptObjectTransformerKt.scriptObjectToJsonElement((ScriptObjectMirror) args[0])
        );
        template.setBasePath(basePath);
        var model = new PetpetTemplateModel(template);
        var request = this.requestContext;
        if (args.length >= 2 && args[1] instanceof ScriptObjectMirror) {
            var requestObject = (ScriptObjectMirror) args[1];
            var textObjectRaw = requestObject.get("text");
            Map<String, String> textMap;
            if (textObjectRaw instanceof ScriptObjectMirror) {
                textMap = jsObjectToMap((ScriptObjectMirror) textObjectRaw);
            } else {
                textMap = new HashMap<>();
            }
            var imageMapRaw = requestObject.get("image");
            Map<String, String> imageMap;
            if (imageMapRaw instanceof ScriptObjectMirror) {
                imageMap = jsObjectToMap((ScriptObjectMirror) imageMapRaw);
            } else {
                imageMap = new HashMap<>();
            }
            request = new RequestContext(
                    ImageResourceMap.fromStringMap(imageMap),
                    textMap
            );
        }
        if (request == null) {
            return model.draw();
        }
        return model.draw(request);
    }

    protected static Map<String, String> jsObjectToMap(ScriptObjectMirror object) {
        return object.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof String)
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()
                ));
    }
}
