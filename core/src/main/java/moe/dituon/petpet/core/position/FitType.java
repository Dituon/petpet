package moe.dituon.petpet.core.position;

public enum FitType {
    FILL("fill"),
    CONTAIN("contain"),
    COVER("cover"),
    SCALE_DOWN("scale-down"),
    NONE("none");

    public final String keyword;

    FitType(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return keyword;
    }

    public static FitType fromString(String str) {
        var types = FitType.values();
        for (int i = 0; i < types.length; i++) {
            if (types[i].keyword.equals(str)) {
                return FitType.values()[i];
            }
        }
        return null;
    }
}
