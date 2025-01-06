package moe.dituon.petpet.script.functions;

import moe.dituon.petpet.script.event.EventManager;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public class EventListenerRegisterer extends AbstractJSObject {
    public final EventManager eventManager;
    public EventListenerRegisterer(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    /**
     * <pre>
     * on(type: string, handle: (e: Event) => {})
     * </pre>
     */
    @Override
    public Object call(Object thiz, Object... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("on(event, handler) requires exactly 2 arguments");
        }
        var eventName = (String) args[0];
        var handler = (ScriptObjectMirror) args[1];
        if (!handler.isFunction()) {
            throw new IllegalArgumentException("handler must be a function");
        }
        eventManager.on(eventName, p -> handler.call(thiz, p));
        return null;
    }
}
