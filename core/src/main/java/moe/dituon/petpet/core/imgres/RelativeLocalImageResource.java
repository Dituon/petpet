package moe.dituon.petpet.core.imgres;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public class RelativeLocalImageResource extends LocalImageResource {
    protected final File path;

    public RelativeLocalImageResource(File path) {
        this.path = path;
    }

    @Override
    public @Nullable String getSrc() {
        return path.toString();
    }

    @Override
    protected ImageFrameList getFrameList(@Nullable File base) {
        if (base == null) throw new IllegalArgumentException("base path is undefined");
        return super.readImageFrameList(super.getFrameFileList(
                base.toPath().resolve(path.toPath()).toFile()
        ));
    }
}
