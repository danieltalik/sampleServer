package org.example.mapper;

import org.example.entity.Pets;
import org.openapitools.model.Pet;

import java.util.Locale;

public final class PetMapper {

    private PetMapper() {
    }

    public static Pet toApi(Pets entity) {
        Pet pet = new Pet(entity.getName(), entity.getAge(),
                Pet.TypeEnum.fromValue(entity.getType().toUpperCase(Locale.ROOT)));
        pet.setId(entity.getId());
        return pet;
    }

    /** Copies the writable fields from the API model onto the entity. The id is never taken from the request. */
    public static Pets copyInto(Pet source, Pets target) {
        target.setName(source.getName());
        target.setAge(source.getAge());
        target.setType(source.getType().getValue());
        return target;
    }
}
