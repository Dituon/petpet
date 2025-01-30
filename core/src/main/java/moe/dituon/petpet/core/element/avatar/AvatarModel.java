package moe.dituon.petpet.core.element.avatar;

import lombok.Getter;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.ElementFrame;
import moe.dituon.petpet.core.element.ElementModel;
import moe.dituon.petpet.core.position.AvatarCoords;
import moe.dituon.petpet.template.element.AvatarTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class AvatarModel implements ElementModel {
    public final AvatarTemplate template;
    public final List<AvatarFrame> frames;
    public final List<AvatarCoords> coords;
    @Getter
    public final boolean isAbsolute;
    @Getter
    public final boolean isDependsOnCanvasSize;
    @Getter
    public final boolean isDependsOnElementSize;

    public final int startIndex;
    public final int endIndex;

    protected int expectedWidth = -1;
    protected int expectedHeight = -1;

    protected Set<String> dependentIds = null;

    public AvatarModel(AvatarTemplate template) {
        this.template = template;

        this.coords = template.getCoords();
        this.startIndex = template.getStart();
        this.endIndex = template.getEnd();
        var tempFrames = new AvatarFrame[template.getMaxLength()];
        for (int i = 0; i < template.getMaxLength(); i++) {
            switch (ElementFrame.getNElement(coords, i).getType()) {
                case XYWH:
                    tempFrames[i] = new AvatarXYWHFrame(i, template);
                    break;
                case P4A:
                    tempFrames[i] = new AvatarP4AFrame(i, template);
                    break;
                default:
                    tempFrames[i] = new AvatarEmptyFrame(i, template);
                    break;
            }
        }
        this.frames = List.of(tempFrames);
        this.isAbsolute = coords.stream().anyMatch(AvatarCoords::isAbsolute);
        this.isDependsOnCanvasSize = coords.stream().anyMatch(AvatarCoords::isDependsOnCanvasSize);
        this.isDependsOnElementSize = coords.stream().anyMatch(AvatarCoords::isDependsOnElementSize);
    }

    /**
     * Get the expected width of the image; <br/>
     * Expected value to come from absolute coordinates.
     * @return 0 if coordinates are not absolute
     */
    public int getExpectedWidth() {
        if (this.expectedWidth != -1) {
            return this.expectedWidth;
        }
        initExpectedSize();
        return this.expectedWidth;
    }

    /**
     * Get the expected height of the image; <br/>
     * Expected value to come from absolute coordinates.
     * @return 0 if coordinates are not absolute
     */
    public int getExpectedHeight() {
        if (this.expectedHeight != -1) {
            return this.expectedHeight;
        }
        initExpectedSize();
        return this.expectedHeight;
    }

    protected void initExpectedSize() {
        int maxWidth = 0;
        int maxHeight = 0;
        for (var frame : frames) {
            int w = frame.getExpectedWidth();
            int h = frame.getExpectedHeight();
            if (w == 0 || h == 0) {
                this.expectedWidth = 0;
                this.expectedHeight = 0;
                return;
            }
            maxWidth = Math.max(maxWidth, w);
            maxHeight = Math.max(maxHeight, h);
        }
        this.expectedWidth = maxWidth;
        this.expectedHeight = maxHeight;
    }

    @Override
    public String getId() {
        return this.template.getId();
    }

    @Override
    public Type getElementType() {
        return Type.AVATAR;
    }

    @Override
    public Set<String> getDependentIds() {
        if (this.dependentIds != null) return this.dependentIds;
        if (this.isAbsolute) {
            this.dependentIds = Collections.emptySet();
            return this.dependentIds;
        }
        var tokenSet = new HashSet<String>();
        for (AvatarCoords c : coords) {
            if (c.isAbsolute()) continue;
            tokenSet.addAll(c.getDependentIds());
        }
        this.dependentIds = tokenSet.isEmpty() ? Collections.emptySet() : tokenSet;
        return this.dependentIds;
    }

    public List<String> getRequestKeys() {
        return this.template.getKey();
    }

    @Override
    public RenderedElement render(CanvasContext canvasContext, RequestContext requestContext) {
        return new RenderedElement(canvasContext, requestContext);
    }

    @Override
    public void draw(CanvasContext canvasContext, RequestContext requestContext) {
        var renderedElement = new RenderedElement(canvasContext, requestContext);
        renderedElement.draw();
    }

    public class RenderedElement extends ElementModel.RenderedElement {
        @Getter
        public final int width;
        @Getter
        public final int height;
        @Getter
        public final int length;

        protected final List<AvatarFrame.RenderedFrame> renderedFrames;
        protected final CanvasContext canvasContext;
        protected final RequestContext requestContext;

        public RenderedElement(CanvasContext canvasContext, RequestContext requestContext) {
            this.renderedFrames = frames.stream()
                    .map(frame -> frame.render(canvasContext, requestContext))
                    .collect(Collectors.toList());

            int frameLength = renderedFrames.size();
            if (frameLength == 0) {
                this.width = 0;
                this.height = 0;
                this.length = 1;
            } else if (frameLength == 1) {
                this.width = renderedFrames.get(0).getWidth();
                this.height = renderedFrames.get(0).getHeight();
                this.length = renderedFrames.stream()
                        .mapToInt(ElementFrame.RenderedFrame::getLength)
                        .max().orElse(1);
            } else {
                this.width = renderedFrames.stream()
                        .mapToInt(ElementFrame.RenderedFrame::getWidth)
                        .max().orElse(0);
                this.height = renderedFrames.stream()
                        .mapToInt(ElementFrame.RenderedFrame::getHeight)
                        .max().orElse(0);
                this.length = frameLength;
            }

            this.canvasContext = canvasContext;
            this.requestContext = requestContext;
        }

        @Override
        public void draw() {
            int canvasLength = this.canvasContext.getLength();
            final int absoluteEndIndex = endIndex >= 0 ? endIndex : (canvasLength + endIndex + 1);
            var frameList = this.renderedFrames;
            if (frameList.size() < canvasLength) {
                var tempFrames = new ElementFrame.RenderedFrame[canvasLength];
                for (int i = 0; i < canvasLength; i++) {
                    if (i > absoluteEndIndex || i < startIndex) {
                        tempFrames[i] = null;   // cannot draw
                        continue;
                    }
                    tempFrames[i] = frameList.get(i % frameList.size()).cloneByIndex(i - startIndex);
                }
                frameList = Arrays.asList(tempFrames);
            } else if (frameList.size() > canvasLength) {
                frameList = frameList.subList(0, canvasLength);
            }

            GlobalContext.getInstance().execImageProcess(frameList, (i, frame) -> {
                frame.draw(i);
            });
        }

        @Override
        public int getStartIndex() {
            return startIndex;
        }
    }
}
