package xmmt.dituon.share;

import com.madgag.gif.fmsware.GifDecoder;

import java.io.BufferedInputStream;

public class ReusableGifDecoder extends GifDecoder {

    /**
     * 不会关闭BufferedInputStream, 便于复用
     */
    @Override
    public int read(BufferedInputStream is) {
        init();
        if (is != null) {
            super.in = is;
            readHeader();
            if (!err()) {
                readContents();
                if (super.frameCount < 0) {
                    super.status = STATUS_FORMAT_ERROR;
                }
            }
        } else {
            super.status = STATUS_OPEN_ERROR;
        }
        return super.status;
    }

    @Override
    public boolean err(){
        return super.err();
    }
}