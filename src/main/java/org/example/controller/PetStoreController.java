package org.example.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.example.service.PetService;
import org.openapitools.api.PetsApi;
import org.openapitools.model.Pet;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PetStoreController implements PetsApi {

    private final PetService petService;

    @Override
    public ResponseEntity<List<Pet>> getPets() {
        return petService.getAllPets();
    }

    @Override
    public ResponseEntity<Pet> getPetById(@Parameter(name = "petId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("petId") String petId){
        return petService.getPetById(petId);
    }
}
