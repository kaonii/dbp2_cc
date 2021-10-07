package at.campus02.dbp2.repository;

public class Customer {

    private String lastname;
    private String firstname;
    private String email;



    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Customer - " +
                " lastname: '" + lastname + '\'' +
                ", firstname:'" + firstname + '\'' +
                ", email: '" + email + '\'';
    }
}
