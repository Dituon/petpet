package moe.dituon.petpet.service.event;

import lombok.Getter;
import moe.dituon.petpet.core.element.PetpetModel;
import moe.dituon.petpet.script.PetpetScriptModel;
import moe.dituon.petpet.service.TemplateManger;
import moe.dituon.petpet.template.Metadata;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScriptLoadEvent {
    @Getter
    public final List<TemplateInfo> templates;

    public ScriptLoadEvent(TemplateManger templateManger) {
        this.templates = templateManger.getStaticModelMap().entrySet().stream()
                .filter(e -> e.getValue().getMetadata() != null && !e.getValue().getMetadata().getHidden())
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new TemplateInfo(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @Getter
    public static class TemplateInfo {
        private final String id;
        private final String type;
        private final String preview;
        private final Metadata metadata;

        public TemplateInfo(String id, PetpetModel model) {
            this.id = id;
            this.type = model instanceof PetpetScriptModel ? "script" : "template";
            this.preview = model.getPreviewImage() == null ? "" : model.getPreviewImage().getPath();
            this.metadata = model.getMetadata();
        }
    }
}
