package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.PetService;
import org.openapitools.api.PetsApi;
import org.openapitools.model.Pet;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PetStoreController implements PetsApi {

    private final PetService petService;

    @Override
    public ResponseEntity<List<Pet>> getPets() {
        return ResponseEntity.ok(petService.getAllPets());
    }

    @Override
    public ResponseEntity<Pet> getPetById(String petId) {
        return petService.getPetById(parseId(petId))
                .map(ResponseEntity::ok)
                .orElseThrow(PetStoreController::notFound);
    }

    @Override
    public ResponseEntity<Pet> createPet(Pet pet) {
        Pet created = petService.createPet(pet);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Override
    public ResponseEntity<Pet> updatePet(String petId, Pet pet) {
        return petService.updatePet(parseId(petId), pet)
                .map(ResponseEntity::ok)
                .orElseThrow(PetStoreController::notFound);
    }

    @Override
    public ResponseEntity<Void> deletePet(String petId) {
        if (!petService.deletePet(parseId(petId))) {
            throw notFound();
        }
        return ResponseEntity.noContent().build();
    }

    /** The spec types petId as a string; anything that isn't a number can't match a pet. */
    private static int parseId(String petId) {
        try {
            return Integer.parseInt(petId);
        } catch (NumberFormatException e) {
            throw notFound();
        }
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found");
    }
}
