package moe.dituon.petpet.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.element.PetpetModel;
import moe.dituon.petpet.core.element.PetpetTemplateModel;
import moe.dituon.petpet.old_template.OldPetpetTemplate;
import moe.dituon.petpet.script.PetpetJsScriptModel;
import moe.dituon.petpet.script.PetpetScriptModel;
import moe.dituon.petpet.service.event.ScriptLoadEvent;
import moe.dituon.petpet.template.Metadata;
import moe.dituon.petpet.template.PetpetTemplate;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class TemplateManger {
    public static final String LOAD_EVENT_KEY = "load";

    public enum ElementType {
        SCRIPT("main.js"),
        TEMPLATE("template.json"),
        OLD_TEMPLATE("data.json"),
        UNKNOWN("");

        public final String mainFile;

        ElementType(String mainFile) {
            this.mainFile = mainFile;
        }

        public static ElementType getPathType(File path) {
            if (!path.exists() || !path.isDirectory()) return UNKNOWN;
            String[] files = path.list();
            if (files == null || files.length == 0) return UNKNOWN;
            var set = Set.of(files);

            if (set.contains(SCRIPT.mainFile)) {
                return SCRIPT;
            } else if (set.contains(TEMPLATE.mainFile)) {
                return TEMPLATE;
            } else if (set.contains(OLD_TEMPLATE.mainFile)) {
                return OLD_TEMPLATE;
            } else {
                return UNKNOWN;
            }
        }
    }

    @Getter
    protected final Map<String, PetpetModel> staticModelMap = new HashMap<>(256);
    @Getter
    protected final Map<String, PetpetScriptModel> scriptModelMap = new HashMap<>(32);

    protected TemplateManger() {
    }

    public TemplateManger addTemplates(File basePath) {
        if (!basePath.exists() || !basePath.isDirectory()) return this;
        var files = basePath.listFiles();
        if (files == null || files.length == 0) return this;
        return addTemplates(List.of(files));
    }

    public TemplateManger addTemplates(Iterable<File> paths) {
        for (var path : paths) {
            try {
                this.addTemplate(path);
            } catch (Exception ex) {
                log.warn("Failed to load template {}", path.getName(), ex);
            }
        }
        return this;
    }

    public @Nullable PetpetModel addTemplate(String id, PetpetTemplate template) {
        return addTemplate(id, new PetpetTemplateModel(template));
    }

    public @Nullable PetpetModel addTemplate(String id, PetpetModel model) {
        checkTemplateApiVersion(id, model.getMetadata());
        if (model instanceof PetpetScriptModel) {
            if (model.getMetadata() == null) return null;
            this.scriptModelMap.put(id, (PetpetScriptModel) model);
        }
        return staticModelMap.put(id, model);
    }

    protected void checkTemplateApiVersion(String id, @Nullable Metadata metadata) {
        if (metadata == null) return;
        if (metadata.getApiVersion() > GlobalContext.API_VERSION) {
            log.warn(
                    "Template '{}' version {} is higher than the current API version {}",
                    id,
                    metadata.getApiVersion(),
                    GlobalContext.API_VERSION
            );
        }
    }

    public @Nullable PetpetModel addTemplate(File path) {
        var type = ElementType.getPathType(path);
        var id = path.getName();
        switch (type) {
            case TEMPLATE: {
                var mainFile = path.toPath().resolve(ElementType.TEMPLATE.mainFile).toFile();
                var template = PetpetTemplate.fromJsonFile(mainFile);
                return this.addTemplate(id, template);
            }
            case OLD_TEMPLATE: {
                var mainFile = path.toPath().resolve(ElementType.OLD_TEMPLATE.mainFile).toFile();
                var template = OldPetpetTemplate.fromJsonFile(mainFile).toTemplate();
                return this.addTemplate(id, template);
            }
            case SCRIPT: {
                var mainFile = path.toPath().resolve(ElementType.SCRIPT.mainFile).toFile();
                try {
                    var model = new PetpetJsScriptModel(mainFile);
                    return this.addTemplate(id, model);
                } catch (IOException | ScriptException ex) {
                    log.warn("Failed to load script {}: {}", id, mainFile, ex);
                }
                return null;
            }
            default:
                return null;
        }
    }

    public @Nullable PetpetModel getTemplate(String id) {
        return staticModelMap.get(id);
    }

    public @Nullable PetpetModel removeTemplate(String id) {
        scriptModelMap.remove(id);
        return staticModelMap.remove(id);
    }

    public void clear() {
        staticModelMap.clear();
        scriptModelMap.clear();
    }

    public void updateScriptService() {
        if (scriptModelMap.isEmpty()) return;
        var loadEvent = new ScriptLoadEvent(this);
        for (var model : scriptModelMap.values()) {
            model.getEventManager().trigger(LOAD_EVENT_KEY, loadEvent);
        }
    }
}
