package moe.dituon.petpet.share.position;

import kotlinx.serialization.json.JsonArray;

public interface PositionCollection<T> {
    T getPosition(int index);

    T getPosition(int index, PositionDynamicData data);

    int size();

    boolean isDynamical();

    static <T> PositionCollection<T> fromJson(JsonArray jsonArray) {
        throw new RuntimeException();
    }

    static int calculateDepth(JsonArray jsonArray) {
        int maxDepth = 0;

        for (Object element : jsonArray) {
            if (element instanceof JsonArray) {
                int childDepth = calculateDepth((JsonArray) element);
                maxDepth = Math.max(maxDepth, childDepth);
            }
        }

        return maxDepth + 1;
    }
}
