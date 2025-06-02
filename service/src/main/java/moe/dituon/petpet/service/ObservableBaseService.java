package moe.dituon.petpet.service;

import moe.dituon.petpet.core.element.PetpetModel;

public abstract class ObservableBaseService extends BaseService {
    protected int updateVersion = Integer.MIN_VALUE;

    @Override
    public PetpetModel addTemplate(String id, PetpetModel model) {
        var prev = super.addTemplate(id, model);
        if (!model.equals(prev)) {
            updateVersion++;
        }
        return prev;
    }

    @Override
    public PetpetModel removeTemplate(String id) {
        var removed = super.removeTemplate(id);
        if (removed == null) return null;
        updateVersion++;
        return removed;
    }

    @Override
    public void clear() {
        updateVersion = Integer.MIN_VALUE;
        super.clear();
    }
}
