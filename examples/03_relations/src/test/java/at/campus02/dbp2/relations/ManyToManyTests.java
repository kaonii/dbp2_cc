package at.campus02.dbp2.relations;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;

public class ManyToManyTests {

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
    public void manyToManyExample(){
        // given
        Animal cat = new Animal("cat");
        Animal elephant = new Animal("elephant");
        Animal tiger = new Animal("tiger");

        Country austria = new Country("Austria");
        Country india = new Country("India");
        Country southAfrica = new Country("South Africa");

        // falls animal die Quelle ist (mapped by auf country verwendet),
        // müssen die Länder in die Liste bei den animals vorhanden sein
        // damit die relationen geschlossenn werden
        // nicht nötig, falls country die quelle ist (mapped by auf animal)

        // relations
        cat.getCountries().addAll(Arrays.asList(austria,india,southAfrica));
        elephant.getCountries().addAll(Arrays.asList(india,southAfrica));
        tiger.getCountries().add(india);



        // wegen cascade persist auf country müssen die animals jedenfalls
        // in der Liste bei den Ländern vorhanden sein, damit persist
        // überhaupt cascadieren kann
        austria.getAnimals().add(cat);
        southAfrica.getAnimals().addAll(Arrays.asList(cat, elephant));
        india.getAnimals().addAll(Arrays.asList(cat, elephant, tiger));

        // when
        manager.getTransaction().begin();
        manager.persist(austria);
        manager.persist(india);
        manager.persist(southAfrica);
        manager.getTransaction().commit();

        // then
        Country indiaFromDb = manager.find(Country.class, india.getName());
        manager.refresh(indiaFromDb);
        assertThat(indiaFromDb.getAnimals(), Matchers.containsInAnyOrder(cat, tiger, elephant));

        Country southAfricaFromDb = manager.find(Country.class, southAfrica.getName());
        manager.refresh(southAfricaFromDb);
        assertThat(southAfricaFromDb.getAnimals(), Matchers.containsInAnyOrder(cat, elephant));

        Country austriaFromDb = manager.find(Country.class, austria.getName());
        manager.refresh(austriaFromDb);
        assertThat(austriaFromDb.getAnimals(), Matchers.contains(cat));

        Animal catFromDb = manager.find(Animal.class, cat.getId());
        manager.refresh(catFromDb);
        assertThat(catFromDb.getCountries(), Matchers.containsInAnyOrder(austria, southAfrica, india));
    }


}
