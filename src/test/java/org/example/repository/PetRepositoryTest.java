package org.example.repository;

import org.example.entity.Pets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs against the real schema.sql and data.sql. */
@DataJpaTest
class PetRepositoryTest {

    @Autowired
    private PetRepository repository;

    @ParameterizedTest(name = "seed pet {0} is {1}, a {3} aged {2}")
    @CsvSource({
            "1, Alphonse, 4, CAT",
            "2, Edward,   4, CAT",
            "3, Sloopy,   1, DOG"
    })
    void seedDataIsLoaded(int id, String name, int age, String type) {
        assertThat(repository.findById(id)).get()
                .extracting(Pets::getName, Pets::getAge, Pets::getType)
                .containsExactly(name, age, type);
    }

    @Test
    void newPetsGetIdsAfterTheSeedData() {
        // Guards against seeding with explicit ids, which would make this collide with id 1.
        Pets saved = repository.saveAndFlush(new Pets(null, "Nemo", 1, "FISH"));

        assertThat(saved.getId()).isGreaterThan(3);
        assertThat(repository.count()).isEqualTo(4);
    }
}
