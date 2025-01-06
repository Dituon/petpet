package moe.dituon.petpet.script.event;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EventManager {
    private final Map<String, Consumer<Object>> eventHandlers = new HashMap<>(8);

    public void on(String event, Consumer<Object> handler) {
        eventHandlers.put(event, handler);
    }

    public void trigger(String event, Object data) {
        Consumer<Object> handler = eventHandlers.get(event);
        if (handler == null) return;
        handler.accept(data);
    }

    public boolean has(String event) {
        return eventHandlers.containsKey(event);
    }
}
