package org.example.utli;

import lombok.RequiredArgsConstructor;
import org.example.entity.Pets;
import org.example.repository.PetRepository;
import org.openapitools.model.Pet;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MappingUtil {

    public static List<Pet> petList(List<Pets> petsList) {
        return petsList.stream()
                .map(pets -> {
                    Pet pet = new Pet(pets.getName(), pets.getAge(), Pet.TypeEnum.valueOf(pets.getType().toUpperCase()));
                    return pet;
                })
                .toList();
    }
    public static Pet getPet(Optional<Pets> optionalPetEntity) {
        if (optionalPetEntity.isEmpty()) {
            return new Pet();
        }
        Pets pets = optionalPetEntity.get();
        return new Pet(pets.getName(), pets.getAge(), Pet.TypeEnum.fromValue(pets.getType().toUpperCase()));
    }
}
