package moe.dituon.petpet.share.service;

import moe.dituon.petpet.share.BasePetService;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class ResourceManager {
    static class ResourceManagerInstanceHolder {
        static ResourceManager INSTANCE = new ResourceManager(new ResourceManagerConfig());
    }

    public static ResourceManager getDefaultInstance() {
        return ResourceManagerInstanceHolder.INSTANCE;
    }

    protected final HashMap<String, BackgroundResource> backgroundMap = new HashMap<>(BasePetService.DEFAULT_INITIAL_CAPACITY);
    protected final LinkedHashMap<String, BufferedImage[]> backgroundRefCache;

    protected final HashMap<File, ImageResource> localImageMap = new HashMap<>(BasePetService.DEFAULT_INITIAL_CAPACITY);
    protected final LinkedHashMap<File, BufferedImage[]> localImageRefCache;

    protected final WeakHashMap<URI, ImageResource> webImageMap = new WeakHashMap<>(BasePetService.DEFAULT_INITIAL_CAPACITY);
    protected final LinkedHashMap<URI, BufferedImage[]> webImageRefCache;

    public ResourceManager(ResourceManagerConfig config) {
        backgroundRefCache = createCache(config.getBackgroundResourceCacheSize());
        localImageRefCache = createCache(config.getAvatarLocalResourceCacheSize());
        webImageRefCache = createCache(config.getAvatarWebResourceCacheSize());
    }

    private static <K, V> LinkedHashMap<K, V> createCache(int size) {
        return new LinkedHashMap<>(size, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > size;
            }
        };
    }

    public BackgroundResource pushBackground(String id, BackgroundResource resource) {
        return backgroundMap.put(id, resource);
    }

    public BackgroundResource pushBackground(File templateRoot) {
        return pushBackground(templateRoot, false);
    }

    /**
     * @param templateRoot 背景资源路径
     * @param randomFlag   随机选取一个图像
     */
    public BackgroundResource pushBackground(File templateRoot, boolean randomFlag) {
        if (!templateRoot.isDirectory()) return null;
        var id = templateRoot.getName();
        var resource = new BackgroundResource(templateRoot, randomFlag);
        backgroundMap.put(id, resource);
        return resource;
    }

    public BackgroundResource getBackgrounds(String key) throws IOException {
        if (!backgroundMap.containsKey(key)) throw new RuntimeException("Background not loaded");
        var images = backgroundMap.get(key).getImages();
        backgroundRefCache.put(key, images);
        return backgroundMap.get(key);
    }

    public BufferedImage[] getImages(URI uri) throws IOException {
        try {
            var file = new File(uri);
            if (!localImageMap.containsKey(file)) {
                localImageMap.put(file, new ImageResource(uri));
            }
            var images = localImageMap.get(file).getImages();
            localImageRefCache.put(file, images);
            return images;
        } catch (IllegalArgumentException ignored) {
            if (!webImageMap.containsKey(uri)) {
                webImageMap.put(uri, new ImageResource(uri));
            }
            var images = webImageMap.get(uri).getImages();
            webImageRefCache.put(uri, images);
            return images;
        }
    }

    public Supplier<List<BufferedImage>> getImageSupplier(URI uri, Path base) {
        var defaultImageUri = uri;
        if (base != null && defaultImageUri.getScheme().equals("file") && !isAbsolutePath(defaultImageUri)) {
            defaultImageUri = base.resolve(defaultImageUri.getSchemeSpecificPart()).toUri();
        }
        final URI finalDefaultImageUri = defaultImageUri;
        return () -> {
            try {
                return List.of(this.getImages(finalDefaultImageUri));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    protected static boolean isAbsolutePath(URI uri) {
        if (!uri.isOpaque()) {
            return uri.getPath().startsWith("/");
        }
        return false;
    }
}
