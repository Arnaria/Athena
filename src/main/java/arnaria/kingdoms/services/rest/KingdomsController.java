package arnaria.kingdoms.services.rest;

import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.rest.templates.KingdomTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class KingdomsController {

    @GetMapping("/kingdoms")
    ArrayList<KingdomTemplate> kingdoms() {
        ArrayList<KingdomTemplate> kingdoms = new ArrayList<>();
        for (String kingdomId : KingdomsData.getKingdomIds()) kingdoms.add(new KingdomTemplate(kingdomId));
        return kingdoms;
    }
}
