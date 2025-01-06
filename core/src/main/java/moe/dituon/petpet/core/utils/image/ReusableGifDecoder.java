package moe.dituon.petpet.core.utils.image;

import com.madgag.gif.fmsware.GifDecoder;

import java.io.IOException;

public class ReusableGifDecoder extends GifDecoder {
    @Override
    protected int[] readColorTable(int ncolors) {
        int nbytes = 3 * ncolors;
        int[] tab = null;
        byte[] c = new byte[nbytes];
        int bytesRead = 0;
        int offset = 0;

        try {
            while (offset < nbytes) {
                bytesRead = in.read(c, offset, nbytes - offset);
                if (bytesRead == -1) {
                    status = STATUS_FORMAT_ERROR;
                    return tab;
                }
                offset += bytesRead;
            }
        } catch (IOException e) {
            status = STATUS_FORMAT_ERROR;
        }
        tab = new int[256];
        int i = 0;
        int j = 0;
        while (i < ncolors) {
            int r = (c[j++]) & 0xff;
            int g = (c[j++]) & 0xff;
            int b = (c[j++]) & 0xff;
            tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
        }

        return tab;
    }
}
