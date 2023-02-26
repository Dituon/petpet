package moe.dituon.petpet.share;

import com.madgag.gif.fmsware.GifDecoder;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class ReusableGifDecoder extends GifDecoder {

    /**
     * 不会关闭BufferedInputStream, 便于复用
     */
    @Override
    public int read(InputStream is) {
        init();
        if (is != null) {
            if (!(is instanceof BufferedInputStream)) {
                is = new BufferedInputStream(is);
            }
            super.in = (BufferedInputStream) is;
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
    public boolean err() {
        return super.err();
    }
}