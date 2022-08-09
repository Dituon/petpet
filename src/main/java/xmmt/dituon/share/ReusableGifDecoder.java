package xmmt.dituon.share;

import com.madgag.gif.fmsware.GifDecoder;

import java.io.BufferedInputStream;

/**
 * Class GifDecoder - Decodes a GIF file into one or more frames.
 *
 * Example:
 *
 * <pre>
 * {@code
 *    GifDecoder d = new GifDecoder();
 *    d.read("sample.gif");
 *    int n = d.getFrameCount();
 *    for (int i = 0; i < n; i++) {
 *       BufferedImage frame = d.getFrame(i);  // frame i
 *       int t = d.getDelay(i);  // display duration of frame in milliseconds
 *       // do something with frame
 *    }
 * }
 * </pre>
 * No copyright asserted on the source code of this class.  May be used for
 * any purpose, however, refer to the Unisys LZW patent for any additional
 * restrictions.  Please forward any corrections to questions at fmsware.com.
 *
 * @author Kevin Weiner, FM Software; LZW decoder adapted from John Cristy's ImageMagick.
 * @version 1.03 November 2003
 *
 */

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