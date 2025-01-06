package moe.dituon.petpet.core.length;

public enum LengthType {
    PX("px"),
    CW("cw"),
    CH("ch"),
    VW("vw"),
    VH("vh"),
    PERCENT("%");

    public final String suffix;

    LengthType(String suffix) {
        this.suffix = suffix;
    }
}
