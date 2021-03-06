package at.campus02.dbp2.mappings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerRepositoryCrudSpec {

    //#region test data
    private final String firstname = "Firstname";
    private final String lastname = "Lastname";
    private final AccountType accountType = AccountType.BASIC;
    private final LocalDate registeredSince = LocalDate.of(2021,10,1);

    private Customer initDefaultCustomer() {
        Customer customer = new Customer();
        customer.setFirstname(firstname);
        customer.setLastname(lastname);
        customer.setAccountType(accountType);
        customer.setRegisteredSince(registeredSince);
        return customer;
    }
    //#endregion

    //#region setup / tear down (after each)
    private EntityManagerFactory factory;
    private EntityManager manager;
    private CustomerRepository repository;

    @BeforeEach
    public void beforeEach() {
        factory = Persistence.createEntityManagerFactory("persistenceUnitName");
        manager = factory.createEntityManager();
        repository = new CustomerRepositoryJpa(factory);
    }

    @AfterEach
    public void afterEach() {
        if (manager.isOpen()) {
            manager.close();
        }
        if (factory.isOpen()) {
            factory.close();
        }
    }
    //#endregion

    //create
    // if customer is null, return false
    // start with general use case, or easiest use case


    //#region CRUD

    @Test
    public void createNullAsCustomerReturnsFalse() {
        // given
        // when
        boolean result = repository.create(null);
        // then
        assertFalse(result);
    }

    @Test
    public void createPersistsCustomerInDatabaseAndReturnsTrue() {
        // given
        Customer toCreate = initDefaultCustomer();

        // when
        boolean result = repository.create(toCreate);

        // then
        assertTrue(result);

        // Kontrolle auch in der Datenbank
        Customer fromDb = manager.find(Customer.class, toCreate.getId());
        assertEquals(firstname, fromDb.getFirstname());
        assertEquals(lastname, fromDb.getLastname());
        assertEquals(accountType, fromDb.getAccountType());
        assertEquals(registeredSince, fromDb.getRegisteredSince());
    }

    @Test
    public void createExistingCustomerReturnsFalse() {
        // given
        Customer toCreate = initDefaultCustomer();

        manager.getTransaction().begin();
        manager.persist(toCreate);
        manager.getTransaction().commit();

        // when
        boolean result = repository.create(toCreate);

        // then
        assertFalse(result);
    }

    @Test
    public void createCustomerWithNullAsAccountTypeThrowException() {
        //given
        Customer notValid = initDefaultCustomer();
        notValid.setAccountType(null);

        //when
        assertThrows(RuntimeException.class, () -> repository.create(notValid));

    }

    @Test
    public void readFindsCustomerInDatabase() {
        //given
        Customer existing = initDefaultCustomer();

        manager.getTransaction().begin();
        manager.persist(existing);
        manager.getTransaction().commit();

        //when
        Customer fromRepository = repository.read(existing.getId());
        assertEquals(firstname, fromRepository.getFirstname());
        assertEquals(lastname, fromRepository.getLastname());
        assertEquals(accountType, fromRepository.getAccountType());
        assertEquals(registeredSince, fromRepository.getRegisteredSince());
    }

    @Test
    public void readWithNotExistingIdReturnsNull() {
        //when
        Customer fromRepository = repository.read(-1);
        //then
        assertNull(fromRepository);
    }

    @Test
    public void readWithNullAsIdReturnsNull() {
        //when
        Customer fromRepository = repository.read(null);
        //then
        assertNull(fromRepository);
    }

    @Test
    public void updateChangesAttributesInDatabase() {
        //given
        Customer existing = initDefaultCustomer();

        manager.getTransaction().begin();
        manager.persist(existing);
        manager.getTransaction().commit();

        String changedFirstName = "changedFirstname";
        String changedLastName = "changedLastname";
        AccountType changedAccountType = AccountType.PREMIUM;
        LocalDate changedRegisteredSince = LocalDate.of(2021,10,14);

        //when
        existing.setFirstname(changedFirstName);
        existing.setLastname(changedLastName);
        existing.setAccountType(changedAccountType);
        existing.setRegisteredSince(changedRegisteredSince);
        Customer updated = repository.update(existing);

        //then
        assertEquals(existing.getId(), updated.getId());
        assertEquals(changedFirstName, updated.getFirstname());
        assertEquals(changedLastName, updated.getLastname());
        assertEquals(changedAccountType, updated.getAccountType());
        assertEquals(changedRegisteredSince, updated.getRegisteredSince());

        //until now you only have response from repository, not from database
        //check into database, clear cash first
        manager.clear();
        Customer fromDb = manager.find(Customer.class, updated.getId());

        assertEquals(existing.getId(), fromDb.getId());
        assertEquals(changedFirstName, fromDb.getFirstname());
        assertEquals(changedLastName, fromDb.getLastname());
        assertEquals(changedAccountType, fromDb.getAccountType());
        assertEquals(changedRegisteredSince, fromDb.getRegisteredSince());

    }

    @Test
    public void updateNotExistingCustomerThrowsIllegalArgumentException() {
        //given
        Customer notExisting = initDefaultCustomer();
        //do not persist

        //when / then
        //any exception? -> exception.class
        assertThrows(IllegalArgumentException.class,  () -> repository.update(notExisting));

    }

    @Test
    public void updateWithNullAsCustomerReturnsNull() {
        //when

        Customer updated = repository.update(null);

        //then
        assertNull(updated);
    }

    //#endregion

    //#region CRUD: delete

    @Test
    public void deleteRemovesCustomerFromDatabaseAndReturnsTrue() {
        //given
        Customer existing = initDefaultCustomer();

        manager.getTransaction().begin();
        manager.persist(existing);
        manager.getTransaction().commit();

        //when
        boolean result = repository.delete(existing);

        //then
        assertTrue(result);
        manager.clear();
        Customer hopefullyDeleted = manager.find(Customer.class, existing.getId());
        assertNull(hopefullyDeleted);
    }

    @Test
    public void deleteNotExistingCustomerThrowsIllegalArgumentException() {
        //given
        Customer notExisting = initDefaultCustomer();
        //do not persist so customer doesnt exist

        //when / then
        //any exception? -> exception.class
        assertThrows(IllegalArgumentException.class,  () -> repository.delete(notExisting));
    }

    @Test
    public void deleteNullAsCustomerReturnsFalse() {
        //when
        boolean result = repository.delete(null);

        //then
        assertFalse(result);
    }

    //#endregion
}
