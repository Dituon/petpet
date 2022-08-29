package xmmt.dituon.share;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.NeuQuant;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.OutputStream;

public class FastAnimatedGifEncoder extends AnimatedGifEncoder {
    /**
     * 等效于父类的addFrame, 单线程处理BufferedImage(构建FrameData)
     *
     * @deprecated 应当在多线程环境中预构建FrameData
     */
    @Deprecated
    @Override
    public boolean addFrame(BufferedImage image) {
        FrameData frame = new FrameData(image, (byte) sample);
        addFrame(frame);
        return true;
    }

    public void addFrame(FrameData frame) {
        try {
            pixels = frame.pixels;
//            getImagePixels(); // convert to correct format if necessary
            analyzePixels(frame); // build color table & map pixels
            if (firstFrame) {
                writeLSD(); // logical screen descriptior
                writePalette(); // global color table
                if (repeat >= 0) {
                    // use NS app extension to indicate reps
                    writeNetscapeExt();
                }
            }
            writeGraphicCtrlExt(); // write graphic control extension
            writeImageDesc(); // image descriptor
            if (!firstFrame) {
                writePalette(); // local color table
            }
//            writePixels(); // encode and write pixel data
            frame.encoder.encode(out);
            firstFrame = false;
        } catch (IOException ignored) {
        }
    }

    protected void analyzePixels(FrameData frame) {
        colorTab = frame.colorTab; // create reduced palette
        usedEntry = frame.usedEntry;
        indexedPixels = frame.indexedPixels;

        pixels = null;
        palSize = 7;
        // get closest match to transparent color if specified
        if (transparent != null) {
            transIndex = transparentExactMatch ? findExact(transparent) : findClosest(transparent);
        }
    }

    public static class FrameData {
        public byte[] pixels;
        public byte[] colorTab;
        public NeuQuant neuQuant;
        byte[] indexedPixels;
        boolean[] usedEntry = new boolean[256];
        LZWEncoder encoder;

        FrameData(BufferedImage image, byte quality) {
            pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            neuQuant = new NeuQuant(pixels, pixels.length, quality < 1 ? 1 : quality);
            colorTab = neuQuant.process();
            // convert map from BGR to RGB
            for (int i = 0; i < colorTab.length; i += 3) {
                byte temp = colorTab[i];
                colorTab[i] = colorTab[i + 2];
                colorTab[i + 2] = temp;
                usedEntry[i / 3] = false;
            }

            int nPix = pixels.length / 3;
            indexedPixels = new byte[nPix];

            int k = 0;
            for (int i = 0; i < nPix; i++) {
                int index =
                        neuQuant.map(pixels[k++] & 0xff,
                                pixels[k++] & 0xff,
                                pixels[k++] & 0xff);
                usedEntry[index] = true;
                indexedPixels[i] = (byte) index;
            }

            encoder = new LZWEncoder(image.getWidth(), image.getHeight(), indexedPixels, 8);
        }
    }

    private static class LZWEncoder {
        //==============================================================================
//  Adapted from Jef Poskanzer's Java port by way of J. M. G. Elliott.
//  K Weiner 12/00

        private static final int EOF = -1;
        private final int imgW, imgH;
        private final byte[] pixAry;
        private final int initCodeSize;
        private int remaining;
        private int curPixel;

        // GIFCOMPR.C       - GIF Image compression routines
        //
        // Lempel-Ziv compression based on 'compress'.  GIF modifications by
        // David Rowley (mgardi@watdcsu.waterloo.edu)

        // General DEFINEs

        static final int BITS = 12;

        static final int HSIZE = 5003; // 80% occupancy

        // GIF Image compression - modified 'compress'
        //
        // Based on: compress.c - File compression ala IEEE Computer, June 1984.
        //
        // By Authors:  Spencer W. Thomas      (decvax!harpo!utah-cs!utah-gr!thomas)
        //              Jim McKie              (decvax!mcvax!jim)
        //              Steve Davies           (decvax!vax135!petsd!peora!srd)
        //              Ken Turkowski          (decvax!decwrl!turtlevax!ken)
        //              James A. Woods         (decvax!ihnp4!ames!jaw)
        //              Joe Orost              (decvax!vax135!petsd!joe)

        int n_bits; // number of bits/code
        int maxbits = BITS; // user settable max # bits/code
        int maxcode; // maximum code, given n_bits
        int maxmaxcode = 1 << BITS; // should NEVER generate this code

        int[] htab = new int[HSIZE];
        int[] codetab = new int[HSIZE];

        int hsize = HSIZE; // for dynamic table sizing

        int free_ent = 0; // first unused entry

        // block compression parameters -- after all codes are used up,
        // and compression rate changes, start over.
        boolean clear_flg = false;
        int g_init_bits;

        int ClearCode;
        int EOFCode;

        // output
        //
        // Output the given code.
        // Inputs:
        //      code:   A n_bits-bit integer.  If == -1, then EOF.  This assumes
        //              that n_bits =< wordsize - 1.
        // Outputs:
        //      Outputs code to the file.
        // Assumptions:
        //      Chars are 8 bits long.
        // Algorithm:
        //      Maintain a BITS character long buffer (so that 8 codes will
        // fit in it exactly).  Use the VAX insv instruction to insert each
        // code in turn.  When the buffer fills up empty it and start over.

        int cur_accum = 0;
        int cur_bits = 0;

