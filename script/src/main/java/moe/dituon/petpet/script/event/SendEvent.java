package moe.dituon.petpet.script.event;

import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.template.PetpetTemplate;
import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public interface SendEvent {
    ScriptRequestContextWrapper getRequest();

    void setRequest(ScriptRequestContextWrapper request);

    EncodedImage generate(PetpetTemplate template, @Nullable ScriptRequestContextWrapper requestData);

    void result(EncodedImage image);

    void result(ScriptObjectMirror template);

    void result(String path);
}
