package coms309.people;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;



@RestController
public class PeopleController {

    // Note that there is only ONE instance of PeopleController in 
    // Springboot system.
    HashMap<String, Person> peopleList = new  HashMap<>();

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    // gets all the people in the list and returns it in JSON format
    // This controller takes no input. 
    // Springboot automatically converts the list to JSON format 
    // in this case because of @ResponseBody
    // Note: To LIST, we use the GET method
    @GetMapping("/people")
    public ResponseEntity<HashMap<String, Person>> getAllPersons() {
        return new ResponseEntity<>(peopleList, HttpStatus.OK);
    }

    // THIS IS THE CREATE OPERATION
    // springboot automatically converts JSON input into a person object and 
    // the method below enters it into the list.
    // It returns a string message in THIS example.
    // in this case because of @ResponseBody
    // Note: To CREATE we use POST method
    @PostMapping("/people")
    public ResponseEntity<String> createPerson(@RequestBody Person person) {
        if (peopleList.containsKey(person.getFirstName())) {
            return new ResponseEntity<>("Person already exists.", HttpStatus.CONFLICT);
        }
        peopleList.put(person.getFirstName(), person);
        return new ResponseEntity<>("New person " + person.getFirstName() + " Saved", HttpStatus.CREATED);
    }

    // THIS IS THE READ OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We extract the person from the HashMap.
    // springboot automatically converts Person to JSON format when we return it
    // in this case because of @ResponseBody
    // Note: To READ we use GET method
    @GetMapping("/people/{firstName}")
    public ResponseEntity<Person> getPerson(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        if (p == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(p, HttpStatus.OK);
    }

    // THIS IS THE UPDATE OPERATION
    // We extract the person from the HashMap and modify it.
    // Springboot automatically converts the Person to JSON format
    // Springboot gets the PATHVARIABLE from the URL
    // Here we are returning what we sent to the method
    // in this case because of @ResponseBody
    // Note: To UPDATE we use PUT method
    @PutMapping("/people/{firstName}")
    public ResponseEntity<Person> updatePerson(@PathVariable String firstName, @RequestBody Person updatedPerson) {
        if (!peopleList.containsKey(firstName)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        peopleList.put(firstName, updatedPerson);
        return new ResponseEntity<>(updatedPerson, HttpStatus.OK);
    }

    // THIS IS THE DELETE OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We return the entire list -- converted to JSON
    // in this case because of @ResponseBody
    // Note: To DELETE we use delete method
    
    @DeleteMapping("/people/{firstName}")
    public ResponseEntity<HashMap<String, Person>> deletePerson(@PathVariable String firstName) {
        if (!peopleList.containsKey(firstName)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        peopleList.remove(firstName);
        return new ResponseEntity<>(peopleList, HttpStatus.OK);
    }
}

