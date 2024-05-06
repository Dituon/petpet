package moe.dituon.petpet.share.element;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FrameInfo {
    public int index;
    public int canvasWidth;
    public int canvasHeight;
    public float multiple;

    public FrameInfo(int index, int canvasWidth, int canvasHeight){
        this.index = index;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
    }
}
