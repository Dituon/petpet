package moe.dituon.petpet.core.context;

import lombok.Getter;
import lombok.Setter;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.imgres.ImageFrameList;
import moe.dituon.petpet.core.imgres.ImageResource;
import moe.dituon.petpet.core.imgres.ImageResourceManager;
import moe.dituon.petpet.core.imgres.ImageResourceMap;
import moe.dituon.petpet.core.length.DynamicLength;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RequestContext {
    public static final RequestContext EMPTY = new RequestContext(ImageResourceMap.EMPTY, null);

    @Getter
    public final ImageResourceMap imageResourceMap;
    @Getter
    public final Map<String, String> textDataMap;
    @Setter
    @Getter
    public ImageResourceManager resourceManager;
    protected final Map<String, ImageFrameList> frameListCache;

    public static RequestContext fromTextMap(Map<String, String> map) {
        return new RequestContext(ImageResourceMap.EMPTY, map);
    }

    public static RequestContext fromImageStringMap(Map<String, String> map) {
        return new RequestContext(map, null);
    }

    public static RequestContext fromImageResourceMap(ImageResourceMap map) {
        return new RequestContext(map, null);
    }

    public RequestContext(
            @Nullable final Map<String, String> imageDataMap,
            @Nullable final Map<String, String> textDataMap
    ) {
        this(
                imageDataMap == null ? null : ImageResourceMap.fromStringMap(imageDataMap),
                textDataMap,
                null
        );
    }

    public RequestContext(
            @Nullable final ImageResourceMap imageDataMap,
            @Nullable final Map<String, String> textDataMap
    ) {
        this(imageDataMap, textDataMap, null);
    }

    public RequestContext(
            @Nullable final ImageResourceMap imageDataMap,
            @Nullable final Map<String, String> textDataMap,
            @Nullable ImageResourceManager resourceManager
    ) {
        this.imageResourceMap = imageDataMap == null ? ImageResourceMap.EMPTY : imageDataMap;
        this.textDataMap = textDataMap == null ? Collections.emptyMap() : textDataMap;
        this.frameListCache = new HashMap<>(imageDataMap == null ? 4 : imageDataMap.size());
        this.resourceManager = resourceManager == null ? GlobalContext.getInstance().resourceManager : resourceManager;
    }

    public Map<String, Integer> requestVariables(Set<String> requiredIds, File basePath) {
        if (requiredIds.isEmpty()) return new HashMap<>(8);
        var tasks = new ArrayList<CompletableFuture<Void>>(requiredIds.size());
        var varsMap = new HashMap<String, Integer>(requiredIds.size() * 3 + 4);

        for (String id : requiredIds) {
            var imageResource = this.imageResourceMap.get(id);
            if (imageResource != null) {
                tasks.add(imageResource.getFrameListAsync(basePath)
                        .thenAccept(frameList -> {
                            varsMap.put(id + DynamicLength.ELEMENT_WIDTH_SUFFIX, frameList.width);
                            varsMap.put(id + DynamicLength.ELEMENT_HEIGHT_SUFFIX, frameList.height);
                            varsMap.put(id + DynamicLength.ELEMENT_LENGTH_SUFFIX, frameList.size());
                            this.frameListCache.put(id, frameList);
                        }));
                continue;
            }
            // text data
            var textData = textDataMap.get(id);
            if (textData == null) throw new IllegalArgumentException("No request data found for id: " + id);
            varsMap.put(id + DynamicLength.ELEMENT_LENGTH_SUFFIX, textData.length());
        }
        if (tasks.isEmpty()) return Collections.emptyMap();
        CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
        return varsMap;
    }

    public ImageResource getImageResource(String id, @Nullable String defaultUrl) {
        var resource = this.imageResourceMap.get(id);
        if (resource != null) return resource;
        if (defaultUrl == null) throw new IllegalArgumentException("No image data found for id: " + id);
        return this.resourceManager.getImageResource(defaultUrl);
    }

    public ImageFrameList getFrameList(
            @NotNull String id,
            @Nullable String defaultUrl,
            @NotNull File basePath
    ) {
        if (this.frameListCache.containsKey(id)) {
            return this.frameListCache.get(id);
        }
        boolean hasData = this.imageResourceMap.containsKey(id);
        if (!hasData && this.frameListCache.containsKey(defaultUrl)) {
            return this.frameListCache.get(defaultUrl);
        }
        String cacheKey = hasData ? id : defaultUrl;
        if (cacheKey == null) {
            throw new IllegalArgumentException("No image data or default src found for id: " + id);
        }
        synchronized (cacheKey) {
            return this.frameListCache.computeIfAbsent(cacheKey, key -> {
                try {
                    return this.getImageResource(id, defaultUrl)
                            .getFrameListAsync(basePath)
                            .get();
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            });
        }
    }

    public static RequestContext newEmpty() {
        return newEmpty(16);
    }

    public static RequestContext newEmpty(int initialCapacity) {
        return new RequestContext(new ImageResourceMap(initialCapacity), new HashMap<>(initialCapacity));
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestContext)) return false;

        RequestContext that = (RequestContext) o;
        return imageResourceMap.equals(that.imageResourceMap) && textDataMap.equals(that.textDataMap) && resourceManager.equals(that.resourceManager);
    }

    @Override
    public int hashCode() {
        int result = imageResourceMap.hashCode();
        result = 31 * result + textDataMap.hashCode();
        result = 31 * result + resourceManager.hashCode();
        return result;
    }
}
