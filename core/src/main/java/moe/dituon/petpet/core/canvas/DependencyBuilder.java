package moe.dituon.petpet.core.canvas;

import lombok.Getter;
import moe.dituon.petpet.core.element.Dependable;
import moe.dituon.petpet.core.element.ElementModel;
import moe.dituon.petpet.core.element.avatar.AvatarModel;
import moe.dituon.petpet.core.element.background.BackgroundModel;
import moe.dituon.petpet.core.element.text.TextModel;
import moe.dituon.petpet.template.PetpetTemplate;
import moe.dituon.petpet.template.element.AvatarTemplate;
import moe.dituon.petpet.template.element.BackgroundTemplate;
import moe.dituon.petpet.template.element.ElementTemplate;
import moe.dituon.petpet.template.element.TextTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DependencyBuilder {
    @Getter
    protected final List<ElementModel> elementList;
    protected boolean backgroundImageIsCanvas = false;
    @Nullable
    protected final BackgroundModel backgroundModel;
    @Getter
    protected final CanvasModel canvasModel;

    private static final String CANVAS_TEMP_ID = "canvas";

    @Getter
    protected final List<Dependable> buildOrder;
    @Getter
    protected final Map<ElementModel, String> elementIdMap;
    @Getter
    protected final Set<String> undefinedIds;

    public DependencyBuilder(@NotNull PetpetTemplate template) {
        this.elementList = new ArrayList<>(template.getElements().size());
        BackgroundModel tempBackgroundModel = null;

        int i = 0;
        for (ElementTemplate elementTemplate : template.getElements()) {
            if (elementTemplate instanceof BackgroundTemplate) {
                if (tempBackgroundModel != null) throw new IllegalArgumentException("Only one background is allowed");
                BackgroundModel model = new BackgroundModel((BackgroundTemplate) elementTemplate, template.getDelay());
                tempBackgroundModel = model;
                if (i == 0) {
                    // if background image in bottom as canvas, don't need to draw it
                    this.backgroundImageIsCanvas = true;
                } else {
                    this.elementList.add(model);
                }
            } else if (elementTemplate instanceof TextTemplate) {
                this.elementList.add(TextModel.createTextModel((TextTemplate) elementTemplate));
            } else if (elementTemplate instanceof AvatarTemplate) {
                this.elementList.add(new AvatarModel((AvatarTemplate) elementTemplate));
            }
            i++;
        }

        this.canvasModel = new CanvasModel(template.getCanvas(), tempBackgroundModel, this.backgroundImageIsCanvas);
        this.backgroundModel = tempBackgroundModel;

        BuildOrderCalculator calculator = new BuildOrderCalculator();
        this.buildOrder = calculator.calculate();
        this.elementIdMap = calculator.getElementIdMap();
        this.undefinedIds = calculator.getUndefinedIds();
    }

    protected class BuildOrderCalculator {
        private final List<Dependable> orderedElements;
        private final Map<String, String> idAliaMap;
        private final Map<String, Integer> inDegree;
        private final Map<String, List<String>> adjList;
        private final Map<Dependable, String> idMap;
        private final Map<String, Dependable> eleMap;
        @Getter
        private final Set<String> undefinedIds;
        private int contentDependencyCount = 0;

        public BuildOrderCalculator() {
            int mapCapacity = elementList.size() + 1;
            this.orderedElements = new ArrayList<>(mapCapacity);
            this.idAliaMap = new HashMap<>(elementList.size());
            this.inDegree = new HashMap<>(mapCapacity);
            this.adjList = new HashMap<>(mapCapacity);
            this.idMap = new HashMap<>(mapCapacity);
            this.eleMap = new HashMap<>(elementList.size() * 2 + 1);
            this.undefinedIds = new HashSet<>();
            initializeMaps();
        }

        protected void initializeMaps() {
            int index = 0;
            for (ElementModel model : elementList) {
                var id = "element_" + index++;
                idMap.put(model, id);
                eleMap.put(id, model);
                if (model.getId() != null) {
                    idAliaMap.put(model.getId(), id);
                }
            }
            idMap.put(canvasModel, CANVAS_TEMP_ID);
            eleMap.put(CANVAS_TEMP_ID, canvasModel);

            populateAdjacencyAndInDegree();
        }

        protected void populateAdjacencyAndInDegree() {
            for (ElementModel element : elementList) {
                var id = idMap.get(element);
                putNode(id, element.getDependentIds());
                if (element.isDependsOnCanvasSize()) {
                    adjList.computeIfAbsent(CANVAS_TEMP_ID, k -> new ArrayList<>()).add(id);
                    inDegree.put(id, inDegree.getOrDefault(id, 0) + 1);
                }
//                if (element.isDependsOnElementSize()) {
//                    if (element instanceof AvatarModel) {
//                        AvatarModel avatar = (AvatarModel) element;
//                        if (undefinedIds.addAll(avatar.getRequestKeys())) {
//                            contentDependencyCount++;
//                        }
//                    }
//                    // TODO: background model
//                }
            }

            putNode(CANVAS_TEMP_ID, canvasModel.getDependentIds());
        }

        protected void putNode(String id, Set<String> dependentIds) {
            inDegree.putIfAbsent(id, 0);
            for (String dep : dependentIds) {
                dep = aliaId(dep);
                adjList.computeIfAbsent(dep, k -> new ArrayList<>()).add(id);
                inDegree.put(id, inDegree.getOrDefault(id, 0) + 1);
                if (!eleMap.containsKey(dep)) {
                    undefinedIds.add(dep);
                    eleMap.put(dep, null);
                }
            }
        }

        public List<Dependable> calculate() {
            Queue<Dependable> queue = new LinkedList<>();
            for (var entry : eleMap.entrySet()) {
                String id = entry.getKey();
                Dependable element = entry.getValue();
                if (element != null && inDegree.get(idMap.get(element)) == 0) {
                    queue.add(element);
                } else if (element == null && inDegree.getOrDefault(id, 0) == 0) {
                    queue.add(new UndefinedDependable(id));
                }
            }

            while (!queue.isEmpty()) {
                var current = queue.poll();
                if (!(current instanceof UndefinedDependable)) {
                    this.orderedElements.add(current);
                }
                var id = idMap.get(current);
                if (id == null) {
                    id = ((UndefinedDependable) current).getId();
                }
                if (!adjList.containsKey(id)) continue;

                for (String neighborId : adjList.get(id)) {
                    inDegree.put(neighborId, inDegree.get(neighborId) - 1);
                    if (inDegree.get(neighborId) == 0) {
                        queue.add(eleMap.get(neighborId) != null ? eleMap.get(neighborId) : new UndefinedDependable(neighborId));
                    }
                }
            }

            validateOrder();
            return orderedElements;
        }

        protected void validateOrder() {
            if (orderedElements.size() + undefinedIds.size() - contentDependencyCount != eleMap.size()) {
                throw new IllegalArgumentException("Circular dependency detected or unexpected missing element in build order");
            }
        }

        protected String aliaId(String id) {
            return idAliaMap.getOrDefault(id, id);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        protected Map<ElementModel, String> getElementIdMap() {
            this.idMap.remove(canvasModel);
            return (Map) this.idMap;
        }

        @Getter
        private class UndefinedDependable implements Dependable {
            private final String id;

            public UndefinedDependable(String id) {
                this.id = id;
            }

            @Override
            public boolean isAbsolute() {
                return true;
            }
        }
    }
}
