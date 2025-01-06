package moe.dituon.petpet.core.element;

import java.util.Collections;
import java.util.Set;

public interface Dependable {
    /**
     * coords and position are absolute
     */
    boolean isAbsolute();

    default Set<String> getDependentIds() {
        return Collections.emptySet();
    }

    default boolean isDependsOnCanvasSize() {
        return false;
    }

    default boolean isDependsOnElementSize() {
        return false;
    }
}
