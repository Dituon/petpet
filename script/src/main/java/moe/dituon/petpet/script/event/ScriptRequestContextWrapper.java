package moe.dituon.petpet.script.event;

import lombok.Getter;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.imgres.ImageResourceMap;

import java.util.HashMap;
import java.util.Map;

public class ScriptRequestContextWrapper {
    protected Map<String, String> image = null;
    @Getter
    protected Map<String, String> text = null;
    protected boolean imageUpdateFlag = false;
    protected boolean textUpdateFlag = false;
    protected ImageResourceMap imageResourceMap = null;
    protected RequestContext context = null;

    public ScriptRequestContextWrapper(RequestContext context) {
        this.context = context;
        imageResourceMap = context.imageResourceMap;
        text = context.textDataMap;
    }

    public ScriptRequestContextWrapper() {
    }

    public Map<String, String> getImage() {
        if (image == null) {
            if (imageResourceMap == null) {
                image = createMap(8);
            } else {
                image = createMap(imageResourceMap.size());
                boolean prevFlag = imageUpdateFlag;
                imageResourceMap.forEach((key, value) -> image.put(key, value.getSrc()));
                imageUpdateFlag = prevFlag;
            }
        }
        return image;
    }

    public void setText(Map<String, String> text) {
        if (text == this.text) {
            return;
        }
        textUpdateFlag = true;
        this.text = text;
    }

    public void setImage(Map<String, String> image) {
        if (image == this.image) {
            return;
        }
        imageUpdateFlag = true;
        this.image = image;
    }

    protected Map<String, String> createMap(int size) {
        return new HashMap<>(size) {
            @Override
            public String put(String key, String value) {
                var prev = super.put(key, value);
                if (value != null && !value.equals(prev)) {
                    imageUpdateFlag = true;
                }
                return prev;
            }

            @Override
            public boolean remove(Object key, Object value) {
                var flag = super.remove(key, value);
                imageUpdateFlag = imageUpdateFlag || flag;
                return flag;
            }
        };
    }

    public ImageResourceMap getImageResourceMap() {
        if (imageResourceMap == null || imageUpdateFlag) {
            if (image == null) throw new IllegalArgumentException("imageMap is null");
            if (context == null) {
                imageResourceMap = ImageResourceMap.fromStringMap(image);
            } else {
                imageResourceMap = ImageResourceMap.fromStringMap(image, context.resourceManager);
            }
            imageUpdateFlag = false;
        }
        return imageResourceMap;
    }

    protected void updateImageResourceMap() {
        if (imageResourceMap == null) {
            imageResourceMap = ImageResourceMap.EMPTY;
        }
    }

    public RequestContext toRequestContext() {
        if (!imageUpdateFlag && !textUpdateFlag && context != null) {
            return context;
        }
        if (!imageUpdateFlag && image != null && context != null) {
            return new RequestContext(getImageResourceMap(), text, context.resourceManager);
        }
        return new RequestContext(
                getImageResourceMap(), text,
                context == null ? null : context.resourceManager
        );
    }
}
