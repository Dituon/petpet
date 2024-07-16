package moe.dituon.petpet.share.service;

public class PetpetService {
    static PetpetService INSTANCE;
    public static PetpetService getInstance() {
        return INSTANCE;
    }

    protected final ResourceManager resourceManager;

    public PetpetService(PetpetServiceConfig config) {
        resourceManager = new ResourceManager(config.getCache());
    }
}
