/*
NeuQuant Neural-Net Quantization Algorithm by Anthony Dekker, 1994.
See "Kohonen neural networks for optimal colour quantization"
in "Network: Computation in Neural Systems" Vol. 5 (1994) pp 351-367.
for a discussion of the algorithm.
See also http://members.ozemail.com.au/~dekker/NEUQUANT.HTML

Incorporated bugfixes and alpha channel handling from pngnq
http://pngnq.sourceforge.net

Copyright (c) 2014 The Piston Developers

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

NeuQuant Neural-Net Quantization Algorithm
------------------------------------------

Copyright (c) 1994 Anthony Dekker

NEUQUANT Neural-Net quantization algorithm by Anthony Dekker, 1994.
See "Kohonen neural networks for optimal colour quantization"
in "Network: Computation in Neural Systems" Vol. 5 (1994) pp 351-367.
for a discussion of the algorithm.
See also  http://members.ozemail.com.au/~dekker/NEUQUANT.HTML

Any party obtaining a copy of these files from the author, directly or
indirectly, is granted, free of charge, a full and unrestricted irrevocable,
world-wide, paid up, royalty-free, nonexclusive right and license to deal
in this software and documentation files (the "Software"), including without
limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons who receive
copies from any such party to do so, with the only requirement being
that this copyright notice remain intact.

*/

package moe.dituon.petpet.core.utils.image;

import java.awt.*;

public class NeuQuantABGR {
    protected static final int NETSIZE = 256;
    private static final int CHANNELS = 4;

    private static final int RADIUS_DEC = 30; // factor of 1/30 each cycle

    private static final int ALPHA_BIASSHIFT = 10; // alpha starts at 1
    private static final int INIT_ALPHA = 1 << ALPHA_BIASSHIFT; // biased by 10 bits

    private static final float GAMMA = 1024f;
    private static final float BETA = 1f / GAMMA;
    private static final float BETAGAMMA = BETA * GAMMA;

    private static final int[] PRIMES = {499, 491, 478, 503};

    private float[] network = new float[CHANNELS * NETSIZE];
    private byte[] colormap = new byte[CHANNELS * NETSIZE];
    private int[] netindex = new int[256];
    private float[] bias = new float[NETSIZE]; // bias and freq arrays for learning
    private float[] freq = new float[NETSIZE];
    private int samplefac;

    public NeuQuantABGR(int samplefac, byte[] pixels) {
        this.samplefac = samplefac;

        float freq = 1.0f / NETSIZE;
        for (int i = 0; i < NETSIZE; i++) {
            float tmp = (i * 256f) / NETSIZE;
            float a = (i < 16) ? i * 16f : 255f;
            int ni = CHANNELS * i;
            this.network[ni + 3] = tmp;
            this.network[ni + 2] = tmp;
            this.network[ni + 1] = tmp;
            this.network[ni] = a;
            this.colormap[ni] = (byte) 255;
            this.freq[i] = freq;
        }

        learn(pixels);
        buildColormap();
        buildNetindex();
    }

    public void learn(byte[] pixels) {
        int initrad = NETSIZE / 8; // for 256 cols, radius starts at 32
        int radiusbiasshift = 6;
        int radiusbias = 1 << radiusbiasshift;
        int biasRadius = initrad * radiusbias;
        int alphadec = 30 + ((samplefac - 1) / 3);
        int lengthcount = pixels.length / CHANNELS;
        int samplepixels = lengthcount / samplefac;

        // learning cycles
        int nCycles = Math.max(NETSIZE >> 1, 100);
        int delta = Math.max(samplepixels / nCycles, 1);
        int alpha = INIT_ALPHA;

        int rad = biasRadius >> radiusbiasshift;
        if (rad <= 1) {
            rad = 0;
        }

        int pos = 0;
        int step = findStep(lengthcount);

        int i = 0;
        while (i < samplepixels) {
            // Extract RGBA values
            int r = pixels[CHANNELS * pos + 3] & 0xFF;
            int g = pixels[CHANNELS * pos + 2] & 0xFF;
            int b = pixels[CHANNELS * pos + 1] & 0xFF;
            int a = pixels[CHANNELS * pos] & 0xFF;

            // Perform the contest operation
            int j = contest(b, g, r, a);

            float alpha_ = (1f * alpha) / INIT_ALPHA;
            salterSingle(alpha_, j, b, g, r, a);

            if (rad > 0) {
                alterNeighbour(alpha_, rad, j, b, g, r, a);
            }

            pos += step;
            if (pos >= lengthcount) {
                pos -= lengthcount;
            }

            i++;
            if (i % delta == 0) {
                alpha -= alpha / alphadec;
                biasRadius -= biasRadius / RADIUS_DEC;
                rad = biasRadius >> radiusbiasshift;
                if (rad <= 1) {
                    rad = 0;
                }
            }
        }
    }

