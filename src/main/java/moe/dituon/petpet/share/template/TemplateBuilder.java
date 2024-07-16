package moe.dituon.petpet.share.template;

import lombok.Getter;
import moe.dituon.petpet.share.TemplateDTO;
import moe.dituon.petpet.share.element.text.TextBuilder;
import moe.dituon.petpet.share.service.BackgroundResource;
import moe.dituon.petpet.share.element.avatar.AvatarBuilder;
import moe.dituon.petpet.share.template.background.BackgroundFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TemplateBuilder {
    @Getter
    protected final List<AvatarBuilder> avatarBuilders;
    @Getter
    protected final List<TextBuilder> textBuilders;
    @Getter
    protected final BackgroundFactory backgroundBuilder;

    protected final Set<String> avatarTypeSet;

    public TemplateBuilder(PetpetTemplate templateData, BackgroundResource resource) {
        this.avatarTypeSet = templateData.getAvatar().stream().map(data -> data.getType().toString()).collect(Collectors.toSet());
        this.avatarBuilders = templateData.getAvatar().stream()
                .map(AvatarBuilder::new)
                .collect(Collectors.toList());
        this.textBuilders = templateData.getText().stream()
                .map(TextBuilder::new)
                .collect(Collectors.toList());
        this.backgroundBuilder = new BackgroundFactory(resource, templateData.getBackground());
    }

    public TemplateModel build(ExtraData data) {
        if (!data.getAvatar().keySet().containsAll(avatarTypeSet)) {
            throw new RuntimeException("missing some type");
        }

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
