package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.Pets;
import org.example.mapper.PetMapper;
import org.example.repository.PetRepository;
import org.openapitools.model.Pet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;

    public List<Pet> getAllPets() {
        return petRepository.findAll().stream().map(PetMapper::toApi).toList();
    }

    public Optional<Pet> getPetById(int id) {
        return petRepository.findById(id).map(PetMapper::toApi);
    }

    @Transactional
    public Pet createPet(Pet pet) {
        return PetMapper.toApi(petRepository.save(PetMapper.copyInto(pet, new Pets())));
    }

    @Transactional
    public Optional<Pet> updatePet(int id, Pet pet) {
        return petRepository.findById(id)
                .map(existing -> PetMapper.toApi(petRepository.save(PetMapper.copyInto(pet, existing))));
    }

    /** Returns false when no pet has that id. */
    @Transactional
    public boolean deletePet(int id) {
        if (!petRepository.existsById(id)) {
            return false;
        }
        petRepository.deleteById(id);
        return true;
    }
}
