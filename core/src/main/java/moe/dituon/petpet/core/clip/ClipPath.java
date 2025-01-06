package moe.dituon.petpet.core.clip;

import moe.dituon.petpet.core.length.LengthContext;

import java.awt.*;

public abstract class ClipPath {
    public abstract Shape getShape(RealPosition context);

    public static class RealPosition {
        public final int canvasWidth;
        public final int canvasHeight;
        public final int width;
        public final int height;
        public final int x;
        public final int y;

        public RealPosition(int canvasWidth, int canvasHeight, int x, int y, int width, int height) {
            this.canvasWidth = canvasWidth;
            this.canvasHeight = canvasHeight;
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
        }

        public LengthContext toLengthContext() {
            return new LengthContext(this.canvasWidth, this.canvasHeight, this.width, this.height);
        }

        public static RealPosition fromLengthContext(LengthContext context, int x, int y) {
            return new RealPosition(context.canvasWidth, context.canvasHeight, x, y, context.elementWidth, context.elementHeight);
        }

        public static RealPosition fromLengthContext(LengthContext context) {
            return fromLengthContext(context, 0, 0);
        }
    }
}
