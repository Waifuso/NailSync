package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple Hello World Controller to display the string returned
 *
 * @author Vivek Bengre
 */

@RestController()
@RequestMapping(path = "/welcome")
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return"Hello it me, I am Phong Le ";
    }
    @GetMapping("em/{firstName}")
    public String welcome  (@PathVariable String firstName ){

        return " HEllO customer name: " + firstName;
    }


}
