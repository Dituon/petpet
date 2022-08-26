package xmmt.dituon.share;

import com.madgag.gif.fmsware.AnimatedGifEncoder;

import java.awt.image.DataBufferByte;

public class FastAnimatedGifEncoder extends AnimatedGifEncoder {

    @Override
    protected void getImagePixels() { //Type由图片处理器负责多线程转换
        super.pixels = ((DataBufferByte) super.image.getRaster().getDataBuffer()).getData();
    }
}
