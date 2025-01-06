package moe.dituon.petpet.bot;

public interface EventHandler<T> {
    void handle(T event);
}
