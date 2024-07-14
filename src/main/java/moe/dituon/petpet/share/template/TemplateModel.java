package moe.dituon.petpet.share.template;

import moe.dituon.petpet.share.BackgroundModel;
import moe.dituon.petpet.share.TextModel;
import moe.dituon.petpet.share.element.avatar.AvatarModel;

import java.util.List;

public class TemplateModel {
    protected List<AvatarModel> avatarList;
    protected List<TextModel> textList;
    protected BackgroundModel background;


    public TemplateModel(List<AvatarModel> avatarList, List<TextModel> textList) {
        this.avatarList = avatarList;
        this.textList = textList;
    }
}
