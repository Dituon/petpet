package moe.dituon.petpet.core.imgres;

import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.utils.image.ImageDecoder;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class LocalImageResource extends ImageResource {
    protected WeakReference<ImageFrameList> framesRef = null;

    @Override
    public CompletableFuture<ImageFrameList> getFrameListAsync() {
        return CompletableFuture.completedFuture(this.getOrCacheFrameList(null));
    }

    @Override
    public CompletableFuture<ImageFrameList> getFrameListAsync(File basePath) {
        return CompletableFuture.completedFuture(this.getOrCacheFrameList(basePath));
    }

    public static LocalImageResource getLocalResource(File path) {
        return path.isAbsolute() ? new AbsoluteLocalImageResource(path) : new RelativeLocalImageResource(path);
    }


    protected List<File> getFrameFileList(File path) {
        if (!path.isDirectory()) {
            return Collections.singletonList(path);
        }
        var result = getDirectoryFrameFileList(path);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("image directory is empty: " + path);
        }
        return result;
    }

    protected ImageFrameList readImageFrameList(List<File> files) {
        if (files == null || files.isEmpty()) throw new IllegalStateException("file list is empty");

        if (files.size() == 1) {
            try {
                return ImageDecoder.readImage(new FileInputStream(files.get(0)));
            } catch (IOException ignored) {
                // never
            }
        }

        var images = GlobalContext.getInstance().execImageProcess(files, (index, file) -> {
            try {
                // TODO: read gif?
                return ImageIO.read(file);
            } catch (IOException e) {
                throw new IllegalStateException(file.getPath(), e);
            }
        });
        return ImageFrameList.byImages(images);
    }

    protected abstract ImageFrameList getFrameList(@Nullable File base);

    public ImageFrameList getOrCacheFrameList(File base) {
        if (framesRef != null && framesRef.get() != null) {
            return framesRef.get();
        }
        synchronized (this) {
            if (framesRef != null && framesRef.get() != null) {
                return framesRef.get();
            }
            var frames = getFrameList(base);
            this.framesRef = new WeakReference<>(frames);
            return frames;
        }
    }

    public static List<File> getDirectoryFrameFileList(File basePath) {
        File[] files = basePath.listFiles();
        if (files == null) return Collections.emptyList();

        List<File> backgroundFiles = new ArrayList<>(Collections.nCopies(files.length, null));

        for (File file : files) {
            if (!file.isFile()) continue;

            String[] fileNames = file.getName().split("\\.");
            if (fileNames.length != 2) continue;

            // check suffix
            if (Arrays.stream(supportedImageSuffixes)
                    .noneMatch(suffix -> suffix.equalsIgnoreCase(fileNames[1]))) {
                continue;
            }

            int index;
            try {
                index = Integer.parseInt(fileNames[0]);
            } catch (NumberFormatException e) {
                continue;
            }

            backgroundFiles.set(index, file);
        }

        return fillNullsWithPrevious(backgroundFiles, basePath);
    }

    /**
     * Removes leading and trailing nulls, and fills any in-between nulls with the previous non-null element.
     */
    protected static <T> List<T> fillNullsWithPrevious(List<T> list, File basePath) {
        if (list == null || list.isEmpty()) return Collections.emptyList();

        // Skip leading nulls
        int startIndex = 0;
        while (startIndex < list.size() && list.get(startIndex) == null) {
            startIndex++;
        }

        if (startIndex == list.size()) return Collections.emptyList();

        // Skip trailing nulls
        int endIndex = list.size() - 1;
        while (endIndex >= 0 && list.get(endIndex) == null) {
            endIndex--;
        }

        List<Integer> warnIndex = new ArrayList<>();
        for (int i = 0; i < startIndex; i++) {
            warnIndex.add(i);
        }

        // Fill in-between nulls using last non-null element
        T lastNonNull = null;
        for (int i = startIndex; i <= endIndex; i++) {
            T current = list.get(i);
            if (current != null) {
                lastNonNull = current;
            } else {
                if (lastNonNull != null) {
                    list.set(i, lastNonNull);
                }
                warnIndex.add(i);
            }
        }

        if (!warnIndex.isEmpty()) {
            log.warn("兼容性问题: 模板目录的图像序列索引以 0 开始且必须连续; 已自动跳过开头并填充空缺元素 {}: {}",
                    warnIndex, basePath);
        }

        return list.subList(startIndex, endIndex + 1);
    }
}
