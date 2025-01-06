package moe.dituon.petpet.core.element;

import lombok.Getter;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import org.jetbrains.annotations.Nullable;

public interface ElementModel extends DrawableElement, Dependable {
    @Nullable
    String getId();

    Type getElementType();

    @Getter
    enum Type {
        AVATAR("avatar"),
        TEXT("text"),
        BACKGROUND("background");

        public final String name;

        Type(String name) {
            this.name = name;
        }
    }

    RenderedElement render(CanvasContext canvasContext, RequestContext requestContext);

    abstract class RenderedElement {
        public abstract void draw();

        public abstract int getWidth();

        public abstract int getHeight();

        public abstract int getLength();
    }
}
