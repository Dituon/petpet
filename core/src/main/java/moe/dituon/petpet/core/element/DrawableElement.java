package moe.dituon.petpet.core.element;

import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;

public interface DrawableElement {
    void draw(CanvasContext canvasContext, RequestContext requestContext);
}
