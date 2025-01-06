package moe.dituon.petpet.bot;

import moe.dituon.petpet.service.event.ScriptLoadEvent;

public class ScriptBotLoadEvent extends ScriptLoadEvent {
    public final String defaultTemplate;

    public ScriptBotLoadEvent(BotService botService) {
        super(botService);
        defaultTemplate = botService.defaultTemplateId;
    }
}
