package moe.dituon.petpet.core.utils.image;

import moe.dituon.petpet.core.imgres.ImageFrame;
import moe.dituon.petpet.core.imgres.ImageFrameList;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reversing frame sequences
 */
public class FrameListReverser {

    private FrameListReverser() {
        // Utility class
    }

    /**
     * Reverse the order of frames in an ImageFrameList
     * @param frameList the original frame list
     * @return a new ImageFrameList with reversed frame order
     */
    public static ImageFrameList reverseFrameList(ImageFrameList frameList) {
        if (frameList == null || frameList.size() <= 1) {
            return frameList;
        }

        List<ImageFrame> reversedFrames = new ArrayList<>(frameList.size());

        // Add frames in reverse order
        for (int i = frameList.size() - 1; i >= 0; i--) {
            reversedFrames.add(frameList.get(i));
        }

        return new ImageFrameList(reversedFrames);
    }
}