package moe.dituon.petpet.core.position;

import kotlinx.serialization.Serializable;
import lombok.Getter;
import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.LengthContext;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Serializable
public class AvatarP4ACoords extends AvatarCoords {
    public final List<List<Length>> coordsList;
    @Getter
    public final boolean isAbsolute;
    @Getter
    public final boolean isEmpty;
    @Getter
    public final boolean isDependsOnCanvasSize;
    @Getter
    public final boolean isDependsOnElementSize;

    protected Set<String> dependentIds = null;

    public AvatarP4ACoords(List<List<Length>> coordsList) {
        if (coordsList.size() != 5) throw new IllegalArgumentException("P4A pos format must has 5 point");
        for (List<Length> point : coordsList) {
            if (point.size() != 2) throw new IllegalArgumentException("point must has 2 value");
        }
        this.coordsList = coordsList;
        this.isAbsolute = coordsList.stream().allMatch(
                c -> c.stream().allMatch(Length::isAbsolute)
        );
        this.isEmpty = this.isAbsolute && checkIsEmpty(coordsList);
        this.isDependsOnCanvasSize = coordsList.stream().anyMatch(l ->
                l.get(0).isDependOnCanvasSize() || l.get(1).isDependOnCanvasSize()
        );
        this.isDependsOnElementSize = coordsList.stream().anyMatch(l ->
                l.get(0).isDependOnElementSize() || l.get(1).isDependOnElementSize()
        );
    }

    public Point2D[] getValue() {
        return coordsList.stream()
                .map(p -> new Point(
                        Math.round(p.get(0).getValue()),
                        Math.round(p.get(1).getValue())
                ))
                .toArray(Point2D[]::new);
    }

    public Point2D[] getValue(LengthContext context) {
        return coordsList.stream()
                .map(p -> new Point(
                        Math.round(p.get(0).getValue(context)),
                        Math.round(p.get(1).getValue(context))
                ))
                .toArray(Point2D[]::new);
    }

    public static boolean checkIsEmpty(List<List<Length>> points) {
        int[][] absCoords = new int[4][2];
        for (int i = 0; i < 4; i++) {
            Length x = points.get(i).get(0);
            Length y = points.get(i).get(1);
            absCoords[i][0] = Math.round(x.getRawValue());
            absCoords[i][1] = Math.round(y.getRawValue());
        }
        return !isValidPolygon(absCoords);
    }

    protected static boolean isValidPolygon(int[][] corners) {
        // points coincide
        // 四点重合
        for (int i = 1; true; i++) {
            if (corners[0][0] != corners[i][0] || corners[0][1] != corners[i][1]) {
                break;
            }
            if (i == 3) {
                return false;
            }
        }

        // vector cross product to determine collinearity
        // 向量叉积判断共线
        for (int i = 0; i < 3; i++) {
            int[] p1 = corners[i];
            int[] p2 = corners[i + 1];
            int[] p3 = corners[(i + 2) % 4];

            if (!arePointsCollinear(p1, p2, p3)) {
                return true;
            }
        }

        return false;
    }

    /**
     * check collinearity
     * <br/>
     * 判断共线性
     */
    protected static boolean arePointsCollinear(int[] p1, int[] p2, int[] p3) {
        int x1 = p2[0] - p1[0];
        int y1 = p2[1] - p1[1];
        int x2 = p3[0] - p1[0];
        int y2 = p3[1] - p1[1];

        return x1 * y2 - x2 * y1 == 0;
    }

    @Override
    public Set<String> getDependentIds() {
        if (this.dependentIds != null) return this.dependentIds;
        var tokenSet = new HashSet<String>();
        for (List<Length> points : coordsList) {
            var p0 = points.get(0);
            if (!p0.isAbsolute()) {
                tokenSet.addAll(p0.getDependentIds());
            }
            var p1 = points.get(1);
            if (!p1.isAbsolute()) {
                tokenSet.addAll(p1.getDependentIds());
            }
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
        return CoordType.P4A;
    }
}
