package moe.dituon.petpet.core.imgres;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ImageResourceManager {
    protected final LinkedHashMap<File, AbsoluteLocalImageResource> backgroundCache;
    protected final LinkedHashMap<File, LocalImageResource> localImageCache;
    protected final LinkedHashMap<String, WebImageResource> webImageCache;

    public final List<Pattern> blockedUrlList;
    public final List<Pattern> allowedUrlList;
    public final ResourceManagerConfig config;
    public final boolean blockWebResource;
    /**
     * Enable user local file access
     */
    public final boolean allowLocalFile;

    public final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            0, 8, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
    );

    public ImageResourceManager(ResourceManagerConfig config) {
        this.backgroundCache = createCache(config.getBackgroundResourceCacheSize());
        this.localImageCache = createCache(config.getAvatarLocalResourceCacheSize());
        this.webImageCache = createCache(config.getAvatarWebResourceCacheSize());

        this.blockWebResource = !config.getEnableWebResource();
        this.blockedUrlList = this.blockWebResource ? Collections.emptyList() :
                config.getBlockList().stream()
                        .map(String::trim)
                        .map(Pattern::compile)
                        .collect(Collectors.toList());
        this.allowedUrlList = !this.blockWebResource ? Collections.emptyList() :
                config.getAllowList().stream()
                        .map(String::trim)
                        .map(Pattern::compile)
                        .collect(Collectors.toList());

        this.allowLocalFile = config.getEnableUserLocalFileAccess();
        this.config = config;
    }

    public boolean isBlocked(String url) {
        return this.blockWebResource ?
                this.allowedUrlList.isEmpty() || this.allowedUrlList.stream().noneMatch(pattern -> pattern.matcher(url).matches())
                : !this.blockedUrlList.isEmpty() && this.blockedUrlList.stream().anyMatch(pattern -> pattern.matcher(url).matches());
    }

    public InputStream openStream(URL url) {
        try {
            String urlString = url.toString();
            if (isBlocked(urlString)) throw new IllegalArgumentException("web resource has been blocked: " + urlString);
            return url.openStream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static <K, V> LinkedHashMap<K, V> createCache(int size) {
        return new LinkedHashMap<>(size, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > size;
            }
        };
    }

    public AbsoluteLocalImageResource getBackgroundResource(File path) {
        return backgroundCache.computeIfAbsent(path.getAbsoluteFile(), AbsoluteLocalImageResource::new);
    }

    public LocalImageResource getLocalResource(File path) {
        return localImageCache.computeIfAbsent(path, LocalImageResource::getLocalResource);
    }

    public WebImageResource getWebResource(URL url) {
        return webImageCache.computeIfAbsent(url.toString(), k -> new WebImageResource(url, this::openStream));
    }

    public WebImageResource getUserWebResource(URL url) {
        if (this.config.getEnableURLFileProtocol() && url.getProtocol().equals("file")) {
            throw new IllegalStateException("URL file protocol has been disabled");
        }
        return getWebResource(url);
    }

    public ImageResource getImageResource(String urlOrPath) {
        if (this.webImageCache.containsKey(urlOrPath)) {
            return this.webImageCache.get(urlOrPath);
        }
        try {
            var url = new URL(urlOrPath);
            return getWebResource(url);
        } catch (MalformedURLException e) {
            return getLocalResource(new File(urlOrPath));
        }
    }

    public ImageResource getUserImageResource(String urlOrPath) {
        if (this.webImageCache.containsKey(urlOrPath)) {
            return this.webImageCache.get(urlOrPath);
        }
        try {
            var url = new URL(urlOrPath);
            return getUserWebResource(url);
        } catch (MalformedURLException e) {
            if (!allowLocalFile) throw new IllegalArgumentException("Local file access has been disabled");
            return getLocalResource(new File(urlOrPath));
        }
    }

    /**
     * Quickly determine the legitimacy of a URL based on a string. <br/>
     * The returned result may be inaccurate.
     */
    public boolean checkUrlValidity(String url) {
        if (!this.config.getEnableURLFileProtocol() && url.startsWith("file:")) {
            return false;
        }
        return !isBlocked(url);
    }
}