    // Move neuron i towards biased (a,b,g,r) by factor alpha
    private void salterSingle(float alpha, int i, int b, int g, int r, int a) {
        int ni = CHANNELS * i;
        network[ni + 3] -= alpha * (network[ni + 3] - r); // Red
        network[ni + 2] -= alpha * (network[ni + 2] - g); // Green
        network[ni + 1] -= alpha * (network[ni + 1] - b); // Blue
        network[ni] -= alpha * (network[ni] - a); // Alpha
    }

    // Move neuron adjacent neurons towards biased (a,b,g,r) by factor alpha
    private void alterNeighbour(float alpha, int rad, int i, int b, int g, int r, int a) {
        int lo = Math.max(i - rad, 0);
        int hi = Math.min(i + rad, NETSIZE);
        int j = i + 1;
        int k = i - 1;
        int q = 0;

        while (j < hi || k > lo) {
            float radSq = (rad * rad);
            float alphaFactor = (alpha * (radSq - (q * q))) / radSq;
            q++;
            if (j < hi) {
                int ni = CHANNELS * j;
                network[ni + 3] -= alphaFactor * (network[ni + 3] - r); // Red
                network[ni + 2] -= alphaFactor * (network[ni + 2] - g); // Green
                network[ni + 1] -= alphaFactor * (network[ni + 1] - b); // Blue
                network[ni] -= alphaFactor * (network[ni] - a); // Alpha
                j++;
            }
            if (k > lo) {
                int ni = CHANNELS * k;
                network[ni + 3] -= alphaFactor * (network[ni + 3] - r);
                network[ni + 2] -= alphaFactor * (network[ni + 2] - g);
                network[ni + 1] -= alphaFactor * (network[ni + 1] - b);
                network[ni] -= alphaFactor * (network[ni] - a);
                k--;
            }
        }
    }

    private int contest(float b, float g, float r, float a) {
        float bestd = Float.MAX_VALUE;
        float bestbiasd = bestd;
        int bestpos = -1;
        int bestbiaspos = bestpos;

        for (int i = 0; i < NETSIZE; i++) {
            int ci = CHANNELS * i;
            float bestbiasdBiased = bestbiasd + bias[i];
            float dist = Math.abs(network[ci + 1] - b)
                    + Math.abs(network[ci + 3] - r);

            if (dist < bestd || dist < bestbiasdBiased) {
                dist += Math.abs(network[ci + 2] - g)
                        + Math.abs(network[ci] - a);

                if (dist < bestd) {
                    bestd = dist;
                    bestpos = i;
                }

                float biasdist = dist - bias[i];
                if (biasdist < bestbiasd) {
                    bestbiasd = biasdist;
                    bestbiaspos = i;
                }
            }

            // Update frequency and bias
            freq[i] -= BETA * freq[i];
            bias[i] += BETAGAMMA * freq[i];
        }

        // Final updates for best position
        freq[bestpos] += BETA;
        bias[bestpos] -= BETAGAMMA;

        return bestbiaspos;
    }


    private int findStep(int lengthcount) {
        for (int prime : PRIMES) {
            if (lengthcount % prime != 0) {
                return prime;
            }
        }
        return PRIMES[3]; // Fallback to default prime
    }

    // Build colormap from the network
    public void buildColormap() {
        for (int i = 0; i < NETSIZE; i++) {
            int ci = CHANNELS * i;
            int r = Math.round(network[ci + 3]);
            int g = Math.round(network[ci + 2]);
            int b = Math.round(network[ci + 1]);
            int a = Math.round(network[ci]);
            if (((r | g | b | a) & ~0xFF) != 0) {
                r = (r & ~0xFF) != 0 ? (((~r) >> 31) & 0xFF) : r;
                g = (g & ~0xFF) != 0 ? (((~g) >> 31) & 0xFF) : g;
                b = (b & ~0xFF) != 0 ? (((~b) >> 31) & 0xFF) : b;
                a = (a & ~0xFF) != 0 ? (((~a) >> 31) & 0xFF) : a;
            }
            colormap[ci + 3] = (byte) r;
            colormap[ci + 2] = (byte) g;
            colormap[ci + 1] = (byte) b;
            colormap[ci] = (byte) a;
        }
    }

