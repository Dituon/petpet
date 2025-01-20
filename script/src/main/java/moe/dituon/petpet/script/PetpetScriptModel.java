package moe.dituon.petpet.script;

import moe.dituon.petpet.core.element.PetpetModel;
import moe.dituon.petpet.script.event.EventManager;
import moe.dituon.petpet.template.Metadata;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface PetpetScriptModel extends PetpetModel {
    /**
     * if metadata is null, the script is not registered
     */
    @Nullable Metadata getMetadata();
    EventManager getEventManager();
    default @Nullable File getBasePath() {
        return null;
    }
}
