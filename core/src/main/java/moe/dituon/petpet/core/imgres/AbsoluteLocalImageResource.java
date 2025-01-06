package moe.dituon.petpet.core.imgres;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class AbsoluteLocalImageResource extends LocalImageResource {
    @Getter
    protected final List<File> frameFileList;
    public final int length;
    @Getter
    public final String src;

    public AbsoluteLocalImageResource(File path) {
        if (!path.isAbsolute()) throw new IllegalArgumentException("path must be absolute");

        this.frameFileList = getFrameFileList(path);
        this.length = this.frameFileList.size();
        this.src = path.getAbsolutePath();
    }

    @Override
    protected ImageFrameList getFrameList(@Nullable File base) {
        return this.readImageFrameList(this.frameFileList);
    }
}
