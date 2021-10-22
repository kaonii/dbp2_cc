package at.campus02.dbp2.relations;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;


public class OneToOneTests {

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
    public void justATest() {
        //given

        Student student = new Student();
        student.setName("Hansi");
        Animal animal = new Animal();
        animal.setName("Flipper");

        // im Speicher selber um die Referenzen kümmern, in beiden Richtungen
        student.setPet(animal);
        animal.setOwner(student);

        //when
        manager.getTransaction().begin();
        // in JPA, order does not matter
        manager.persist(student);
        manager.persist(animal);
        manager.getTransaction().commit();

        manager.clear();

        //then
        Animal flipperFromDb = manager.find(Animal.class, animal.getId());
        assertThat(flipperFromDb.getOwner(), is(student));

        Student ownerFromDb = manager.find(Student.class, student.getId());
        assertThat(ownerFromDb.getPet(), is(animal));
    }

    @Test
    public void persistStudentWithCascadeAlsoPersistAnimal() {
        //given
        Student hansi = new Student("Hansi");
        Animal bunny = new Animal("Bunny");

        //Referenzen im Speicher verwalten:
        //1) owner setzen, um in der DB die Relation zu schließen
        bunny.setOwner(hansi);

        //2) pet setzen, damit Cascade funktioniert
        hansi.setPet(bunny);

        //when
        manager.getTransaction().begin();
        // persist only owner, animal should also be persisted automatically
        manager.persist(hansi);
        manager.getTransaction().commit();
        manager.clear();
        //then
        Animal bunnyFromDb = manager.find(Animal.class, bunny.getId());
        assertThat(bunnyFromDb.getOwner(), is(hansi));

        Student hansiFromDb = manager.find(Student.class, hansi.getId());
        assertThat(hansiFromDb.getPet(), is(bunny));

    }

    @Test
    public void refreshClosesReferencesNotHandledInMemory() {
        //given
        Student hansi = new Student("Hansi");
        Animal bunny = new Animal("Bunny");

        //Referenzen im Speicher verwalten:
        //1) owner setzen, um in der DB die Relation zu schließen. IMMER.
        bunny.setOwner(hansi);

        //2) pet absichtlich nicht setzen, um refresh zu demonstieren
        // hansi.setPet(bunny);

        //when
        manager.getTransaction().begin();
        manager.persist(bunny);
        // nachdem am hansi kein Pet gesetzt ist, reicht es nicht
        // hansi allein zu persistieren (cascade kann nicht greifen)
        // d.h. wir müssen beide entities persistieren (reihenfolge
        // innerhalb der Transaktion ist egal.)
        manager.persist(hansi);
        manager.getTransaction().commit();

        manager.clear();
        //then
        //1) referenz von Animal auf Student ist gesetzt
        Animal bunnyFromDb = manager.find(Animal.class, bunny.getId());
        assertThat(bunnyFromDb.getOwner(), is(hansi));

        //2) ohne refresh wird referenz von hansi auf bunny
        // nicht geschlossen (auch nicht nach manager.clear(),
        // welches den Level1 Cache leert - bei Relationen
        // is null, weil dem hansi den pet nie gesetzt wurde
        Student hansiFromDb = manager.find(Student.class, hansi.getId());
        assertThat(hansiFromDb.getPet(), is(nullValue()));

        //3) "refresh" erzwingt das Neu-Einlesen aus der Datenbank,
        // auch mit Relationen
        manager.refresh(hansiFromDb);
        assertThat(hansiFromDb.getPet(), is(bunny));

    }
}
