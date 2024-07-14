package moe.dituon.petpet.share.service;

import moe.dituon.petpet.share.BasePetService;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

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

    /**
     * @param templateRoot 背景资源路径
     * @param randomFlag   随机选取一个图像
     */
    public void pushBackgroundRoot(File templateRoot, boolean randomFlag) {
        if (!templateRoot.isDirectory()) return;
        var name = templateRoot.getName();
        backgroundMap.put(name, new BackgroundResource(templateRoot, randomFlag));
    }

    public BufferedImage[] getBackgrounds(String key) throws IOException {
        if (!backgroundMap.containsKey(key)) throw new RuntimeException("Background not loaded");
        var images = backgroundMap.get(key).getImages();
        backgroundRefCache.put(key, images);
        return images;
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
}
