package moe.dituon.petpet.share.service;

import moe.dituon.petpet.share.ImageSynthesisCore;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ImageResource {
    public static final String[] supportedImageSuffixes;
    public static final String[] supportedImageMIMETypes;
    public static final Pattern indexedImagePattern;

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
            indexedImagePattern = Pattern.compile(String.format("\\d+\\.(%s)", String.join("|", supportedImageSuffixes)));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    protected WeakReference<BufferedImage[]> imagesRef = new WeakReference<>(null);
    protected final URI uri;

    protected ImageResource() {
        uri = null;
    }

    public ImageResource(URI uri) {
        this.uri = uri;
    }

    public BufferedImage[] getImages() throws IOException {
        if (uri == null) throw new MalformedURLException("uri is null");
        if (imagesRef.get() != null) return imagesRef.get();
        var images = ImageSynthesisCore.getImageAsList(uri.toURL().openStream()).toArray(BufferedImage[]::new);
        imagesRef = new WeakReference<>(images);
        return images;
    }
}
