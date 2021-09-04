package arnaria.kingdoms.rest;

import arnaria.kingdoms.util.claims.Claim;
import arnaria.kingdoms.util.claims.ClaimManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClaimController {

    @GetMapping("/claims")
    List<Claim> claims() {
        return ClaimManager.getClaims();
    }
}
