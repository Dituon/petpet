package moe.dituon.petpet.share.service;

import lombok.Getter;

public class PetpetService {
    protected static PetpetService INSTANCE;
    public static PetpetService getInstance() {
        return INSTANCE;
    }

    @Getter
    protected final ResourceManager resourceManager;
    @Getter
    protected final TemplateManager templateManager;

    public PetpetService(PetpetServiceConfig config) {
        resourceManager = new ResourceManager(config.getCache());
        ResourceManager.ResourceManagerInstanceHolder.INSTANCE = resourceManager;
        this.templateManager = new TemplateManager(resourceManager);
    }
}
