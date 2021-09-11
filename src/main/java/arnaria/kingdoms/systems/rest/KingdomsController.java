package arnaria.kingdoms.systems.rest;

import arnaria.kingdoms.systems.rest.templates.KingdomTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import static arnaria.kingdoms.systems.procedures.KingdomProcedures.kingdomData;

@RestController
public class KingdomsController {

    @GetMapping("/kingdoms")
    ArrayList<KingdomTemplate> kingdoms() {
        ArrayList<KingdomTemplate> kingdoms = new ArrayList<>();
        for (String kingdomId : kingdomData.getIds()) kingdoms.add(new KingdomTemplate(kingdomId));
        return kingdoms;
    }
}
