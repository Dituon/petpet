package moe.dituon.petpet.core.element.background;

import lombok.Getter;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.ElementModel;
import moe.dituon.petpet.core.imgres.AbsoluteLocalImageResource;
import moe.dituon.petpet.core.imgres.ImageFrameList;
import moe.dituon.petpet.core.utils.image.ImageUtils;
import moe.dituon.petpet.template.element.BackgroundTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class BackgroundModel implements ElementModel {
    protected static final int[] DEFAULT_DELAY = new int[]{65};

    @Getter
    public final int length;
    protected final AbsoluteLocalImageResource backgroundResource;
    protected final int[] delay;
    protected final boolean reverse;
    protected int imageType = -1;
    protected int width = -1;
    protected int height = -1;
    protected boolean isInitialized = false;

    public BackgroundModel(@NotNull BackgroundTemplate template) {
        this(template, null);
    }

    public BackgroundModel(@NotNull BackgroundTemplate template, int delay) {
        this(template, new int[]{delay});
    }

    public BackgroundModel(
            @NotNull BackgroundTemplate template,
            int @Nullable [] delay
    ) {
        this.backgroundResource = GlobalContext.getInstance().resourceManager.getBackgroundResource(
                template.getBasePath().toPath().resolve(template.getSrc()).toFile()
        );
        this.delay = delay == null ? DEFAULT_DELAY : delay;
        this.reverse = template.getReverse();
        this.length = backgroundResource.length;
    }

    protected ImageFrameList getFrameListSync() {
        try {
            ImageFrameList frameList = backgroundResource.getFrameListAsync().get();
            // Apply element-level reverse if needed
            if (this.reverse) {
                frameList = moe.dituon.petpet.core.utils.image.FrameListReverser.reverseFrameList(frameList);
            }
            this.imageType = frameList.imageType;
            this.width = frameList.width;
            this.height = frameList.height;
            return frameList;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    protected void init() {
        getFrameListSync();
        this.isInitialized = true;
    }

    public List<BufferedImage> getImages() {
        return getFrameListSync().getImages();
    }

    public List<BufferedImage> getClonedImages() {
        return getFrameListSync()
                .stream().map(f -> f.image.getType() == BufferedImage.TYPE_BYTE_INDEXED
                        ? ImageUtils.cloneImageAsAbgr(f.image)
                        : ImageUtils.cloneImage(f.image))
                .collect(Collectors.toList());
    }

    private BufferedImage createEmptyImage() {
        return new BufferedImage(this.width, this.height, this.imageType);
    }

    public int getWidth() {
        if (this.width == -1) init();
        return this.width;
    }

    public int getHeight() {
        if (this.height == -1) init();
        return this.height;
    }

    public int getImageType() {
        if (this.imageType == -1) init();
        return imageType;
    }

    @Override
    public @Nullable String getId() {
        return null;
    }

    @Override
    public Type getElementType() {
        return Type.BACKGROUND;
    }

    @Override
    public boolean isAbsolute() {
        return true;
    }

    @Override
    public RenderedElement render(CanvasContext canvasContext, RequestContext requestContext) {
        return new RenderedElement() {
            @Override
            public void draw() {
                BackgroundModel.this.draw(canvasContext, requestContext);
            }

            @Override
            public int getWidth() {
                return BackgroundModel.this.getWidth();
            }

            @Override
            public int getHeight() {
                return BackgroundModel.this.getHeight();
            }

            @Override
            public int getLength() {
                return length;
            }
        };
    }

    @Override
    public void draw(CanvasContext canvasContext, RequestContext requestContext) {
        ImageFrameList frames = getFrameListSync();
        // TODO: thread pool
        for (int i = 0; i < canvasContext.getLength(); i++) {
            canvasContext.getGraphics(i).drawImage(
                    frames.get(i).image, 0, 0, null
            );
        }
    }

    public File getPreviewImage() {
        return backgroundResource.getFrameFileList().get(0);
    }
}
