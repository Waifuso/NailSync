/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @Modified By Tanmay Ghosh
 * @Modified By Vivek Bengre
 */
@RestController
class OwnerController {

    @Autowired
    OwnerRepository ownersRepository;

    private final Logger logger = LoggerFactory.getLogger(OwnerController.class);

    @RequestMapping(method = RequestMethod.POST, path = "/owners/new")
    public String saveOwner(Owners owner) {
        ownersRepository.save(owner);
        return "New Owner "+ owner.getFirstName() + " Saved";
    }
    // function just to create dummy data
    @RequestMapping(method = RequestMethod.GET, path = "/owner/create")
    public String createDummyData() {
        Owners o1 = new Owners(1, "John", "Doe", "404 Not found", "some numbers");
        Owners o2 = new Owners(2, "Jane", "Doe", "Its a secret", "you wish");
        Owners o3 = new Owners(3, "Some", "Pleb", "Right next to the Library", "515-345-41213");
        Owners o4 = new Owners(4, "Chad", "Champion", "Reddit memes corner", "420-420-4200");
        Owners o5 = new Owners(5, "Bao", "Phan", "Vietnam", "319-677-2752");
        ownersRepository.save(o1);
        ownersRepository.save(o2);
        ownersRepository.save(o3);
        ownersRepository.save(o4);
        ownersRepository.save(o5);
        return "Successfully created dummy data";
    }
//Get all owner
    @RequestMapping(method = RequestMethod.GET, path = "/owners")
    public List<Owners> getAllOwners() {
        logger.info("Entered into Controller Layer");
        List<Owners> results = ownersRepository.findAll();
        logger.info("Number of Records Fetched:" + results.size());
        return results;
    }
//Get owners by ID
    @RequestMapping(method = RequestMethod.GET, path = "/owners/{ownerId}")
    public Optional<Owners> findOwnerById(@PathVariable("ownerId") int id) {
        logger.info("Entered into Controller Layer");
        Optional<Owners> results = ownersRepository.findById(id);
        return results;
    }

    // Update Owner by ID (PUT)

    @PutMapping("/owners/{ownerId}")
    public String updateOwner(@PathVariable("ownerId") int ownerId, @RequestBody Owners updatedOwner) {
        logger.info("Entered into Update Layer");
        Optional<Owners> ownerOptional = ownersRepository.findById(ownerId);

        if (ownerOptional.isPresent()) {
            Owners existingOwner = ownerOptional.get();

            // Update fields
            existingOwner.setFirstName(updatedOwner.getFirstName());
            existingOwner.setLastName(updatedOwner.getLastName());
            existingOwner.setAddress(updatedOwner.getAddress());
            existingOwner.setTelephone(updatedOwner.getTelephone());

            ownersRepository.save(existingOwner);
            logger.info("Owner Updated: " + existingOwner.getFirstName());
            return "Owner " + existingOwner.getFirstName() + " Updated Successfully";
        } else {
            logger.warn("Owner with ID " + ownerId + " not found.");
            return "Owner with ID " + ownerId + " not found!";
        }
    }

    // Delete Owner by ID (DELETE)
    @DeleteMapping("/owners/{ownerId}")
    public String deleteOwner(@PathVariable("ownerId") int ownerId) {
        logger.info("Entered into Delete Layer");
        Optional<Owners> ownerOptional = ownersRepository.findById(ownerId);

        if (ownerOptional.isPresent()) {
            ownersRepository.delete(ownerOptional.get());
            logger.info("Owner with ID " + ownerId + " deleted successfully.");
            return "Owner with ID " + ownerId + " deleted successfully.";
        } else {
            logger.warn("Owner with ID " + ownerId + " not found.");
            return "Owner with ID " + ownerId + " not found!";
        }
    }

}


