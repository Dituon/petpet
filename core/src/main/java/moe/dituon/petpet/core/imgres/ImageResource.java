package moe.dituon.petpet.core.imgres;

import org.jetbrains.annotations.Nullable;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ImageResource {
    /**
     * supported image suffixes: <code>[png, jpg, jpeg, gif, bmp]</code>
     */
    public static final String[] supportedImageSuffixes;

    /**
     * supported image MIME types: <code>[image/png, image/jpeg, image/gif, image/bmp]</code>
     */
    public static final String[] supportedImageMIMETypes;

    static {
        try {
            var readerFileSuffixesMethod = ImageReaderSpi.class.getMethod("getFileSuffixes");
            var readerMIMETypesMethod = ImageReaderSpi.class.getMethod("getMIMETypes");
            Iterator<ImageReaderSpi> it = IIORegistry.getDefaultInstance().getServiceProviders(ImageReaderSpi.class, false);

            var suffixList = new ArrayList<String>(8);
            var mimeList = new ArrayList<String>(8);
            while (it.hasNext()) {
                var spi = it.next();
                suffixList.addAll(List.of((String[]) readerFileSuffixesMethod.invoke(spi)));
                mimeList.addAll(List.of((String[]) readerMIMETypesMethod.invoke(spi)));
            }
            supportedImageSuffixes = suffixList.toArray(String[]::new);
            supportedImageMIMETypes = mimeList.toArray(String[]::new);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public abstract CompletableFuture<ImageFrameList> getFrameListAsync();
    public abstract CompletableFuture<ImageFrameList> getFrameListAsync(File basePath);
    public abstract @Nullable String getSrc();
}
