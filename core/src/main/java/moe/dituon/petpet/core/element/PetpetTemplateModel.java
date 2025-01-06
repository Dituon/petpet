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
    protected File previewImage;

    public PetpetTemplateModel(PetpetTemplate template) {
        this.template = template;
        this.dependencyBuilder = new DependencyBuilder(template);
        this.elementList = dependencyBuilder.getElementList();
        this.orderedElements = dependencyBuilder.getBuildOrder();
        this.elementIdMap = dependencyBuilder.getElementIdMap();
        this.canvasModel = dependencyBuilder.getCanvasModel();
        this.dependentRequestImageIds = dependencyBuilder.getUndefinedIds();
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
                maxLength = Math.max(maxLength, rendered.getLength());
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
        if (canvasContext.getLength() == 1) canvasContext.setLength(maxLength);
        for (ElementModel.RenderedElement ele : renderedElementList) {
            ele.draw();
        }
        var result = ImageEncoder.encodeImage(canvasContext);
        result.setBasePath(this.template.getBasePath());
        return result;
    }

    @Override
    public Metadata getMetadata() {
        return template.getMetadata();
    }

    @Override
    public @Nullable File getPreviewImage() {
        var previewPath = template.getMetadata().getPreview();
        if (previewPath != null) {
            return template.getBasePath().toPath().resolve(previewPath).toFile();
        }
        if (canvasModel.getBackgroundModel() != null) {
            return canvasModel.getBackgroundModel().getPreviewImage();
        }
        return null;
    }
}