        int[] masks =
                {
                        0x0000,
                        0x0001,
                        0x0003,
                        0x0007,
                        0x000F,
                        0x001F,
                        0x003F,
                        0x007F,
                        0x00FF,
                        0x01FF,
                        0x03FF,
                        0x07FF,
                        0x0FFF,
                        0x1FFF,
                        0x3FFF,
                        0x7FFF,
                        0xFFFF};

        // Number of characters so far in this 'packet'
        int a_count;

        // Define the storage for the packet accumulator
        byte[] accum = new byte[256];

        //----------------------------------------------------------------------------
        LZWEncoder(int width, int height, byte[] pixels, int color_depth) {
            imgW = width;
            imgH = height;
            pixAry = pixels;
            initCodeSize = Math.max(2, color_depth);
        }

        // Add a character to the end of the current packet, and if it is 254
        // characters, flush the packet to disk.
        void char_out(byte c, OutputStream outs) throws IOException {
            accum[a_count++] = c;
            if (a_count >= 254)
                flush_char(outs);
        }

        // Clear out the hash table

        // table clear for block compress
        void cl_block(OutputStream outs) throws IOException {
            cl_hash(hsize);
            free_ent = ClearCode + 2;
            clear_flg = true;

            output(ClearCode, outs);
        }

        // reset code table
        void cl_hash(int hsize) {
            for (int i = 0; i < hsize; ++i)
                htab[i] = -1;
        }

        void compress(int init_bits, OutputStream outs) throws IOException {
            int fcode;
            int i /* = 0 */;
            int c;
            int ent;
            int disp;
            int hsize_reg;
            int hshift;

            // Set up the globals:  g_init_bits - initial number of bits
            g_init_bits = init_bits;

            // Set up the necessary values
            clear_flg = false;
            n_bits = g_init_bits;
            maxcode = MAXCODE(n_bits);

            ClearCode = 1 << (init_bits - 1);
            EOFCode = ClearCode + 1;
            free_ent = ClearCode + 2;

            a_count = 0; // clear packet

            ent = nextPixel();

            hshift = 0;
            for (fcode = hsize; fcode < 65536; fcode *= 2)
                ++hshift;
            hshift = 8 - hshift; // set hash code range bound

            hsize_reg = hsize;
            cl_hash(hsize_reg); // clear hash table

            output(ClearCode, outs);

            outer_loop:
            while ((c = nextPixel()) != EOF) {
                fcode = (c << maxbits) + ent;
                i = (c << hshift) ^ ent; // xor hashing

                if (htab[i] == fcode) {
                    ent = codetab[i];
                    continue;
                } else if (htab[i] >= 0) // non-empty slot
                {
                    disp = hsize_reg - i; // secondary hash (after G. Knott)
                    if (i == 0)
                        disp = 1;
                    do {
                        if ((i -= disp) < 0)
                            i += hsize_reg;

                        if (htab[i] == fcode) {
                            ent = codetab[i];
                            continue outer_loop;
                        }
                    } while (htab[i] >= 0);
                }
                output(ent, outs);
                ent = c;
                if (free_ent < maxmaxcode) {
                    codetab[i] = free_ent++; // code -> hashtable
                    htab[i] = fcode;
                } else
                    cl_block(outs);
            }
            // Put out the final code.
            output(ent, outs);
            output(EOFCode, outs);
        }

        //----------------------------------------------------------------------------
        void encode(OutputStream os) throws IOException {
            os.write(initCodeSize); // write "initial code size" byte

            remaining = imgW * imgH; // reset navigation variables
            curPixel = 0;

            compress(initCodeSize + 1, os); // compress and write the pixel data

            os.write(0); // write block terminator
        }

        // Flush the packet to disk, and reset the accumulator
        void flush_char(OutputStream outs) throws IOException {
            if (a_count > 0) {
                outs.write(a_count);
                outs.write(accum, 0, a_count);
                a_count = 0;
            }
        }

        final int MAXCODE(int n_bits) {
            return (1 << n_bits) - 1;
        }

        //----------------------------------------------------------------------------
        // Return the next pixel from the image
        //----------------------------------------------------------------------------
        private int nextPixel() {
            if (remaining == 0)
                return EOF;

            --remaining;

            byte pix = pixAry[curPixel++];

            return pix & 0xff;
        }

        void output(int code, OutputStream outs) throws IOException {
            cur_accum &= masks[cur_bits];

            if (cur_bits > 0)
                cur_accum |= (code << cur_bits);
            else
                cur_accum = code;

            cur_bits += n_bits;

            while (cur_bits >= 8) {
                char_out((byte) (cur_accum & 0xff), outs);
                cur_accum >>= 8;
                cur_bits -= 8;
            }

            // If the next entry is going to be too big for the code size,
            // then increase it, if possible.
            if (free_ent > maxcode || clear_flg) {
                if (clear_flg) {
                    maxcode = MAXCODE(n_bits = g_init_bits);
                    clear_flg = false;
                } else {
                    ++n_bits;
                    if (n_bits == maxbits)
                        maxcode = maxmaxcode;
                    else
                        maxcode = MAXCODE(n_bits);
                }
            }

            if (code == EOFCode) {
                // At EOF, write the rest of the buffer.
                while (cur_bits > 0) {
                    char_out((byte) (cur_accum & 0xff), outs);
                    cur_accum >>= 8;
                    cur_bits -= 8;
                }

                flush_char(outs);
            }
        }
    }
}
