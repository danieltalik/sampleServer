package org.example.mapper;

import org.example.entity.Pets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.model.Pet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PetMapperTest {

    @ParameterizedTest(name = "stored type \"{0}\" maps to {1}")
    @CsvSource({
            "CAT,  CAT",
            "cat,  CAT",
            "Dog,  DOG",
            "dOG,  DOG",
            "fish, FISH",
            "FISH, FISH"
    })
    void mapsStoredTypeCaseInsensitively(String storedType, Pet.TypeEnum expected) {
        Pet pet = PetMapper.toApi(new Pets(7, "Buddy", 3, storedType));

        assertThat(pet.getType()).isEqualTo(expected);
        assertThat(pet.getId()).isEqualTo(7);
        assertThat(pet.getName()).isEqualTo("Buddy");
        assertThat(pet.getAge()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"BIRD", "", "cats", " CAT"})
    void rejectsUnknownStoredType(String storedType) {
        Pets entity = new Pets(1, "Tweety", 2, storedType);

        assertThatIllegalArgumentException().isThrownBy(() -> PetMapper.toApi(entity));
    }

    @ParameterizedTest
    @EnumSource(Pet.TypeEnum.class)
    void storesEveryTypeAsItsSpecValue(Pet.TypeEnum type) {
        Pets entity = PetMapper.copyInto(new Pet("Buddy", 3, type), new Pets());

        assertThat(entity.getType()).isEqualTo(type.getValue());
    }

    @ParameterizedTest
    @EnumSource(Pet.TypeEnum.class)
    void roundTripsEveryType(Pet.TypeEnum type) {
        Pets entity = PetMapper.copyInto(new Pet("Buddy", 3, type), new Pets());
        entity.setId(1);

        assertThat(PetMapper.toApi(entity).getType()).isEqualTo(type);
    }

    @Test
    void copyIntoNeverOverwritesTheId() {
        Pet request = new Pet("Buddy", 3, Pet.TypeEnum.DOG);
        request.setId(999);
        Pets existing = new Pets(5, "Old", 1, "CAT");

        Pets updated = PetMapper.copyInto(request, existing);

        assertThat(updated).isSameAs(existing);
        assertThat(updated.getId()).isEqualTo(5);
        assertThat(updated.getName()).isEqualTo("Buddy");
        assertThat(updated.getAge()).isEqualTo(3);
        assertThat(updated.getType()).isEqualTo("DOG");
    }
}
