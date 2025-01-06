package moe.dituon.petpet.core.imgres;

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

        fillNullsWithPrevious(backgroundFiles);

        return backgroundFiles;
    }

    protected static <T> void fillNullsWithPrevious(List<T> list) {
        if (list == null || list.isEmpty()) return;
        if (list.get(0) == null) {
            list.clear();
        }

        while (true) {
            var lastIndex = list.size() - 1;
            if (lastIndex < 0) return;
            if (list.get(lastIndex) == null) {
                list.remove(lastIndex);
            } else {
                break;
            }
        }
        T lastNonNull = null;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null) {
                lastNonNull = list.get(i);
            } else if (lastNonNull != null) {
                list.set(i, lastNonNull);
            }
        }
    }
}
