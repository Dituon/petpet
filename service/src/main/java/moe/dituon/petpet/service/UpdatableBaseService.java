package moe.dituon.petpet.service;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public abstract class UpdatableBaseService extends ObservableBaseService {
    /**
     * 未指定路径时默认用于下载更新的本地路径
     */
    @Nullable
    protected File localTemplateDirectory = null;

    @Override
    public TemplateManger addTemplates(File basePath) {
        super.addTemplates(basePath);
        localTemplateDirectory = basePath;
        return this;
    }

    public @Nullable File getLocalTemplateDirectory() {
        if (localTemplateDirectory == null && !staticModelMap.isEmpty()) {
            var templateDirectory = staticModelMap.values().iterator().next().getDirectory();
            if (templateDirectory != null) {
                localTemplateDirectory = templateDirectory.getParentFile();
            }
        }
        return localTemplateDirectory;
    }

    @Override
    public void clear() {
        localTemplateDirectory = null;
        super.clear();
    }
}
