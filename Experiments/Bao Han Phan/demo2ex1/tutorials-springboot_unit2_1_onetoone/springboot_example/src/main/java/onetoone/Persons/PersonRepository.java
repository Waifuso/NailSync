package onetoone.Persons;

import onetoone.Laptops.Laptop;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

public interface PersonRepository extends JpaRepository<Person, Long> {
    
    Person findById(int id);

    Person findByName(String name);

    Person findByNameAndEmailId(String name, String emailId);  // Find by both name and email

    Person findByNameOrEmailId(String name, String emailId);  // Find by name or email

    void deleteById(int id);

    Person findByLaptop_Id(int id);
}
