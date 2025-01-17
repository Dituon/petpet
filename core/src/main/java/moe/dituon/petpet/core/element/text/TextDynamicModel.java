package moe.dituon.petpet.core.element.text;

import lombok.Getter;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.ElementFrame;
import moe.dituon.petpet.core.utils.text.TextStringTemplate;
import moe.dituon.petpet.template.element.TextTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TextDynamicModel extends TextModel {
    public static final String PREFIX = "$";
    public final boolean isDynamic;
    @Getter
    public final Set<String> requestKeys = new HashSet<>(8);

    /**
     * TextStringTemplate | GraphicsParagraph
     *
     * @see TextStringTemplate
     * @see GraphicsParagraph
     */
    protected final List<Object> stringTemplateList; // Dynamic string templates

    public TextDynamicModel(TextTemplate template) {
        super(template, false);
        this.stringTemplateList = new ArrayList<>(template.getText().size());
        int i = 0;

        boolean dynamicFlag = false;
        for (String s : template.getText()) {
            if (s.contains(PREFIX)) {
                var stringTemplate = TextStringTemplate.parse(s);
                requestKeys.addAll(stringTemplate.getVariables());
                stringTemplateList.add(stringTemplate);
                dynamicFlag = true;
            } else {
                stringTemplateList.add(
                        createParagraph(new GraphicsAttributedString(s, template, i))
                );
            }
            i++;
        }
        this.isDynamic = dynamicFlag;
    }

    @Override
    public boolean isDynamic() {
        return isDynamic;
    }

    @Override
    public void draw(CanvasContext canvasContext, RequestContext requestContext) {
        var renderedElement = new RenderedElement(canvasContext, requestContext);
        renderedElement.draw();
    }

    @Override
    public RenderedElement render(CanvasContext canvasContext, RequestContext requestContext) {
        return new RenderedElement(canvasContext, requestContext);
    }

    public class RenderedElement extends TextModel.RenderedElement {
        protected final List<GraphicsParagraph> paragraphList;

        public RenderedElement(CanvasContext canvasContext, RequestContext requestContext) {
            super(canvasContext, requestContext);
            this.paragraphList = new ArrayList<>(template.getMaxLength());

            for (int i = 0; i < template.getMaxLength(); i++) {
                var stringTemplate = ElementFrame.getNElement(TextDynamicModel.this.stringTemplateList, i);
                if (stringTemplate instanceof TextStringTemplate) {
                    var text = ((TextStringTemplate) stringTemplate).expand(requestContext.textDataMap);
                    if (text.isEmpty()) continue;
                    var paragraph = createParagraph(
                            new GraphicsAttributedString(text, template, i)
                    );
                    this.paragraphList.add(paragraph);
                    initParagraph(paragraph, i);
                } else if (stringTemplate instanceof GraphicsParagraph) {
                    this.paragraphList.add((GraphicsParagraph) stringTemplate);
                }
            }
            super.width = this.paragraphList.stream().mapToInt(p -> p.width).max().orElse(0);
            super.height = this.paragraphList.stream().mapToInt(p -> p.height).max().orElse(0);
        }

        protected void initParagraph(GraphicsParagraph paragraph, int index) {
            if (paragraph instanceof GraphicsBreakParagraph) {
                var p = (GraphicsBreakParagraph) paragraph;
                if (!p.isInitialized()) {
                    p.breakText(
                            ElementFrame.getNElement(template.getCoords(), index)
                                    .getWidth(canvasContext.getLengthContext())
                    );
                }
            } else if (paragraph instanceof GraphicsZoomParagraph) {
                var p = (GraphicsZoomParagraph) paragraph;
                if (!p.isInitialized()) {
                    p.zoomText(
                            ElementFrame.getNElement(template.getCoords(), index)
                                    .getWidth(canvasContext.getLengthContext())
                    );
                }
            }
        }

        @Override
        public void draw() {
            int canvasLength = canvasContext.getLength();
            var frameList = paragraphList;
            if (paragraphList.size() <= canvasLength) {
                frameList = repeatByLength(frameList, canvasLength);
            }
            for (int i = 0; i < canvasLength; i++) {
                draw(frameList.get(i), i);
            }
        }

        protected void draw(GraphicsParagraph paragraph, int i) {
            paragraph.draw(
                    super.canvasContext.getGraphics(i),
                    super.canvasContext.createLengthContext(paragraph.width, paragraph.height)
            );
        }
    }
}

