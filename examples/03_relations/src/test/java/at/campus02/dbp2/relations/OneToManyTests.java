package at.campus02.dbp2.relations;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;


public class OneToManyTests {

    private EntityManagerFactory factory;
    private EntityManager manager;

    @BeforeEach
    public void setup() {
        factory = Persistence.createEntityManagerFactory("persistenceUnitName");
        manager = factory.createEntityManager();
    }

    @AfterEach
    public void teardown(){
        if (manager.isOpen()){
            manager.close();
        }
        if (factory.isOpen()){
            factory.close();
        }
    }

    @Test
    public void persistSpeciesWithCascadeStoresAnimalsInDatabase() {
        //given

        Animal bunny = new Animal("Bunny");
        Animal dog = new Animal("Leo");

        Species mammals = new Species("Mammals");

        //Referenzen für FK in DB
        bunny.setSpecies(mammals);
        dog.setSpecies(mammals);

        //Referenzen für Cascade
        mammals.getAnimals().add(bunny);
        mammals.getAnimals().add(dog);

        //when
        // persist list of species, bunny and dog should persist automatically
        manager.getTransaction().begin();
        manager.persist(mammals);
        manager.getTransaction().commit();
        manager.clear();

        //then
        Species mammalsFromDb = manager.find(Species.class, mammals.getId());
        assertThat(mammalsFromDb.getAnimals().size(), is(2));
        assertThat(mammalsFromDb.getAnimals(), containsInAnyOrder(bunny, dog));

    }

    @Test
    public void updateExampleWithCorrectingReferencesDOESNOTWORKONPURPOSE(){
        //------------------------------------------------
        //given
        Animal clownfish = new Animal("Nemo");
        Animal squirrel = new Animal("Squirrel");

        Species fish = new Species("Fish");

        // references for DB
        clownfish.setSpecies(fish);

        // FEHLER -> den wollen wir dann korrigieren
        squirrel.setSpecies(fish);

        //references for cascade
        fish.getAnimals().add(clownfish);
        fish.getAnimals().add(squirrel);

        // speichern
        manager.getTransaction().begin();
        manager.persist(fish);
        manager.getTransaction().commit();

        manager.clear();

        //------------------------------------------------
        // when: korrekturversuch, zum Scheitern verurteilt...
        manager.getTransaction().begin();
        fish.getAnimals().remove(squirrel);
        manager.merge(fish);
        manager.getTransaction().commit();
        manager.clear();

        //------------------------------------------------
        //then:
        // squirrel existiert noch in DB
        Animal squirrelFromDb = manager.find(Animal.class, squirrel.getId());
        assertThat(squirrelFromDb, is(notNullValue()));

        // squirrel ist immer noch ein fisch
        // wir haben im Speicher die Liste von Fish geändert,
        // aber species von squirred zeigt nach wie vor auf Fish
        // auch inder DB
        assertThat(squirrelFromDb.getSpecies().getId(), is(fish.getId()));

        // auch wenn wir die Liste mittels refresh neu einlesen,
        // kommt das gleiche Problem
        // referenz von squirrel auf Fisch in der Datenbank neu eingelesen
        // Squirrel ist wieder in der Liste drin
        Species mergedFish = manager.merge(fish);
        manager.refresh(mergedFish);
        assertThat(mergedFish.getAnimals().size(), is(2));
    }

    @Test
    public void updateExampleWithCorrectingReferencesWORKS(){
        //------------------------------------------------
        //given
        Animal clownfish = new Animal("Nemo");
        Animal squirrel = new Animal("Squirrel");
        Species fish = new Species("Fish");

        // references for DB
        clownfish.setSpecies(fish);

        // FEHLER -> den wollen wir dann korrigieren
        squirrel.setSpecies(fish);

        //references for cascade
        fish.getAnimals().add(clownfish);
        fish.getAnimals().add(squirrel);

        // speichern
        manager.getTransaction().begin();
        manager.persist(fish);
        manager.getTransaction().commit();
        manager.clear();

        //------------------------------------------------
        // when: korrekturversuch, diesmal richtig
        manager.getTransaction().begin();
        squirrel.setSpecies(null);
        manager.merge(squirrel);
        manager.getTransaction().commit();
        manager.clear();

        //------------------------------------------------
        //then:
        // squirrel existiert noch in DB
        Animal squirrelFromDb = manager.find(Animal.class, squirrel.getId());
        assertThat(squirrelFromDb, is(notNullValue()));

        // squirrel ist kein Fisch mehr
        assertThat(squirrelFromDb.getSpecies(), is(nullValue()));

        // squirrel ist nicht mehr in der Liste
        Species mergedFish = manager.merge(fish);
        manager.refresh(mergedFish);
        assertThat(mergedFish.getAnimals().size(), is(1));
    }

}
