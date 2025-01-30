package moe.dituon.petpet.core.element;

import lombok.Getter;
import moe.dituon.petpet.core.canvas.CanvasModel;
import moe.dituon.petpet.core.canvas.DependencyBuilder;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.imgres.ImageFrameList;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.core.utils.image.ImageEncoder;
import moe.dituon.petpet.template.Metadata;
import moe.dituon.petpet.template.PetpetTemplate;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class PetpetTemplateModel implements PetpetModel {
    public final PetpetTemplate template;
    @Getter
    protected final List<ElementModel> elementList;
    protected final DependencyBuilder dependencyBuilder;
    protected final List<Dependable> orderedElements;
    protected final Map<ElementModel, String> elementIdMap;
    protected final CanvasModel canvasModel;
    protected final Set<String> dependentRequestImageIds;
    @Getter
    protected final Set<String> requestImageKeys;
    protected int requestImageListLength = -1;
    @Getter
    protected final Set<String> requestTextKeys;
    protected int requestTextListLength = -1;
    protected File previewImage = null;

    public PetpetTemplateModel(PetpetTemplate template) {
        this.template = template;
        this.dependencyBuilder = new DependencyBuilder(template);
        this.elementList = dependencyBuilder.getElementList();
        this.orderedElements = dependencyBuilder.getBuildOrder();
        this.elementIdMap = dependencyBuilder.getElementIdMap();
        this.canvasModel = dependencyBuilder.getCanvasModel();
        this.dependentRequestImageIds = dependencyBuilder.getUndefinedIds();
        this.requestImageKeys = dependencyBuilder.getDependentRequestImageKeys();
        this.requestTextKeys = dependencyBuilder.getDependentRequestTextKeys();
    }

    @Override
    public EncodedImage draw(RequestContext requestContext) {
        var variables = requestContext.requestVariables(this.dependentRequestImageIds, this.template.getBasePath());
        CanvasContext canvasContext = canvasModel.createCanvasContext(variables);
        canvasContext.setBasePath(this.template.getBasePath());
        canvasContext.putLength("canvas", canvasContext.getLength());
        int maxLength = (int) canvasModel.length.getValue(canvasContext.getLengthContext());

        List<ElementModel.RenderedElement> renderedElementList = new ArrayList<>(Collections.nCopies(this.elementList.size(), null));
        for (Dependable ordered : this.orderedElements) {
            if (ordered instanceof ElementModel) {
                ElementModel element = (ElementModel) ordered;
                var rendered = element.render(canvasContext, requestContext);
                int width = rendered.getWidth();
                int height = rendered.getHeight();

                canvasContext.putSize(this.elementIdMap.get(element), width, height);
                maxLength = Math.max(maxLength, rendered.getLength() + rendered.getStartIndex());
                if (element.getId() != null) {
                    canvasContext.putSize(element.getId(), width, height);
                }
                renderedElementList.set(elementList.indexOf(element), rendered);
            } else if (ordered instanceof CanvasModel) {
                CanvasModel canvas = (CanvasModel) ordered;
                var imgList = canvas.createCanvasImages(canvasContext, requestContext);
                canvasContext.setCanvasList(new ImageFrameList(
                        imgList,
                        this.template.getDelay()
                ));
            }
        }
        if (canvasContext.getLength() == 1) {
            canvasContext.setLength(maxLength, this.template.getDelay());
        }
        for (ElementModel.RenderedElement ele : renderedElementList) {
            ele.draw();
        }
        var result = ImageEncoder.encodeImage(canvasContext);
        result.setBasePath(this.template.getBasePath());
        return result;
    }

    /**
     * 获取请求图像参数列表长度:
     * 例:
     * <pre>
     * key: "0"             // length: 1
     * key: ["0", "2"]      // length: 3
     * key: "3"             // length: 4
     * key: "to"            // length: 0
     * </pre>
     */
    public int getRequestImageListLength() {
        if (requestImageListLength == -1) {
            requestImageListLength = getKeySetLength(requestImageKeys);
        }
        return requestImageListLength;
    }

    /**
     * 获取请求文本参数列表长度:
     * 例:
     * <pre>
     * Hello, ${0}!         // length: 1
     * Hello, ${0} ${2}!    // length: 3
     * Hello, ${3}!         // length: 4
     * Hello, ${to} (${to_id})! // length: 0
     * </pre>
     */
    public int getRequestTextListLength() {
        if (requestTextListLength == -1) {
            requestTextListLength = getKeySetLength(requestTextKeys);
        }
        return requestTextListLength;
    }

    protected static int getKeySetLength(Set<String> keySet) {
        int maxIndex = -1;
        for (String key : keySet) {
            try {
                int index = Integer.parseInt(key);
                maxIndex = Math.max(maxIndex, index);
            } catch (NumberFormatException ignored) {
            }
        }
        return maxIndex + 1;
    }

    @Override
    public Metadata getMetadata() {
        return template.getMetadata();
    }

    @Override
    public @Nullable File getPreviewImage() {
        if (previewImage != null) return previewImage;
        var previewPath = template.getMetadata().getPreview();
        if (previewPath != null) {
            previewImage = template.getBasePath().toPath().resolve(previewPath).toFile();
        }
        if (canvasModel.getBackgroundModel() != null) {
            previewImage = canvasModel.getBackgroundModel().getPreviewImage();
        }
        return previewImage;
    }
}