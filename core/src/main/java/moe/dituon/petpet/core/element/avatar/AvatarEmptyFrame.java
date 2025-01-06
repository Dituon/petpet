package moe.dituon.petpet.core.element.avatar;

import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.ElementFrame;
import moe.dituon.petpet.template.element.AvatarTemplate;

public class AvatarEmptyFrame extends AvatarFrame {
    public static final RenderedFrame RENDERED_FRAME = new RenderedFrame();

    public AvatarEmptyFrame(int index, AvatarTemplate data) {
        super(index, data);
    }

    @Override
    public void draw(CanvasContext canvasContext, RequestContext requestContext) {
    }

    @Override
    public void draw(CanvasContext canvasContext, RequestContext requestContext, int index) {
    }

    @Override
    public RenderedFrame render(CanvasContext canvasContext, RequestContext requestContext) {
        return RENDERED_FRAME;
    }

    public static class RenderedFrame extends ElementFrame.RenderedFrame {
        @Override
        public void draw() {
        }

        @Override
        public void draw(int customIndex) {
        }

        @Override
        public int getWidth() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public int getLength() {
            return 0;
        }

        @Override
        public ElementFrame.RenderedFrame cloneByIndex(int index) {
            return this;
        }
    }
}
