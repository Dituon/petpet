package moe.dituon.petpet.share.element;

public interface TemplateElement extends Drawable {
    int getWidth();

    int getHeight();

    Type getElementType();

    enum Type {
        AVATAR, TEXT
    }
}
