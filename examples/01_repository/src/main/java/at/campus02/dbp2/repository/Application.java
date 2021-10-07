package at.campus02.dbp2.repository;

public class Application {

    public static void log(String msg) {

        System.out.println("Application:  --> " + msg);
    }

    public static void main(String[] args) {

        log("application started");

        CustomerRepository repository = new InMemoryRepository();

        Customer customer1 = new Customer();
        customer1.setEmail("customer1@mail.com");
        customer1.setFirstname("Carlo");
        customer1.setLastname("Customer");

        // 1) Create

        repository.create(customer1);
        log("Customer created: " + customer1);

        // 2) Read
        Customer fromRepository = repository.read(customer1.getEmail());
        log("Customer read " + fromRepository);

        // 3) Update
        fromRepository.setFirstname("Conrad");
        repository.update(fromRepository);
        Customer updated = repository.read(fromRepository.getEmail());
        log("Customer updated " + updated);


        // 4) Delete
        repository.delete(updated);
        Customer deleted = repository.read(updated.getEmail());
        log("Customer deleted: " + deleted);

    }
}
