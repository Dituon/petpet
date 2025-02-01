package moe.dituon.petpet.bot.qq;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.bot.BotSendEvent;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.script.PetpetScriptModel;
import moe.dituon.petpet.script.event.EventManager;
import moe.dituon.petpet.template.Metadata;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;

@Getter
@Slf4j
public class TemplateIndexScriptModel implements PetpetScriptModel {
    public final Metadata metadata = new Metadata(
            GlobalContext.API_VERSION, 0,
            Collections.emptyList(), Collections.emptyList(),
            "", "",
            true, false, 1,
            null
    );
    public final EventManager eventManager = new EventManager();

    public TemplateIndexScriptModel(QQBotService service) {
        switch (service.config.getDefaultReplyType()) {
            case TEXT:
                eventManager.on("bot_send", e -> {
                    ((BotSendEvent) e).response(service.getIndexString());
                });
                break;
            case TEMPLATE:
            case FORWARD_TEXT:
                eventManager.on("bot_send", e -> {
                    ((BotSendEvent) e).responseInForward(true);
                    ((BotSendEvent) e).response(service.getIndexString());
                });
                break;
        }
    }

    @Override
    public EncodedImage draw(RequestContext requestContext) {
        return null;
    }

    @Override
    public void setMetadata(Metadata metadata) {
    }

    @Override
    public @Nullable File getPreviewImage() {
        return null;
    }
}
