package moe.dituon.petpet.core.element;

import lombok.Getter;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;

import java.util.List;

@Getter
public abstract class ElementFrame {
    protected int index;

    public abstract void draw(
            CanvasContext canvasContext,
            RequestContext requestContext
    );

    public abstract void draw(
            CanvasContext canvasContext,
            RequestContext requestContext,
            int index
    );

    public abstract RenderedFrame render(
            CanvasContext canvasContext,
            RequestContext requestContext
    );

    protected float getNElement(float[] array) {
        return array[this.index % array.length];
    }

    protected <T> T getNElement(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(this.index % list.size());
    }

    public static float getNElement(float[] array, int index) {
        return array[index % array.length];
    }

    public static <T> T getNElement(List<T> list, int index) {
        if (list == null || list.isEmpty()) return null;
        return list.get(index % list.size());
    }

    public abstract static class RenderedFrame {
        public abstract void draw();
        public abstract void draw(int index);

        public abstract int getWidth();
        public abstract int getHeight();

        /**
         * Get the length of the frame image.<br/>
         * e.t.c. return gif image length.
         */
        public abstract int getLength();

        /**
         * Constructs a new RenderedFrame object by copying properties from another RenderedFrame object and cropping an image.
         * This constructor is used to create a new instance by referencing an existing RenderedFrame object while focusing on a specific frame index.
         *
         * @param index The index of the frame to focus on.
         */
        public abstract RenderedFrame cloneByIndex(int index);
    }
}