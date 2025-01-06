package moe.dituon.petpet.bot;

import lombok.Getter;
import moe.dituon.petpet.core.element.PetpetModel;
import moe.dituon.petpet.service.BaseService;
import moe.dituon.petpet.service.ObservableBaseService;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对于通用社交媒体的抽象 Bot 服务
 */
public class BotService extends ObservableBaseService {
    public static final String BOT_LOAD_EVENT_KEY = "bot_load";

    @Getter
    protected List<String> randomIdList = new ArrayList<>(256);
    protected int previousVersion = super.updateVersion;
    protected String indexString = null;
    @Getter
    @Nullable
    protected PetpetModel defaultTemplate = null;
    @Getter
    @Nullable
    protected String defaultTemplateId = null;
    protected boolean defaultTemplateLock = false;

    @Override
    public PetpetModel addTemplate(String id, PetpetModel model) {
        var prev = super.addTemplate(id, model);
        if (model.getMetadata() == null) {
            return prev;
        }
        if (model.getMetadata().getInRandomList()) {
            randomIdList.add(id);
        }
        if (!defaultTemplateLock && model.getMetadata().getDefaultTemplateWeight() != 0 && (this.defaultTemplate == null
                || (model.getMetadata().getDefaultTemplateWeight() > this.defaultTemplate.getMetadata().getDefaultTemplateWeight())
        )) {
            this.defaultTemplate = model;
            this.defaultTemplateId = id;
        }
        return prev;
    }

    public String randomId() {
        if (randomIdList.isEmpty()) {
            throw new IllegalStateException("No template in random list!");
        }
        return randomIdList.get(BaseService.RANDOM.nextInt(randomIdList.size()));
    }

    public PetpetModel randomTemplate() {
        return staticModelMap.get(randomId());
    }

    public String getIndexString() {
        if (indexString != null && previousVersion == updateVersion) {
            return indexString;
        }
        indexString = buildIndexString();
        previousVersion = updateVersion;
        return indexString;
    }

    protected String buildIndexString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, PetpetModel> entry : staticModelMap.entrySet()) {
            var model = entry.getValue();
            if (model.getMetadata() == null || model.getMetadata().getHidden()) {
                continue;
            }
            var alias = model.getMetadata().getAlias();
            if (alias.isEmpty()) {
                sb.append(entry.getKey()).append('\n');
                continue;
            }
            sb.append(entry.getKey()).append("  ( ").append(String.join(", ", alias)).append(" )\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 当全部模板加载完后调用以发布事件
     */
    @Override
    public void updateScriptService() {
        if (scriptModelMap.isEmpty()) return;
        var loadEvent = new ScriptBotLoadEvent(this);
        for (var model : scriptModelMap.values()) {
            if (model.getEventManager().has(BOT_LOAD_EVENT_KEY)) {
                model.getEventManager().trigger(BOT_LOAD_EVENT_KEY, loadEvent);
            } else {
                model.getEventManager().trigger(LOAD_EVENT_KEY, loadEvent);
            }
        }
    }
}
