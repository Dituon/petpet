package moe.dituon.petpet.core.position;

import java.util.Collections;
import java.util.Set;

public abstract class AvatarCoords {
    public enum CoordType {
        P4A, XYWH, EMPTY
    }

    public abstract CoordType getType();

    /**
     * value is context-independent
     */
    public boolean isAbsolute() {
        return true;
    }

    /**
     * The coordinates are relative to the canvas size <br>
     * e.g. coords containing <code>vw</code> <code>vh</code>
     */
    public boolean isDependsOnCanvasSize() {
        return false;
    }

    /**
     * The coordinates are relative to the element size <br>
     * e.g. coords containing <code>ew</code> <code>eh</code>
     */
    public boolean isDependsOnElementSize() {
        return false;
    }

    public Set<String> getDependentIds() {
        return Collections.emptySet();
    }

    /**
     * The coordinates are illegal or represent an area of 0
     */
    public boolean isEmpty() {
        return false;
    }
}
