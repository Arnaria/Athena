package arnaria.kingdoms.systems.rest;

import arnaria.kingdoms.systems.claims.Claim;
import arnaria.kingdoms.systems.claims.ClaimManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class ClaimsController {

    @GetMapping("/claims")
    ArrayList<Claim> claims() {
        return ClaimManager.getClaims();
    }
}
