package moe.dituon.petpet.core.position;

import kotlinx.serialization.Serializable;
import lombok.Getter;
import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.length.LengthType;
import moe.dituon.petpet.core.length.NumberLength;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Serializable
public class AvatarXYWHCoords extends AvatarCoords {
    public static final AvatarXYWHCoords FILL = new AvatarXYWHCoords(List.of(
            NumberLength.EMPTY, NumberLength.EMPTY,
            new NumberLength(100, LengthType.CW), new NumberLength(100, LengthType.CH)
    ));

    public final List<Length> coordsList;
    @Getter
    public final boolean isAbsolute;
    @Getter
    public final boolean isEmpty;
    @Getter
    public final boolean isDependsOnCanvasSize;
    @Getter
    public final boolean isDependsOnElementSize;

    protected Set<String> dependentIds = null;

    public AvatarXYWHCoords(String x, String y, String width, String height) {
        this(Length.fromString(x), Length.fromString(y), Length.fromString(width), Length.fromString(height));
    }

    public AvatarXYWHCoords(int x, int y, int width, int height) {
        this(NumberLength.px(x), NumberLength.px(y), NumberLength.px(width), NumberLength.px(height));
    }

    public AvatarXYWHCoords(Length x, Length y, Length width, Length height) {
        this(List.of(x, y, width, height));
    }

    public AvatarXYWHCoords(List<Length> coordsList) {
        if (coordsList.size() != 4) {
            throw new IllegalArgumentException("XYWH pos format must has 4 element");
        }
        this.coordsList = coordsList;
        this.isAbsolute = coordsList.stream().allMatch(Length::isAbsolute);
        Length width = coordsList.get(2);
        Length height = coordsList.get(3);
        if (width.isAbsolute() && height.isAbsolute()) {
            this.isEmpty = width.getValue() == 0f && height.getValue() == 0f;
        } else {
            this.isEmpty = false;
        }
        this.isDependsOnCanvasSize = coordsList.stream().anyMatch(Length::isDependOnCanvasSize);
        this.isDependsOnElementSize = coordsList.stream().anyMatch(Length::isDependOnElementSize);
    }

    public int[] getValue() {
        return coordsList.stream()
                .mapToInt(l -> Math.round(l.getValue()))
                .toArray();
    }

    /**
     * @return int[x, y, width, height]
     */
    public int[] getValue(LengthContext context) {
        return coordsList.stream()
                .mapToInt(l -> Math.round(l.getValue(context)))
                .toArray();
    }

    @Override
    public Set<String> getDependentIds() {
        if (this.dependentIds != null) return this.dependentIds;
        var tokenSet = new HashSet<String>();
        for (Length length : coordsList) {
            if (length.isAbsolute()) continue;
            tokenSet.addAll(length.getDependentIds());
        }
        this.dependentIds = tokenSet.isEmpty() ? Collections.emptySet() : tokenSet;
        return this.dependentIds;
    }

    @Override
    public String toString() {
        return coordsList.toString();
    }

    @Override
    public CoordType getType() {
        return CoordType.XYWH;
    }
}
