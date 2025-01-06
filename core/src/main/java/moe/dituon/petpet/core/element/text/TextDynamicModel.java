package moe.dituon.petpet.core.element.text;

import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.ElementFrame;
import moe.dituon.petpet.template.element.TextTemplate;
import org.apache.commons.text.StringSubstitutor;

import java.util.ArrayList;
import java.util.List;

// https://github.com/fizyr/subst
public class TextDynamicModel extends TextModel {
    public static final String PREFIX = StringSubstitutor.DEFAULT_VAR_START;
    public final boolean isDynamic;

    /**
     * String | GraphicsParagraph
     *
     * @see String
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
                stringTemplateList.add(s);
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

            var substitutor = new StringSubstitutor(requestContext.textDataMap);
            substitutor.setEnableSubstitutionInVariables(true);
            for (int i = 0; i < template.getMaxLength(); i++) {
                var stringTemplate = ElementFrame.getNElement(TextDynamicModel.this.stringTemplateList, i);
                if (stringTemplate instanceof String) {
                    var text = substitutor.replace(stringTemplate);
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

