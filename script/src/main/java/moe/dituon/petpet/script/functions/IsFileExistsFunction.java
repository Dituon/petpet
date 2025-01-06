package moe.dituon.petpet.script.functions;

import org.openjdk.nashorn.api.scripting.AbstractJSObject;

import java.io.File;

public class IsFileExistsFunction extends AbstractJSObject {
    public final File basePath;
    public IsFileExistsFunction(File basePath) {
        this.basePath = basePath;
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public Object call(Object thiz, Object... args) {
        if (args.length < 1) {
            return false;
        }
        if (args[0] instanceof String) {
            return basePath.toPath().resolve((String) args[0]).toFile().exists();
        }
        return false;
    }
}
