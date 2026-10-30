package org.example.service;

import org.example.entity.Pets;
import org.example.repository.PetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.Pet;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository repository;

    @InjectMocks
    private PetService service;

    @Test
    void mapsAllPets() {
        when(repository.findAll()).thenReturn(List.of(
                new Pets(1, "Alphonse", 4, "cat"),
                new Pets(2, "Sloopy", 1, "DOG")));

        assertThat(service.getAllPets())
                .extracting(Pet::getId, Pet::getName, Pet::getType)
                .containsExactly(
                        tuple(1, "Alphonse", Pet.TypeEnum.CAT),
                        tuple(2, "Sloopy", Pet.TypeEnum.DOG));
    }

    @Test
    void returnsEmptyListWhenNoPets() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(service.getAllPets()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 999, Integer.MAX_VALUE})
    void getPetByIdIsEmptyForUnknownIds(int id) {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThat(service.getPetById(id)).isEmpty();
    }

    @Test
    void createIgnoresAnyIdInTheRequest() {
        Pet request = new Pet("Nemo", 1, Pet.TypeEnum.FISH);
        request.setId(42);
        Integer[] idPassedToSave = new Integer[1];
        when(repository.save(any())).thenAnswer(inv -> {
            Pets toSave = inv.getArgument(0);
            idPassedToSave[0] = toSave.getId();
            toSave.setId(4); // what the database would assign
            return toSave;
        });

        Pet created = service.createPet(request);

        assertThat(idPassedToSave[0]).isNull();
        assertThat(created.getId()).isEqualTo(4);
        assertThat(created.getType()).isEqualTo(Pet.TypeEnum.FISH);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 999})
    void updateIsEmptyAndSavesNothingForUnknownIds(int id) {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThat(service.updatePet(id, new Pet("X", 1, Pet.TypeEnum.CAT))).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void updateChangesTheExistingPet() {
        Pets existing = new Pets(3, "Sloopy", 1, "DOG");
        when(repository.findById(3)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Optional<Pet> updated = service.updatePet(3, new Pet("Sloopy", 2, Pet.TypeEnum.DOG));

        assertThat(updated).get().extracting(Pet::getId, Pet::getAge).containsExactly(3, 2);
    }

    @Test
    void deleteReturnsTrueAndDeletesWhenPetExists() {
        when(repository.existsById(2)).thenReturn(true);

        assertThat(service.deletePet(2)).isTrue();
        verify(repository).deleteById(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -3, 999})
    void deleteReturnsFalseAndDeletesNothingForUnknownIds(int id) {
        when(repository.existsById(id)).thenReturn(false);

        assertThat(service.deletePet(id)).isFalse();
        verify(repository, never()).deleteById(anyInt());
    }
}
