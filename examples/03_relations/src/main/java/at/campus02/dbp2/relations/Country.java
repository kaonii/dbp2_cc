package at.campus02.dbp2.relations;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Country {

    @Id
    private String name;

    @ManyToMany(mappedBy = "countries", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    // not cascade.type all otherwise if
    // you delete a country you would delete animal too
    // if you dont have mappedBy countries, it makes two tables : animal_country and country_animal
    // mapped by countries : animal_country
    // mapped by animals : country_animal -> you can remove all the animal relations eg cat.getCountry in tests
    private List<Animal> animals = new ArrayList<>();

    public Country(String name) {
        this.name = name;
    }

    public Country() {
    }

    public String getName() {
        return name;
    }

    public List<Animal> getAnimals() {
        return animals;
    }
}
