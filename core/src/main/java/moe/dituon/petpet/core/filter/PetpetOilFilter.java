/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package moe.dituon.petpet.core.filter;

import com.jhlabs.image.WholeImageFilter;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Setter
@Getter
public class PetpetOilFilter extends WholeImageFilter {
    private int skip = 4;
    private int range = 12;
    private int levels = 8;

    public PetpetOilFilter() {
    }
    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        int index = 0;
        int[] rHistogram = new int[levels];
        int[] gHistogram = new int[levels];
        int[] bHistogram = new int[levels];
        int[] rTotal = new int[levels];
        int[] gTotal = new int[levels];
        int[] bTotal = new int[levels];
        int[] outPixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int i = 0; i < levels; i++)
                    rHistogram[i] = gHistogram[i] = bHistogram[i] = rTotal[i] = gTotal[i] = bTotal[i] = 0;

                for (int row = -range; row <= range; row += skip) {
                    int iy = y + row;
                    int ioffset;
                    if (0 <= iy && iy < height) {
                        ioffset = iy * width;
                        for (int col = -range; col <= range; col += skip) {
                            int ix = x + col;
                            if (0 <= ix && ix < width) {
                                int rgb = inPixels[ioffset + ix];
                                int r = (rgb >> 16) & 0xff;
                                int g = (rgb >> 8) & 0xff;
                                int b = rgb & 0xff;
                                int ri = r * levels >> 8;
                                int gi = g * levels >> 8;
                                int bi = b * levels >> 8;
                                rTotal[ri] += r;
                                gTotal[gi] += g;
                                bTotal[bi] += b;
                                rHistogram[ri]++;
                                gHistogram[gi]++;
                                bHistogram[bi]++;
                            }
                        }
                    }
                }

                int r = 0, g = 0, b = 0;
                for (int i = 1; i < levels; i++) {
                    if (rHistogram[i] > rHistogram[r])
                        r = i;
                    if (gHistogram[i] > gHistogram[g])
                        g = i;
                    if (bHistogram[i] > bHistogram[b])
                        b = i;
                }
                r = rTotal[r] / rHistogram[r];
                g = gTotal[g] / gHistogram[g];
                b = bTotal[b] / bHistogram[b];
                outPixels[index] = (inPixels[index] & 0xff000000) | (r << 16) | (g << 8) | b;
                index++;
            }
        }

        return outPixels;
    }

    public String toString() {
        return "Stylize/Oil...";
    }

}