    // Insertion sort of network and building of netindex[0..255]
    public void buildNetindex() {
        int previouscol = 0;
        int startpos = 0;

        for (int i = 0; i < NETSIZE; i++) {
            int ci = CHANNELS * i;
            byte pr = colormap[ci + 3];
            byte pg = colormap[ci + 2];
            byte pb = colormap[ci + 1];
            byte pa = colormap[ci];
            byte qr, qg, qb, qa;
            int smallpos = i;
            int smallval = pg & 0xFF; // index on g

            // Find smallest in i..netsize-1
            for (int j = i + 1; j < NETSIZE; j++) {
                int ji = CHANNELS * j;
                qg = colormap[ji + 2];
                if ((qg & 0xFF) < smallval) {
                    smallpos = j;
                    smallval = qg & 0xFF; // index on g
                }
            }

            int jsp = CHANNELS * smallpos;
            qr = colormap[jsp + 3];
            qg = colormap[jsp + 2];
            qb = colormap[jsp + 1];
            qa = colormap[jsp];
            // Swap p (i) and q (smallpos) entries
            if (i != smallpos) {
                colormap[ci + 3] = qr;
                colormap[ci + 2] = qg;
                colormap[ci + 1] = qb;
                colormap[ci] = qa;
                colormap[jsp + 3] = pr;
                colormap[jsp + 2] = pg;
                colormap[jsp + 1] = pb;
                colormap[jsp] = pa;
            }

            // smallval entry is now in position i
            if (smallval != previouscol) {
                netindex[previouscol] = (startpos + i) >> 1;
                for (int j = previouscol + 1; j < smallval; j++) {
                    netindex[j] = i;
                }
                previouscol = smallval;
                startpos = i;
            }
        }

        int maxNetpos = NETSIZE - 1;
        netindex[previouscol] = (startpos + maxNetpos) >> 1;
        for (int j = previouscol + 1; j < 256; j++) {
            netindex[j] = maxNetpos;
        } // really 256
    }

    public int indexOf(byte[] pixel) {
        if (pixel.length != 4) {
            throw new IllegalArgumentException("Pixel array must have length 4");
        }

        int r = pixel[3] & 0xFF; // Convert byte to unsigned int
        int g = pixel[2] & 0xFF; // Convert byte to unsigned int
        int b = pixel[1] & 0xFF; // Convert byte to unsigned int
        int a = pixel[0] & 0xFF; // Convert byte to unsigned int

        return searchNetindexBgra(b, g, r, a);
    }

    public int indexOf(Color color) {
        return searchNetindexBgra(color.getBlue(), color.getGreen(), color.getRed(), color.getAlpha());
    }

    public int searchNetindexAbgr(int a, int b, int g, int r) {
        return searchNetindexBgra(b, g, r, a);
    }

    public int searchNetindexArgb(int a, int r, int g, int b) {
        return searchNetindexBgra(b, g, r, a);
    }

    public int searchNetindexRgba(int r, int g, int b, int a) {
        return searchNetindexBgra(b, g, r, a);
    }

    public int searchNetindexBgra(int b, int g, int r, int a) {
        int bestd = 1 << 30; // ~ 1_000_000
        int best = 0;

        // Start at netindex[g] and work outwards
        int i = netindex[g];
        int j = (i > 0) ? i - 1 : 0;

        while (i < NETSIZE || j > 0) {
            if (i < NETSIZE) {
                int ci = CHANNELS * i;
                byte pr = colormap[ci + 3];
                byte pg = colormap[ci + 2];
                byte pb = colormap[ci + 1];
                byte pa = colormap[ci];
                int e = (pg & 0xFF) - g;
                int dist = e * e; // inx key
                if (dist >= bestd) {
                    break;
                } else {
                    e = (pb & 0xFF) - b;
                    dist += e * e;
                    if (dist < bestd) {
                        e = (pr & 0xFF) - r;
                        dist += e * e;
                        if (dist < bestd) {
                            e = (pa & 0xFF) - a;
                            dist += e * e;
                            if (dist < bestd) {
                                bestd = dist;
                                best = i;
                            }
                        }
                    }
                    i++;
                }
            }
            if (j > 0) {
                int cj = CHANNELS * j;
                byte pr = colormap[cj + 3];
                byte pg = colormap[cj + 2];
                byte pb = colormap[cj + 1];
                byte pa = colormap[cj];
                int e = (pg & 0xFF) - g;
                int dist = e * e; // inx key
                if (dist >= bestd) {
                    break;
                } else {
                    e = (pb & 0xFF) - b;
                    dist += e * e;
                    if (dist < bestd) {
                        e = (pr & 0xFF) - r;
                        dist += e * e;
                        if (dist < bestd) {
                            e = (pa & 0xFF) - a;
                            dist += e * e;
                            if (dist < bestd) {
                                bestd = dist;
                                best = j;
                            }
                        }
                    }
                    j--;
                }
            }
        }
        return best;
    }

    public byte[] colorMapAbgr() {
        return colormap;
    }

    public byte[] colorMapRgb() {
        byte[] rgb = new byte[NETSIZE * 3];
        for (int i = 0; i < NETSIZE; i++) {
            int ci = CHANNELS * i;
            int ri = i * 3;
            rgb[ri] = colormap[ci + 3];
            rgb[ri + 1] = colormap[ci + 2];
            rgb[ri + 2] = colormap[ci + 1];
        }
        return rgb;
    }

    public byte[] colorMapBgr() {
        byte[] bgr = new byte[NETSIZE * 3];
        for (int i = 0; i < NETSIZE; i++) {
            int ci = CHANNELS * i;
            int ri = i * 3;
            bgr[ri] = colormap[ci + 1];
            bgr[ri + 1] = colormap[ci + 2];
            bgr[ri + 2] = colormap[ci + 3];
        }
        return bgr;
    }
}
