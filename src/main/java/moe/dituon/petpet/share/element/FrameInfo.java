package moe.dituon.petpet.share.element;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

@Setter
@Getter
public class FrameInfo {
    public int index;
    public int canvasWidth;
    public int canvasHeight;
    public float multiple = 1.0F;

    public FrameInfo(int index, int canvasWidth, int canvasHeight){
        this.index = index;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static FrameInfo fromImage(BufferedImage image, int index){
        return new FrameInfo(image.getWidth(), image.getWidth(), index);
    }
}
