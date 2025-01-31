package moe.dituon.petpet.core.imgres;

import lombok.experimental.Delegate;
import moe.dituon.petpet.core.GlobalContext;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ImageResourceMap implements Map<String, ImageResource> {
    public static final ImageResourceMap EMPTY = new ImageResourceMap(Collections.emptyMap());

    @Delegate
    protected final Map<String, ImageResource> map;

    public ImageResourceMap() {
        this(16);
    }

    public ImageResourceMap(int initialCapacity) {
        this.map = new HashMap<>(initialCapacity);
    }

    public ImageResourceMap(Map<String, ImageResource> map) {
        this.map = map;
    }

    public static ImageResourceMap fromUserStringMap(Map<String, String> map){
        return fromStringMap(map, GlobalContext.getInstance().resourceManager);
    }

    public static ImageResourceMap fromUserStringMap(
            Map<String, String> map,
            ImageResourceManager manager
    ) {
        if (map.isEmpty()) return EMPTY;

        HashMap<String, ImageResource> resultMap = new HashMap<>(map.size());
        for (Entry<String, String> entry : map.entrySet()) {
            resultMap.put(entry.getKey(), manager.getUserImageResource(entry.getValue()));
        }
        return new ImageResourceMap(resultMap);
    }

    public static ImageResourceMap fromStringMap(Map<String, String> map) {
        return fromStringMap(map, GlobalContext.getInstance().resourceManager);
    }

    public static ImageResourceMap fromStringMap(
            Map<String, String> map,
            ImageResourceManager manager
    ) {
        return fromStringMap(map, manager, null);
    }

    public static ImageResourceMap fromStringMap(
            Map<String, String> map,
            ImageResourceManager manager,
            @Nullable File basePath
    ) {
        if (map.isEmpty()) return EMPTY;

        HashMap<String, ImageResource> resultMap = new HashMap<>(map.size());
        for (Entry<String, String> entry : map.entrySet()) {
            resultMap.put(entry.getKey(), manager.getImageResource(entry.getValue(), basePath));
        }
        return new ImageResourceMap(resultMap);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageResourceMap)) return false;

        ImageResourceMap that = (ImageResourceMap) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
