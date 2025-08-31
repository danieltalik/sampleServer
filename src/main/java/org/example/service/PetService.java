package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.repository.PetRepository;
import org.openapitools.model.Pet;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import static org.example.utli.MappingUtil.petList;
import static org.example.utli.MappingUtil.getPet;


@RequiredArgsConstructor
@Service
public class PetService {

    private final PetRepository petRepository;

    public ResponseEntity<List<Pet>> getAllPets(){
        List<Pet> petsList = petList(petRepository.findAll());
        return new ResponseEntity<>(petsList, HttpStatusCode.valueOf(200));
    }

    public ResponseEntity<Pet> getPetById(String petId){
        Pet pet = getPet(Optional.of(petRepository.getReferenceById(Integer.valueOf(petId))));
        return new ResponseEntity<>(pet,HttpStatusCode.valueOf(200));
    }

}
