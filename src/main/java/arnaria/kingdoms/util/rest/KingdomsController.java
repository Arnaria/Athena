package arnaria.kingdoms.util.rest;

import arnaria.kingdoms.util.rest.templates.KingdomTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import static arnaria.kingdoms.util.procedures.KingdomProcedures.kingdomData;

@RestController
public class KingdomsController {

    @GetMapping("/kingdoms")
    ArrayList<KingdomTemplate> kingdoms() {
        ArrayList<KingdomTemplate> kingdoms = new ArrayList<>();
        for (String kingdomId : kingdomData.getIds()) kingdoms.add(new KingdomTemplate(kingdomId));
        return kingdoms;
    }
}
