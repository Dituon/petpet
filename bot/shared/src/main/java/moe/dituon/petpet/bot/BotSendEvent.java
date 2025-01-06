package moe.dituon.petpet.bot;

import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.script.event.ScriptSendEvent;
import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.File;


public abstract class BotSendEvent extends ScriptSendEvent {
    protected boolean isResponseInForward = false;

    protected BotSendEvent(RequestContext requestContext, @Nullable File basePath) {
        super(requestContext, basePath);
    }

    public abstract void responseNewParagraph();

    public abstract void responseInForward(boolean flag);

    public abstract void response(String text);

    public abstract void responseImage(EncodedImage image);

    public void responseImage(ScriptObjectMirror template){
        super.result(template);
        responseImage(super.result);
    }

    public void responseImage(String path){
        super.result(path);
        responseImage(super.result);
    }
}
