package moe.dituon.petpet.script.event;

import lombok.Getter;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.PetpetTemplateModel;
import moe.dituon.petpet.core.imgres.ImageResourceMap;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.script.ScriptObjectTransformerKt;
import moe.dituon.petpet.template.PetpetTemplate;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class ScriptSendEvent implements SendEvent {
    @Getter
    protected ScriptRequestContextWrapper request;
    protected final File basePath;
    @Getter
    protected EncodedImage result;

    public ScriptSendEvent(RequestContext requestContext, File basePath) {
        this.request = new ScriptRequestContextWrapper(requestContext);
        this.basePath = basePath;
    }

    @Override
    public EncodedImage generate(PetpetTemplate template, ScriptRequestContextWrapper requestData) {
        template.setBasePath(basePath);
        var model = new PetpetTemplateModel(template);
        if (requestData == null) {
            return model.draw(this.request.toRequestContext());
        }
        return model.draw(requestData.toRequestContext());
    }

    @Override
    public void result(EncodedImage image) {
        this.result = image;
    }

    @Override
    public void result(ScriptObjectMirror template) {
        var temp = PetpetTemplate.fromJsonElement(
                ScriptObjectTransformerKt.scriptObjectToJsonElement(template)
        );
        temp.setBasePath(basePath);
        result(generate(temp, null));
    }

    @Override
    public void result(String path) {
        try {
            // TODO: EncodedImage.fromFile
            this.result = new EncodedImage(
                    Files.readAllBytes(basePath.toPath().resolve(path)),
                    0, 0, "unknown"
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setRequest(ScriptRequestContextWrapper request) {
        this.request = request;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public void setRequest(ScriptObjectMirror obj) {
        this.request = new ScriptRequestContextWrapper(new RequestContext(
                ImageResourceMap.fromStringMap((Map<String, String>) obj.get("image")),
                (Map<String, String>) obj.get("text")
        ));
    }
}
