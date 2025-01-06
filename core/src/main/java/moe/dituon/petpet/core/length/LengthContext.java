package moe.dituon.petpet.core.length;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Map;

public class LengthContext {
    public static final LengthContext EMPTY = new LengthContext(0, 0, 0, 0);
    public final int canvasWidth;
    public final int canvasHeight;
    public final int elementWidth;
    public final int elementHeight;
    @Setter
    @Getter
    public Map<String, Integer> variables = Collections.emptyMap();

    public LengthContext(
            int canvasWidth,
            int canvasHeight,
            int elementWidth,
            int elementHeight
    ) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.elementWidth = elementWidth;
        this.elementHeight = elementHeight;
    }

    public LengthContext createContext(int elementWidth, int elementHeight) {
        return new LengthContext(canvasWidth, canvasHeight, elementWidth, elementHeight);
    }
}
