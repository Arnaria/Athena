package arnaria.kingdoms.services.rest;

import arnaria.kingdoms.services.procedures.LinkProcedures;
import org.springframework.web.bind.annotation.*;

@RestController
public class LinkController {

    @GetMapping("/getLinkToken")
    String linkToken() {
        return LinkProcedures.getValidLinkToken();
    }
}
