package moe.dituon.petpet.share.template;

import lombok.Getter;
import moe.dituon.petpet.share.element.avatar.AvatarBuilder;
import moe.dituon.petpet.share.element.avatar.AvatarTemplate;
import moe.dituon.petpet.share.element.text.TextBuilder;
import moe.dituon.petpet.share.service.BackgroundResource;
import moe.dituon.petpet.share.template.background.BackgroundBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TemplateBuilder {
    public final PetpetTemplate templateData;
    @Getter
    protected final List<AvatarBuilder> avatarBuilders;
    @Getter
    protected final List<TextBuilder> textBuilders;
    @Getter
    protected final BackgroundBuilder backgroundBuilder;

    protected final Set<String> avatarTypeSet;

    public TemplateBuilder(PetpetTemplate templateData, BackgroundResource resource) {
        this.templateData = templateData;
        this.avatarTypeSet = templateData.getAvatar().stream().map(AvatarTemplate::getType).collect(Collectors.toSet());
        this.avatarBuilders = templateData.getAvatar().stream()
                .map(template -> new AvatarBuilder(
                        template,
                        resource == null ? null : resource.getBasePath()
                )).collect(Collectors.toList());
        this.textBuilders = templateData.getText().stream()
                .map(TextBuilder::new)
                .collect(Collectors.toList());
        this.backgroundBuilder = new BackgroundBuilder(resource, templateData.getBackground());
    }

    public BackgroundResource getBackgroundResource() {
        return this.backgroundBuilder.getResource();
    }

    public TemplateModel build(ExtraData data) {
        var avatarList = avatarBuilders.stream()
                .map(avatarBuilder -> avatarBuilder.build(data))
                .collect(Collectors.toList());
        var textList = textBuilders.stream()
                .map(textBuilder -> textBuilder.build(data))
                .collect(Collectors.toList());

        var background = backgroundBuilder.build(avatarList, textList);

        return new TemplateModel(data, background, avatarList, textList);
    }
}
