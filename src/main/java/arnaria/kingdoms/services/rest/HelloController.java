package arnaria.kingdoms.services.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api")
    public String greeting() {
        return "Greetings from the Arnaria API!";
    }
}
